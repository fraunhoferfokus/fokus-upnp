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
package de.fraunhofer.fokus.upnp.core.examples.gui_control_point.helpers;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.UPnPDoc;
import de.fraunhofer.fokus.upnp.core.UPnPDocEntry;
import de.fraunhofer.fokus.upnp.core.control_point.CPAction;
import de.fraunhofer.fokus.upnp.core.control_point.CPService;
import de.fraunhofer.fokus.upnp.core.control_point.CPStateVariable;
import de.fraunhofer.fokus.upnp.core.examples.gui_common.GUIConstants;
import de.fraunhofer.fokus.upnp.util.swing.ButtonConstants;
import de.fraunhofer.fokus.upnp.util.swing.SmoothValueButton;

/**
 * This context is used for a UPnP service that has no custom plugin.
 * 
 * @author Alexander Koenig
 */
public class ServiceGUIContext
{

  private DeviceGUIContext deviceGUIContext;

  private CPService        service;

  private Vector           stateVariableButtons = new Vector();

  private Vector           stateVariables       = new Vector();

  private Vector           actionList           = new Vector();

  /**
   * Creates a new instance of ServiceGUIContext.
   * 
   * @param deviceGUIContext
   * @param service
   */
  public ServiceGUIContext(DeviceGUIContext deviceGUIContext, CPService service)
  {
    this.deviceGUIContext = deviceGUIContext;
    this.service = service;

    // create state variable buttons
    for (int i = 0; i < service.getCPStateVariableTable().length; i++)
    {
      CPStateVariable variable = service.getCPStateVariableTable()[i];
      // do not show A_ARG_TYPE variables and unevented variables
      if (!variable.getName().startsWith("A_ARG_TYPE") && variable.isEvented())
      {
        SmoothValueButton button =
          new SmoothValueButton(new Dimension(GUIConstants.STATEVARIABLE_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
            12,
            variable.getName() + ":",
            variable.getValueAsString(),
            variable.getName());
        // try to translate initial value
        deviceGUIContext.getDeviceTranslations().setTranslationForButtonValue(button,
          variable.getValueAsString(),
          "",
          "");

        button.setDisabledButtonColor(button.getButtonColor());
        button.setSelectable(false);
        button.setAutomaticTooltip(false);
        stateVariableButtons.add(button);
        stateVariables.add(variable);
      }
    }
    // create action context
    for (int i = 0; i < service.getCPActionTable().length; i++)
    {
      actionList.add(new ActionGUIContext(service.getCPActionTable()[i]));
    }
  }

  /** Adds a listener for an action invoked button click. */
  public void addActionListener(ActionListener listener)
  {
    for (int i = 0; i < service.getCPActionTable().length; i++)
    {
      getActionContext(i).addActionListener(listener);
    }
  }

  /** Adds UPnP doc tooltips */
  public void upnpDocRead()
  {
    UPnPDoc upnpDoc = (UPnPDoc)service.getCPDevice().getUPnPDocFromLanguageTable().get("de");
    if (upnpDoc != null)
    {
      // update for all state variables
      for (int i = 0; i < getStateVariableCount(); i++)
      {
        CPStateVariable currentStateVariable = getStateVariable(i);
        UPnPDocEntry upnpDocEntry = upnpDoc.getUPnPDocEntry(service.getServiceType(), currentStateVariable.getName());

        // add tooltip if possible
        if (upnpDocEntry != null)
        {
          getStateVariableButton(i).setToolTipText(upnpDocEntry.getDescription());
        }
      }
      // update for all actions
      for (int i = 0; i < getActionCount(); i++)
      {
        getActionContext(i).upnpDocRead();
      }
    }
  }

  public int getStateVariableCount()
  {
    return stateVariableButtons.size();
  }

  /** Updates the button for a changed state variable */
  public void updateStateVariable(CPStateVariable variable)
  {
    for (int i = 0; i < stateVariables.size(); i++)
    {
      if ((CPStateVariable)stateVariables.elementAt(i) == variable)
      {
        getStateVariableButton(i).setValue(variable.getValueAsString());
        // try to translate value
        deviceGUIContext.getDeviceTranslations().setTranslationForButtonValue(getStateVariableButton(i),
          variable.getValueAsString(),
          "",
          "");
      }
    }
  }

  public SmoothValueButton getStateVariableButton(int index)
  {
    if (index >= 0 && index < stateVariableButtons.size())
    {
      return (SmoothValueButton)stateVariableButtons.elementAt(index);
    }

    return null;
  }

  public CPStateVariable getStateVariable(int index)
  {
    if (index >= 0 && index < stateVariables.size())
    {
      return (CPStateVariable)stateVariables.elementAt(index);
    }

    return null;
  }

  public int getActionCount()
  {
    return actionList.size();
  }

  public ActionGUIContext getActionContext(int index)
  {
    if (index >= 0 && index < actionList.size())
    {
      return (ActionGUIContext)actionList.elementAt(index);
    }

    return null;
  }

  public CPAction getAction(int index)
  {
    if (index >= 0 && index < actionList.size())
    {
      return ((ActionGUIContext)actionList.elementAt(index)).getCPAction();
    }

    return null;
  }

  public CPService getService()
  {
    return service;
  }

}
