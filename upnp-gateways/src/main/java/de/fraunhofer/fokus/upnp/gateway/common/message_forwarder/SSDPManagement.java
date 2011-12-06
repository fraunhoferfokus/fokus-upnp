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
package de.fraunhofer.fokus.upnp.gateway.common.message_forwarder;

import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.AbstractDevice;
import de.fraunhofer.fokus.upnp.core.MSearchMessageProcessor;
import de.fraunhofer.fokus.upnp.core.control_point.CPDevice;
import de.fraunhofer.fokus.upnp.gateway.common.SSDPMSearchHelper;
import de.fraunhofer.fokus.upnp.gateway.common.SSDPNotifyHelper;
import de.fraunhofer.fokus.upnp.gateway.common.forwarder_module.IForwarderModule;
import de.fraunhofer.fokus.upnp.ssdp.MSearchMessageProcessorResult;
import de.fraunhofer.fokus.upnp.util.CollectionHelper;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.SSDPHelper;
import de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageHelper;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.network.SocketHelper;
import de.fraunhofer.fokus.upnp.util.network.UDPPacketManager;
import de.fraunhofer.fokus.upnp.util.threads.IEventListener;

/**
 * This class forwards multicast NOTIFY messages between different forwarder modules, M-SEARCH
 * messages are answered directly. It is part of the central message forwarder
 * 
 * @author Alexander Koenig
 */
public class SSDPManagement implements IEventListener
{

  private MessageForwarder messageForwarder;

  private Vector           newModuleList = new Vector();

  public SSDPManagement(MessageForwarder messageForwarder)
  {
    this.messageForwarder = messageForwarder;
  }

  /**
   * Processes a received NOTIFY message.
   * 
   * @param sourceModuleID
   *          The ID of the module that received the NOTIFY
   * @param message
   *          The received message
   * 
   */
  public void processNotifyMessage(IForwarderModule sourceModule, HTTPMessageObject message)
  {
    // System.out.println("SSDPManagement: Process " + message.toString() + " from " +
    // sourceModule);

    String header = message.getHeader();
    // extract location to find invalid devices
    try
    {
      URL location = new URL(HTTPMessageHelper.getHeaderLine(header, CommonConstants.LOCATION));

      if (messageForwarder.getTemplateControlPoint().getBasicControlPoint().isIgnoredDeviceAddress(location.getHost()))
      {
        System.out.println("Ignore discovery NOTIFY for " + location);
        return;
      }
    } catch (Exception e)
    {
    }

    // extract device UUID to check visibility
    String uuid = SSDPHelper.getUUIDFromNotifyMessage(message.getHeader());

    // forward message to all other modules for which this device should be visible
    IForwarderModule[] forwarderModules = messageForwarder.getForwarderModules();
    for (int i = 0; i < forwarderModules.length; i++)
    {
      if (!forwarderModules[i].getModuleID().equals(sourceModule.getModuleID()) &&
        forwarderModules[i].announceDeviceToForwarderModule(uuid))
      {
        forwarderModules[i].forwardNotifyMessage(sourceModule.getModuleID(), message);
      }
    }
  }

  /**
   * Forwards a received M-SEARCH message. This instructs forwarder modules to forward the M-SEARCH
   * over their interfaces.
   * 
   * @param sourceModuleID
   *          The receiving source module
   * @param message
   *          The received message
   * @param replyAddress
   *          The target address for response messages
   * 
   * @return A vector with all generated response messages
   * 
   */
  public void forwardMSearchMessage(String sourceModuleID, HTTPMessageObject message, InetSocketAddress replyAddress)
  {
    System.out.println("Forward " + HTTPMessageHelper.getHeaderDescription(message.getHeader()) + " from " +
      sourceModuleID + " to " + (messageForwarder.getForwarderModuleCount() - 1) + " other module(s)");

    // forward M-SEARCH to all other modules
    // modules can decide individually if they forward the M-SEARCH or not
    IForwarderModule[] forwarderModules = messageForwarder.getForwarderModules();
    for (int i = 0; i < forwarderModules.length; i++)
    {
      if (!forwarderModules[i].getModuleID().equals(sourceModuleID))
      {
        forwarderModules[i].forwardMSearchMessage(sourceModuleID, message, replyAddress);
      }
    }
  }

  /**
   * Processes a received M-SEARCH message. This is done by creating response messages for all
   * devices known by the internal control point.
   * 
   * @param sourceModule
   *          The module that received the M-SEARCH message
   * @param message
   *          The received message
   * 
   * @return A vector with all generated response messages
   * 
   */
  public Vector processMSearchMessage(IForwarderModule sourceModule, HTTPMessageObject message)
  {
    System.out.println("      " + sourceModule.toString() + ": Process " + message.toString() +
      " in message forwarder control point (" + messageForwarder.getTemplateControlPoint().getCPDeviceCount() +
      " known devices)");

    InetSocketAddress sourceHTTPServerAddress = sourceModule.getHTTPServerSocketAddress();

    // generate response messages for all devices known by the internal control point
    Vector result = new Vector();

    // enumerate through all known devices
    for (int i = 0; i < messageForwarder.getTemplateControlPoint().getCPDeviceCount(); i++)
    {
      CPDevice currentDevice = messageForwarder.getTemplateControlPoint().getCPDevice(i);

      // retrieve module that handle requests for the current device
      IForwarderModule forwarderModule = messageForwarder.getForwarderModuleByDevice(currentDevice);

      // send responses for all devices that were not discovered at the source module
      if (forwarderModule != null && !forwarderModule.getModuleID().equals(sourceModule.getModuleID()) &&
        sourceModule.announceDeviceToForwarderModule(currentDevice))
      {
        // get responses for current device
        MSearchMessageProcessorResult msearchMessageProcessorResult =
          MSearchMessageProcessor.processMessage(currentDevice,
            currentDevice.getDeviceDescriptionSocketAddress(),
            message.getHeader(),
            currentDevice.getIPVersion());

        if (msearchMessageProcessorResult != null && msearchMessageProcessorResult.getMessageCount() > 0)
        {
          // go through all response messages
          for (int j = 0; j < msearchMessageProcessorResult.getMessageCount(); j++)
          {
            String responseMessage = msearchMessageProcessorResult.getResponseMessage(j);
            // change response messages to allow proper forwarding of resulting GET message
            String modifiedResponseMessage =
              SSDPMSearchHelper.extendMSearchResponseMessage(responseMessage,
                forwarderModule.getModuleID(),
                sourceHTTPServerAddress);

            // add changed response message to result
            result.add(modifiedResponseMessage);
          }
        }
      }
    }
    return result;
  }

  /**
   * Forwards a M-SEARCH response message to a forwarder module.
   * 
   * @param sourceModuleID
   *          The module that received the M-SEARCH message
   * @param forwarderModuleID
   *          The ID of the module that received the M-SEARCH response
   * @param message
   *          The response message
   * @param replyAddress
   *          The address that should receive the responses
   */
  public void forwardMSearchResponseMessage(String sourceModuleID,
    String forwarderModuleID,
    HTTPMessageObject message,
    InetSocketAddress replyAddress)
  {
    // forward response to module that received the M-SEARCH message
    IForwarderModule sourceModule = messageForwarder.getForwarderModuleByID(sourceModuleID);
    if (sourceModule != null)
    {
      sourceModule.forwardMSearchResponseMessage(message, forwarderModuleID, replyAddress);
    }
  }

  /**
   * Generates NOTIFY messages for all local devices to be sent over a new forwarder module.
   * 
   * @param newModule
   *          The new module that should forward the NOTIFY messages
   * 
   */
  public void sendInitialNotifyMessages(IForwarderModule newModule)
  {
    // generate response messages for all devices known by the internal control point
    Vector result = generateInitialNotifyMessages(newModule);

    System.out.println(newModule + ": Send " + result.size() + " NOTIFY messages to " + newModule);

    newModule.sendMessagesToSSDPMulticast(result);
  }

  /**
   * Generates NOTIFY messages for all local devices to be sent over a new forwarder module.
   * 
   * @param newModule
   *          The new module that should forward the NOTIFY messages
   * 
   */
  public Vector generateInitialNotifyMessages(IForwarderModule newModule)
  {
    // generate response messages for all devices known by the internal control point
    Vector result = new Vector();

    // enumerate through all known devices
    for (int i = 0; i < messageForwarder.getTemplateControlPoint().getCPDeviceCount(); i++)
    {
      CPDevice currentDevice = messageForwarder.getTemplateControlPoint().getCPDevice(i);
      Vector replyMessages = generateNotifyMessagesForDevice(newModule, currentDevice);
      // add reply messages for the current device to the result
      if (replyMessages != null)
      {
        result.addAll(replyMessages);
      }
    }
    return result;
  }

  /**
   * Sends NOTIFY messages for a new device to all forwarder modules.
   * 
   * @param device
   *          The newly discovered device
   * 
   */
  public void sendInitialNotifyMessagesForDevice(CPDevice device)
  {
    IForwarderModule[] forwarderModules = messageForwarder.getForwarderModules();
    // enumerate through all known forwarder modules
    for (int i = 0; i < forwarderModules.length; i++)
    {
      Vector notifyMessages = generateNotifyMessagesForDevice(forwarderModules[i], device);
      if (notifyMessages != null)
      {
        System.out.println("      Send initial NOTIFY messages for device " + device.getFriendlyName() + " to " +
          forwarderModules[i].getModuleID());

        forwarderModules[i].sendMessagesToSSDPMulticast(notifyMessages);
      }
    }
  }

  /**
   * Generates initial NOTIFY messages for one local device.
   * 
   * @param forwarderModule
   *          The module that should forward the NOTIFY messages
   * @param device
   *          The device for which the messages should be generated
   * 
   * @return A vector with all generated NOTIFY messages or null
   * 
   */
  public Vector generateNotifyMessagesForDevice(IForwarderModule forwarderModule, AbstractDevice device)
  {
    // retrieve module that discovered the current device
    IForwarderModule discoveryModule = messageForwarder.getForwarderModuleByDevice(device);

    // send NOTIFY messages for devices that were not discovered at the forwarder module
    // and that should be visible over this module
    if (discoveryModule != null && !discoveryModule.getModuleID().equals(forwarderModule.getModuleID()) &&
      forwarderModule.announceDeviceToForwarderModule(device))
    {
      // create NOTIFY messages
      Vector replyMessages =
        SSDPNotifyHelper.createNotifyMessagesForForwarding(device,
          discoveryModule.getModuleID(),
          forwarderModule.getHTTPServerAddress(),
          forwarderModule.getHTTPServerPort());

      System.out.println("      Generate " + replyMessages.size() + " NOTIFY message(s) for device " +
        device.toDiscoveryString() + " for forwarder module " + forwarderModule);

      return replyMessages;
    }

    return null;
  }

  /**
   * Send final NOTIFY bye bye messages for all devices that were connected over a removed module to
   * all other forwarder modules.
   * 
   * @param removedModule
   *          Module that was removed
   * 
   */
  public void sendFinalNotifyByeByeMessages(IForwarderModule removedModule)
  {
    // generate response messages for all devices connected over the removed forwarder module
    Vector result = new Vector();

    // enumerate through all known devices
    for (int i = 0; i < messageForwarder.getTemplateControlPoint().getCPDeviceCount(); i++)
    {
      CPDevice currentDevice = messageForwarder.getTemplateControlPoint().getCPDevice(i);
      Vector replyMessages = generateNotifyByeByeMessagesForDevice(removedModule, currentDevice);
      // add reply messages for the current device to the result
      if (replyMessages != null)
      {
        result.addAll(replyMessages);
      }
    }
    System.out.println("ForwarderModule: Send " + result.size() + " NOTIFY bye bye messages for removed module " +
      removedModule.getModuleID() + " to all other modules");

    IForwarderModule[] forwarderModules = messageForwarder.getForwarderModules();
    for (int i = 0; i < forwarderModules.length; i++)
    {
      if (!forwarderModules[i].getModuleID().equals(removedModule.getModuleID()))
      {
        forwarderModules[i].sendMessagesToSSDPMulticast(result);
      }
    }
  }

  /**
   * Tries to generate NOTIFY bye bye messages for one local device.
   * 
   * @param removedModule
   *          The module that was removed
   * @param device
   *          The device for which the messages should be generated
   * 
   * @return A vector with all generated NOTIFY bye bye messages or null
   * 
   */
  public Vector generateNotifyByeByeMessagesForDevice(IForwarderModule removedModule, AbstractDevice device)
  {
    // retrieve module that discovered the current device
    IForwarderModule discoveryModule = messageForwarder.getForwarderModuleByDevice(device);

    // send NOTIFY bye bye messages for devices that were discovered at the removed module
    if (discoveryModule != null && discoveryModule.getModuleID().equals(removedModule.getModuleID()))
    {
      // create NOTIFY messages
      Vector byebyeMessages = SSDPNotifyHelper.createNotifyByeByeMessages(device);

      if (byebyeMessages != null)
      {
        System.out.println("Generate " + byebyeMessages.size() + " NOTIFY byebye messages for " +
          device.getFriendlyName());
      }

      return byebyeMessages;
    }
    return null;
  }

  /** Adds a new forwarder module for sending initial NOTIFY messages. */
  public void addNewForwarderModule(IForwarderModule forwarderModule)
  {
    // store new module to send initial NOTIFY messages to the module after some delay
    if (!newModuleList.contains(forwarderModule))
    {
      newModuleList.add(forwarderModule);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.threads.IEventListener#triggerEvents()
   */
  public void triggerEvents()
  {
    UDPPacketManager udpPacketManager = messageForwarder.getUDPPacketManager();
    // read SSDP messages from all modules
    IForwarderModule[] forwarderModules = messageForwarder.getForwarderModules();
    for (int i = 0; i < forwarderModules.length; i++)
    {
      boolean readUDP = true;
      while (readUDP)
      {
        BinaryMessageObject message =
          SocketHelper.readBinaryMessage(udpPacketManager,
            forwarderModules[i].getSSDPSocket(),
            SocketHelper.DEFAULT_SOCKET_TIMEOUT);

        if (message != null)
        {
          // check packet type
          String packetContent = message.getBodyAsString();

          if (packetContent.startsWith(CommonConstants.NOTIFY))
          {
            // start handling in receiving module
            forwarderModules[i].receivedNotifyMessage(new HTTPMessageObject(packetContent, message.getSourceAddress()));
          }
          if (packetContent.startsWith(CommonConstants.M_SEARCH))
          {
            // start handling in receiving module
            forwarderModules[i].receivedMSearchMessage(new HTTPMessageObject(packetContent, message.getSourceAddress()));
          }
        } else
        {
          readUDP = false;
        }
      }
      // send initial NOTIFY messages to new forwarder modules
      Enumeration newModules = CollectionHelper.getPersistentEntryEnumeration(newModuleList);
      while (newModules.hasMoreElements())
      {
        IForwarderModule currentModule = (IForwarderModule)newModules.nextElement();
        // wait some time after module creation until the first NOTIFY messages are sent
        if (System.currentTimeMillis() - currentModule.getCreationTime() > 3000)
        {
          sendInitialNotifyMessages(currentModule);
          newModuleList.remove(currentModule);
        }
      }
    }
  }

}
