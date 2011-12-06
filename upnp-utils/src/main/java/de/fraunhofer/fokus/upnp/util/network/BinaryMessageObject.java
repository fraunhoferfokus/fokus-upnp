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

import de.fraunhofer.fokus.upnp.util.StringHelper;

/** This class is used to store one binary message. */
public class BinaryMessageObject
{

  private byte[]            body;

  private InetSocketAddress sourceAddress;

  private InetSocketAddress destinationAddress;

  /**
   * Creates a new instance of BinaryMessageObject.
   * 
   * @param body
   *          The message body
   * @param sourceAddress
   *          The address that send the message
   */
  public BinaryMessageObject(byte[] body, InetSocketAddress sourceAddress)
  {
    this.body = body;
    this.sourceAddress = sourceAddress;
  }

  /**
   * Creates a new instance of BinaryMessageObject.
   * 
   * @param body
   *          The message body
   * @param sourceAddress
   *          The address that send the message
   * @param destinationAddress
   *          The address that should receive the message
   */
  public BinaryMessageObject(byte[] body, InetSocketAddress sourceAddress, InetSocketAddress destinationAddress)
  {
    this.body = body;
    this.sourceAddress = sourceAddress;
    this.destinationAddress = destinationAddress;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#clone()
   */
  public Object clone()
  {
    return new BinaryMessageObject(body != null ? (byte[])body.clone() : null,
      IPHelper.toSocketAddress(IPHelper.toString(sourceAddress)),
      IPHelper.toSocketAddress(IPHelper.toString(destinationAddress)));
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
   * Retrieves the body as UTF-8 encoded string.
   * 
   * @return The body as UTF-8 encoded string
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
   * Retrieves the source address.
   * 
   * @return The sourceAddress.
   */
  public InetSocketAddress getSourceAddress()
  {
    return sourceAddress;
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
   * Retrieves the destinationAddress.
   * 
   * @return The destinationAddress
   */
  public InetSocketAddress getDestinationAddress()
  {
    return destinationAddress;
  }

  /**
   * Sets the destinationAddress.
   * 
   * @param destinationAddress
   *          The new value for destinationAddress
   */
  public void setDestinationAddress(InetSocketAddress destinationAddress)
  {
    this.destinationAddress = destinationAddress;
  }

  /** Converts the binary message to a HTTPMessage. */
  public HTTPMessageObject toHTTPMessageObject()
  {
    if (body == null)
    {
      return null;
    }

    int headerEndIndex = HTTPMessageHelper.getHeaderEndIndex(body);
    if (headerEndIndex != -1)
    {
      String header = HTTPMessageHelper.getHeader(body, headerEndIndex);
      byte[] httpBody = HTTPMessageHelper.getBody(body, headerEndIndex);

      return new HTTPMessageObject(header, httpBody, sourceAddress, destinationAddress);
    } else
    {
      // message is header only
      return new HTTPMessageObject(getBodyAsString(), null, sourceAddress, destinationAddress);
    }
  }

  /** Returns a debug string describing this message. */
  public String toDebugOutput()
  {
    int headerEndIndex = HTTPMessageHelper.getHeaderEndIndex(body);
    if (headerEndIndex != -1)
    {
      String header = HTTPMessageHelper.getHeader(body, headerEndIndex);
      return HTTPMessageHelper.getHeaderDescription(header);
    } else
    {
      // message is header only
      return HTTPMessageHelper.getHeaderDescription(getBodyAsString());
    }
  }

}
