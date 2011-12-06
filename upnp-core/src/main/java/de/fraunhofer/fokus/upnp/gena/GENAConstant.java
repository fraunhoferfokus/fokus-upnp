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
package de.fraunhofer.fokus.upnp.gena;

/**
 * This class supplies header keywords for GENA messages.
 * 
 * @author icu, Alexander Koenig
 */
public class GENAConstant
{
  public static final String NT                    = "NT:";

  public static final String NTS                   = "NTS:";

  public static final String SID                   = "SID:";

  public static final String SEQ                   = "SEQ:";

  public static final String SECOND                = "Second-";

  public static final String INFINITE              = "infinite";

  public static final String PROPERTYSET_BEGIN_TAG = "<e:propertyset xmlns:e=" + "\"urn:schemas-upnp-org:event-1-0\">";

  public static final String PROPERTYSET_END_TAG   = "</e:propertyset>";

  public static final String PROPERTY_BEGIN_TAG    = "<e:property>";

  public static final String PROPERTY_END_TAG      = "</e:property>";

  public static final String UPNP_PROPCHANGE       = "upnp:propchange";

  public static final String UPNP_EVENT            = "upnp:event";

}
