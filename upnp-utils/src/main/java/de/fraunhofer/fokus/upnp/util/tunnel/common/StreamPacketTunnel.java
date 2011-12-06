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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.logging.LogHelper;

/**
 * This class can be used to send and receive packets over a tunnel that provides in- and output streams. Data is read
 * in a non-blocking fashion by checking inputStream.available().
 * 
 * 
 * @author Alexander Koenig
 * 
 */
public class StreamPacketTunnel extends AbstractPacketTunnel
{
  /** The associated input stream */
  protected InputStream  inputStream;

  /** The associated output stream */
  protected OutputStream outputStream;

  /** Buffer to store received data */
  protected byte[]       streamBuffer = new byte[BUFFER_SIZE];

  /**
   * Creates a new instance of StreamPacketTunnel.
   * 
   * @param tunnelListener
   */
  public StreamPacketTunnel(IPacketTunnelListener tunnelListener)
  {
    super(tunnelListener);
    streamTunnel = true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.tunnel.IPacketTunnel#sendPacketInternal(byte[])
   */
  public void sendPacketInternal(byte[] data) throws IOException
  {
    if (outputStream == null)
    {
      return;
    }
    outputStream.write(data);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.tunnel.IPacketTunnel#isValidConnection()
   */
  public boolean isConnected()
  {
    return inputStream != null && outputStream != null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.tunnel.IPacketTunnel#closeConnection()
   */
  public void closeConnection()
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

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.threads.IEventListener#triggerEvents()
   */
  public void triggerEvents()
  {
    if (isConnected())
    {
      try
      {
        int available = inputStream.available();
        // read socket until empty
        while (available > 0)
        {
          if (getRemainingBufferSize() == 0)
          {
            Portable.println("Receive buffer overflow");
            continue;
          }

          // read as much bytes as possible
          int requestedLength = Math.min(available, getRemainingBufferSize());

          int length = inputStream.read(streamBuffer, 0, requestedLength);

          //          System.out.println("Received " + length + " bytes from tunnel");

          if (length == -1)
          {
            throw new Exception("End of stream reached");
          }

          // handle received data
          int handledData = handleReceivedData(streamBuffer, 0, length);
          if (handledData < length)
          {
            LogHelper.info("NOT ALL DATA from the tunnel could be handled.");
            resetBuffer();
          }
          if (inputStream == null)
          {
            throw new Exception("Connection closed");
          }
          available = inputStream.available();
        }
      } catch (Exception ex)
      {
        LogHelper.warn("ERROR in stream packet tunnel: " + ex.getMessage() + ". Close socket...");
        closeConnection();
      }
    }
  }

  /**
   * Sets the inputStream.
   * 
   * @param inputStream
   *          The new value for inputStream
   */
  public void setInputStream(InputStream inputStream)
  {
    this.inputStream = inputStream;
  }

  /**
   * Sets the outputStream.
   * 
   * @param outputStream
   *          The new value for outputStream
   */
  public void setOutputStream(OutputStream outputStream)
  {
    this.outputStream = outputStream;
  }

}
