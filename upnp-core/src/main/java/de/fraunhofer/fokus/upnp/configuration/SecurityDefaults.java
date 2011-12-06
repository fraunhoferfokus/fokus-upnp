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
package de.fraunhofer.fokus.upnp.configuration;

/**
 * This class holds the default values for various security settings.
 * 
 * @author Alexander König
 * 
 * 
 */
public class SecurityDefaults
{
  /** Only the root device is announced via SSDP */
  public static final boolean ANONYMOUS_DISCOVERY             = true;

  /** URLs for control and eventing are translated into GUIDs */
  public static final boolean ANONYMOUS_URLS                  = true;

  /** Description documents are encrypted */
  public static final boolean ENCRYPTED_DESCRIPTION_RESPONSES = true;

  /** Events sent by this device will be encrypted */
  public static final boolean ENCRYPTED_EVENTS                = true;

  /** Description requests to this device must be signed */
  public static final boolean SIGNED_DESCRIPTION_REQUESTS     = true;

  /** Subscriptions requests to this device must be signed */
  public static final boolean SIGNED_EVENT_SUBSCRIPTIONS      = true;

  /** Events sent by this device will be signed */
  public static final boolean SIGNED_EVENTS                   = true;

}
