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
package de.fraunhofer.fokus.upnp.core.test;

import java.io.File;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.core.xml.ActionDescriptionHandler;
import de.fraunhofer.fokus.upnp.core.xml.ArgumentHandler;
import de.fraunhofer.fokus.upnp.core.xml.ServiceDescriptionHandler;

/**
 * @author icu
 * 
 */
public class TestServiceSax
{

  private static Logger logger = Logger.getLogger("upnp");

  public static void main(String[] args)
  {
    try
    {
      ServiceDescriptionHandler dh = new ServiceDescriptionHandler();
      dh.parse(new File("test2.xml"));

      Vector v = dh.getActionHandlerList();

      for (int i = 0; i < v.size(); i++)
      {
        ActionDescriptionHandler ah = (ActionDescriptionHandler)v.elementAt(i);
        logger.info("Action Name = " + ah.getName());

        Vector v2 = ah.getArgumentHandlerList();

        for (int j = 0; j < v2.size(); j++)
        {
          ArgumentHandler argh = (ArgumentHandler)v2.elementAt(j);
          logger.info("Argument Name = " + argh.getName() + " direction = " + argh.getDirection() +
            " relatedStateVariable = " + argh.getRelatedStateVariableName());
        }
      }
    } catch (Exception se)
    {
      se.printStackTrace();
      logger.error(se.getMessage());
    }
  }
}
