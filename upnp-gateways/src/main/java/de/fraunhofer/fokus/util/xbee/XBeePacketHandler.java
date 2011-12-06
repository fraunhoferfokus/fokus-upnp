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
package de.fraunhofer.fokus.util.xbee;

import java.io.ByteArrayOutputStream;
import java.util.Hashtable;

import de.fraunhofer.fokus.upnp.util.ByteArrayHelper;
import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.logging.LogHelper;
import de.fraunhofer.fokus.upnp.util.network.listener.IDataSender;
import de.fraunhofer.fokus.upnp.util.network.listener.INetworkStatus;
import de.fraunhofer.fokus.upnp.util.network.listener.IRawDataReceiveListener;
import de.fraunhofer.fokus.upnp.util.network.listener.IRawDataSender;
import de.fraunhofer.fokus.upnp.util.network.smep.ISMEPPacketListener;
import de.fraunhofer.fokus.upnp.util.network.smep.SMEPPacket;

/**
 * This class can be used to handle data received by XBEE modules which are running in API mode. It also sends data via
 * API commands.
 * 
 * @author Alexander Koenig
 */
public class XBeePacketHandler implements IDataSender, IRawDataReceiveListener, INetworkStatus
{
  protected static final byte FRAME_DELIMITER                    = 0x7E;

  protected static final byte ESCAPE                             = 0x7D;

  protected static final byte XON                                = 0x11;

  protected static final byte XOFF                               = 0x13;

  public static final int     BROADCAST_ADDRESS                  = 0xFFFF;

  public static final byte    API_COMMAND_ID_TX_64BIT            = 0x00;

  public static final byte    API_COMMAND_ID_TX_16BIT            = 0x01;

  public static final byte    API_COMMAND_ID_AT_COMMAND          = 0x08;

  public static final byte    API_COMMAND_ID_RX_64BIT            = (byte)0x80;

  public static final byte    API_COMMAND_ID_RX_16BIT            = (byte)0x81;

  public static final byte    API_COMMAND_ID_AT_COMMAND_RESPONSE = (byte)0x88;

  public static final byte    API_COMMAND_ID_TX_STATUS           = (byte)0x89;

  public static final byte    API_COMMAND_ID_MODEM_STATUS        = (byte)0x8A;

  public static final byte    API_OPTION_DISABLE_ACK             = 1;

  public static final byte    API_OPTION_BROADCAST_PAN_ID        = 4;

  public static final byte    API_OPTION_ADDRESS_BROADCAST_FLAG  = 2;

  public static final byte    API_OPTION_PAN_BROADCAST_FLAG      = 4;

  protected static final int  BUFFER_SIZE                        = 262144;

  /** SOF (0x7E) + Size(2 bytes) */
  protected static final int  HEADER_SIZE                        = 3;

  /** Listener for received packets. */
  private ISMEPPacketListener smepPacketListener;

  /** Listener for XBee events */
  private IXBeeEventListener  xbeeEventListener;

  /** Class to send raw data. */
  private IRawDataSender      rawDataSender;

  /** Buffer to hold received data */
  private byte[]              inputBuffer                        = new byte[BUFFER_SIZE];

  /** Index of first byte after currently available input data */
  private int                 inputBufferEndOffset               = 0;

  private int                 expectedFrameSize                  = 0;

  private int                 sendPacketIndex                    = 0;

  /** Flag that a pending packet is currently processed */
  private boolean             headerProcessed                    = false;

  /** Flag that the next received byte was escaped */
  private boolean             escapeNextReceivedByte             = false;

  /** Local 802.15.4 address */
  private byte[]              localAddress                       = null;

  /**
   * Creates a new instance of XBeePacketHandler.
   * 
   */
  public XBeePacketHandler()
  {
  }

  /**
   * Retrieves the value of smepPacketListener.
   * 
   * @return The value of smepPacketListener
   */
  public ISMEPPacketListener getSmepPacketListener()
  {
    return smepPacketListener;
  }

  /**
   * Sets the new value for smepPacketListener.
   * 
   * @param smepPacketListener
   *          The new value for smepPacketListener
   */
  public void setSmepPacketListener(ISMEPPacketListener smepPacketListener)
  {
    this.smepPacketListener = smepPacketListener;
  }

  /**
   * Retrieves the value of xbeeEventListener.
   * 
   * @return The value of xbeeEventListener
   */
  public IXBeeEventListener getXbeeEventListener()
  {
    return xbeeEventListener;
  }

  /**
   * Sets the new value for xbeeEventListener.
   * 
   * @param xbeeEventListener
   *          The new value for xbeeEventListener
   */
  public void setXbeeEventListener(IXBeeEventListener xbeeEventListener)
  {
    this.xbeeEventListener = xbeeEventListener;
  }

  /**
   * Retrieves the value of rawDataSender.
   * 
   * @return The value of rawDataSender
   */
  public IRawDataSender getRawDataSender()
  {
    return rawDataSender;
  }

  /**
   * Sets the new value for rawDataSender.
   * 
   * @param rawDataSender
   *          The new value for rawDataSender
   */
  public void setRawDataSender(IRawDataSender rawDataSender)
  {
    this.rawDataSender = rawDataSender;
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.network.listener.INetworkStatus#getLocalAddress()
   */
  public byte[] getLocalAddress()
  {
    return localAddress;
  }

  /** Escapes a sent value if needed */
  private byte[] escapeIfNeeded(byte data)
  {
    if (data == FRAME_DELIMITER || data == ESCAPE || data == XON || data == XOFF)
    {
      return new byte[] {
          ESCAPE, (byte)(data ^ 0x20)
      };
    } else
    {
      return new byte[] {
        data
      };
    }
  }

  /**
   * Encapsulates a packet and sends it to the XBEE module.
   * 
   * 
   * @param frameData
   *          The packet
   */
  private void sendFrame(byte[] frameData) throws Exception
  {
    if (rawDataSender == null)
    {
      Portable.println("Error: No data sender available");
      return;
    }
    ByteArrayOutputStream frameOutputStream = new ByteArrayOutputStream();
    int checksum = 0;

    // add frame delimiter
    frameOutputStream.write(FRAME_DELIMITER);
    // add size
    frameOutputStream.write(escapeIfNeeded((byte)(frameData.length >> 8 & 0xFF)));
    frameOutputStream.write(escapeIfNeeded((byte)(frameData.length & 0xFF)));
    for (int i = 0; i < frameData.length; i++)
    {
      checksum = (checksum + frameData[i]) % 256;
      frameOutputStream.write(escapeIfNeeded(frameData[i]));
    }
    checksum = 0xFF - checksum;
    frameOutputStream.write(escapeIfNeeded((byte)checksum));

    //    Portable.println("Raw sent data is " + StringHelper.byteArrayToHexString(frameOutputStream.toByteArray(), " "));

    rawDataSender.sendRawData(frameOutputStream.toByteArray());
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.network.listener.IDataSender#sendData(byte[], byte[], byte[], java.util.Hashtable)
   */
  public void sendData(byte[] sourceAddress, byte[] destinationAddress, byte[] payload, Hashtable options) throws Exception
  {
    // build frame
    ByteArrayOutputStream frameDataOutputStream = new ByteArrayOutputStream();

    // add command ID
    frameDataOutputStream.write(API_COMMAND_ID_TX_16BIT);

    int destinationAddressInt = ByteArrayHelper.byteArrayToUInt16(destinationAddress, 0);
    // data frame number
    if (destinationAddressInt == BROADCAST_ADDRESS)
    {
      frameDataOutputStream.write(0);
    } else
    {
      sendPacketIndex = (sendPacketIndex + 1) % 256;
      frameDataOutputStream.write(sendPacketIndex);
    }
    // destination address
    frameDataOutputStream.write(destinationAddress);
    // options
    if (destinationAddressInt == BROADCAST_ADDRESS)
    {
      frameDataOutputStream.write(API_OPTION_DISABLE_ACK);
    } else
    {
      frameDataOutputStream.write(0);
    }
    frameDataOutputStream.write(payload);

    // build and send complete frame
    sendFrame(frameDataOutputStream.toByteArray());
  }

  /**
   * Sends an AT command to the XBEE module.
   * 
   * 
   * @param command
   * 
   * @param data
   *          Data to set or null for queries
   */
  public int sendATCommand(String command, byte[] data) throws Exception
  {
    Portable.println("Send AT command");
    // build frame
    ByteArrayOutputStream frameDataOutputStream = new ByteArrayOutputStream();

    // add command ID
    frameDataOutputStream.write(API_COMMAND_ID_AT_COMMAND);

    sendPacketIndex = (sendPacketIndex + 1) % 256;
    frameDataOutputStream.write(sendPacketIndex);

    frameDataOutputStream.write(StringHelper.stringToByteArray(command));

    // optional data
    if (data != null)
    {
      frameDataOutputStream.write(data);

    }
    // build and send complete frame
    sendFrame(frameDataOutputStream.toByteArray());

    // return frame index
    return sendPacketIndex;
  }

  /** Resets the data buffer. Can be used to clear the cache after a connection reset. */
  public void resetBuffer()
  {
    inputBufferEndOffset = 0;
    expectedFrameSize = 0;
    headerProcessed = false;
  }

  /** Retrieves the number of bytes that can currently be stored in the internal byte array. */
  protected int getRemainingBufferSize()
  {
    return inputBuffer.length - inputBufferEndOffset;
  }

  /** Processes received xbee frames. */
  private void xbeeDataReceived(byte apiIdentifier, byte[] cmdData)
  {
    //    Portable.println("Frame received");
    if (apiIdentifier == XBeePacketHandler.API_COMMAND_ID_RX_16BIT && cmdData.length > 4)
    {
      int sourceAddress = ByteArrayHelper.byteArrayToUInt16(cmdData, 0);
      int rssi = cmdData[2] & 0xFF;
      int options = cmdData[3] & 0xFF;

      byte[] smepPayload = new byte[cmdData.length - 4];
      Portable.arraycopy(cmdData, 4, smepPayload, 0, smepPayload.length);

      //      Portable.println("Received XBee data packet with " + smepPayload.length + " bytes payload");
      //      Portable.println(StringHelper.byteArrayToMACString(smepPayload));

      SMEPPacket smepPacket = new SMEPPacket(smepPayload);
      if (!smepPacket.isValid())
      {
        Portable.println("Received packet which cannot be parsed as SMEP packet");
        return;
      }
      smepPacket.setSourceAddress(ByteArrayHelper.uint16ToByteArray(sourceAddress));
      smepPacket.getProperties().put("RSSI", new Integer(rssi));
      if ((options & API_OPTION_ADDRESS_BROADCAST_FLAG) > 0)
      {
        smepPacket.setDestinationAddress(ByteArrayHelper.uint16ToByteArray(XBeePacketHandler.BROADCAST_ADDRESS));
      } else
      {
        smepPacket.setDestinationAddress(localAddress);
      }
      if (localAddress == null)
      {
        Portable.println("Discard received data packets as long as local address is unknown");
        return;
      }
      if (smepPacketListener != null)
      {
        smepPacketListener.smepPacketReceived(smepPacket);
      }
    }
    if (apiIdentifier == XBeePacketHandler.API_COMMAND_ID_AT_COMMAND_RESPONSE && cmdData.length >= 4)
    {
      //      int frameID = payload[0] & 0xFF;
      String atCommand = StringHelper.byteArrayToString(cmdData, 1, 2);
      int status = cmdData[3] & 0xFF;
      int frameID = cmdData[0];

      byte[] value = new byte[cmdData.length - 4];
      Portable.arraycopy(cmdData, 4, value, 0, value.length);

      if (atCommand.equalsIgnoreCase("MY") && status == 0 && cmdData.length == 6)
      {
        localAddress = new byte[] {
            cmdData[4], cmdData[5]
        };
        Portable.println("Local address is " + ByteArrayHelper.byteArrayToUInt16(localAddress, 0) + "(0x" +
          Integer.toHexString(ByteArrayHelper.byteArrayToUInt16(localAddress, 0)) + ")");

        if (xbeeEventListener != null)
        {
          xbeeEventListener.xbeeLocalAddressRetrieved(this);
        }
      }
      if (xbeeEventListener != null)
      {
        xbeeEventListener.xbeeATCommandResponseReceived(frameID, atCommand, status, value);
      }
    }
    if (apiIdentifier == XBeePacketHandler.API_COMMAND_ID_TX_STATUS && cmdData.length == 2)
    {
      int frameID = cmdData[0] & 0xFF;
      int status = cmdData[1] & 0xFF;
      Portable.println("Received TX status: " + status);
      if (xbeeEventListener != null)
      {
        xbeeEventListener.xbeeTXStatusReceived(frameID, status);
      }
    }
    if (apiIdentifier == XBeePacketHandler.API_COMMAND_ID_MODEM_STATUS && cmdData.length == 1)
    {
      int status = cmdData[0] & 0xFF;
      Portable.println("Received modem status: " + status);
      if (xbeeEventListener != null)
      {
        xbeeEventListener.xbeeModemStatusReceived(status);
      }
    }
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.network.listener.IRawDataReceiveListener#rawDataReceived(byte[], int, int)
   */
  public void rawDataReceived(byte[] newData, int newDataOffset, int newDataLength)
  {
    int handledLength = 0;
    try
    {
      // remove escaped values
      ByteArrayOutputStream newDataArrayOutputStream = new ByteArrayOutputStream();
      boolean escapedValues = false;
      int i = newDataOffset;
      while (i < newDataOffset + newDataLength)
      {
        if (newData[i] == ESCAPE)
        {
          //          Portable.println("Escaped value found");
          escapedValues = true;
          i++;
          if (i < newDataOffset + newDataLength)
          {
            newDataArrayOutputStream.write(newData[i] ^ 0x20);
          } else
          {
            // the escape was the last received byte, store flag to xor next received byte
            Portable.println("Store escape flag");
            escapeNextReceivedByte = true;
          }
        } else
        {
          if (escapeNextReceivedByte)
          {
            escapeNextReceivedByte = false;
            newDataArrayOutputStream.write(newData[i] ^ 0x20);
          } else
          {
            newDataArrayOutputStream.write(newData[i]);
          }
        }
        i++;
      }
      if (escapedValues)
      {
        newData = newDataArrayOutputStream.toByteArray();
        newDataOffset = 0;
        newDataLength = newData.length;
      }

      boolean hasFrameHeader;
      // loop until all data has been processed
      do
      {
        // assume no further packets
        hasFrameHeader = false;

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

        // check for valid frame
        boolean isValidPacketHeader = true;

        if (inputBufferEndOffset >= HEADER_SIZE && !headerProcessed)
        {
          // check frame delimiter
          isValidPacketHeader = inputBuffer[0] == FRAME_DELIMITER;

          if (isValidPacketHeader)
          {
            // retrieve frame size (frame data and checksum)
            expectedFrameSize = ByteArrayHelper.byteArrayToUInt16(inputBuffer, 1) + 1;

            headerProcessed = true;
          } else
          {
            // we are unsynchronized
            // try to find the next frame
            int frameDelimiterIndex = 1;
            boolean frameDelimiterFound = false;
            while (!frameDelimiterFound && frameDelimiterIndex < inputBufferEndOffset)
            {
              frameDelimiterFound = inputBuffer[frameDelimiterIndex] == FRAME_DELIMITER;
              if (!frameDelimiterFound)
              {
                frameDelimiterIndex++;
              }
            }
            if (frameDelimiterFound)
            {
              System.out.println("Frame delimiter found at " + frameDelimiterIndex);
              // copy new packet to start of input buffer
              int unprocessedInputBufferSize = inputBufferEndOffset - frameDelimiterIndex;
              // copy remaining bytes of input data to start
              if (unprocessedInputBufferSize > 0)
              {
                System.arraycopy(inputBuffer, frameDelimiterIndex, inputBuffer, 0, unprocessedInputBufferSize);
              }
              // set offset to end of remaining data
              inputBufferEndOffset = unprocessedInputBufferSize;
            } else
            {
              // clear the complete buffer
              inputBufferEndOffset = 0;
            }
            // allow new header detection in next (do while) loop
            hasFrameHeader = true;
          }
        }
        int totalFrameSize = expectedFrameSize + HEADER_SIZE;
        // a complete packet is available
        if (headerProcessed && inputBufferEndOffset >= totalFrameSize)
        {
          //          Portable.println("Frame received (" + totalFrameSize + " bytes)");

          byte apiIdentifier = inputBuffer[HEADER_SIZE];
          int checksum = inputBuffer[totalFrameSize - 1] & 0xFF;

          // retrieve cmdData
          byte[] cmdData = new byte[expectedFrameSize - 2];
          // copy cmData to new byte array
          System.arraycopy(inputBuffer, HEADER_SIZE + 1, cmdData, 0, cmdData.length);

          // calculate checksum
          checksum = (checksum + apiIdentifier) % 256;
          for (i = 0; i < cmdData.length; i++)
          {
            checksum = (checksum + cmdData[i] & 0xFF) % 256;
          }

          int unprocessedInputBufferSize = inputBufferEndOffset - totalFrameSize;
          // copy remaining bytes of input data to start
          if (unprocessedInputBufferSize > 0)
          {
            System.arraycopy(inputBuffer, totalFrameSize, inputBuffer, 0, unprocessedInputBufferSize);
          }
          // set offset to end of remaining data
          inputBufferEndOffset = unprocessedInputBufferSize;

          headerProcessed = false;
          // assume further packets
          hasFrameHeader = true;

          if (checksum != 0xFF)
          {
            Portable.println("Received packet with invalid checksum");
          }
          // handle received packet
          if (checksum == 0xFF)
          {
            xbeeDataReceived(apiIdentifier, cmdData);
          }
        } else
        {
          if (headerProcessed)
          {
            //            System.out.println(" A partial packet is pending: " + inputBufferEndOffset);
          }
        }
      } while (hasFrameHeader);
    } catch (Exception e)
    {
      LogHelper.warn("Error handling packets from tunnel: " + e.getMessage());
      e.printStackTrace();
    }
  }

}
