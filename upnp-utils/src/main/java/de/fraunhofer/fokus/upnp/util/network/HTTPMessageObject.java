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

import java.net.InetSocketAddress;
import java.net.URL;

import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.StringHelper;

/** This class is used to store one HTTP message */
public class HTTPMessageObject
{

  /** Header of HTTP message */
  private String            header;

  /** Body of HTTP message */
  private byte[]            body;

  /** Source address of HTTP message */
  private InetSocketAddress sourceAddress;

  /** Destination address of HTTP message */
  private InetSocketAddress destinationAddress;

  /** Flag to close the connection after sending the response */
  private boolean           closeConnection = false;

  /**
   * Creates a new instance of HTTPMessageObject.
   * 
   * @param header
   *          The message header
   * @param body
   *          The message body
   * @param sourceAddress
   *          The address that send the message
   * @param destinationAddress
   *          The address that received the message
   * 
   */
  public HTTPMessageObject(String header,
    byte[] body,
    InetSocketAddress sourceAddress,
    InetSocketAddress destinationAddress)
  {
    // remove trailing empty line from the header
    this.header =
      header != null && header.endsWith(CommonConstants.NEW_LINE + CommonConstants.NEW_LINE) ? header.substring(0,
        header.length() - CommonConstants.NEW_LINE.length()) : header;
    this.body = body;
    this.sourceAddress = sourceAddress;
    this.destinationAddress = destinationAddress;
  }

  /**
   * Creates a new instance of HTTPMessageObject.
   * 
   * @param header
   *          The message header
   * @param body
   *          The message body
   * @param sourceAddress
   *          The address that send the message
   */
  public HTTPMessageObject(String header, byte[] body, InetSocketAddress sourceAddress)
  {
    this(header, body, sourceAddress, null);
  }

  /**
   * Creates a new instance of HTTPMessageObject with an empty body.
   * 
   * @param header
   *          The message header
   * @param sourceAddress
   *          The address that send the message
   */
  public HTTPMessageObject(String header, InetSocketAddress sourceAddress)
  {
    this(header, null, sourceAddress, null);
  }

  /**
   * Creates a new instance of HTTPMessageObject with an empty body and an empty source address.
   * 
   * @param header
   *          The message header
   */
  public HTTPMessageObject(String header)
  {
    this(header, null, null, null);
  }

  /**
   * Creates a new instance of HTTPMessageObject with an empty body and an empty source address.
   * 
   * @param header
   *          The message header
   * @param destinationURL
   *          Target URL for message
   */
  public HTTPMessageObject(String header, URL destinationURL)
  {
    this(header, null, null, IPHelper.toSocketAddress(destinationURL));
  }

  /**
   * Retrieves the source address.
   * 
   * @return The sourceAddress.
   */
  public InetSocketAddress getSourceAddress()
  {
    return sourceAddress;
  }

  /**
   * Retrieves the destination address.
   * 
   * @return The destinationAddress.
   */
  public InetSocketAddress getDestinationAddress()
  {
    return destinationAddress;
  }

  /**
   * Retrieves a byte array for the whole message.
   * 
   * @return The message as byte array
   */
  public byte[] toByteArray()
  {
    if (body != null)
    {
      return HTTPMessageHelper.createHTTPMessage(header, body);
    } else
    {
      return HTTPMessageHelper.createHTTPMessage(header);
    }
  }

  /**
   * Converts this message to a binary message.
   * 
   * @return The message as byte array
   */
  public BinaryMessageObject toBinaryMessage()
  {
    if (body != null)
    {
      return new BinaryMessageObject(HTTPMessageHelper.createHTTPMessage(header, body),
        sourceAddress,
        destinationAddress);
    } else
    {
      return new BinaryMessageObject(HTTPMessageHelper.createHTTPMessage(header), sourceAddress, destinationAddress);
    }
  }

  /** Returns a short description of the message */
  public String toString()
  {
    return HTTPMessageHelper.getHeaderDescription(header) + (body != null ? ":" + body.length : "");
  }

  /**
   * Returns a long description of the message
   * 
   */
  public String toVerboseString()
  {
    return getHeader() + "\n" + getBodyAsUTF8String();
  }

  /**
   * Retrieves the body.
   * 
   * @return The body.
   */
  public byte[] getBody()
  {
    return body;
  }

  /**
   * Retrieves the body as string.
   * 
   * @return The body encoded as string
   */
  public String getBodyAsString()
  {
    if (body != null)
    {
      return StringHelper.byteArrayToString(body);
    } else
    {
      return "";
    }
  }

  /**
   * Decodes the body to UTF-8.
   * 
   * @return The decoded body
   */
  public String getBodyAsUTF8String()
  {
    if (body != null)
    {
      return StringHelper.byteArrayToUTF8String(body);
    } else
    {
      return "";
    }
  }

  /**
   * Retrieves the size of the message.
   * 
   * @return A string in the form headerLength:bodyLength
   */
  public String getSize()
  {
    if (header != null)
    {
      if (body != null)
      {
        return header.length() + ":" + body.length;
      } else
      {
        return header.length() + ":0";
      }
    }
    return "0:0";
  }

  /**
   * Sets the body
   * 
   * @param body
   *          The body to set.
   */
  public void setBody(byte[] body)
  {
    this.body = body;
  }

  /**
   * Retrieves the header.
   * 
   * @return The header.
   */
  public String getHeader()
  {
    return header;
  }

  /**
   * Sets the header
   * 
   * @param header
   *          The header to set.
   */
  public void setHeader(String header)
  {
    this.header = header;
  }

  /**
   * Sets the source address.
   * 
   * @param sourceAddress
   *          The sourceAddress to set.
   */
  public void setSourceAddress(InetSocketAddress sourceAddress)
  {
    this.sourceAddress = sourceAddress;
  }

  /**
   * Sets the destination address.
   * 
   * @param destinationAddress
   *          The destinationAddress to set.
   */
  public void setDestinationAddress(InetSocketAddress destinationAddress)
  {
    this.destinationAddress = destinationAddress;
  }

  /**
   * Retrieves the closeConnection.
   * 
   * @return The closeConnection
   */
  public boolean isCloseConnection()
  {
    return closeConnection;
  }

  /**
   * Sets the closeConnection.
   * 
   * @param closeConnection
   *          The new value for closeConnection
   */
  public void setCloseConnection(boolean closeConnection)
  {
    this.closeConnection = closeConnection;
  }

}
