/**
* 
* Copyright (C) 2004-2008 FhG Fokus
*
* This file is part of the FhG Fokus UPnP stack - an open source UPnP implementation
* with some additional features
*
* You can redistribute the FhG Fokus UPnP stack and/or modify it
* under the terms of the GNU General Public License Version 3 as published by
* the Free Software Foundation.
*
* For a license to use the FhG Fokus UPnP stack software under conditions
* other than those described here, or to purchase support for this
* software, please contact Fraunhofer FOKUS by e-mail at the following
* addresses:
*   upnpstack@fokus.fraunhofer.de
*
* The FhG Fokus UPnP stack is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see <http://www.gnu.org/licenses/>
* or write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
*
*/
package de.fraunhofer.fokus.upnp.core.control_point;

import java.net.InetSocketAddress;
import java.net.URL;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.configuration.UPnPDefaults;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.templates.TemplateControlPoint;
import de.fraunhofer.fokus.upnp.gena.GENAConstant;
import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.http.HTTPParser;
import de.fraunhofer.fokus.upnp.ssdp.SSDPConstant;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.DateTimeHelper;
import de.fraunhofer.fokus.upnp.util.SSDPHelper;
import de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;

/**
 * This class is used to process M-SEARCH response or NOTIFY messages received by control points.
 * 
 * @author icu, Alexander Koenig
 * 
 */
public class CPSSDPMessageProcessor
{

  /**
   * UPnP logger
   */
  protected static Logger logger    = Logger.getLogger("upnp.ssdp");

  /**
   * IP version
   */
  protected int           IPVersion = UPnPConstant.IP4;

  /** Associated control point */
  protected ControlPoint  controlPoint;

  /**
   * Creates CPSSDPMessageProcessor object
   */
  public CPSSDPMessageProcessor(ControlPoint cp)
  {
    this.controlPoint = cp;
    this.IPVersion = cp.getIPVersion();
  }

  /**
   * Processes a received discovery message (NOTIFY or HTTP_OK).
   * 
   * @param discoverySocketStructure
   *          Socket structure that received the message
   * @param packetContent
   *          The received message
   */
  public void processMessage(ControlPointHostAddressSocketStructure discoverySocketStructure,
    BinaryMessageObject message)
  {
    String packetContent = message.getBodyAsString();
    HTTPParser httpParser = new HTTPParser();
    httpParser.parse(packetContent);

    if (httpParser.isMSEARCHResponseMessage())
    {
      logger.info("Received SSDP M-SEARCH response message from " + IPHelper.toString(message.getSourceAddress()));

      // ignore IPv6 responses if IPv4
      if (httpParser.getValue(CommonConstants.LOCATION).startsWith("http://[") && IPVersion != UPnPConstant.IP6)
      {
        return;
      }
      processAliveMessage(false,
        httpParser.getValue(HTTPConstant.CACHE_CONTROL),
        httpParser.getValue(CommonConstants.LOCATION),
        httpParser.getValue(HTTPConstant.SERVER),
        httpParser.getValue(SSDPConstant.ST),
        httpParser.getValue(CommonConstants.USN),
        httpParser.getValue(SSDPConstant.NLS),
        discoverySocketStructure);

      return;
    }
    if (httpParser.isNOTIFYAliveMessage())
    {
      logger.info("Received SSDP ALIVE message from " + IPHelper.toString(message.getSourceAddress()));
      // check for multihomed control points
      if (controlPoint.getSocketStructureCount() > 1)
      {
        // check if the NOTIFY is from another local network interface and was looped back
        //
        // URL = http://localIP1:gatewayPort/Interface.../LocalIP2/LocalPort2/...
        // ignore such messages
        if (SSDPHelper.isLoopedBackGatewayMessage(httpParser.getValue(CommonConstants.LOCATION)))
        {
          System.out.println("Discard looped back NOTIFY packet with location " +
            httpParser.getValue(CommonConstants.LOCATION));
          return;
        }
      }
      processAliveMessage(true,
        httpParser.getValue(HTTPConstant.CACHE_CONTROL),
        httpParser.getValue(CommonConstants.LOCATION),
        httpParser.getValue(HTTPConstant.SERVER),
        httpParser.getValue(GENAConstant.NT),
        httpParser.getValue(CommonConstants.USN),
        httpParser.getValue(SSDPConstant.NLS),
        discoverySocketStructure);

      return;
    }
    if (httpParser.isNOTIFYByeByeMessage())
    {
      logger.info("Received SSDP BYEBYE message from " + IPHelper.toString(message.getSourceAddress()));
      removeDevice(httpParser);

      return;
    }
    if (httpParser.isMSEARCHMessage())
    {
      logger.debug("Ignore SSDP M-SEARCH message from " + IPHelper.toString(message.getSourceAddress()));

      return;
    }
    logger.warn("Unknown message from " + IPHelper.toString(message.getSourceAddress()) + " [\n" + packetContent + "]");
  }

  /**
   * Processes a device discovery message.
   * 
   * @param cacheControlValue
   *          Value of cache-control header
   * @param locationValue
   * @param serverValue
   * @param NTValue
   * @param USNValue
   * @param NLSValue
   * @param discoverySocketStructure
   * 
   */
  private void processAliveMessage(boolean isNotifyMessage,
    String cacheControlValue,
    String locationValue,
    String serverValue,
    String NTValue,
    String USNValue,
    String NLSValue,
    ControlPointHostAddressSocketStructure discoverySocketStructure)
  {
    URL descriptionURL = null;
    InetSocketAddress descriptionSocketAddress = null;

    // check device description URL
    try
    {
      descriptionURL = new URL(locationValue);
      descriptionSocketAddress = IPHelper.toSocketAddress(descriptionURL);
    } catch (Exception e)
    {
      logger.error("invalid device description URL = " + locationValue);
      logger.error("reason: " + e);

      return;
    }

    // check if device address is in ignore list
    if (controlPoint.isIgnoredDeviceAddress(descriptionURL.getHost()))
    {
      System.out.println("Ignore discovery info for host " + descriptionURL.getHost());
      return;
    }
    synchronized(controlPoint.getDeviceInfoLock())
    {
      // add infos from message to new or existing discovery info
      CPDeviceDiscoveryInfo deviceDiscoveryInfo;

      // remove deprecated invalid device info
      if (controlPoint.getDiscoveryInfoFromDescriptionURLTable().containsKey(descriptionURL))
      {
        deviceDiscoveryInfo =
          (CPDeviceDiscoveryInfo)controlPoint.getDiscoveryInfoFromDescriptionURLTable().get(descriptionURL);
        if (deviceDiscoveryInfo.isInvalidDevice() &&
          System.currentTimeMillis() - deviceDiscoveryInfo.getInvalidationTime() >= UPnPDefaults.CP_INVALID_DEVICE_DISCOVERY_TIMEOUT * 1000)
        {
          System.out.println("Remove outdated invalid device info for URL " + descriptionURL);
          controlPoint.getDiscoveryInfoFromDescriptionURLTable().remove(descriptionURL);
        }
      }

      // check if device is known
      if (controlPoint.getDiscoveryInfoFromDescriptionURLTable().containsKey(descriptionURL))
      {
        // info exists
        logger.info("Device from " + descriptionURL + " is already known");
        deviceDiscoveryInfo =
          (CPDeviceDiscoveryInfo)controlPoint.getDiscoveryInfoFromDescriptionURLTable().get(descriptionURL);

        // check if device is invalid
        if (deviceDiscoveryInfo.isInvalidDevice() &&
          System.currentTimeMillis() - deviceDiscoveryInfo.getInvalidationTime() < UPnPDefaults.CP_INVALID_DEVICE_DISCOVERY_TIMEOUT * 1000)
        {
          // System.out.println("Last invalid request to " + descriptionURL + " is too fresh. Ignore
          // discovery info");
          return;
        }
        deviceDiscoveryInfo.processDiscoveryInfo(NTValue, USNValue);
      } else
      {
        // create new info
        logger.info("Device from " + descriptionURL + " is a new device");
        // create discovery info
        deviceDiscoveryInfo =
          new CPDeviceDiscoveryInfo(cacheControlValue, descriptionURL, serverValue, NTValue, USNValue, NLSValue);

        // store discovery info for description URL
        controlPoint.getDiscoveryInfoFromDescriptionURLTable().put(descriptionURL, deviceDiscoveryInfo);

        TemplateControlPoint.printMessage("\n    " + DateTimeHelper.formatCurrentDateForDebug() + ":" +
          controlPoint.toString() + ": Add new  DEVICE INFO for new URL " + descriptionURL);
      }
      // renew expected lifetime
      CPDevice device = (CPDevice)controlPoint.getCPDeviceFromDescriptionURLTable().get(descriptionURL);
      if (device != null)
      {
        device.setExpectedLifetime(System.currentTimeMillis() + deviceDiscoveryInfo.getMaxage() * 1000);
      }
      if (isNotifyMessage)
      {
        deviceDiscoveryInfo.updateLastDiscoveryTime();
      }

      // check if the root device UUID is already known
      String rootDeviceUUID = deviceDiscoveryInfo.getRootDeviceUUID();
      if (rootDeviceUUID != null)
      {
        CPDeviceDiscoveryInfo uuidDeviceDiscoveryInfo = null;
        if (!controlPoint.getDeviceDiscoveryInfoFromUUIDTable().containsKey(rootDeviceUUID))
        {
          controlPoint.getDeviceDiscoveryInfoFromUUIDTable().put(rootDeviceUUID, deviceDiscoveryInfo);
          uuidDeviceDiscoveryInfo = deviceDiscoveryInfo;
        } else
        {
          uuidDeviceDiscoveryInfo =
            (CPDeviceDiscoveryInfo)controlPoint.getDeviceDiscoveryInfoFromUUIDTable().get(rootDeviceUUID);
        }
        if (isNotifyMessage)
        {
          // update discovery time
          uuidDeviceDiscoveryInfo.updateLastDiscoveryTime();
        }

        // add new URL if possible
        if (!uuidDeviceDiscoveryInfo.getPublishedIPAddressList().contains(descriptionSocketAddress))
        {
          uuidDeviceDiscoveryInfo.getPublishedIPAddressList().add(descriptionSocketAddress);
        }

        URL storedDescriptionURL = (URL)controlPoint.getDeviceDescriptionURLFromUUIDTable().get(rootDeviceUUID);
        // check for existing entry with this UUID (possible if UUID is not unique)
        if (storedDescriptionURL == null)
        {
          // store device description URL in hashtable
          controlPoint.getDeviceDescriptionURLFromUUIDTable().put(rootDeviceUUID, descriptionURL);

          if (!controlPoint.isSetDiscoveryOnly())
          {
            TemplateControlPoint.printMessage(controlPoint.toString() + ": Start    DEVICE RETRIEVAL for URL " +
              descriptionURL + ", UUID is " + rootDeviceUUID);
            // start device description retrieval
            startDeviceDescriptionRetrieval(discoverySocketStructure,
              descriptionURL,
              rootDeviceUUID,
              serverValue,
              deviceDiscoveryInfo.getMaxage(),
              NLSValue);
          }
        } else
        {
          // check if the published URL is equal to the stored one
          if (!storedDescriptionURL.equals(descriptionURL))
          {
            // this also happens if both the device and control point run
            // on the same computer and this computer has more than one network interface
            if (IPHelper.isLocalHostAddressString(storedDescriptionURL.getHost()) &&
              IPHelper.isLocalHostAddressString(descriptionURL.getHost()))
            {
              TemplateControlPoint.printMessage(controlPoint.toString() +
                ": INFO: Found local UPnP device that is announced on multiple network interfaces");

              // store new description URL in old discovery info
              CPDeviceDiscoveryInfo storedDiscoveryInfo =
                (CPDeviceDiscoveryInfo)controlPoint.getDiscoveryInfoFromDescriptionURLTable().get(storedDescriptionURL);

              if (storedDiscoveryInfo != null)
              {
                TemplateControlPoint.printMessage(controlPoint.toString() + ": Store alternative IP " +
                  descriptionURL.getHost());

                storedDiscoveryInfo.getAlternativeIPAddressList().add(descriptionURL.getHost());
              }
            } else
            {
              TemplateControlPoint.printMessage(controlPoint.toString() +
                ": ERROR: Found two UPnP devices with the same UUID " + rootDeviceUUID +
                " but different description URLs (" + storedDescriptionURL + ", " + descriptionURL +
                "). Ignore device with URL " + descriptionURL.toString());

              controlPoint.getDiscoveryInfoFromDescriptionURLTable().remove(descriptionURL);
            }
          }
        }
      }
    }
  }

  /**
   * Removes a device from the control point.
   * 
   * @param notify
   *          NotifyParser object which contains notify message information
   * 
   */
  private void removeDevice(HTTPParser httpParser)
  {
    // System.out.println("REMOVE DEVICE with USN " + notify.getUSNValue());
    try
    {
      String inUSN = httpParser.getValue(CommonConstants.USN);

      if (inUSN.startsWith(UPnPConstant.DEVICE_UUID))
      {
        String[] stsplit = inUSN.split("::");

        if (stsplit.length < 1)
        {
          logger.warn("invalid unique device name value: " + inUSN);

          return;
        }

        String UUID = stsplit[0];
        CPDevice deviceToRemove = null;
        synchronized(controlPoint.getDeviceInfoLock())
        {
          // check if a device with this UUID is currently known
          URL deviceURL = (URL)controlPoint.getDeviceDescriptionURLFromUUIDTable().get(UUID);
          if (deviceURL != null)
          {
            deviceToRemove = (CPDevice)controlPoint.getCPDeviceFromDescriptionURLTable().get(deviceURL);
            if (deviceToRemove == null)
            {
              TemplateControlPoint.printMessage(controlPoint.toString() +
                ": DeviceURL is known, but no device associated");
              controlPoint.getDeviceDescriptionURLFromUUIDTable().remove(UUID);
              controlPoint.getDiscoveryInfoFromDescriptionURLTable().remove(deviceURL);
            }
          }
        }
        // remove outside of the synchronized block
        if (deviceToRemove != null)
        {
          controlPoint.removeRootDevice(deviceToRemove, false);
        }

      }
    } catch (Exception e)
    {
      e.printStackTrace();
      logger.error("removeDevice not possible:" + e);
    }
  }

  /**
   * Starts a new device description retrieval
   * 
   * @param discoverySocketStructure
   * @param descriptionURL
   * @param rootDeviceUUID
   * @param serverValue
   * @param maxAge
   * @param NLSValue
   */
  protected void startDeviceDescriptionRetrieval(ControlPointHostAddressSocketStructure discoverySocketStructure,
    URL descriptionURL,
    String rootDeviceUUID,
    String serverValue,
    int maxAge,
    String NLSValue)
  {
    // create and start device description retrieval thread
    new CPDeviceDescriptionRetrieval(controlPoint,
      discoverySocketStructure,
      descriptionURL,
      rootDeviceUUID,
      serverValue,
      maxAge,
      NLSValue,
      IPVersion);
  }

}
