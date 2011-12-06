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
package de.fraunhofer.fokus.upnp.util.network;

import java.io.InputStream;
import java.io.OutputStream;

import de.fraunhofer.fokus.upnp.util.ThreadHelper;
import de.fraunhofer.fokus.upnp.util.logging.LogHelper;
import de.fraunhofer.fokus.upnp.util.tunnel.common.IPacketTunnelListener;

/**
 * This class can be used to send and receive packets over a singular connection. This class is deprecated and should
 * not be used. Use the classes from util.tunnel instead.
 * 
 * 
 * @author Alexander Koenig
 */
public class PacketTunnel extends Thread
{
  protected static final int      BUFFER_SIZE           = 32768;

  protected static final int      TUNNEL_HEADER_SIZE    = 15;

  /** The associated input stream */
  protected InputStream           inputStream;

  /** The associated output stream */
  protected OutputStream          outputStream;

  /** Listener for received packets */
  protected IPacketTunnelListener tunnelListener;

  protected boolean               terminateThread       = false;

  protected boolean               terminated            = false;

  /** Buffer to hold received data */
  private byte[]                  inputData             = new byte[BUFFER_SIZE];

  private int                     inputDataOffset       = 0;

  private int                     expectedPacketLength  = 0;

  private byte                    expectedPacketType    = 0;

  private boolean                 packetHeaderProcessed = false;

  /**
   * Creates a new instance of PacketTunnel. The thread is not started.
   * 
   * @param name
   * @param inputStream
   * @param outputStream
   * @param tunnelListener
   */
  public PacketTunnel(String name,
    InputStream inputStream,
    OutputStream outputStream,
    IPacketTunnelListener tunnelListener)
  {
    setName(name);
    this.inputStream = inputStream;
    this.outputStream = outputStream;
    this.tunnelListener = tunnelListener;
  }

  /**
   * Sends a packet to the tunnel.
   * 
   * 
   * @param packetType
   *          The packet type
   * @param payload
   *          The packet
   */
  public void sendPacketToTunnel(byte packetType, byte[] payload)
  {
    // System.out.println("Send packet to tunnel: Type: " + packetType + " Size: " +
    // payload.length);
    int payloadSize = payload.length;

    byte[] data = new byte[payloadSize + TUNNEL_HEADER_SIZE];
    // magic number
    for (int i = 0; i < 10; i++)
    {
      data[i] = (byte)0xFF;
    }
    data[10] = (byte)((payloadSize & 0xFF000000) >> 24);
    data[11] = (byte)((payloadSize & 0xFF0000) >> 16);
    data[12] = (byte)((payloadSize & 0xFF00) >> 8);
    data[13] = (byte)(payloadSize & 0xFF);
    data[14] = packetType;

    System.arraycopy(payload, 0, data, TUNNEL_HEADER_SIZE, payloadSize);

    // String message = " Packet content: ";
    // for (int i = 0; i < data.length; i++)
    // {
    // message += Integer.toHexString(((int)data[i]) & 0xFF) + " ";
    // }
    // System.out.println(message);

    try
    {
      outputStream.write(data);
    } catch (Exception e)
    {
      closeTunnel();
    }
  }

  /** Checks if the connection is valid. */
  public boolean isValidConnection()
  {
    return inputStream != null && outputStream != null;
  }

  /** Resets the data buffer. */
  private void resetBuffer()
  {
    inputDataOffset = 0;
    expectedPacketLength = 0;
    expectedPacketType = 0;
    packetHeaderProcessed = false;
  }

  /** Retrieves the number of bytes that can currently be stored in the internal byte array. */
  private int getRemainingBufferSize()
  {
    return inputData.length - inputDataOffset;
  }

  /**
   * Handle data received via the tunnel connection.
   * 
   * 
   * @param newData
   *          The received data
   * 
   * @return The number of bytes that were handled
   */
  private int handleReceivedData(byte[] newData, int newDataOffset, int newDataLength)
  {
    int handledLength = 0;
    try
    {
      // calculate size that fits into the buffer
      int fittingLength = Math.min(newDataLength, inputData.length - inputDataOffset);

      // copy data to input array
      System.arraycopy(newData, newDataOffset, inputData, inputDataOffset, fittingLength);

      inputDataOffset += fittingLength;
      handledLength += fittingLength;
      // shift offset to start of not handled data
      newDataOffset += fittingLength;
      newDataLength -= fittingLength;

      // now, the next unprocessed packet starts at inputData[0]
      // System.out.println(timeStamp() + length + " bytes received...");
      //          
      // System.out.print(" Upper layer data: ");
      // for (int i = 0; i < inputDataOffset; i++)
      // {
      // System.out.print(((int)inputData[i] & 0xFF) + " ");
      // }
      // System.out.println();

      boolean hasPacketHeader = true;
      // process all found packets
      while (hasPacketHeader)
      {
        // assume new packet
        hasPacketHeader = false;

        // we have read at least a complete packet header
        // and no packets are pending
        if (inputDataOffset >= TUNNEL_HEADER_SIZE && !packetHeaderProcessed)
        {
          // check of magic number not needed because outer TCP assures correct stream

          // retrieve packet size
          expectedPacketLength =
            ((inputData[10] & 0xFF) << 24) + ((inputData[11] & 0xFF) << 16) + ((inputData[12] & 0xFF) << 8) +
              (inputData[13] & 0xFF);

          expectedPacketType = inputData[14];

          // if (expectedPacketLength != length)
          // {
          // System.out.println("Read partial packet from TCP stream: Offset:" + inputDataOffset +
          // " Length:" + inputData.length + " Received:" + length);
          // }
          packetHeaderProcessed = true;
        }
        // we have read a complete packet
        if (packetHeaderProcessed && inputDataOffset >= expectedPacketLength + TUNNEL_HEADER_SIZE)
        {
          LogHelper.debug("  Received packet from tunnel: Type: " + expectedPacketType + " Size: " +
            expectedPacketLength);

          // retrieve packet
          byte[] packetData = new byte[expectedPacketLength];
          // copy packet to new byte array
          System.arraycopy(inputData, TUNNEL_HEADER_SIZE, packetData, 0, packetData.length);

          int remainingSize = inputDataOffset - expectedPacketLength - TUNNEL_HEADER_SIZE;
          // copy remaining bytes to start of input array
          if (remainingSize > 0)
          {
            System.arraycopy(inputData, expectedPacketLength + TUNNEL_HEADER_SIZE, inputData, 0, remainingSize);
          }
          // set offset to end of remaining data
          inputDataOffset = remainingSize;
          // if not all new data could yet be copied, try to copy the remaining new data
          if (newDataLength > 0)
          {
            // calculate size that fits into the buffer
            fittingLength = Math.min(newDataLength, inputData.length - inputDataOffset);

            // copy data to input array
            System.arraycopy(newData, newDataOffset, inputData, inputDataOffset, fittingLength);

            inputDataOffset += handledLength;

            handledLength += fittingLength;
            // shift offset to start of not handled data
            newDataOffset += fittingLength;
            newDataLength -= fittingLength;
          }

          packetHeaderProcessed = false;
          // assume further packets
          hasPacketHeader = true;

          // forward received packet
          if (tunnelListener != null)
          {
            tunnelListener.packetReceived(expectedPacketType, packetData);
          }
        }
      }
    } catch (Exception e)
    {
      LogHelper.warn(getName() + ": Error handling packets from tunnel: " + e.getMessage());
      e.printStackTrace();
      closeTunnel();
    }
    return handledLength;
  }

  public void run()
  {
    LogHelper.info(getName() + ": Start packet tunnel...");
    byte[] buffer = new byte[BUFFER_SIZE];

    while (!terminateThread && inputStream != null && outputStream != null)
    {
      try
      {
        int available = inputStream.available();
        // read socket until empty
        while (available > 0)
        {
          // read as much bytes as possible
          int requestedLength = Math.min(available, getRemainingBufferSize());

          int length = inputStream.read(buffer, 0, requestedLength);

          // System.out.println("Received " + length + " bytes from tunnel");

          if (length == -1)
          {
            throw new Exception(getName() + ": End of stream reached");
          }

          // handle received data
          int handledData = handleReceivedData(buffer, 0, length);
          if (handledData < length)
          {
            LogHelper.debug("NOT ALL DATA from the tunnel could be handled.");
            resetBuffer();
          }
          if (inputStream == null)
          {
            throw new Exception("Connection closed");
          }
          available = inputStream.available();
          ThreadHelper.sleep(1);
        }
      } catch (Exception ex)
      {
        LogHelper.warn(getName() + ": ERROR in packet tunnel: " + ex.getMessage() + ". Close socket...");
        closeTunnel();
      }
      // limit processor usage
      ThreadHelper.sleep(50);
    }
    // signal proper termination
    terminated = true;
    LogHelper.debug(getName() + ": Packet tunnel thread was shut down");
    closeTunnel();
  }

  /** Closes the tunnel by closing in- and output streams. */
  private void closeTunnel()
  {
    try
    {
      inputStream.close();
      outputStream.close();
    } catch (Exception e)
    {
    }
    inputStream = null;
    outputStream = null;

  }

  /**
   * Terminates the complete connection to the client and waits for the termination of the thread.
   */
  public void terminate()
  {
    terminateThread = true;
    // wait for termination of thread
    while (!terminated)
    {
      ThreadHelper.sleep(50);
    }
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

}
