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
package de.fraunhofer.fokus.upnp.gateway.internet.device_directory_device;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.control_point.CPDevice;
import de.fraunhofer.fokus.upnp.core.device.common.TranslationService;
import de.fraunhofer.fokus.upnp.core.templates.TemplateControlPoint;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;
import de.fraunhofer.fokus.upnp.core_security.templates.SecuredTemplateDevice;
import de.fraunhofer.fokus.upnp.gateway.common.ExternalSSDPManagement;
import de.fraunhofer.fokus.upnp.gateway.common.message_forwarder.SSDPManagement;
import de.fraunhofer.fokus.upnp.gateway.internet.InternetForwarderModule;
import de.fraunhofer.fokus.upnp.ssdp.NotifyMessageBuilder;
import de.fraunhofer.fokus.upnp.ssdp.SSDPConstant;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;

/**
 * This is the main class for the device directory device. It works as gateway between different
 * UPnP domains and can be used to build a clustered UPnP network. Domains can be connected in
 * different ways, e.g. transparent, evented or manual.
 * 
 * @author Alexander Koenig
 */
public class DeviceDirectoryDevice extends SecuredTemplateDevice
{

  private static final String            AUTO_CONNECTED_PEERS_FILE_NAME = "autoConnectedPeers.txt";

  // List of URLs with all peers that should be autoconnected
  private Vector                         autoConnectedPeersList;

  // List with all peers that are really connected
  private Vector                         connectedPeersList;

  /** Forwards M-SEARCH messages to other peers and listens for response packets */
  private SSDPMSearchToInternetForwarder ssdpMSearchToInternetForwarder;

  /** Handles DeviceDirectoryDevice peer discovery */
  private DDPeerDiscovery                ddPeerDiscovery;

  /** Module to receive SSDP messages over Internet router */
  private ExternalSSDPManagement         externalSSDPManagement;

  /** Message modifier for HTTP requests */
  private DDDeviceHTTPMessageModifier    dddeviceHTTPMessageModifier;

  /** Message modifier for SSDP requests */
  private DDDeviceSSDPMessageModifier    dddeviceSSDPMessageModifier;

  /** Service that handles device discovery */
  private DDDeviceDiscoveryService       discoveryService;

  private int                            lastConnectionID               = 0;

  /** Creates a new instance of DeviceDirectoryServer */
  public DeviceDirectoryDevice(DeviceDirectoryEntity anEntity, UPnPStartupConfiguration startupConfiguration)
  {
    super(anEntity, startupConfiguration);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.Device#initDeviceContent()
   */
  public void initDeviceContent()
  {
    super.initDeviceContent();
    setFriendlyName(getFriendlyName() + "(" + IPHelper.getLocalHostName() + ")");

    autoConnectedPeersList = new Vector();
    connectedPeersList = new Vector();
    loadAutoConnectedPeersFromFile(getWorkingDirectory() + AUTO_CONNECTED_PEERS_FILE_NAME);

    // start external SSDP reception without packet manager
    externalSSDPManagement = new ExternalSSDPManagement(null);

    // start thread for NOTIFY and M-SEARCH forwarding
    ssdpMSearchToInternetForwarder = new SSDPMSearchToInternetForwarder(this);

    // start peer device discovery
    ddPeerDiscovery = new DDPeerDiscovery(this);

    // create message modifier for device HTTP server
    dddeviceHTTPMessageModifier = new DDDeviceHTTPMessageModifier(this);
    // associate message modifier with device HTTP server
    httpMessageProcessor.setMessageModifier(dddeviceHTTPMessageModifier);

    // create message modifier for device SSDP server
    dddeviceSSDPMessageModifier = new DDDeviceSSDPMessageModifier(this);

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // Services //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    TranslationService translationService = new TranslationService(this);
    addTemplateService(translationService);

    discoveryService = new DDDeviceDiscoveryService(this);
    addTemplateService(discoveryService);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp_security.templates.SecuredTemplateDevice#runDevice()
   */
  public void runDevice()
  {
    super.runDevice();

    // associate message modifier with M-SEARCH server
    getDeviceMSearchServer().setSSDPMessageModifier(dddeviceSSDPMessageModifier);
  }

  /** Starts the connection process to known peers. */
  public void autoConnectDevices()
  {
    // try to autoconnect peer devices
    for (int i = 0; i < autoConnectedPeersList.size(); i++)
    {
      DeviceDirectoryPeer currentPeer = (DeviceDirectoryPeer)autoConnectedPeersList.elementAt(i);
      discoveryService.autoConnect(currentPeer);
    }
    discoveryService.updateAutoConnectionIDs();
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Device management //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /** Retrieves the type of a specific device */
  public boolean isLocalDevice(String udn)
  {
    CPDevice device = getMessageForwarderControlPoint().getCPDeviceByUDN(udn);

    InetSocketAddress internetHTTPServerAddress =
      getDeviceDirectoryEntity().getInternetManagement()
        .getInternetHostAddressSocketStructure()
        .getHTTPServerSocketAddress();

    // device is global if its device description URL is equal to address:port of the Internet HTTP
    // server
    if (device != null)
    {
      InetSocketAddress deviceHTTPServerAddress = device.getDeviceDescriptionSocketAddress();
      return !deviceHTTPServerAddress.equals(internetHTTPServerAddress);
    }
    return false;
  }

  /** Checks if a specific device is known */
  public boolean isKnownDevice(String udn)
  {
    return getMessageForwarderControlPoint().isKnownCPDevice(udn);
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Peer management //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  public void newDeviceDirectoryDevice(DeviceDirectoryCPDevice device)
  {
    // forward to discovery service, the appropriate peer is connected to the device
    discoveryService.newDeviceDirectoryDevice(device);
    // check if a peer was found
    if (device.getPeer() != null)
    {
      connectedPeersList.add(device);
      discoveryService.updateConnectedIDs();

      // call establish connection to finish handshake
      discoveryService.invokeConnectionEstablished(device);
    }
  }

  public void deviceDirectoryDeviceGone(CPDevice device)
  {
    int index = getConnectedPeerIndex(device);
    // check if a peer was found
    if (index != -1)
    {
      DeviceDirectoryCPDevice goneDevice = getConnectedPeer(index);
      connectedPeersList.remove(index);
      discoveryService.updateConnectedIDs();
      // check if peer is in autoConnect list
      for (int i = 0; i < autoConnectedPeersList.size(); i++)
      {
        DeviceDirectoryPeer currentPeer = (DeviceDirectoryPeer)autoConnectedPeersList.elementAt(i);
        if (currentPeer == goneDevice.getPeer())
        {
          // peer found
          currentPeer.resetPeer();
          discoveryService.autoConnect(currentPeer);
        }
      }
    }
  }

  /** Returns a list with IDs for connected peers */
  public String buildConnectedIDList()
  {
    String result = "";
    for (int i = 0; i < getConnectedPeersCount(); i++)
    {
      result += (result.length() > 0 ? "," : "") + getConnectedPeer(i).getConnectionID();
    }

    return result;
  }

  /** Returns a list with IDs for autoconnected peers */
  public String buildAutoconnectIDList()
  {
    String result = "";
    for (int i = 0; i < getAutoConnectedPeersCount(); i++)
    {
      if (getAutoConnectedPeer(i).getConnectionID() != -1)
      {
        result += (result.length() > 0 ? "," : "") + getAutoConnectedPeer(i).getConnectionID();
      }
    }

    return result;
  }

  /** Returns a unique ID */
  public int getUniqueConnectionID()
  {
    lastConnectionID++;
    return lastConnectionID;
  }

  /** Retrieve the CPDevice for a specific peer address */
  public DeviceDirectoryCPDevice getDeviceDirectoryCPDevice(DeviceDirectoryPeer peer)
  {
    Vector deviceList = getDeviceDirectoryEntity().getDeviceDirectoryDeviceList();
    for (int i = 0; i < deviceList.size(); i++)
    {
      DeviceDirectoryCPDevice currentCPDevice = (DeviceDirectoryCPDevice)deviceList.elementAt(i);
      if (currentCPDevice.getPeer().equals(peer))
      {
        return currentCPDevice;
      }
    }

    return null;
  }

  public int getAutoConnectedPeersCount()
  {
    return autoConnectedPeersList.size();
  }

  public DeviceDirectoryPeer getAutoConnectedPeer(int index)
  {
    if (index >= 0 && index < autoConnectedPeersList.size())
    {
      return (DeviceDirectoryPeer)autoConnectedPeersList.elementAt(index);
    }

    return null;
  }

  public int getConnectedPeersCount()
  {
    return connectedPeersList.size();
  }

  public DeviceDirectoryCPDevice getConnectedPeer(int index)
  {
    if (index >= 0 && index < connectedPeersList.size())
    {
      return (DeviceDirectoryCPDevice)connectedPeersList.elementAt(index);
    }

    return null;
  }

  /** Retrieve the index of a peer from a device */
  public int getConnectedPeerIndex(CPDevice device)
  {
    for (int i = 0; i < connectedPeersList.size(); i++)
    {
      DeviceDirectoryCPDevice currentCPDevice = (DeviceDirectoryCPDevice)connectedPeersList.elementAt(i);
      if (currentCPDevice.getCPDevice() == device)
      {
        return i;
      }
    }
    return -1;
  }

  /** Retrieve the index of a peer from its address */
  public int getConnectedPeerIndex(InetAddress address)
  {
    for (int i = 0; i < connectedPeersList.size(); i++)
    {
      DeviceDirectoryCPDevice currentCPDevice = (DeviceDirectoryCPDevice)connectedPeersList.elementAt(i);
      if (currentCPDevice.getPeerAddress().equals(address))
      {
        return i;
      }
    }
    return -1;
  }

  private void loadAutoConnectedPeersFromFile(String fileName)
  {
    autoConnectedPeersList.clear();
    System.out.println("  Loading auto connected peers from file: " + fileName);
    if (new File(fileName).exists())
    {
      System.out.println("    File found");
      try
      {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "utf-8"));
        String line;
        line = reader.readLine();
        while (line != null)
        {
          // make sure there wasn`t just an unnecessary newline at the end...
          // ignore comments
          if (line.length() > 0 && line.charAt(0) != '#')
          {
            DeviceDirectoryPeer peer = DeviceDirectoryPeer.parsePeerAddress(line);
            // compare peer to own global address
            if (peer != null &&
              !getDeviceDirectoryEntity().getInternetManagement().getGlobalIPAddress().equals(peer.getAddress()))
            {
              autoConnectedPeersList.add(peer);
            }
          }
          line = reader.readLine();
        }
        reader.close();
      } catch (Exception ex)
      {
        ex.printStackTrace(System.out);
      }
      System.out.println("    " + autoConnectedPeersList.size() + " peers loaded");
    } else
    {
      System.out.println("    File not found");
    }
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Management //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  public DeviceDirectoryEntity getDeviceDirectoryEntity()
  {
    return (DeviceDirectoryEntity)getTemplateEntity();
  }

  public SSDPMSearchToInternetForwarder getSSDPMSearchToInternetForwarder()
  {
    return ssdpMSearchToInternetForwarder;
  }

  public DDPeerDiscovery getDDPeerDiscovery()
  {
    return ddPeerDiscovery;
  }

  public TemplateControlPoint getMessageForwarderControlPoint()
  {
    return getDeviceDirectoryEntity().getMessageForwarderControlPoint();
  }

  /**
   * Retrieves the externalSSDPManagement.
   * 
   * @return The externalSSDPManagement.
   */
  public ExternalSSDPManagement getExternalSSDPManagement()
  {
    return externalSSDPManagement;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.Device#terminate()
   */
  public void terminate()
  {
    // send ssdp:byebye message for all local devices that will not be available after removal
    // of the gateway
    // send ssdp:byebye message for all remote devices to local multicast
    for (int i = 0; i < getMessageForwarderControlPoint().getCPDeviceCount(); i++)
    {
      CPDevice currentDevice = getMessageForwarderControlPoint().getCPDevice(i);
      if (isLocalDevice(currentDevice.getUDN()))
      {
        Vector ssdpByeByeMessages =
          NotifyMessageBuilder.createAllMessages(currentDevice,
            currentDevice.getDeviceDescriptionSocketAddress(),
            SSDPConstant.SSDP_BYEBYE,
            getIPVersion());

        System.out.println("Send " + ssdpByeByeMessages.size() + " byebye messages for " +
          currentDevice.getFriendlyName() + " to transparent peers");

        // forward these messages to all transparently connected peers
        for (int j = 0; j < ssdpByeByeMessages.size(); j++)
        {
          String currentMessage = (String)ssdpByeByeMessages.elementAt(j);
          getDeviceDirectoryEntity().getInternetManagement()
            .getInternetForwarderModule()
            .forwardMessageToAllTransparentPeers(currentMessage);
        }
      } else
      {
        Vector ssdpByeByeMessages =
          NotifyMessageBuilder.createAllMessages(currentDevice,
            currentDevice.getDeviceDescriptionSocketAddress(),
            SSDPConstant.SSDP_BYEBYE,
            getIPVersion());

        System.out.println("Send " + ssdpByeByeMessages.size() + " byebye messages for " +
          currentDevice.getFriendlyName() + " to multicast");

        SSDPManagement ssdpManagement =
          getDeviceDirectoryEntity().getInternetManagement().getMessageForwarder().getSSDPManagement();

        InternetForwarderModule internetForwarderModule =
          getDeviceDirectoryEntity().getInternetManagement().getInternetForwarderModule();

        // process message in messageForwarder itself
        for (int j = 0; j < ssdpByeByeMessages.size(); j++)
        {
          String currentMessage = (String)ssdpByeByeMessages.elementAt(j);
          ssdpManagement.processNotifyMessage(internetForwarderModule, new HTTPMessageObject(currentMessage));
        }
      }
    }
    ssdpMSearchToInternetForwarder.terminate();
    externalSSDPManagement.terminate();

    super.terminate();
  }

}
