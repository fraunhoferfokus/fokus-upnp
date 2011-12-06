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
package de.fraunhofer.fokus.upnp.util.tunnel.common;

import de.fraunhofer.fokus.upnp.util.ByteArrayHelper;
import de.fraunhofer.fokus.upnp.util.logging.LogHelper;
import de.fraunhofer.fokus.upnp.util.threads.IEventListener;

/**
 * This is an abstract class that can be used to send and receive packets over a tunnel. The class handles packing and
 * unpacking of simple byte arrays so data can be sent over a singular connection like a TCP connection or other
 * stream-oriented connections. The tunnel does not reorder received packets; therefore the tunnel connection is
 * responsible for maintaining the correct packet order during reception. If the streamTunnel property is set to false,
 * additional measures are taken to ensure proper recovery from lost tunnel packets (which are simply ignored).
 * 
 * Receiving data must be done in the subclass. Received data can then be forwarded to handleReceivedData() which
 * unpacks the tunneled packets and forwards them to the registered packet tunnel listener.
 * 
 * 
 * @author Alexander Koenig
 */
public abstract class AbstractPacketTunnel implements IEventListener, IPacketTunnel
{
  protected static final int      BUFFER_SIZE                 = 262144;

  /** Magic number(10 bytes) + Size(4 bytes) + Packet type(1 byte) */
  protected static final int      INSECURE_TUNNEL_HEADER_SIZE = 16;

  /** Size(4 bytes) + Packet type(1 byte) */
  protected static final int      STREAM_TUNNEL_HEADER_SIZE   = 5;

  /** Listener for received packets */
  protected IPacketTunnelListener tunnelListener;

  /** Buffer to hold received data */
  private byte[]                  inputBuffer                 = new byte[BUFFER_SIZE];

  /** Index of first byte after currently available input data */
  private int                     inputBufferEndOffset        = 0;

  private int                     packetPayloadSize           = 0;

  private byte                    packetPayloadType           = 0;

  private int                     sendPacketIndex             = 0;

  private int                     receivedPacketIndex         = 0;

  private int                     lostPacketCount             = 0;

  /** Flag that a pending packet is currently processed */
  private boolean                 tunnelHeaderProcessed       = false;

  /** Size of tunneled packet header */
  private int                     tunnelHeaderSize            = 0;

  /** Flag that the outer tunnel connection handles lost or reordered packet */
  protected boolean               streamTunnel                = false;

  /**
   * Creates a new instance of PacketTunnel.
   * 
   * @param tunnelListener
   *          Listener for received packets
   */
  public AbstractPacketTunnel(IPacketTunnelListener tunnelListener)
  {
    this.tunnelListener = tunnelListener;
  }

  /**
   * Encapsulates a packet and sends it to the tunnel.
   * 
   * 
   * @param packetType
   *          The packet type
   * @param payload
   *          The packet
   */
  public void sendPacket(byte packetType, byte[] payload)
  {
    // System.out.println("Send packet to tunnel: Type: " + packetType + " Size: " +
    // payload.length);
    int payloadSize = payload.length;

    byte[] data = null;
    if (streamTunnel)
    {
      data = new byte[payloadSize + STREAM_TUNNEL_HEADER_SIZE];
      System.arraycopy(ByteArrayHelper.int32ToByteArray(payloadSize), 0, data, 0, 4);
      data[4] = packetType;

      System.arraycopy(payload, 0, data, STREAM_TUNNEL_HEADER_SIZE, payloadSize);
    } else
    {
      data = new byte[payloadSize + INSECURE_TUNNEL_HEADER_SIZE];
      // magic number
      for (int i = 0; i < 10; i++)
      {
        data[i] = (byte)0xFF;
      }
      // packet send count
      data[10] = (byte)sendPacketIndex;
      sendPacketIndex = (sendPacketIndex + 1) % 256;

      System.arraycopy(ByteArrayHelper.int32ToByteArray(payloadSize), 0, data, 11, 4);
      data[15] = packetType;

      System.arraycopy(payload, 0, data, INSECURE_TUNNEL_HEADER_SIZE, payloadSize);
    }
    try
    {
      sendPacketInternal(data);
    } catch (Exception e)
    {
      LogHelper.warn("Error sending packet to tunnel: " + e.getMessage());
      closeConnection();
    }
  }

  /** Resets the data buffer. Can be used to clear the cache after a connection reset. */
  public void resetBuffer()
  {
    inputBufferEndOffset = 0;
    packetPayloadSize = 0;
    packetPayloadType = 0;
    tunnelHeaderProcessed = false;
  }

  /** Retrieves the number of bytes that can currently be stored in the internal byte array. */
  protected int getRemainingBufferSize()
  {
    return inputBuffer.length - inputBufferEndOffset;
  }

  /** Checks if the data array contains the magic number at a certain offset */
  private boolean isMagicNumberAt(byte[] data, int offset)
  {
    if (data == null || data.length < offset + 10)
    {
      return false;
    }

    for (int i = 0; i < 10; i++)
    {
      if (inputBuffer[offset + i] != (byte)0xFF)
      {
        return false;
      }
    }
    return true;
  }

  /**
   * Handles data received via the tunnel connection. This method must be called by derived classes for all received
   * tunnel data.
   * 
   * 
   * @param newData
   *          The received data
   * 
   * @return The number of bytes that were handled
   */
  public int handleReceivedData(byte[] newData, int newDataOffset, int newDataLength)
  {
    int handledLength = 0;
    try
    {
      boolean hasPacketHeader;
      // loop until all data has been processed
      do
      {
        // assume no further packets
        hasPacketHeader = false;

        // calculate size that fits into the buffer
        int fittingLength = Math.min(newDataLength, inputBuffer.length - inputBufferEndOffset);

        // copy pending data to input array
        if (fittingLength > 0)
        {
          //          Portable.println("  Append " + fittingLength + " bytes new data at " + inputBufferEndOffset + " from " +
          //            newDataOffset);
          System.arraycopy(newData, newDataOffset, inputBuffer, inputBufferEndOffset, fittingLength);

          inputBufferEndOffset += fittingLength;
          handledLength += fittingLength;
          // shift offset to start of unhandled data
          newDataOffset += fittingLength;
          newDataLength -= fittingLength;
          //          Portable.println("  Pending data length is " + newDataLength);
        }
        //        Portable.println("  Remaining buffer length is " + (inputBuffer.length - inputBufferEndOffset));

        // now, the next unprocessed packet starts at inputData[0]

        // outer connection assures correct stream
        if (streamTunnel)
        {
          if (inputBufferEndOffset >= STREAM_TUNNEL_HEADER_SIZE && !tunnelHeaderProcessed)
          {
            // retrieve size of tunneled packet
            packetPayloadSize = ByteArrayHelper.byteArrayToInt32(inputBuffer, 0);
            packetPayloadType = inputBuffer[4];

            tunnelHeaderProcessed = true;
            tunnelHeaderSize = STREAM_TUNNEL_HEADER_SIZE;
          }
        } else
        // we use an insecure tunnel that can loose or reorder packets
        {
          boolean isValidPacketHeader = true;

          if (inputBufferEndOffset >= INSECURE_TUNNEL_HEADER_SIZE && !tunnelHeaderProcessed)
          {
            // check magic number
            isValidPacketHeader = isMagicNumberAt(inputBuffer, 0);

            if (isValidPacketHeader)
            {
              // check packet index
              byte packetIndex = inputBuffer[10];

              if ((receivedPacketIndex + 1) % 256 != packetIndex)
              {
                System.out.println("Packet was lost. ");
                // the magic number was at the right position so we simply ignore the lost packets
                lostPacketCount++;
              }
              receivedPacketIndex = packetIndex;

              // retrieve packet size
              packetPayloadSize = ByteArrayHelper.byteArrayToInt32(inputBuffer, 11);
              // ((inputData[10] & 0xFF) << 24) + ((inputData[11] & 0xFF) << 16) + ((inputData[12] &
              // 0xFF) << 8) +
              // (inputData[13] & 0xFF);

              packetPayloadType = inputBuffer[15];

              // if (expectedPacketLength != length)
              // {
              // System.out.println("Read partial packet from TCP stream: Offset:" + inputDataOffset
              // +
              // " Length:" + inputData.length + " Received:" + length);
              // }
              tunnelHeaderProcessed = true;
              tunnelHeaderSize = INSECURE_TUNNEL_HEADER_SIZE;
            } else
            {
              // we are unsynchronized
              // try to find the next magic number
              int magicNumberStartIndex = 1;
              boolean magicNumberFound = false;
              while (!magicNumberFound && magicNumberStartIndex + 10 < inputBufferEndOffset)
              {
                magicNumberFound = isMagicNumberAt(inputBuffer, magicNumberStartIndex);
                if (!magicNumberFound)
                {
                  magicNumberStartIndex++;
                }
              }
              if (magicNumberFound)
              {
                System.out.println("Magic number found at " + magicNumberStartIndex);
                // copy new packet to start of input buffer
                int unprocessedInputBufferSize = inputBufferEndOffset - magicNumberStartIndex;
                // copy remaining bytes of input data to start
                if (unprocessedInputBufferSize > 0)
                {
                  System.arraycopy(inputBuffer, magicNumberStartIndex, inputBuffer, 0, unprocessedInputBufferSize);
                }
                // set offset to end of remaining data
                inputBufferEndOffset = unprocessedInputBufferSize;
              } else
              {
                // the remaining input buffer does not hold a complete magic number
                // it is possible but very unlikely that the buffer ends with an incomplete magic
                // number
                // we ignore this case and simply clear the complete buffer
                inputBufferEndOffset = 0;
              }
              // allow new header detection
              hasPacketHeader = true;
            }
          }
        }
        int totalPacketSize = packetPayloadSize + tunnelHeaderSize;
        // a complete packet is available
        if (tunnelHeaderProcessed && inputBufferEndOffset >= totalPacketSize)
        {
          //          System.out.println("    Received packet from tunnel: Type: " + packetPayloadType + " Size: " +
          //            tunnelHeaderSize + ":" + packetPayloadSize);

          // retrieve packet
          byte[] packetPayload = new byte[packetPayloadSize];
          // copy packet to new byte array
          System.arraycopy(inputBuffer, tunnelHeaderSize, packetPayload, 0, packetPayload.length);

          int unprocessedInputBufferSize = inputBufferEndOffset - totalPacketSize;
          // copy remaining bytes of input data to start
          if (unprocessedInputBufferSize > 0)
          {
            System.arraycopy(inputBuffer, totalPacketSize, inputBuffer, 0, unprocessedInputBufferSize);
          }
          // set offset to end of remaining data
          inputBufferEndOffset = unprocessedInputBufferSize;

          tunnelHeaderProcessed = false;
          tunnelHeaderSize = 0;
          // assume further packets
          hasPacketHeader = true;

          // forward received packet
          if (tunnelListener != null)
          {
            tunnelListener.packetReceived(packetPayloadType, packetPayload);
          }
        } else
        {
          if (tunnelHeaderProcessed)
          {
            //            System.out.println(" A partial packet is pending: " + inputBufferEndOffset);
          }
        }
      } while (hasPacketHeader);
    } catch (Exception e)
    {
      LogHelper.warn("Error handling packets from tunnel: " + e.getMessage());
      e.printStackTrace();
      closeConnection();
    }
    return handledLength;
  }

  /**
   * Retrieves the tunnelListener.
   * 
   * @return The tunnelListener
   */
  public IPacketTunnelListener getTunnelListener()
  {
    return tunnelListener;
  }

  /**
   * Sets the tunnelListener.
   * 
   * @param tunnelListener
   *          The new value for tunnelListener
   */
  public void setTunnelListener(IPacketTunnelListener tunnelListener)
  {
    this.tunnelListener = tunnelListener;
  }

  /**
   * Retrieves the value of streamTunnel.
   * 
   * @return The value of streamTunnel
   */
  public boolean isStreamTunnel()
  {
    return streamTunnel;
  }

  /**
   * Sets the new value for streamTunnel.
   * 
   * @param streamTunnel
   *          The new value for streamTunnel
   */
  public void setStreamTunnel(boolean streamTunnel)
  {
    this.streamTunnel = streamTunnel;
  }

}
