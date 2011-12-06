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
package de.fraunhofer.fokus.upnp.core_av.server;

/**
 * This class represents a transfer status.
 * 
 * @author Alexander Koenig
 * 
 */
public class TransferStatus
{

  private String transferStatus;

  private long   transferTotal;

  private long   transferLength;

  /**
   * Creates a new instance of TransferStatus.
   * 
   * @param transferStatus
   * @param transferTotal
   * @param transferLength
   */
  public TransferStatus(String transferStatus, long transferTotal, long transferLength)
  {
    this.transferStatus = transferStatus;
    this.transferTotal = transferTotal;
    this.transferLength = transferLength;
  }

  /**
   * Retrieves the transferTotal.
   * 
   * @return The transferTotal.
   */
  public long getTransferTotal()
  {
    return transferTotal;
  }

  /**
   * Retrieves the transferLength.
   * 
   * @return The transferLength.
   */
  public long getTransferLength()
  {
    return transferLength;
  }

  /**
   * Retrieves the transferStatus.
   * 
   * @return The transferStatus.
   */
  public String getTransferStatus()
  {
    return transferStatus;
  }

  /**
   * Sets the transferTotal.
   * 
   * @param transferTotal
   *          The transferTotal to set.
   */
  public void setTransferTotal(long transferLength)
  {
    this.transferTotal = transferLength;
  }

  /**
   * Sets the transferLength.
   * 
   * @param transferLength
   *          The transferLength to set.
   */
  public void setTransferLength(long transferProgress)
  {
    this.transferLength = transferProgress;
  }

  /**
   * Sets the transferStatus.
   * 
   * @param transferStatus
   *          The transferStatus to set.
   */
  public void setTransferStatus(String transferStatus)
  {
    this.transferStatus = transferStatus;
  }

}
