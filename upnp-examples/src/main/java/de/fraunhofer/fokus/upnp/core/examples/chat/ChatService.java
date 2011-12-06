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
package de.fraunhofer.fokus.upnp.core.examples.chat;

import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.DeviceConstant;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.device.Action;
import de.fraunhofer.fokus.upnp.core.device.StateVariable;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateService;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;

/**
 * This class provides a basic chat service for UPnP devices.
 * 
 * @author Alexander Koenig
 */
public class ChatService extends TemplateService
{

  private StateVariable messageCount;

  private StateVariable A_ARG_TYPE_int;

  private StateVariable A_ARG_TYPE_string;

  private Action        getMessage;

  private Action        addMessage;

  private Vector        messageList;

  /** Creates a new instance of ChatService */
  public ChatService(TemplateDevice device)
  {
    super(device, DeviceConstant.CHAT_SERVICE_TYPE, DeviceConstant.CHAT_SERVICE_ID);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateService#setupServiceVariables()
   */
  public void setupServiceVariables()
  {
    super.setupServiceVariables();
    messageList = new Vector();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateService#initServiceContent()
   */
  public void initServiceContent()
  {
    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // State variables //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    messageCount = new StateVariable("MessageCount", 0, true);
    messageCount.setModeration(true, 2000);
    A_ARG_TYPE_int = new StateVariable("A_ARG_TYPE_int", -1, false);
    A_ARG_TYPE_string = new StateVariable("A_ARG_TYPE_string", "", false);

    StateVariable[] stateVariableList = {
        messageCount, A_ARG_TYPE_int, A_ARG_TYPE_string
    };
    setStateVariableTable(stateVariableList);

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // Actions //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    addMessage = new Action("AddMessage");
    addMessage.setArgumentTable(new Argument[] {
        new Argument("Name", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_string),
        new Argument("Message", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_string)
    });

    getMessage = new Action("GetMessage");
    getMessage.setArgumentTable(new Argument[] {
        new Argument("Index", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_int),
        new Argument("Name", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_string),
        new Argument("Message", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_string)
    });

    Action[] actionList = {
        addMessage, getMessage
    };
    setActionTable(actionList);
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Action implementation //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  public void addMessage(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    try
    {
      String name = args[0].getStringValue();
      String message = args[1].getStringValue();
      MessageEntry entry = new MessageEntry(name, message);

      // prevent large message lists
      while (messageList.size() > 1000)
      {
        messageList.remove(0);
      }
      messageList.add(entry);
      messageCount.setNumericValue(messageList.size());
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void getMessage(Argument[] args) throws ActionFailedException
  {
    if (args.length != 3)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    try
    {
      int index = (int)args[0].getNumericValue();
      if (index >= 0 && index < messageList.size())
      {
        MessageEntry entry = (MessageEntry)messageList.elementAt(index);

        args[1].setValue(entry.name);
        args[2].setValue(entry.content);
        return;
      } else
      {
        throw new ActionFailedException(801, "Invalid index");
      }
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  private class MessageEntry
  {
    public long   timeStamp;

    public String name;

    public String content;

    public MessageEntry(String name, String content)
    {
      timeStamp = System.currentTimeMillis();
      this.name = name;
      this.content = content;
    }
  }

}
