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
package de.fraunhofer.fokus.upnp.util.exceptions;

/**
 * Exception indicating that the requested action has not been performed. This extends the normal
 * exception with a functionality to contain an error code and description.
 * 
 * @author Michael Rother, Alexander Koenig
 * 
 */
public class ActionFailedException extends Exception
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * The code of the error that has occurred.
   */
  private int               errorCode;

  /**
   * Creates a new <code>ActionFailedException</code>
   * 
   * @param errorCode
   *          The code of the error that has occurred.
   * @param errorDescription
   *          Error Description
   */
  public ActionFailedException(int errorCode, String errorDescription)
  {
    super(errorDescription);
    this.errorCode = errorCode;
  }

  /**
   * Returns the error code.
   * 
   * @return The error code.
   */
  public int getErrorCode()
  {
    return errorCode;
  }

  /**
   * Returns the error description.
   * 
   * @return The error description.
   */
  public String getErrorDescription()
  {
    return super.getMessage();
  }

  /**
   * Returns the error as string.
   * 
   * @return The complete error as string.
   */
  public String toString()
  {
    return getMessage();
  }

  /**
   * Returns the error as string.
   * 
   * @return The complete error as string.
   */
  public String getMessage()
  {
    return "Errorcode: " + errorCode + " (" + super.getMessage() + ")";
  }

}
