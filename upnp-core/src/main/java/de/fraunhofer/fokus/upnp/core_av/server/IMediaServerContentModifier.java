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

import java.util.Vector;

import de.fraunhofer.fokus.upnp.core_av.didl.DIDLObject;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;

/**
 * This listener provides methods to manipulate a media server. It is used both for local and remote
 * views on a media server.
 * 
 * @author Alexander Koenig
 */
public interface IMediaServerContentModifier
{

  /**
   * Creates a new object in a certain container.
   * 
   * @param didlObject
   *          The new object
   * @param containerID
   *          The parent container ID
   * 
   * @return The newly created object
   * 
   */
  public DIDLObject createObject(DIDLObject didlObject, String containerID) throws ActionFailedException;

  /**
   * Destroys an object.
   * 
   * @param didlObject
   *          The object
   * 
   */
  public void destroyObject(DIDLObject didlObject) throws ActionFailedException;

  /**
   * Updates an object.
   * 
   * @param didlObject
   *          The object that should be updated
   * @param currentTagValues
   *          A vector with strings, containing the current values
   * @param newTagValues
   *          A vector with strings, containing the new values
   * 
   */
  public void updateObject(DIDLObject didlObject, Vector currentTagValues, Vector newTagValues) throws ActionFailedException;

  /**
   * Imports a resource for a certain object.
   * 
   * @param didlObject
   *          The object
   * @param sourceURI
   *          The URL that should be imported
   * 
   * @return The transferID
   * 
   */
  public long importResource(DIDLObject didlObject, String sourceURI) throws ActionFailedException;

  /**
   * Retrieves the transfer progress for a transferID.
   * 
   * @param transferID
   *          The transfer ID
   * 
   * @return The transfers status
   */
  public TransferStatus getTransferProgress(long transferID) throws ActionFailedException;

}
