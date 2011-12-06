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
package de.fraunhofer.fokus.upnp.core.examples.control_point.point_of_interest;

import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.DeviceConstant;
import de.fraunhofer.fokus.upnp.core.control_point.CPAction;
import de.fraunhofer.fokus.upnp.core.control_point.CPDevice;
import de.fraunhofer.fokus.upnp.core.control_point.CPService;
import de.fraunhofer.fokus.upnp.core.templates.TemplateCPDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateControlPoint;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;

/**
 * A PointOfInterestCPDevice is a remote view on a POI device that allows a direct access to the
 * provided messages.
 * 
 * @author Alexander Koenig
 */
public class PointOfInterestCPDevice extends TemplateCPDevice implements Runnable
{

  private Vector            messageList   = new Vector();

  private int               itemCount     = 0;

  private boolean           messagesRead  = false;

  private IPOIEventListener eventListener = null;

  /**
   * Creates a new instance of PointOfInterestCPDevice.
   * 
   * @param controlPoint
   *          The control point for action invocations
   * @param device
   *          The POI device
   */
  public PointOfInterestCPDevice(TemplateControlPoint controlPoint, CPDevice device, IPOIEventListener eventListener)
  {
    super(controlPoint, device);

    this.eventListener = eventListener;

    // start thread to enumerate items
    Thread thread = new Thread(this);
    thread.setName("PointOfInterestCPDevice");
    thread.start();
  }

  /**
   * Retrieves the messageList.
   * 
   * @return The messageList
   */
  public Vector getMessageList()
  {
    return messageList;
  }

  /**
   * Retrieves the value of messagesRead.
   * 
   * @return The value of messagesRead
   */
  public boolean isMessagesRead()
  {
    return messagesRead;
  }

  /** Collect all messages from POI service */
  public void run()
  {
    itemCount = 0;
    CPService service = getCPDevice().getCPServiceByType(DeviceConstant.POINT_OF_INTEREST_SERVICE_TYPE);
    CPAction action = service.getCPAction("GetItemCount");
    if (action != null)
    {
      try
      {
        action.getArgument("Filter").setValue("*");

        getTemplateControlPoint().invokeAction(action);

        itemCount = (int)action.getArgument("ItemCount").getNumericValue();
      } catch (Exception e)
      {

      }
    }
    action = service.getCPAction("GetItemGroup");
    if (action != null)
    {
      try
      {
        action.getArgument("Offset").setNumericValue(0);
        action.getArgument("Count").setNumericValue(itemCount);
        action.getArgument("Filter").setValue("*");

        getTemplateControlPoint().invokeAction(action);

        String description = action.getArgument("Items").getStringValue();

        POIParser parser = new POIParser();
        parser.parse(description);

        messageList = parser.getMessageList();
        System.out.println(messageList.size() + " messages read.");
      } catch (ActionFailedException afe)
      {
        System.out.println("An error occured:" + afe.getMessage());
      } catch (Exception ex)
      {
        System.out.println("An error occured:" + ex.getMessage());
      }
    }
    System.out.println("POI thread terminated");
    messagesRead = true;
    if (eventListener != null)
    {
      eventListener.poiMessagesRead(service, messageList);
    }
  }

}
