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
package de.fraunhofer.fokus.upnp.core_av.renderer;

/**
 * Service constants for the MediaRenderer.
 * 
 * @author tje
 */
public interface RendererConstants
{

  public final static String VALUE_INPUT            = "Input";

  public final static String VALUE_OUTPUT           = "Output";

  public final static String VALUE_UNKNOWN          = "Unknown";

  public final static String INTERNAL_ERROR         = "Internal Error";

  public final static int    INTERNAL_ERROR_NO      = 500;

  public final static String AV_ERROR_INSTANCEID    = "The specified instanceID is invalid for this AVTransport";

  public final static int    AV_ERROR_INSTANCEID_NO = 718;

  public final static String AV_ERROR_RESOURCE      = "Resource not found";

  public final static int    AV_ERROR_RESOURCE_NO   = 716;

  public final static String CM_ERROR_REFID         = "Invalid connection reference";

  public final static int    CM_ERROR_REFID_NO      = 706;

  /**
   * Vendor specific maximum volume. The minimum is specified by UPnPAV as zero.
   */
  public final static int    VOLUME_MAX             = 100;

  /**
   * Vendor specific initial volume.
   */
  public final static int    VOLUME_INITIAL         = 80;

}
