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

import de.fraunhofer.fokus.upnp.core_av.didl.DIDLObject;

/**
 * This class represents a browse response.
 * 
 * @author Alexander Koenig
 * 
 */
public class BrowseResponse
{

  private DIDLObject[] result;

  private long         numberReturned;

  private long         totalMatches;

  private long         updateID;

  /**
   * Creates a new instance of BrowseResponse.
   * 
   * @param result
   * @param numberReturned
   * @param totalMatches
   * @param updateID
   */
  public BrowseResponse(DIDLObject[] result, long numberReturned, long totalMatches, long updateID)
  {
    this.result = result;
    this.numberReturned = numberReturned;
    this.totalMatches = totalMatches;
    this.updateID = updateID;
  }

  /**
   * Creates a new instance of BrowseResponse.
   * 
   * @param result
   * @param numberReturned
   * @param totalMatches
   * @param updateID
   */
  public BrowseResponse(DIDLObject result, long updateID)
  {
    this.result = new DIDLObject[] {
      result
    };
    this.numberReturned = 1L;
    this.totalMatches = 1L;
    this.updateID = updateID;
  }

  /**
   * Retrieves the numberReturned.
   * 
   * @return The numberReturned.
   */
  public long getNumberReturned()
  {
    return numberReturned;
  }

  /**
   * Sets the numberReturned.
   * 
   * @param numberReturned
   *          The numberReturned to set.
   */
  public void setNumberReturned(long numberReturned)
  {
    this.numberReturned = numberReturned;
  }

  /**
   * Retrieves the result.
   * 
   * @return The result.
   */
  public DIDLObject[] getResult()
  {
    return result;
  }

  /**
   * Retrieves the first DIDL object.
   * 
   * @return The result.
   */
  public DIDLObject getFirstResult()
  {
    if (result != null && result.length > 0)
    {
      return result[0];
    }

    return null;
  }

  /**
   * Sets the result.
   * 
   * @param result
   *          The result to set.
   */
  public void setResult(DIDLObject[] result)
  {
    this.result = result;
  }

  /**
   * Retrieves the totalMatches.
   * 
   * @return The totalMatches.
   */
  public long getTotalMatches()
  {
    return totalMatches;
  }

  /**
   * Sets the totalMatches.
   * 
   * @param totalMatches
   *          The totalMatches to set.
   */
  public void setTotalMatches(long totalMatches)
  {
    this.totalMatches = totalMatches;
  }

  /**
   * Retrieves the updateID.
   * 
   * @return The updateID.
   */
  public long getUpdateID()
  {
    return updateID;
  }

  /**
   * Sets the updateID.
   * 
   * @param updateID
   *          The updateID to set.
   */
  public void setUpdateID(long updateID)
  {
    this.updateID = updateID;
  }

}
