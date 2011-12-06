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
package de.fraunhofer.fokus.lsf.core;

import de.fraunhofer.fokus.upnp.util.Portable;

/**
 * This class represents one message tupel as defined in the LSF (Type, Length, Value).
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class MessageTupel
{

  /** Complete tupel, including type and length */
  private byte[] tupelContent;

  /**
   * Creates a new instance of MessageTupel.
   * 
   * @param tupelContent
   */
  public MessageTupel(byte[] tupelContent)
  {
    this.tupelContent = tupelContent;
  }

  /**
   * Retrieves the value of tupelType.
   * 
   * @return The value of tupelType
   */
  public byte getTupelType()
  {
    return tupelContent[0];
  }

  /**
   * Retrieves the value of tupelLength.
   * 
   * @return The value of tupelLength
   */
  public int getTupelLength()
  {
    return tupelContent.length;
  }

  /**
   * Retrieves the value of tupelContent.
   * 
   * @return The value of tupelContent
   */
  public byte[] getTupelContent()
  {
    return tupelContent;
  }

  /** Retrieves the length of the payload */
  public int getPayloadLength()
  {
    return tupelContent.length - 2;
  }

  /** Retrieves the payload */
  public byte[] getPayload()
  {
    byte[] result = new byte[tupelContent.length - 2];
    Portable.arraycopy(tupelContent, 2, result, 0, result.length);

    return result;
  }

  /** Sets a new payload */
  public void setPayload(byte[] payload)
  {
    int encodedLength = BinaryUPnPConstants.encodeUnitLength(payload.length);
    byte[] result = new byte[encodedLength + 2];
    result[0] = tupelContent[0];
    result[1] = (byte)encodedLength;
    Portable.arraycopy(payload, 0, result, 2, payload.length);
    // add padding bytes
    for (int i = payload.length; i < encodedLength; i++)
    {
      result[i + 2] = BinaryUPnPConstants.UnitTypePadding;
    }
    tupelContent = result;
  }

}
