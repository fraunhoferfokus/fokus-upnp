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
package de.fraunhofer.fokus.upnp.core.examples.control_point;

import de.fraunhofer.fokus.upnp.core.templates.TemplateControlPoint;
import de.fraunhofer.fokus.upnp.core.templates.TemplateEntity;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;
import de.fraunhofer.fokus.upnp.util.threads.IKeyListener;

/**
 * This class starts an UPnP template control point.
 * 
 * @author Alexander Koenig
 */
public class ControlPointEntity extends TemplateEntity implements IKeyListener
{

  /** Creates a new instance of ControlPointEntity */
  public ControlPointEntity(UPnPStartupConfiguration startupConfiguration)
  {
    super(startupConfiguration);
    System.out.println("  Type <s> to start a new search, <a> to print statistics\n");
    setTemplateControlPoint(new TemplateControlPoint(this, getStartupConfiguration()));
    if (getKeyboardThread() != null)
    {
      getKeyboardThread().setKeyListener(this);
    }
  }

  public static void main(String[] args)
  {
    new ControlPointEntity(args.length > 0 ? new UPnPStartupConfiguration(args[0]) : null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.threads.IKeyListener#keyEvent(int)
   */
  public void keyEvent(int code)
  {
    if (code == 'a')
    {
      System.out.println(getTemplateControlPoint().getBasicControlPoint().toDeviceStatisticsString());
    }
    if (code == 's')
    {
      TemplateControlPoint.printMessage("Trigger root device search");
      getTemplateControlPoint().searchRootDevices();
    }
  }

}
