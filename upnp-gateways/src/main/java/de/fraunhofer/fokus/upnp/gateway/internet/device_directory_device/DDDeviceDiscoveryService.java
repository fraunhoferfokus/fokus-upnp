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

import java.net.InetAddress;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.control_point.CPAction;
import de.fraunhofer.fokus.upnp.core.control_point.CPService;
import de.fraunhofer.fokus.upnp.core.control_point.CPStateVariable;
import de.fraunhofer.fokus.upnp.core.device.Action;
import de.fraunhofer.fokus.upnp.core.device.StateVariable;
import de.fraunhofer.fokus.upnp.core_security.helpers.Permission;
import de.fraunhofer.fokus.upnp.core_security.templates.SecuredTemplateService;
import de.fraunhofer.fokus.upnp.gateway.common.SSDPNotifyHelper;
import de.fraunhofer.fokus.upnp.gateway.common.forwarder_module.IForwarderModule;
import de.fraunhofer.fokus.upnp.gateway.common.message_forwarder.MessageForwarder;
import de.fraunhofer.fokus.upnp.gateway.internet.InternetManagement;
import de.fraunhofer.fokus.upnp.gateway.internet.InternetManagementConstants;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;
import de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject;
import de.fraunhofer.fokus.upnp.util.network.DatagramSocketWrapper;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;

/**
 * This service is used to connect different UPnP networks to form a new clustered network. It
 * handles the exchange of SSDP messages for device discovery.
 * 
 * @author Alexander Koenig
 */
public class DDDeviceDiscoveryService extends SecuredTemplateService implements Runnable
{

  /** IDs of currently connected peers (manual and auto) */
  private StateVariable connectedIDs;

  /** IDs of peers that should be autoconnected (independent from their actual connection status) */
  private StateVariable autoConnectionIDs;

  /** Port used for common device discovery */
  private StateVariable discoveryPort;

  /** IP address used for common device discovery */
  private StateVariable discoveryAddress;

  private StateVariable A_ARG_TYPE_ConnectionType;

  private StateVariable A_ARG_TYPE_ConnectionID;

  private StateVariable A_ARG_TYPE_ConnectionStatus;

  private StateVariable A_ARG_TYPE_DeviceType;

  private StateVariable A_ARG_TYPE_string;

  private StateVariable A_ARG_TYPE_int;

  private StateVariable A_ARG_TYPE_boolean;

  private Action        connect;

  private Argument      inIPAddress;

  private Argument      inPort;

  private Argument      inConnectionType;

  private Argument      outConnectionID;

  private Action        connectionEstablished;

  private Action        isBidirectionalConnection;

  private Argument      outBidirectional;

  private Action        disconnect;

  private Argument      inConnectionID;

  private Action        addAutoConnection;

  private Action        deleteAutoConnection;

  private Action        getCurrentConnectionIDs;

  private Argument      outConnectionIDs;

  private Action        getAutoConnectionIDs;

  private Action        getDiscoveryPort;

  private Argument      outDiscoveryPort;

  private Action        getDiscoveryAddress;

  private Argument      outDiscoveryAddress;

  private Action        getConnectionInfo;

  private Argument      outIPAddress;

  private Argument      outPort;

  private Argument      outConnectionType;

  private Argument      outConnectionStatus;

  private Action        getDeviceLocation;

  private Argument      inUDN;

  private Argument      outDeviceLocation;

  /** List of pending peers */
  private Vector        pendingPeers;

  private boolean       terminateThread = false;

  /** Creates a new instance of DeviceDirectoryDiscoveryService */
  public DDDeviceDiscoveryService(DeviceDirectoryDevice device)
  {
    super(device, InternetManagementConstants.DISCOVERY_SERVICE_TYPE, InternetManagementConstants.DISCOVERY_SERVICE_ID);
  }

  public void setupServiceVariables()
  {
    super.setupServiceVariables();
    pendingPeers = new Vector();
  }

  /** Initializes service specific properties */
  public void initServiceContent()
  {
    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // State variables //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    connectedIDs = new StateVariable("ConnectedIDs", "", true);
    autoConnectionIDs = new StateVariable("AutoConnectionIDs", "", true);
    // Port used for common device discovery
    discoveryPort = new StateVariable("DiscoveryPort", InternetManagementConstants.SSDP_DEVICE_PORT, true);

    InetAddress globalIPAddress =
      getDeviceDirectoryDevice().getDeviceDirectoryEntity().getInternetManagement().getGlobalIPAddress();
    discoveryAddress = new StateVariable("DiscoveryAddress", globalIPAddress.getHostAddress(), true);

    A_ARG_TYPE_string = new StateVariable("A_ARG_TYPE_string", "", false);
    A_ARG_TYPE_int = new StateVariable("A_ARG_TYPE_int", 0, false);
    A_ARG_TYPE_boolean = new StateVariable("A_ARG_TYPE_boolean", false, false);
    A_ARG_TYPE_ConnectionID = new StateVariable("A_ARG_TYPE_ConnectionID", 0, false);
    A_ARG_TYPE_ConnectionType =
      new StateVariable("A_ARG_TYPE_ConnectionType", InternetManagementConstants.CONNECTION_TYPE_MANUAL, false);
    A_ARG_TYPE_ConnectionType.setAllowedValueList(new String[] {
        InternetManagementConstants.CONNECTION_TYPE_TRANSPARENT, InternetManagementConstants.CONNECTION_TYPE_EVENTED,
        InternetManagementConstants.CONNECTION_TYPE_MANUAL
    });
    A_ARG_TYPE_ConnectionStatus =
      new StateVariable("A_ARG_TYPE_ConnectionStatus",
        InternetManagementConstants.CONNECTION_STATUS_DISCONNECTED,
        false);
    A_ARG_TYPE_ConnectionStatus.setAllowedValueList(new String[] {
        InternetManagementConstants.CONNECTION_STATUS_CONNECTED,
        InternetManagementConstants.CONNECTION_STATUS_SEARCHED,
        InternetManagementConstants.CONNECTION_STATUS_DISCONNECTED
    });
    A_ARG_TYPE_DeviceType =
      new StateVariable("A_ARG_TYPE_DeviceType", InternetManagementConstants.DEVICE_LOCATION_UNKNOWN, false);
    A_ARG_TYPE_DeviceType.setAllowedValueList(new String[] {
        InternetManagementConstants.DEVICE_LOCATION_GLOBAL, InternetManagementConstants.DEVICE_LOCATION_LOCAL,
        InternetManagementConstants.DEVICE_LOCATION_UNKNOWN
    });

    StateVariable[] stateVariableList =
      {
          connectedIDs, autoConnectionIDs, discoveryPort, discoveryAddress, A_ARG_TYPE_int, A_ARG_TYPE_string,
          A_ARG_TYPE_boolean, A_ARG_TYPE_ConnectionID, A_ARG_TYPE_ConnectionType, A_ARG_TYPE_ConnectionStatus,
          A_ARG_TYPE_DeviceType
      };
    setStateVariableTable(stateVariableList);

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // Actions //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    connect = new Action("Connect");
    inIPAddress = new Argument("IPAddress", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_string);
    inPort = new Argument("Port", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_int);
    inConnectionType = new Argument("ConnectionType", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_ConnectionType);
    outConnectionID = new Argument("ConnectionID", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_ConnectionID);
    connect.setArgumentTable(new Argument[] {
        inIPAddress, inPort, inConnectionType, outConnectionID
    });

    connectionEstablished = new Action("ConnectionEstablished");
    inIPAddress = new Argument("IPAddress", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_string);
    inPort = new Argument("Port", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_int);
    connectionEstablished.setArgumentTable(new Argument[] {
        inIPAddress, inPort
    });

    isBidirectionalConnection = new Action("IsBidirectionalConnection");
    inIPAddress = new Argument("IPAddress", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_string);
    inPort = new Argument("Port", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_int);
    outBidirectional = new Argument("Bidirectional", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_boolean);
    isBidirectionalConnection.setArgumentTable(new Argument[] {
        inIPAddress, inPort, outBidirectional
    });

    disconnect = new Action("Disconnect");
    inConnectionID = new Argument("ConnectionID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_ConnectionID);
    disconnect.setArgumentTable(new Argument[] {
      inConnectionID
    });

    addAutoConnection = new Action("AddAutoConnection");
    inIPAddress = new Argument("IPAddress", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_string);
    inPort = new Argument("Port", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_int);
    inConnectionType = new Argument("ConnectionType", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_ConnectionType);
    outConnectionID = new Argument("ConnectionID", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_ConnectionID);
    addAutoConnection.setArgumentTable(new Argument[] {
        inIPAddress, inPort, inConnectionType, outConnectionID
    });

    deleteAutoConnection = new Action("DeleteAutoConnection");
    inConnectionID = new Argument("ConnectionID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_ConnectionID);
    deleteAutoConnection.setArgumentTable(new Argument[] {
      inConnectionID
    });

    getCurrentConnectionIDs = new Action("GetCurrentConnectionIDs");
    outConnectionIDs = new Argument("ConnectionIDs", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_string);
    getCurrentConnectionIDs.setArgumentTable(new Argument[] {
      outConnectionIDs
    });

    getAutoConnectionIDs = new Action("GetAutoConnectionIDs");
    outConnectionIDs = new Argument("ConnectionIDs", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_string);
    getAutoConnectionIDs.setArgumentTable(new Argument[] {
      outConnectionIDs
    });

    getDiscoveryPort = new Action("GetDiscoveryPort");
    outDiscoveryPort = new Argument("DiscoveryPort", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_int);
    getDiscoveryPort.setArgumentTable(new Argument[] {
      outDiscoveryPort
    });

    getDiscoveryAddress = new Action("GetDiscoveryAddress");
    outDiscoveryAddress = new Argument("DiscoveryAddress", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_string);
    getDiscoveryAddress.setArgumentTable(new Argument[] {
      outDiscoveryAddress
    });

    getConnectionInfo = new Action("GetConnectionInfo");
    inConnectionID = new Argument("ConnectionID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_ConnectionID);
    outIPAddress = new Argument("IPAddress", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_string);
    outPort = new Argument("Port", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_int);
    outConnectionType = new Argument("ConnectionType", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_ConnectionType);
    outConnectionStatus = new Argument("ConnectionStatus", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_ConnectionStatus);
    getConnectionInfo.setArgumentTable(new Argument[] {
        inConnectionID, outIPAddress, outPort, outConnectionType, outConnectionStatus
    });

    getDeviceLocation = new Action("GetDeviceLocation");
    inUDN = new Argument("UDN", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_string);
    outDeviceLocation = new Argument("DeviceLocation", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_DeviceType);
    getDeviceLocation.setArgumentTable(new Argument[] {
        inUDN, outDeviceLocation
    });

    Action[] actionList =
      {
          connect, connectionEstablished, isBidirectionalConnection, disconnect, addAutoConnection,
          deleteAutoConnection, getCurrentConnectionIDs, getAutoConnectionIDs, getDiscoveryPort, getDiscoveryAddress,
          getConnectionInfo, getDeviceLocation
      };
    setActionTable(actionList);

    // Actions that need signing
    Action[] unsignedActionList =
      {
          connect, connectionEstablished, isBidirectionalConnection, disconnect, getCurrentConnectionIDs,
          getAutoConnectionIDs, getDiscoveryPort, getDiscoveryAddress, getDeviceLocation
      };
    setUnsignedActionTable(unsignedActionList);

    // Actions permitted to all users
    Action[] allUserActions =
      {
          connect, connectionEstablished, isBidirectionalConnection, disconnect, getCurrentConnectionIDs,
          getAutoConnectionIDs, getDiscoveryPort, getDiscoveryAddress, getConnectionInfo, getDeviceLocation
      };
    setPermittedActionTable(allUserActions);
    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // Permissions //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    // trusted control points
    Permission trustedUserPermission =
      new Permission("DeviceDirectoryDiscoveryService.Trusted", "<mfgr:trusted/>", "", "Allows editing of peers");
    Action[] trustedUserActions = {
        addAutoConnection, deleteAutoConnection
    };
    addPermissionEntry(trustedUserPermission, trustedUserActions);
  }

  public void runService()
  {
    super.runService();
    Thread thread = new Thread(this);
    thread.setName("DeviceDirectoryDiscoveryService");
    thread.start();
  }

  public void terminate()
  {
    super.terminate();
    terminateThread = true;
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Action implementation //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /** Tries to establish a connection to a peer DeviceDirectoryDevice */
  public void connect(Argument[] args) throws ActionFailedException
  {
    System.out.println("connect invoked");
    if (args.length != 4)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    String ipAddress = (String)args[0].getValue();
    int port = 0;
    try
    {
      port = (int)args[1].getNumericValue();
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    // String connectionType = (String)args[2].getValue();

    if (ipAddress.length() == 0)
    {
      throw new ActionFailedException(802, "Invalid address");
    }

    InetAddress address = null;
    try
    {
      address = InetAddress.getByName(ipAddress);
    } catch (Exception ex)
    {
      throw new ActionFailedException(802, "Invalid address");
    }
    // check if this peer is already connected
    if (getDeviceDirectoryEntity().isKnownDeviceDirectoryDevice(address))
    {
      throw new ActionFailedException(801, "Already connected");
    }

    // check if peer is already waiting for connection
    if (isPendingPeer(address))
    {
      throw new ActionFailedException(803, "Already waiting for connection");
    }

    DeviceDirectoryPeer deviceDirectoryPeer = new DeviceDirectoryPeer(address, port);

    startSearch(deviceDirectoryPeer);

    // add to pending peers
    pendingPeers.add(deviceDirectoryPeer);

    try
    {
      // read connectionID associated with this peer
      args[3].setNumericValue(deviceDirectoryPeer.getConnectionID());
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  /** Check if a specific peer is already considered as connected */
  public void isBidirectionalConnection(Argument[] args) throws ActionFailedException
  {
    System.out.println("isBidirectionalConnection invoked");
    if (args.length != 3)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    String ipAddress = (String)args[0].getValue();
    int port = 0;
    try
    {
      port = (int)args[1].getNumericValue();
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    if (ipAddress.length() == 0)
    {
      throw new ActionFailedException(802, "Invalid address");
    }

    InetAddress address = null;
    try
    {
      address = InetAddress.getByName(ipAddress);
    } catch (Exception ex)
    {
      throw new ActionFailedException(802, "Invalid address");
    }

    boolean found = false;
    // search in connected devices
    for (int i = 0; !found && i < getDeviceDirectoryDevice().getConnectedPeersCount(); i++)
    {
      DeviceDirectoryCPDevice currentCPDevice = getDeviceDirectoryDevice().getConnectedPeer(i);
      DeviceDirectoryPeer currentPeer = currentCPDevice.getPeer();
      if (currentPeer.getAddress().equals(address) && currentPeer.getPort() == port)
      {
        found = true;
      }
    }
    try
    {
      args[2].setBooleanValue(found);
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }

    // isBidirectional is invoked by another peer that has already discovered me
    // check if I am searching for that peer
    DeviceDirectoryPeer pendingPeer = getPendingPeer(address);
    // peer is waiting, send new M-SEARCH message because the peer just informed us that
    // it is now connected
    if (pendingPeer != null)
    {
      // create M-SEARCH message to find the peer
      String searchMessage =
        getDeviceDirectoryDevice().getMessageForwarderControlPoint()
          .getBasicControlPoint()
          .createSearchRootDeviceMessage();

      BinaryMessageObject searchMessageObject =
        new BinaryMessageObject(StringHelper.stringToByteArray(searchMessage), pendingPeer.getSocketAddress());

      // send new M-SEARCH message
      getDeviceDirectoryDevice().getDDPeerDiscovery().sendSearchMessage(searchMessageObject);
    }
  }

  /** Signal to peer that the connection is now valid in both directions */
  public void connectionEstablished(Argument[] args) throws ActionFailedException
  {
    System.out.println("connectionEstablished invoked");
    System.out.println("Connected peer count is " + getDeviceDirectoryDevice().getConnectedPeersCount());

    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    String ipAddress = (String)args[0].getValue();
    int port = 0;
    try
    {
      port = (int)args[1].getNumericValue();
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }

    if (ipAddress.length() == 0)
    {
      throw new ActionFailedException(802, "Invalid address");
    }

    InetAddress address = null;
    try
    {
      address = InetAddress.getByName(ipAddress);
    } catch (Exception ex)
    {
      throw new ActionFailedException(802, "Invalid address");
    }

    DeviceDirectoryCPDevice peerDevice = null;
    // search in connected devices
    for (int i = 0; peerDevice == null && i < getDeviceDirectoryDevice().getConnectedPeersCount(); i++)
    {
      DeviceDirectoryCPDevice currentCPDevice = getDeviceDirectoryDevice().getConnectedPeer(i);
      DeviceDirectoryPeer currentPeer = currentCPDevice.getPeer();
      if (currentPeer.getAddress().equals(address) && currentPeer.getPort() == port)
      {
        peerDevice = currentCPDevice;
      }
    }

    // send NOTIFY messages for local devices to peer
    if (peerDevice != null)
    {
      sendInitialNotifyMessagesToPeer(peerDevice);
    }
  }

  public void getCurrentConnectionIDs(Argument[] args) throws ActionFailedException
  {
    if (args.length != 1)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    try
    {
      args[0].setValue(getDeviceDirectoryDevice().buildConnectedIDList());
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void getAutoConnectionIDs(Argument[] args) throws ActionFailedException
  {
    if (args.length != 1)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    try
    {
      args[0].setValue(getDeviceDirectoryDevice().buildAutoconnectIDList());
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void getDiscoveryPort(Argument[] args) throws ActionFailedException
  {
    if (args.length != 1)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    try
    {
      args[0].setNumericValue(discoveryPort.getNumericValue());
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void getDiscoveryAddress(Argument[] args) throws ActionFailedException
  {
    if (args.length != 1)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    try
    {
      args[0].setValue(discoveryAddress.getValue());
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  /** Retrieves informations for a specific connection */
  public void getConnectionInfo(Argument[] args) throws ActionFailedException
  {
    System.out.println("getConnectionInfo invoked");
    if (args.length != 5)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    int connectionID = 0;
    try
    {
      connectionID = (int)args[0].getNumericValue();
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }

    // find connection
    DeviceDirectoryPeer peer = null;
    // search in pending peers
    for (int i = 0; peer == null && i < pendingPeers.size(); i++)
    {
      DeviceDirectoryPeer currentPeer = (DeviceDirectoryPeer)pendingPeers.elementAt(i);
      if (currentPeer.getConnectionID() == connectionID)
      {
        peer = currentPeer;
      }
    }
    // nothing found, search in connected devices
    if (peer == null)
    {
      for (int i = 0; peer == null && i < getDeviceDirectoryDevice().getConnectedPeersCount(); i++)
      {
        DeviceDirectoryCPDevice currentCPDevice = getDeviceDirectoryDevice().getConnectedPeer(i);
        if (currentCPDevice.getConnectionID() == connectionID)
        {
          peer = currentCPDevice.getPeer();
        }
      }
    }
    if (peer == null)
    {
      throw new ActionFailedException(805, "Invalid connectionID");
    }
    try
    {
      // fill connection info associated with this peer
      args[1].setValue(peer.getAddress().getHostName());
      args[2].setNumericValue(peer.getPort());
      args[3].setValue(peer.getConnectionType());
      args[4].setValue(peer.getConnectionStatus());
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  /** Checks the location of a specific device */
  public void getDeviceLocation(Argument[] args) throws ActionFailedException
  {
    System.out.println("getDeviceLocation invoked");
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    String udn = (String)args[0].getValue();
    String result = InternetManagementConstants.DEVICE_LOCATION_UNKNOWN;

    if (getDeviceDirectoryDevice().isKnownDevice(udn))
    {
      if (getDeviceDirectoryDevice().isLocalDevice(udn))
      {
        result = InternetManagementConstants.DEVICE_LOCATION_LOCAL;
      } else
      {
        result = InternetManagementConstants.DEVICE_LOCATION_GLOBAL;
      }
    }
    try
    {
      args[1].setValue(result);
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Management //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /** This methods updates the list of connected peers. */
  public void updateConnectedIDs()
  {
    try
    {
      connectedIDs.setValue(getDeviceDirectoryDevice().buildConnectedIDList());
    } catch (Exception ex)
    {
    }
  }

  /** This methods updates the list of autoconnected peers. */
  public void updateAutoConnectionIDs()
  {
    try
    {
      autoConnectionIDs.setValue(getDeviceDirectoryDevice().buildAutoconnectIDList());
    } catch (Exception ex)
    {
    }
  }

  /**
   * This methods tries to connect to a specific peer. It is used for peers that should be
   * autoconnected.
   */
  public void autoConnect(DeviceDirectoryPeer peer)
  {
    // check if this peer is already connected or waiting for connection
    if (getDeviceDirectoryEntity().isKnownDeviceDirectoryDevice(peer.getAddress()) || isPendingPeer(peer.getAddress()))
    {
      return;
    }

    startSearch(peer);

    // add to pending peers
    pendingPeers.add(peer);
  }

  /** Returns the associated deviceDirectory device */
  private DeviceDirectoryDevice getDeviceDirectoryDevice()
  {
    return (DeviceDirectoryDevice)getTemplateDevice();
  }

  /** Returns the associated deviceDirectory entity */
  private DeviceDirectoryEntity getDeviceDirectoryEntity()
  {
    return getDeviceDirectoryDevice().getDeviceDirectoryEntity();
  }

  /** Retrieves a pending peer */
  private DeviceDirectoryPeer getPendingPeer(InetAddress address)
  {
    for (int i = 0; i < pendingPeers.size(); i++)
    {
      if (((DeviceDirectoryPeer)pendingPeers.elementAt(i)).getAddress().equals(address))
      {
        return (DeviceDirectoryPeer)pendingPeers.elementAt(i);
      }
    }

    return null;
  }

  /** Checks if a peer with this address is already searched */
  private boolean isPendingPeer(InetAddress address)
  {
    for (int i = 0; i < pendingPeers.size(); i++)
    {
      if (((DeviceDirectoryPeer)pendingPeers.elementAt(i)).getAddress().equals(address))
      {
        return true;
      }
    }

    return false;
  }

  /** Starts the search for a peer */
  private void startSearch(DeviceDirectoryPeer peer)
  {
    // set connectionID if previously unknown
    if (peer.getConnectionID() == -1)
    {
      peer.setConnectionID(getDeviceDirectoryDevice().getUniqueConnectionID());
    }

    peer.setConnectionStatus(InternetManagementConstants.CONNECTION_STATUS_SEARCHED);
    peer.setLastMSearchRequestTime(System.currentTimeMillis());

    // create M-SEARCH message to find the peer device
    String searchMessage =
      getDeviceDirectoryDevice().getMessageForwarderControlPoint()
        .getBasicControlPoint()
        .createSearchRootDeviceMessage();

    System.out.println("Start new search for peer: " + peer.getAddress().getHostName());

    BinaryMessageObject searchMessageObject =
      new BinaryMessageObject(StringHelper.stringToByteArray(searchMessage), null, peer.getSocketAddress());

    // send new M-SEARCH message
    getDeviceDirectoryDevice().getDDPeerDiscovery().sendSearchMessage(searchMessageObject);
  }

  /** This method sends ssdp:alive messages for all local devices to a newly connected peer */
  private void sendInitialNotifyMessagesToPeer(DeviceDirectoryCPDevice peerDevice)
  {
    InternetManagement internetManagement =
      getDeviceDirectoryDevice().getDeviceDirectoryEntity().getInternetManagement();

    MessageForwarder messageForwarder = internetManagement.getMessageForwarder();

    // create NOTIFY messages for all local devices
    Vector messages =
      messageForwarder.getSSDPManagement()
        .generateInitialNotifyMessages(internetManagement.getInternetForwarderModule());

    // retrieve forwarder module that can forward GET requests for the gateway itself
    IForwarderModule localHostAddressForwarderModule = messageForwarder.getForwarderModuleForLocalHost();
    // create NOTIFY for gateway device itself
    messages.add(SSDPNotifyHelper.createNotifyMessagesForForwarding(getDeviceDirectoryDevice(),
      localHostAddressForwarderModule.getModuleID(),
      internetManagement.getInternetForwarderModule().getHTTPServerAddress(),
      internetManagement.getInternetForwarderModule().getHTTPServerPort()));

    for (int i = 0; i < messages.size(); i++)
    {
      String message = (String)messages.elementAt(i);

      // send messages to peer
      SSDPNotifyHelper.sendNotify(message,
        new DatagramSocketWrapper(internetManagement.getInternetHostAddressSocketStructure().getSSDPDeviceSocket()),
        peerDevice.getDiscoverySocketAddress());
    }
  }

  public void newDeviceDirectoryDevice(DeviceDirectoryCPDevice device)
  {
    // System.out.println("Found new deviceDirectoryDevice:
    // "+device.getCPDevice().getDeviceDescriptionURL().getHost());

    // check if the controlled device has the same IP address as a pending peer
    try
    {
      // retrieve IP address for discovery
      CPService discoveryService =
        device.getCPDevice().getCPServiceByType(InternetManagementConstants.DISCOVERY_SERVICE_TYPE);
      CPStateVariable ipAddressVariable = discoveryService.getCPStateVariable("DiscoveryAddress");
      InetAddress discoveryAddress = InetAddress.getByName((String)ipAddressVariable.getValue());

      int i = 0;
      while (i < pendingPeers.size())
      {
        DeviceDirectoryPeer currentPeer = (DeviceDirectoryPeer)pendingPeers.elementAt(i);
        // compare IP addresses
        if (currentPeer.getAddress().equals(discoveryAddress))
        {
          // associate peer with found device
          device.setPeer(currentPeer);
          // update connection status
          currentPeer.setConnectionStatus(InternetManagementConstants.CONNECTION_STATUS_CONNECTED);

          // remove from pending list
          pendingPeers.remove(i);
        } else
        {
          i++;
        }
      }
    } catch (Exception ex)
    {
    }
  }

  public void invokeConnectionEstablished(DeviceDirectoryCPDevice device)
  {
    try
    {
      // check if the other device already knows me
      CPService discoveryService =
        device.getCPDevice().getCPServiceByType(InternetManagementConstants.DISCOVERY_SERVICE_TYPE);
      CPAction isBidirectionalAction = discoveryService.getCPAction("IsBidirectionalConnection");
      isBidirectionalAction.getInArgument("IPAddress").setValue(IPHelper.getLocalHostAddress().getHostAddress());
      isBidirectionalAction.getInArgument("Port").setNumericValue(InternetManagementConstants.SSDP_DEVICE_PORT);

      getDeviceDirectoryDevice().getMessageForwarderControlPoint().invokeAction(isBidirectionalAction);

      boolean isBidirectional =
        ((Boolean)isBidirectionalAction.getOutArgument("Bidirectional").getValue()).booleanValue();

      // connection is bidirectional, call connectionEstablished to finish handshake
      if (isBidirectional)
      {
        System.out.println("Call connection establish: Connection is bidirectional");

        // send NOTIFY messages for local devices to peer
        sendInitialNotifyMessagesToPeer(device);

        CPAction connectionEstablishedAction = discoveryService.getCPAction("ConnectionEstablished");
        connectionEstablishedAction.getInArgument("IPAddress").setValue(IPHelper.getLocalHostAddress());
        connectionEstablishedAction.getInArgument("Port").setNumericValue(InternetManagementConstants.SSDP_DEVICE_PORT);

        getDeviceDirectoryDevice().getMessageForwarderControlPoint().invokeAction(connectionEstablishedAction);
      } else
      {
        System.out.println("Call connection establish: Connection is not yet bidirectional");
      }
    } catch (Exception ex)
    {
      logger.warn("Could not establish connection:" + ex.getMessage());
    }
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Thread //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  public void run()
  {
    while (!terminateThread)
    {
      // start new M-SEARCH request after a certain amount of time without success
      for (int i = 0; i < pendingPeers.size(); i++)
      {
        DeviceDirectoryPeer currentPeer = (DeviceDirectoryPeer)pendingPeers.elementAt(i);
        // increase timeout with each failed attempt up to a certain maximum
        long timeout = Math.min(20, currentPeer.getMSearchRequestCount()) * 120000;
        if (System.currentTimeMillis() - currentPeer.getLastMSearchRequestTime() > timeout)
        {
          startSearch(currentPeer);
        }
      }
      try
      {
        Thread.sleep(200);
      } catch (Exception ex)
      {
      }
    }
  }

}
