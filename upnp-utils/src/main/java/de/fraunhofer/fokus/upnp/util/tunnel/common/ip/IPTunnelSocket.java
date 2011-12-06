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
package de.fraunhofer.fokus.upnp.util.tunnel.common.ip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.util.ThreadHelper;

/**
 * This class represents a Socket that receives packets over an IP tunnel.
 * 
 * @author Alexander Koenig
 */
public class IPTunnelSocket extends Thread
{
  public static int               CLOSED                               = 1;

  public static int               LISTEN                               = 2;

  public static int               SYN_RCVD                             = 3;

  public static int               SYN_SENT                             = 4;

  public static int               ESTABLISHED                          = 5;

  public static int               FIN_WAIT_1                           = 6;

  public static int               FIN_WAIT_2                           = 7;

  public static int               TIMED_WAIT                           = 8;

  public static int               CLOSING                              = 9;

  public static int               CLOSE_WAIT                           = 10;

  public static int               LAST_ACK                             = 11;

  public static long              INT_MASK                             = 0xFFFFFFFFl;

  public static boolean           DEBUG_STATE_TRANSITIONS              = true;

  /** Number of packets that can be buffered by the socket */
  private int                     RECEIVE_BUFFER_SIZE                  = 100;

  /** Enables Nagle's algorithm (default is true) */
  protected boolean               useNaglesAlgorithm                   = true;

  /** Enables hard closing (default is false) */
  protected boolean               useHardClose                         = false;

  /** Associated factory */
  protected IPTunnelSocketFactory ipTunnelSocketFactory;

  /** Associated server socket */
  protected IPTunnelServerSocket  serverSocket                         = null;

  /** Current TCP state of socket */
  protected int                   state;

  /** Virtual port used by this socket */
  protected int                   virtualPort;

  /** Destination address used by this socket */
  protected InetAddress           destinationAddress;

  /** Destination port used by this socket */
  protected int                   destinationPort;

  /** List of packets that wait for acknowledgment */
  private Vector                  unacknowledgedPacketList             = new Vector();

  /** List of packets that wait for sending (due to possible constrained send window) */
  private Vector                  sendPacketList                       = new Vector();

  /** List of packets that must be read by the input stream */
  private Vector                  receivedPacketList                   = new Vector();

  /** List of packets that were received out of order */
  private Vector                  outOfOrderPacketList                 = new Vector();

  /** Number of bytes that can be read from the input stream without blocking */
  private int                     receivedAvailableBytes;

  /** Buffer for send data */
  private byte[]                  currentSegmentData                   = null;

  /** Offset in send data buffer */
  private int                     currentSegmentOffset;

  /** Buffer for currently read received data */
  private byte[]                  readSegmentData                      = null;

  /** Offset in currently read received data buffer */
  private int                     readSegmentOffset                    = 0;

  /** Next sequence number for sending */
  private long                    sendSequenceNumber;

  /** Last received acknowledgement number */
  protected long                  lastReceivedAcknowledgementNumber;

  /** Time of first received packet that was not yet acknowledged */
  protected long                  firstUnacknowledgeReceivedPacketTime = 0;

  /** Time of last sent packet */
  protected long                  lastSentPacketTime                   = 0;

  /** Initial send sequence number */
  protected long                  initialSendSequenceNumber;

  /** Initial acknowledgement number */
  protected long                  initialAcknowledgementNumber;

  /** Last received window size */
  protected long                  lastReceivedWindowSize;

  /** Last used window size */
  protected long                  lastUsedWindowSize;

  /** Congestion window size */
  protected long                  congestionWindowSize;

  /** Congestion threshold */
  protected long                  congestionThreshold;

  /** Expected sequence number for next received packet == acknowledgment number sent in next packet */
  protected long                  expectedSequenceNumber;

  /** Output stream for this socket */
  protected IPTunnelOutputStream  outputStream;

  /** Input stream for this socket */
  protected IPTunnelInputStream   inputStream;

  /** Maximum data payload for this socket */
  protected int                   maximumPayloadSize                   = IPTunnelConstants.TCP_DEFAULT_PAYLOAD_SIZE;

  /** Flag that this socket only accepts one packet a time */
  protected boolean               acceptOnlySinglePackets              = true;

  /** Estimated round trip time */
  protected long                  roundTripTime                        = 3000;

  /** Round trip time deviation */
  protected long                  rttDeviation                         = 500;

  /** Time the socket was closed */
  protected long                  releaseTime                          = 0;

  /** Time the socket was created */
  protected long                  creationTime                         = System.currentTimeMillis();

  /** Last persistence probe */
  protected long                  lastWindowProbeTime                  = 0;

  /** Sync object */
  protected Object                receiveLock                          = new Object();

  protected Object                ackLock                              = new Object();

  protected Object                sendLock                             = new Object();

  protected int                   socketTimeout                        = 0;

  protected boolean               terminated                           = false;

  protected boolean               terminateThread                      = false;

  protected boolean               showStats                            = true;

  /**
   * Creates an uninitialized socket.
   * 
   * @param ipTunnelSocketFactory
   *          Associated tunnel management
   * @param localPort
   *          Local port used for this socket
   */
  public IPTunnelSocket(IPTunnelSocketFactory ipTunnelSocketFactory)
  {
    super("IPTunnelSocket");
    this.ipTunnelSocketFactory = ipTunnelSocketFactory;
    this.serverSocket = null;
    this.virtualPort = -1;

    destinationAddress = null;
    destinationPort = -1;

    // retrieve an initial sequence number for this socket
    sendSequenceNumber = -1;
    initialSendSequenceNumber = -1;

    // set sequence number for next expected packet (acknowledgement for syn response)
    expectedSequenceNumber = -1;
    initialAcknowledgementNumber = -1;

    // no acknowledgement has been received so far
    lastReceivedAcknowledgementNumber = -1;

    this.maximumPayloadSize = -1;

    currentSegmentOffset = 0;
    readSegmentOffset = 0;
    receivedAvailableBytes = 0;
    lastReceivedWindowSize = -1;
    // congestion window can be greater than just one packet
    congestionWindowSize = IPTunnelConstants.TCP_INITIAL_CONGESTION_WINDOW;
    congestionThreshold = 65536;

    state = CLOSED;
  }

  /**
   * Creates a new instance of IPTunnelSocket for a SYN request on a server socket.
   * 
   * @param ipTunnelSocketFactory
   *          Associated tunnel management
   * @param serverSocket
   *          Associated server socket
   * @param localPort
   *          Local port used for this socket
   * @param destinationSocketAddress
   *          Address this socket is connected to
   * @param receivedSequenceNumber
   *          Initial sequence number of the destination client
   * @param maximumPayloadSize
   *          Maximum payload size usable by this socket
   * 
   */
  public IPTunnelSocket(IPTunnelSocketFactory ipTunnelSocketFactory,
    IPTunnelServerSocket serverSocket,
    int localPort,
    InetSocketAddress destinationSocketAddress,
    long receivedSequenceNumber,
    int maximumPayloadSize)
  {
    super("IPTunnelSocket on server [Port " + localPort + "]");
    this.ipTunnelSocketFactory = ipTunnelSocketFactory;
    this.serverSocket = serverSocket;
    this.virtualPort = localPort;

    destinationAddress = destinationSocketAddress.getAddress();
    destinationPort = destinationSocketAddress.getPort();

    // retrieve an initial sequence number for this socket
    sendSequenceNumber = ipTunnelSocketFactory.getInitialSequenceNumber();
    // printMessage("Initial sequence number is " + sendSequenceNumber);

    initialSendSequenceNumber = sendSequenceNumber;

    // set sequence number for next expected packet (acknowledgement for syn response)
    expectedSequenceNumber = receivedSequenceNumber;
    initialAcknowledgementNumber = expectedSequenceNumber;

    // no acknowledgement has been received so far
    lastReceivedAcknowledgementNumber = -1;

    this.maximumPayloadSize = maximumPayloadSize;

    currentSegmentData = new byte[maximumPayloadSize];
    currentSegmentOffset = 0;
    readSegmentOffset = 0;
    receivedAvailableBytes = 0;
    lastReceivedWindowSize = -1;
    congestionWindowSize = IPTunnelConstants.TCP_INITIAL_CONGESTION_WINDOW;
    congestionThreshold = 65536;

    state = CLOSED;

    // create streams for data exchange
    outputStream = new IPTunnelOutputStream(this);
    inputStream = new IPTunnelInputStream(this);

    start();
  }

  /**
   * Creates a new instance of IPTunnelSocket on the client side
   * 
   * @param ipTunnelSocketFactory
   *          Associated tunnel management
   * @param localPort
   *          Local port used for this socket
   * @param maximumSegmentSize
   *          Maximum segment size usable by this socket
   * 
   */
  public IPTunnelSocket(IPTunnelSocketFactory ipTunnelSocketFactory, int localPort, int maximumSegmentSize)
  {
    super("IPTunnelSocket [" + localPort + "]");
    this.ipTunnelSocketFactory = ipTunnelSocketFactory;
    this.virtualPort = localPort;
    // retrieve an initial sequence number for this socket
    sendSequenceNumber = ipTunnelSocketFactory.getInitialSequenceNumber();
    initialSendSequenceNumber = sendSequenceNumber;
    // printMessage("Initial sequence number is " + sendSequenceNumber);

    // no packet has been received so far
    expectedSequenceNumber = -1;

    // no acknowledgement has been received so far
    lastReceivedAcknowledgementNumber = -1;

    // maximum payload is MSS without TCP header
    this.maximumPayloadSize = maximumSegmentSize - 20;
    // System.out.println("Set MPS for tunnel socket to " + maximumPayloadSize);

    currentSegmentData = new byte[maximumSegmentSize];
    currentSegmentOffset = 0;
    readSegmentOffset = 0;
    receivedAvailableBytes = 0;
    lastReceivedWindowSize = -1;
    congestionWindowSize = IPTunnelConstants.TCP_INITIAL_CONGESTION_WINDOW;
    congestionThreshold = 65536;

    state = CLOSED;

    // create streams for data exchange
    outputStream = new IPTunnelOutputStream(this);
    inputStream = new IPTunnelInputStream(this);

    start();
  }

  public static String state2String(int state)
  {
    if (state == CLOSED)
    {
      return "CLOSED";
    }
    if (state == LISTEN)
    {
      return "LISTEN";
    }
    if (state == SYN_RCVD)
    {
      return "SYN_RCVD";
    }
    if (state == SYN_SENT)
    {
      return "SYN_SENT";
    }
    if (state == ESTABLISHED)
    {
      return "ESTABLISHED";
    }
    if (state == FIN_WAIT_1)
    {
      return "FIN_WAIT_1";
    }
    if (state == FIN_WAIT_2)
    {
      return "FIN_WAIT_2";
    }
    if (state == TIMED_WAIT)
    {
      return "TIMED_WAIT";
    }
    if (state == CLOSING)
    {
      return "CLOSING";
    }
    if (state == CLOSE_WAIT)
    {
      return "CLOSE_WAIT";
    }
    if (state == LAST_ACK)
    {
      return "LAST_ACK";
    }

    return "Unknown state";
  }

  /** Connects to a server socket to establish a connection */
  public void connect(String host, int port) throws SocketTimeoutException, SocketException
  {
    InetSocketAddress serverAddress = null;
    try
    {
      serverAddress = new InetSocketAddress(InetAddress.getByName(host), port);
    } catch (UnknownHostException uhe)
    {
      throw new SocketException("Unknown host");
    }
    connect(serverAddress);
  }

  /** Connects to a server socket to establish a connection */
  public void connect(InetSocketAddress serverAddress) throws SocketTimeoutException, SocketException
  {
    state = SYN_SENT;
    destinationPort = serverAddress.getPort();
    destinationAddress = serverAddress.getAddress();

    // send initial SYN request
    TCPPacket synRequestPacket =
      new TCPPacket(getLocalPort(),
        serverAddress.getPort(),
        getSendSequenceNumber(),
        getWindowSize(),
        maximumPayloadSize);

    // increment by one for sent SYN flag
    incrementSendSequenceNumber(1);

    // this number is still unknown
    expectedSequenceNumber = -1;

    // printMessage("(1) SEND SYN REQUEST...");
    sendTCPPacket(synRequestPacket, true);

    // wait for connection establishment
    long time = System.currentTimeMillis();
    // wait for socket ready for accept()
    while (!terminateThread && state == SYN_SENT &&
      (socketTimeout == 0 || System.currentTimeMillis() - time <= socketTimeout))
    {
      ThreadHelper.sleep(50);
    }
    // catch timeout
    if (socketTimeout > 0 && System.currentTimeMillis() - time > socketTimeout)
    {
      // close the socket
      close();
      throw new SocketTimeoutException("Operation timed out");
    }

    // catch further errors
    if (state != ESTABLISHED)
    {
      close();
      throw new SocketException("Connection refused by foreign host");
    }
  }

  /**
   * Checks if the socket is ready to send and receive application data.
   * 
   * @return True if the socket can send and receive data
   */
  public boolean isEstablished()
  {
    return state == ESTABLISHED;
  }

  /**
   * Closes the socket and sends a FIN to the foreign host. No read or write operations are allowed after this call.
   */
  public void close()
  {
    // printMessage("Active close request, send FIN");
    // terminate if connection is not yet established
    if (state == SYN_SENT)
    {
      // terminate immediately
      terminate(false);
    } else
    {
      // prevent sending of FIN while a received packet is processed
      synchronized(receiveLock)
      {
        if (state == ESTABLISHED || state == CLOSE_WAIT)
        {
          // send last data segment
          sendCurrentSegment();
        }
        if (state == ESTABLISHED || state == CLOSE_WAIT || state == SYN_RCVD)
        {
          // create packet with FIN
          sendFIN();
        }
      }
      // printMessage("Wait for sending of outstanding packets");
      // wait for all outstanding packets
      while (!terminateThread && (sendPacketList.size() > 0 || unacknowledgedPacketList.size() > 0))
      {
        ThreadHelper.sleep(50);
      }
      // printMessage("All outstanding packets were sent and acked, return from close()");
    }
  }

  /** Returns the release time */
  protected long getReleaseTime()
  {
    return releaseTime;
  }

  /**
   * Returns setting for SO_TIMEOUT. 0 returns implies that the option is disabled (i.e., timeout of infinity).
   */
  public int getSoTimeout()
  {
    return socketTimeout;
  }

  /**
   * Enable/disable SO_TIMEOUT with the specified timeout, in milliseconds. With this option set to a non-zero timeout,
   * a read() call on the InputStream associated with this Socket will block for only this amount of time. If the
   * timeout expires, a java.net.SocketTimeoutException is raised, though the Socket is still valid. The option must be
   * enabled prior to entering the blocking operation to have effect. The timeout must be > 0. A timeout of zero is
   * interpreted as an infinite timeout.
   */
  public void setSoTimeout(int timeout)
  {
    socketTimeout = Math.max(0, timeout);
  }

  /**
   * Enable/disable TCP_NODELAY (disable/enable Nagle's algorithm)
   * 
   * @param on
   *          true to enable TCP_NODELAY, false to disable
   */
  public void setTcpNoDelay(boolean on)
  {
    useNaglesAlgorithm = !on;
  }

  /** Tests if TCP_NODELAY is enabled */
  public boolean getTcpNoDelay()
  {
    return !useNaglesAlgorithm;
  }

  /** Sends some bytes from the output stream of this socket to the tunnel */
  protected void sendBytesFromOutputStream(byte[] data, int offset, int length) throws IOException
  {
    // do not accept data after socket has been closed on this side
    if (state == SYN_SENT || state == SYN_RCVD || state == ESTABLISHED || state == CLOSE_WAIT)
    {
      synchronized(sendLock)
      {
        for (int i = offset; i < offset + length; i++)
        {
          // add bytes sequentially
          currentSegmentData[currentSegmentOffset] = data[offset + i];
          currentSegmentOffset++;

          // the send buffer is completely filled
          if (currentSegmentOffset == maximumPayloadSize)
          {
            sendCurrentSegment();
          }
        }
      }
      // Nagle's algorithm is disabled, so send data right away
      if (!useNaglesAlgorithm)
      {
        sendCurrentSegment();
      }

      // wait until all complete packets were written to the socket (blocking write)
      while (!terminateThread && sendPacketList.size() > 0)
      {
        ThreadHelper.sleep(1);
      }
      if (terminateThread)
      {
        throw new IOException("Socket closed");
      }
    }
  }

  /** Sends the current segment to the output stream of this socket */
  protected void sendCurrentSegment()
  {
    if (currentSegmentOffset != 0)
    {
      synchronized(sendLock)
      {
        // create new TCP packet
        byte[] payloadData = new byte[currentSegmentOffset];
        System.arraycopy(currentSegmentData, 0, payloadData, 0, currentSegmentOffset);

        // get sequence number for this packet
        long currentSequenceNumber = getSendSequenceNumber();

        // always acknowledge last received packet
        TCPPacket tcpPacket =
          new TCPPacket(virtualPort,
            destinationPort,
            currentSequenceNumber,
            expectedSequenceNumber,
            getWindowSize(),
            payloadData);

        // increment sequence number for next packet
        incrementSendSequenceNumber(currentSegmentOffset);

        sendTCPPacket(tcpPacket, false);

        // clear segment to prevent resending of data
        currentSegmentOffset = 0;
      }
    }
  }

  /** Sends an empty acknowledgement packet to the output stream of this socket */
  private void sendAcknowledgment()
  {
    // System.out.println("Send empty ack packet");
    // synchronized(sendLock)
    {
      // get sequence number for this packet
      long currentSequenceNumber = getSendSequenceNumber();

      // prevent multiple sending
      firstUnacknowledgeReceivedPacketTime = 0;

      // always acknowledge last received packet
      TCPPacket tcpPacket =
        new TCPPacket(virtualPort,
          destinationPort,
          currentSequenceNumber,
          expectedSequenceNumber,
          getWindowSize(),
          null);

      sendTCPPacket(tcpPacket, true);
    }
  }

  /** Sends a window probe with a wrong sequence number to the output stream of this socket */
  private void sendProbe()
  {
    printMessage("Send probe to check window size");
    // synchronized(sendLock)
    {
      byte[] dummy = new byte[] {
        0
      };

      // always acknowledge last received packet
      // send a packet with an already used sequence number
      TCPPacket tcpPacket =
        new TCPPacket(virtualPort,
          destinationPort,
          initialSendSequenceNumber,
          expectedSequenceNumber,
          getWindowSize(),
          dummy);

      sendTCPPacket(tcpPacket, true);
    }
  }

  /** Sends an empty FIN packet to the output stream of this socket */
  private void sendFIN()
  {
    // synchronized(sendLock)
    {
      // get sequence number for this packet
      long currentSequenceNumber = getSendSequenceNumber();

      // increment sequence number for FIN flag
      incrementSendSequenceNumber(1);

      // always acknowledge last received packet
      TCPPacket tcpPacket =
        new TCPPacket(virtualPort, destinationPort, currentSequenceNumber, expectedSequenceNumber, getWindowSize());

      // at this state, all pending data packets are sent, but not necessarily acknowledged
      // so we send the FIN immediately
      sendTCPPacket(tcpPacket, true);

      // set new state
      if (state == ESTABLISHED || state == SYN_RCVD)
      {
        if (DEBUG_STATE_TRANSITIONS)
        {
          printMessage("Switch to FIN WAIT 1 state");
        }
        state = FIN_WAIT_1;
      }
      if (state == CLOSE_WAIT)
      {
        if (DEBUG_STATE_TRANSITIONS)
        {
          printMessage("Switch to LAST_ACK state");
        }
        state = LAST_ACK;
      }
    }
  }

  /**
   * Sends an TCP/IP packet to the tunnel
   * 
   * @param tcpPacket
   *          The TCP packet that should be sent
   * @param immediately
   *          Set this flag to force an immediate sending of the data (should only be used for packets without upper
   *          layer data)
   * 
   */
  protected void sendTCPPacket(TCPPacket tcpPacket, boolean immediately)
  {
    tcpPacket.calculateChecksum(getLocalAddress(), destinationAddress);

    // create IP packet
    IPPacket ipPacket =
      new IPPacket(IPTunnelConstants.PROTOCOL_TYPE_TCP, getLocalAddress(), destinationAddress, tcpPacket.toByteArray());

    // associate IP packet with TCP packet
    tcpPacket.setIPPacket(ipPacket);

    if (immediately)
    {
      sendTCPPacketImmediately(tcpPacket);
    } else
    {
      synchronized(sendLock)
      {
        // add to list of pending packets
        sendPacketList.add(tcpPacket);
      }
    }
  }

  /**
   * Sends an TCP/IP packet to the tunnel. This method must not be called directly.
   * 
   * @param tcpPacket
   *          The TCP packet that should be sent
   */
  private void sendTCPPacketImmediately(TCPPacket tcpPacket)
  {
    tcpPacket.setLastSendTime();
    tcpPacket.incrementSendCount();
    lastSentPacketTime = System.currentTimeMillis();

    // save packets that need sequence numbers for acknowledgement
    if (tcpPacket.hasSYNFlag() || tcpPacket.hasFINFlag() || tcpPacket.getUpperLayerData() != null)
    {
      synchronized(ackLock)
      {
        unacknowledgedPacketList.add(tcpPacket);
      }
    }

    // check if the packet acknowledges all so far received packets (saves the need for empty
    // acknowledgement packets)
    if (tcpPacket.getAcknowledgementNumber() == expectedSequenceNumber)
    {
      // prevent sending of empty ack packets
      firstUnacknowledgeReceivedPacketTime = 0;
    }

    // printSendPacketToConsole(tcpPacket);

    // send packet
    ipTunnelSocketFactory.getIPTunnelNetworkInterface().sendIPPacketToTunnel(IPTunnelConstants.PACKET_TYPE_TCP,
      tcpPacket.getIPPacket());
  }

  /** Adds a received byte array to the list of received packets */
  protected void addReceivedBytes(byte[] data)
  {
    // printMessage("Add [\n" + StringHelper.byteArrayToString(data) + "]");

    // create a new buffer object and add it to the list of received packets
    ByteArrayOutputStream packetBuffer = new ByteArrayOutputStream(data.length);
    try
    {
      packetBuffer.write(data);
      receivedPacketList.add(packetBuffer);
      receivedAvailableBytes += data.length;
    } catch (IOException e)
    {
    }
    // initialization if current buffer is null
    if (readSegmentData == null)
    {
      pushReceivedPacket();
    }
  }

  /** Retrieves the number of bytes that can be read without blocking */
  public int getAvailableBytes()
  {
    return receivedAvailableBytes;
  }

  /** Read one received byte */
  protected int readByteFromTunnel() throws SocketTimeoutException, IOException
  {
    // cannot read data after closing
    if (state == TIMED_WAIT || state == CLOSED)
    {
      throw new IOException("Cannot read data from closed socket");
    }
    // cannot read data during synchronization
    if (state == SYN_SENT || state == SYN_RCVD)
    {
      throw new IOException("Cannot read data from unsynchronized socket");
    }
    // end of stream reached
    if (state == CLOSE_WAIT && receivedAvailableBytes == 0)
    {
      return -1;
    }

    long time = System.currentTimeMillis();
    // blocking read
    while (!terminateThread && receivedAvailableBytes == 0)
    {
      ThreadHelper.sleep(1);
      if (socketTimeout > 0 && System.currentTimeMillis() - time > socketTimeout)
      {
        throw new SocketTimeoutException("Read operation timed out");
      }
    }
    if (receivedAvailableBytes != 0)
    {
      // prevent reading while a further TCP packet is received
      synchronized(receiveLock)
      {
        int result = readSegmentData[readSegmentOffset] & 0xFF;

        readSegmentOffset++;
        receivedAvailableBytes--;

        // current buffer was read completely
        if (readSegmentOffset == readSegmentData.length)
        {
          pushReceivedPacket();
        }
        return result;
      }
    }

    return -1;
  }

  /** Read some bytes from the tunnel */
  protected int readBytesFromTunnel(byte[] data, int offset, int length) throws IOException, SocketTimeoutException
  {
    if (data == null)
    {
      throw new NullPointerException();
    }
    if (offset > 0 || length < 0 || offset + length > data.length)
    {
      throw new IndexOutOfBoundsException();
    }
    if (length == 0)
    {
      return 0;
    }

    // check states

    // cannot read data after closing
    if (state == TIMED_WAIT || state == CLOSED)
    {
      throw new IOException("Cannot read data from closed socket");
    }
    // cannot read data during synchronization
    if (state == SYN_SENT || state == SYN_RCVD)
    {
      throw new IOException("Cannot read data from unsynchronized socket");
    }
    // end of stream reached
    if (state == CLOSE_WAIT && receivedAvailableBytes == 0)
    {
      return -1;
    }

    long time = System.currentTimeMillis();
    // block until data is available
    while (!terminateThread && receivedAvailableBytes == 0)
    {
      ThreadHelper.sleep(1);
      if (socketTimeout > 0 && System.currentTimeMillis() - time > socketTimeout)
      {
        throw new SocketTimeoutException("Read operation timed out");
      }
    }
    if (terminated)
    {
      throw new IOException("Socket closed");
    }
    if (receivedAvailableBytes != 0)
    {
      // prevent reading during reception
      synchronized(receiveLock)
      {
        // determine, how many bytes can be read from the socket
        int readBytes = Math.min(receivedAvailableBytes, length);
        // read bytes
        for (int i = 0; i < readBytes; i++)
        {
          data[i + offset] = readSegmentData[readSegmentOffset];

          readSegmentOffset++;
          receivedAvailableBytes--;

          // current buffer was read completely
          if (readSegmentOffset == readSegmentData.length)
          {
            pushReceivedPacket();
          }
        }
        printMessage("Read " + readBytes + " from the tunnel");

        return readBytes;
      }
    }
    return -1;
  }

  /** Reads the oldest received packet into the current read buffer */
  private void pushReceivedPacket()
  {
    if (receivedPacketList.size() != 0)
    {
      boolean zeroWindow = getWindowSize() == 0;

      ByteArrayOutputStream nextPacket = (ByteArrayOutputStream)receivedPacketList.remove(0);

      readSegmentData = null;
      readSegmentOffset = 0;
      readSegmentData = nextPacket.toByteArray();

      // announce new window if changed from zero
      if (zeroWindow && getWindowSize() != 0)
      {
        sendAcknowledgment();
      }
    } else
    {
      readSegmentOffset = 0;
      readSegmentData = null;
    }
  }

  /** Adds a received tunnel TCP packet to the input queue of this socket */
  protected void processReceivedPacket(TCPPacket tcpPacket)
  {
    // force complete processing
    synchronized(receiveLock)
    {
      // printReceivedPacketToConsole(tcpPacket);

      // do not further process wrong packets
      if (checkForErrors(tcpPacket))
      {
        return;
      }
      processSYNPacket(tcpPacket);
      processEndOfHandshakePacket(tcpPacket);

      // check if received packet has the expected sequence number
      if (!tcpPacket.hasSYNFlag() && !tcpPacket.hasRSTFlag())
      {
        // packet is in order
        if (tcpPacket.getSequenceNumber() == expectedSequenceNumber)
        {
          processInOrderPacket(tcpPacket);
          // check if out of order packets can now be processed
          processOutOfOrderPackets();
        } else
        {
          // only collect later packets but also check for sequence number overflow
          if (isSequenceNumberInWindow(tcpPacket.getSequenceNumber()))
          {
            // packet is out of order, but no duplicate
            // printMessage("Received later packet. SEQ: " +
            // (tcpPacket.getSequenceNumber() - initialAcknowledgementNumber) +
            // ", check for store");
            boolean knownPacket = false;
            // check for duplicates
            for (int i = 0; i < outOfOrderPacketList.size(); i++)
            {
              TCPPacket currentPacket = (TCPPacket)outOfOrderPacketList.elementAt(i);
              knownPacket |= currentPacket.getSequenceNumber() == tcpPacket.getSequenceNumber();
              printMessage("Later packet has been received before, discard");
            }
            // store for later processing
            if (!knownPacket)
            {
              outOfOrderPacketList.add(tcpPacket);
            }
          } else
          {
            System.out.println("Send ACK for rereceived packet");
            // This packet has probably been received before. We send an ACK because it is possible
            // that the remote side did not receive our first ACK
            sendAcknowledgment();
          }
        }
      }
      // store received window size before the congestion window is calculated
      lastReceivedWindowSize = tcpPacket.getWindowSize();

      processAcknowledgements(tcpPacket);
      // process RST after acknowledgements
      processRSTPacket(tcpPacket);

      // printReceivedPacketToConsole(tcpPacket);

      // reset timeout for window probe if received window size is not zero
      if (lastReceivedWindowSize != 0)
      {
        lastWindowProbeTime = System.currentTimeMillis();
      }
      // update window size for sending
      lastUsedWindowSize = Math.min(congestionWindowSize, lastReceivedWindowSize);

      // printMessage("Window size set to " + lastUsedWindowSize);
    }
  }

  /** Shows a received packet at the console */
  // private void printReceivedPacketToConsole(TCPPacket tcpPacket)
  // {
  // printMessage("Received packet. " +
  // " SEQ: " + (tcpPacket.getSequenceNumber()-initialAcknowledgementNumber) +
  // " ACK: " + (tcpPacket.getAcknowledgementNumber()-initialSendSequenceNumber) +
  // // " Source: " + destinationAddress.getHostAddress() + ":" + destinationPort +
  // " Flags:" + tcpPacket.flagsToString() +
  // (tcpPacket.getMaximumPayloadSize() != 0 ? " MPS:" + tcpPacket.getMaximumPayloadSize() : "") +
  // " Window: " + tcpPacket.getWindowSize() +
  // (tcpPacket.getUpperLayerData() != null ? " Size: " + tcpPacket.getUpperLayerData().length : "")
  // +
  // " " + unacknowledgedPacketList.size() + " unacked" +
  // " " + sendPacketList.size() + " to send");
  // }
  /** Shows a packet that is about to be sent at the console */
  // private void printSendPacketToConsole(TCPPacket tcpPacket)
  // {
  // if (tcpPacket.getUpperLayerData() != null)
  // printMessage("Send packet." +
  // " SEQ: " + (tcpPacket.getSequenceNumber()-initialSendSequenceNumber) +
  // " ACK: " + (tcpPacket.getAcknowledgementNumber()-initialAcknowledgementNumber) +
  // " Window: " + tcpPacket.getWindowSize() +
  // " Flags:" + tcpPacket.flagsToString() +
  // (tcpPacket.getMaximumPayloadSize() != 0 ? " MPS:" + tcpPacket.getMaximumPayloadSize() : "") +
  // " Size:" + tcpPacket.getUpperLayerData().length +
  // " " + sendPacketList.size() + " to send");
  // else
  // printMessage("Send empty packet." +
  // " SEQ: " + (tcpPacket.getSequenceNumber()-initialSendSequenceNumber) +
  // " ACK: " + (tcpPacket.getAcknowledgementNumber()-initialAcknowledgementNumber) +
  // " Window: " + tcpPacket.getWindowSize() +
  // " Flags:" + tcpPacket.flagsToString() +
  // (tcpPacket.getMaximumPayloadSize() != 0 ? " MPS:" + tcpPacket.getMaximumPayloadSize() : "") +
  // " " + sendPacketList.size() + " to send");
  // }
  /**
   * Checks for packets that have the wrong content
   * 
   * @return True if the packet has errors, false otherwise
   */
  private boolean checkForErrors(TCPPacket tcpPacket)
  {
    // pending socket on server side waiting for a call to accept()
    if (state == LISTEN)
    {
      // RST received, terminate socket to allow another connection
      // request handled by the server socket
      if (tcpPacket.hasRSTFlag())
      {
        printMessage("Received RST while waiting for accept()");
        terminate(false);
        return true;
      }
    }
    // socket on client side waiting in connect()
    if (state == SYN_SENT)
    {
      // RST received, connection request was not accepted, close socket
      if (tcpPacket.hasRSTFlag() && tcpPacket.hasACKFlag() &&
        tcpPacket.getAcknowledgementNumber() == sendSequenceNumber)
      {
        printMessage("Received RST while waiting in connect()");
        terminate(false);
        return true;
      }
      // SYN received but wrong acknowledgement number
      if (tcpPacket.hasSYNFlag() && tcpPacket.getAcknowledgementNumber() != sendSequenceNumber)
      {
        printMessage("Received SYN with wrong sequence number, send RST");
        // send RST packet
        TCPPacket resetPacket = new TCPPacket(virtualPort, destinationPort, tcpPacket.getAcknowledgementNumber());

        sendTCPPacket(resetPacket, true);
        // after a timeout the SYN packet will be send again, so we do not close the socket
        return true;
      }
      // wait for SYN response but other side sends normal data
      if (!tcpPacket.hasSYNFlag())
      {
        printMessage("Received packet without SYN while waiting for connection");
        // send RST packet and terminate socket
        TCPPacket resetPacket = new TCPPacket(virtualPort, destinationPort, tcpPacket.getAcknowledgementNumber());

        sendTCPPacket(resetPacket, true);

        terminate(false);

        return true;
      }
    }
    // socket in a connection
    if (state == ESTABLISHED)
    {
      // SYN received, but no copy of original SYN request
      if (tcpPacket.hasSYNFlag() && tcpPacket.getSequenceNumber() != initialAcknowledgementNumber)
      {
        printMessage("Received SYN in ESTABLISHED state");
        // send empty ack packet, this will probably lead to the reception of a RST packet
        sendAcknowledgment();

        return true;
      }
      // sequence number is not in the window
      if (!tcpPacket.hasRSTFlag() && !isSequenceNumberInWindow(tcpPacket.getSequenceNumber()))
      {
        printMessage("Received packet with out of window sequence number");
        // send empty ack packet
        sendAcknowledgment();

        return true;
      }
      // TO-DO: could also handle invalid ack numbers

    }
    return false;
  }

  /** Processes received RST packets with a valid sequence number */
  private void processRSTPacket(TCPPacket tcpPacket)
  {
    if (state != SYN_SENT && state != LISTEN && tcpPacket.hasRSTFlag())
    {
      // handle RSTs for connection termination
      if (tcpPacket.getSequenceNumber() == expectedSequenceNumber && sendPacketList.size() == 0 &&
        unacknowledgedPacketList.size() == 0)
      {
        printMessage("Received RST, but all packets were acknowledged. Shutdown connection");
        terminate(false);
        return;
      }
      // RST received, abort connection
      if (tcpPacket.hasRSTFlag() && isSequenceNumberInWindow(tcpPacket.getSequenceNumber()))
      {
        printMessage("Received RST with valid sequence number");
        terminate(false);
        return;
      }
    }
  }

  /** Processes received SYN response packets */
  private void processSYNPacket(TCPPacket tcpPacket)
  {
    // check for syn response (2nd step in three way handshake)
    if (tcpPacket.isConnectionAccepted() && state == SYN_SENT &&
      tcpPacket.getAcknowledgementNumber() == sendSequenceNumber)
    {
      // printMessage("(2) CLIENT CONNECTION ESTABLISHED");

      // set sequence expected for next packet (first data packet from server to client)
      setExpectedSequenceNumber(tcpPacket.getSequenceNumber() + 1);
      initialAcknowledgementNumber = tcpPacket.getSequenceNumber();

      state = ESTABLISHED;

      int remoteMaximumPayloadSize = IPTunnelConstants.TCP_DEFAULT_PAYLOAD_SIZE;
      // check for maximum sequence size option
      if (tcpPacket.getMaximumPayloadSize() != 0)
      {
        remoteMaximumPayloadSize = tcpPacket.getMaximumPayloadSize();
      }
      // build minimum of local and remote payload size
      maximumPayloadSize = Math.min(maximumPayloadSize, remoteMaximumPayloadSize);

      // printMessage("(3) SEND END OF HANDSHAKE...");

      // send third handshake packet to finish connection establishment
      TCPPacket endOfHandshakePacket =
        new TCPPacket(virtualPort,
          destinationPort,
          getSendSequenceNumber(),
          expectedSequenceNumber,
          getWindowSize(),
          null);

      // send immediately
      sendTCPPacket(endOfHandshakePacket, true);
    }
  }

  /** Processes received SYN packets */
  private void processEndOfHandshakePacket(TCPPacket tcpPacket)
  {
    // check for end of three way handshake
    if (state == SYN_RCVD && tcpPacket.hasACKFlag() && tcpPacket.getAcknowledgementNumber() == sendSequenceNumber)
    {
      // printMessage("(3) SERVER CONNECTION ESTABLISHED");

      state = ESTABLISHED;
    }
  }

  /** Processes received FIN packets */
  private void processFINPacket(TCPPacket tcpPacket)
  {
    // check for FIN flag
    if (tcpPacket.hasFINFlag())
    {
      // in these states, a received FIN must be acknowledged
      if (state == ESTABLISHED || state == FIN_WAIT_1 || state == FIN_WAIT_2)
      {
        // increment sequence number by one for FIN flag
        incrementExpectedSequenceNumber(1);
        // acknowledge received FIN
        sendAcknowledgment();
      } else
      {
        // sendReset();
      }
      // handle state transitions
      if (state == ESTABLISHED)
      {
        if (DEBUG_STATE_TRANSITIONS)
        {
          printMessage("Passive close, switch to CLOSE_WAIT state");
        }
        state = CLOSE_WAIT;
      }
      // simultaneous close
      if (state == FIN_WAIT_1)
      {
        if (DEBUG_STATE_TRANSITIONS)
        {
          printMessage("Received FIN after sending FIN by myself, switch to CLOSING state");
        }
        state = CLOSING;
      }
      // connection is now closed by both sides
      if (state == FIN_WAIT_2)
      {
        terminate(true);
      }
    }
  }

  /** Processes the acknowledgements received with a packet */
  private void processAcknowledgements(TCPPacket tcpPacket)
  {
    // try to acknowledge as much sent packets as possible
    if (tcpPacket.hasACKFlag())
    {
      long packetAcknowledgementNumber = tcpPacket.getAcknowledgementNumber();

      // calculate longest round trip time
      long ackTime = 0;

      // go through all unacknowledged packets and remove those with sequence numbers smaller than
      // the received sequence number
      int i = 0;
      int dataPacketAcknowledgeCount = 0;
      synchronized(ackLock)
      {
        while (i < unacknowledgedPacketList.size())
        {
          TCPPacket currentPacket = (TCPPacket)unacknowledgedPacketList.elementAt(i);
          // new number is greater than last received number
          if (packetAcknowledgementNumber > lastReceivedAcknowledgementNumber &&
            currentPacket.getSequenceNumber() < packetAcknowledgementNumber ||
            packetAcknowledgementNumber < lastReceivedAcknowledgementNumber &&
            (currentPacket.getSequenceNumber() > lastReceivedAcknowledgementNumber || currentPacket.getSequenceNumber() < packetAcknowledgementNumber))
          {
            // do not use retransmitted packets for the calculation of the round trip time (Karn's
            // algorithm)
            if (currentPacket.getSendCount() == 1)
            {
              // store latest acknowledgement
              ackTime = Math.max(ackTime, System.currentTimeMillis() - currentPacket.getLastSendTime());
            }
            // only use data packets to update the congestion control
            if (currentPacket.getUpperLayerData() != null)
            {
              dataPacketAcknowledgeCount++;
            }

            unacknowledgedPacketList.remove(i);
          } else
          {
            i++;
          }
        }
      }

      // update round trip time with the data of the slowest packet
      if (ackTime != 0)
      {
        // update round trip estimation
        roundTripTime = Math.round(7.0 * roundTripTime / 8.0 + ackTime / 8.0);
        // update deviation
        rttDeviation = Math.round(7.0 * rttDeviation / 8.0 + Math.abs(roundTripTime - ackTime) / 8.0);
      }

      // update last received acknowledgement number
      lastReceivedAcknowledgementNumber = packetAcknowledgementNumber;

      // handle congestion control
      for (int j = 0; j < dataPacketAcknowledgeCount; j++)
      {
        // increment congestion window size if the number of acknowledged bytes*2
        // is greater than the current congestion window
        if (congestionWindowSize < congestionThreshold)
        {
          // grow exponentially under threshold
          congestionWindowSize = congestionWindowSize * 2;
        } else
        {
          // grow linearly over threshold
          congestionWindowSize = congestionWindowSize + maximumPayloadSize;
        }
      }
      // limit to receive window if valid
      if (lastReceivedWindowSize > 0)
      {
        congestionWindowSize = Math.min(lastReceivedWindowSize, congestionWindowSize);
      }
      // System.out.println("Congestion window size set to " + congestionWindowSize);

      // handle state changes
      if (unacknowledgedPacketList.size() == 0 && sendPacketList.size() == 0)
      {
        // wait for closing of input stream
        if (state == FIN_WAIT_1)
        {
          if (DEBUG_STATE_TRANSITIONS)
          {
            printMessage("Switch to FIN WAIT 2 state");
          }

          state = FIN_WAIT_2;
        }
        // connection is now closed by both sides
        if (state == CLOSING)
        {
          terminate(true);
        }
        // connection is now also closed on my side
        if (state == LAST_ACK)
        {
          // no need to wait for this socket
          terminate(false);
        }
      }
    }
  }

  /** Processes the next in-order packet */
  private void processInOrderPacket(TCPPacket tcpPacket)
  {
    // process data
    if (tcpPacket.getUpperLayerData() != null)
    {
      // store TCP byte stream
      addReceivedBytes(tcpPacket.getUpperLayerData());

      // increment expected sequence number by length of received data
      incrementExpectedSequenceNumber(tcpPacket.getUpperLayerData().length);

      // check if this is the first packet that was not yet acknowledged
      if (firstUnacknowledgeReceivedPacketTime == 0)
      {
        firstUnacknowledgeReceivedPacketTime = System.currentTimeMillis();
        // System.out.println("Updates first unacked packet time");
      }
    }
    // process FIN flag if received
    processFINPacket(tcpPacket);
  }

  /** Processes the vector with out of order packets */
  private void processOutOfOrderPackets()
  {
    boolean abort = false;
    // search vector for packets with the next expected sequence number until no further
    // packets are found
    while (!abort)
    {
      int i = 0;
      abort = true;
      while (i < outOfOrderPacketList.size())
      {
        TCPPacket tcpPacket = (TCPPacket)outOfOrderPacketList.elementAt(i);
        if (tcpPacket.getSequenceNumber() == expectedSequenceNumber)
        {
          // printMessage(" Processed out of order packet.");
          abort = false;
          processInOrderPacket(tcpPacket);
          outOfOrderPacketList.remove(i);
        } else
        {
          i++;
        }
      }
    }
  }

  /** Checks if a sequence number is acceptable */
  private boolean isSequenceNumberInWindow(long sequenceNumber)
  {
    long windowEnd = expectedSequenceNumber + getWindowSize();
    if (windowEnd > 0xFFFFFFFF)
    {
      // overflow
      return sequenceNumber >= expectedSequenceNumber || sequenceNumber + 0xFFFFFFFF > expectedSequenceNumber &&
        sequenceNumber + 0xFFFFFFFF < windowEnd;
    } else
    {
      // no overflow
      return sequenceNumber >= expectedSequenceNumber && sequenceNumber < windowEnd;
    }
  }

  /** Retrieves the local IP address of this socket (e.g., 192.168.x.y) */
  public InetAddress getLocalAddress()
  {
    return ipTunnelSocketFactory.getIPTunnelNetworkInterface().getIPTunnelInetAddress();
  }

  /** Retrieves the local port of this socket */
  public int getLocalPort()
  {
    return virtualPort;
  }

  /** Retrieves the local IP address and port of this socket */
  public InetSocketAddress getLocalSocketAddress()
  {
    return new InetSocketAddress(getLocalAddress(), virtualPort);
  }

  /** Retrieves the destination IP address for this socket */
  public InetAddress getDestinationAddress()
  {
    return destinationAddress;
  }

  /** Retrieves the destination port for this socket */
  public int getDestinationPort()
  {
    return destinationPort;
  }

  /** Retrieves the destination socket address for this socket */
  public InetSocketAddress getDestinationSocketAddress()
  {
    return new InetSocketAddress(destinationAddress, destinationPort);
  }

  /** Sets the state of the socket */
  protected void setState(int state)
  {
    this.state = state;
  }

  /** Retrieves the current acknowledgementNumber */
  protected long getLastReceivedAcknowledgementNumber()
  {
    return lastReceivedAcknowledgementNumber;
  }

  /** Retrieves the next expected sequence number */
  protected long getExpectedSequenceNumber()
  {
    return expectedSequenceNumber;
  }

  /** Updates the current acknowledgementNumber */
  protected void incrementExpectedSequenceNumber(int increment)
  {
    expectedSequenceNumber = expectedSequenceNumber + increment & INT_MASK;
  }

  /** Updates the current acknowledgementNumber */
  protected void setExpectedSequenceNumber(long sequenceNumber)
  {
    expectedSequenceNumber = sequenceNumber & INT_MASK;
  }

  /** Retrieves the current sequence number for sending */
  protected long getSendSequenceNumber()
  {
    return sendSequenceNumber;
  }

  /** Updates the sequence number after creating a new packet */
  protected void incrementSendSequenceNumber(int increment)
  {
    // catch overflow
    sendSequenceNumber = sendSequenceNumber + increment & INT_MASK;
  }

  /** Retrieves the number of bytes this socket is willing to accept (the receive window) */
  protected int getWindowSize()
  {
    if (acceptOnlySinglePackets)
    {
      return Math.min(maximumPayloadSize, Math.max(0, (RECEIVE_BUFFER_SIZE - receivedPacketList.size()) *
        maximumPayloadSize));
    } else
    {
      return Math.min(65495, Math.max(0, (RECEIVE_BUFFER_SIZE - receivedPacketList.size()) * maximumPayloadSize));
    }
  }

  /** Returns the closed state of the socket. */
  public boolean isClosed()
  {
    return state == CLOSED || state == FIN_WAIT_1 || state == FIN_WAIT_2 || state == CLOSE_WAIT || state == LAST_ACK ||
      state == CLOSING || state == TIMED_WAIT;
  }

  /** Returns the connected state of the socket. */
  public boolean isConnected()
  {
    return state == ESTABLISHED;
  }

  /** Retrieves the output stream for this socket that can be used to send data */
  public OutputStream getOutputStream()
  {
    return outputStream;
  }

  /** Retrieves the input stream for this socket that can be used to read received data */
  public InputStream getInputStream()
  {
    return inputStream;
  }

  /**
   * Checks if this socket only accepts one packet a time
   * 
   * @return
   */
  public boolean acceptOnlySinglePackets()
  {
    return acceptOnlySinglePackets;
  }

  /**
   * Sets the flag acceptOnlySinglePackets
   * 
   * @param acceptOnlySinglePackets
   */
  public void setAcceptOnlySinglePackets(boolean acceptOnlySinglePackets)
  {
    this.acceptOnlySinglePackets = acceptOnlySinglePackets;
  }

  /**
   * Retrieves the creationTime.
   * 
   * @return The creationTime
   */
  public long getCreationTime()
  {
    return creationTime;
  }

  /** Shows some data about the connection */
  protected void showStats()
  {
    // System.out.println("\nSTATS for port : " + getLocalPort());
    // System.out.println("State : " + state2String(state));
    // System.out.println("Send bytes : " + (sendSequenceNumber-initialSendSequenceNumber));
    // System.out.println("Received bytes : " +
    // (expectedSequenceNumber-initialAcknowledgementNumber));
    // System.out.println("Pending packets: : " + sendPacketList.size());
    // System.out.println("Unacknowledged packets: " + unacknowledgedPacketList.size());
    // System.out.println("Received window size : " + lastReceivedWindowSize);
    // System.out.println("Congestion window size: " + congestionWindowSize);
    // System.out.println("Round trip time : " + roundTripTime);
    // System.out.println("Deviation : " + rttDeviation);
    // System.out.println();
  }

  /** Prints a debug message */
  protected void printMessage(String text)
  {
    // System.out.println(IPTunnelNetworkInterface.timeStamp() + "IPTunnelSocket" +
    // (serverSocket == null ? " from " + getLocalPort() : "") +
    // " to " + destinationPort + ": " + text);
  }

  public void run()
  {
    while (!terminateThread)
    {

      // show stats
      if (showStats && System.currentTimeMillis() / 1000 % 3 == 0)
      {
        showStats();
        showStats = false;
      }
      if (!showStats && System.currentTimeMillis() / 1000 % 3 != 0)
      {
        showStats = true;
      }

      // check for acknowledge timeout
      boolean timeout = false;
      synchronized(ackLock)
      {
        for (int i = 0; !timeout && i < unacknowledgedPacketList.size(); i++)
        {
          TCPPacket tcpPacket = (TCPPacket)unacknowledgedPacketList.elementAt(i);
          if (System.currentTimeMillis() - tcpPacket.getLastSendTime() > roundTripTime + 4 * rttDeviation)
          {
            timeout = true;

            // double round trip time (Karn's algorithm)
            roundTripTime *= 2;

            // handle congestion control
            congestionThreshold = Math.max(maximumPayloadSize, congestionWindowSize / 2);
            congestionWindowSize = maximumPayloadSize;

            tcpPacket.setLastSendTime();
            tcpPacket.incrementSendCount();

            printMessage("Need to resend packet with sequence number " +
              (tcpPacket.getSequenceNumber() - initialSendSequenceNumber) + " after timeout, " +
              tcpPacket.getSendCount() + ". attempt. Reset congestion window size to " + congestionWindowSize);

            // resend packet
            ipTunnelSocketFactory.getIPTunnelNetworkInterface().sendIPPacketToTunnel(IPTunnelConstants.PACKET_TYPE_TCP,
              tcpPacket.getIPPacket());
          }
        }
        // check for abort due to send error
        for (int i = 0; i < unacknowledgedPacketList.size(); i++)
        {
          TCPPacket tcpPacket = (TCPPacket)unacknowledgedPacketList.elementAt(i);
          if (System.currentTimeMillis() - tcpPacket.getCreationTime() > IPTunnelConstants.TCP_PACKET_ERROR_TIME)
          {
            printMessage("Close socket due to a send error");

            terminateFromThread();
          }
        }
      }

      // persistence timer to check window size
      if (lastReceivedWindowSize == 0 &&
        System.currentTimeMillis() - lastWindowProbeTime > IPTunnelConstants.TCP_PERSISTENCE_WAIT_TIME)
      {
        sendProbe();
        lastWindowProbeTime = System.currentTimeMillis();
      }
      // Nagle's algorithm: send current buffer if all packets were sent and acknowledged
      if (useNaglesAlgorithm && sendPacketList.size() == 0 && unacknowledgedPacketList.size() == 0 &&
        currentSegmentOffset > 0)
      {
        sendCurrentSegment();
      }

      if (firstUnacknowledgeReceivedPacketTime != 0 &&
        System.currentTimeMillis() - firstUnacknowledgeReceivedPacketTime > IPTunnelConstants.TCP_ACKNOWLEDGEMENT_WAIT_TIME &&
        System.currentTimeMillis() - lastSentPacketTime > IPTunnelConstants.TCP_ACKNOWLEDGEMENT_WAIT_TIME)
      {
        sendAcknowledgment();
      }

      boolean abort = false;

      // manage packet sending
      // FIN WAIT 1 is a valid state, because we must send all outstanding packets
      while ((state == ESTABLISHED || state == FIN_WAIT_1) && !abort && !terminateThread && sendPacketList.size() > 0)
      {
        synchronized(sendLock)
        {
          TCPPacket tcpPacket = (TCPPacket)sendPacketList.elementAt(0);
          // send packets without data immediately, but keep packet order
          if (tcpPacket.getUpperLayerData() == null || tcpPacket.getUpperLayerData().length <= lastUsedWindowSize)
          {
            sendTCPPacketImmediately(tcpPacket);

            // remove from send list
            sendPacketList.remove(0);

            // decrease window size
            if (tcpPacket.getUpperLayerData() != null)
            {
              lastUsedWindowSize -= tcpPacket.getUpperLayerData().length;
            }
          } else
          {
            abort = true;
          }
        }
      }
      ThreadHelper.sleep(1);
    }
    terminated = true;
  }

  /** Terminates and removes a socket */
  private void terminateFromThread()
  {
    if (DEBUG_STATE_TRANSITIONS)
    {
      printMessage("Switch to TIMED WAIT state");
    }
    state = TIMED_WAIT;

    terminateThread = true;

    // store time for closing
    releaseTime = System.currentTimeMillis();

    // choose who must be informed about terminated socket
    if (serverSocket != null)
    {
      serverSocket.removeIPTunnelSocket(this, true);
    } else
    {
      ipTunnelSocketFactory.removeIPTunnelSocket(this, true);
    }
  }

  /**
   * Terminates and removes a socket
   * 
   * @param timedWait
   *          Flag to switch to TIMED_WAIT mode
   * 
   */
  public void terminate(boolean timedWait)
  {
    // set new state
    if (!timedWait)
    {
      if (DEBUG_STATE_TRANSITIONS)
      {
        printMessage("Switch to CLOSED state");
      }
      state = CLOSED;
    } else
    {
      if (DEBUG_STATE_TRANSITIONS)
      {
        printMessage("Switch to TIMED WAIT state");
      }
      state = TIMED_WAIT;
    }
    terminateThread = true;
    // wait for end of thread
    while (!terminated)
    {
      ThreadHelper.sleep(50);
    }
    // store time for closing
    releaseTime = System.currentTimeMillis();

    // choose who must be informed about terminated socket
    if (serverSocket != null)
    {
      serverSocket.removeIPTunnelSocket(this, timedWait);
    } else
    {
      ipTunnelSocketFactory.removeIPTunnelSocket(this, timedWait);
    }
  }

}
