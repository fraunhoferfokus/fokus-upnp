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
package de.fraunhofer.fokus.upnp.ssdp;

import java.util.Vector;

/**
 * This class is used to bundle all response messages created by a M-SEARCH message processor
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class MSearchMessageProcessorResult
{
  private Vector responseMessages = null;

  private int    mxValue;

  /**
   * Creates a new instance of MSearchMessageProcessorResult.
   * 
   * @param responseMessages
   * @param mxValue
   */
  public MSearchMessageProcessorResult(Vector responseMessages, int mxValue)
  {
    this.responseMessages = responseMessages;
    this.mxValue = mxValue;
  }

  /** Retrieves the number of generated response messages. */
  public int getMessageCount()
  {
    return responseMessages.size();
  }

  /** Retrieves a response message by its index. */
  public String getResponseMessage(int index)
  {
    if (index >= 0 && index < responseMessages.size())
    {
      return (String)responseMessages.elementAt(index);
    }
    return null;
  }

  /**
   * Retrieves the mxValue.
   * 
   * @return The mxValue
   */
  public int getMXValue()
  {
    return mxValue;
  }

}
