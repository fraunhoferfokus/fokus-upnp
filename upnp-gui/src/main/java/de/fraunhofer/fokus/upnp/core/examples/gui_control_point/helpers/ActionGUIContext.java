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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.UPnPDoc;
import de.fraunhofer.fokus.upnp.core.UPnPDocEntry;
import de.fraunhofer.fokus.upnp.core.control_point.CPAction;
import de.fraunhofer.fokus.upnp.core.examples.gui_common.GUIConstants;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.GUIControlConstants;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.swing.ButtonConstants;
import de.fraunhofer.fokus.upnp.util.swing.SmoothButton;
import de.fraunhofer.fokus.upnp.util.swing.SmoothValueButton;

/**
 * This class holds all buttons for a standard action panel.
 * 
 * @author Alexander Koenig
 */
public class ActionGUIContext
{

  /** Associated action */
  private CPAction     action;

  private SmoothButton actionButton;

  private SmoothButton invokeActionButton;

  private SmoothButton invokePersonalizedActionButton;

  private SmoothButton invokeSymmetricPersonalizedActionButton;

  private SmoothButton invokeSymmetricEncryptedPersonalizedActionButton;

  private SmoothButton invokeSignedActionButton;

  private SmoothButton invokeEncryptedActionButton;

  private SmoothButton errorButton;

  private Vector       inArgumentButtons  = new Vector();

  private Vector       outArgumentButtons = new Vector();

  private Vector       inArguments        = new Vector();

  private Vector       outArguments       = new Vector();

  /** Creates a new instance of ActionGUIContext */
  public ActionGUIContext(CPAction action)
  {

    this.action = action;

    // create action button
    actionButton =
      new SmoothButton(new Dimension(GUIConstants.ACTION_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        action.getName(),
        action.getName());
    actionButton.setActiveButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);
    actionButton.setAutomaticTooltip(false);

    // create invoke action button
    invokeActionButton =
      new SmoothButton(new Dimension(GUIConstants.IN_ARGUMENT_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        GUIControlConstants.INVOKE_ACTION,
        GUIControlConstants.INVOKE_ACTION);
    invokeActionButton.setButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);
    invokeActionButton.setActiveButtonColor(Color.white);

    invokePersonalizedActionButton =
      new SmoothButton(new Dimension(GUIConstants.IN_ARGUMENT_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        GUIControlConstants.INVOKE_PUBLIC_KEY_PERSONALIZED_ACTION,
        GUIControlConstants.INVOKE_PUBLIC_KEY_PERSONALIZED_ACTION);
    invokePersonalizedActionButton.setButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);
    invokePersonalizedActionButton.setActiveButtonColor(Color.white);

    invokeSymmetricPersonalizedActionButton =
      new SmoothButton(new Dimension(GUIConstants.IN_ARGUMENT_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        GUIControlConstants.INVOKE_SYMMETRIC_KEY_PERSONALIZED_ACTION,
        GUIControlConstants.INVOKE_SYMMETRIC_KEY_PERSONALIZED_ACTION);
    invokeSymmetricPersonalizedActionButton.setButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);
    invokeSymmetricPersonalizedActionButton.setActiveButtonColor(Color.white);

    invokeSymmetricEncryptedPersonalizedActionButton =
      new SmoothButton(new Dimension(GUIConstants.IN_ARGUMENT_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        GUIControlConstants.INVOKE_SYMMETRIC_KEY_ENCRYPTED_PERSONALIZED_ACTION,
        GUIControlConstants.INVOKE_SYMMETRIC_KEY_ENCRYPTED_PERSONALIZED_ACTION);
    invokeSymmetricEncryptedPersonalizedActionButton.setButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);
    invokeSymmetricEncryptedPersonalizedActionButton.setActiveButtonColor(Color.white);

    // create invoke signed action button
    invokeSignedActionButton =
      new SmoothButton(new Dimension(GUIConstants.IN_ARGUMENT_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        GUIControlConstants.INVOKE_SIGNED_ACTION,
        GUIControlConstants.INVOKE_SIGNED_ACTION);
    invokeSignedActionButton.setButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);
    invokeSignedActionButton.setActiveButtonColor(Color.white);

    // create invoke action button with encryption
    invokeEncryptedActionButton =
      new SmoothButton(new Dimension(GUIConstants.IN_ARGUMENT_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        GUIControlConstants.INVOKE_ENCRYPTED_ACTION,
        GUIControlConstants.INVOKE_ENCRYPTED_ACTION);
    invokeEncryptedActionButton.setButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);
    invokeEncryptedActionButton.setActiveButtonColor(Color.white);

    // create error button
    errorButton =
      new SmoothButton(new Dimension(GUIConstants.IN_ARGUMENT_BUTTON_WIDTH + GUIConstants.OUT_ARGUMENT_BUTTON_WIDTH,
        ButtonConstants.BUTTON_HEIGHT), 12, "", null);
    errorButton.setButtonColor(GUIConstants.SAVE_ACTIVE_BUTTON_COLOR);
    errorButton.setDisabledButtonColor(GUIConstants.SAVE_ACTIVE_BUTTON_COLOR);
    errorButton.setSelectable(false);

    if (action.getInArgumentTable() != null)
    {
      // create in argument buttons
      for (int i = 0; i < action.getInArgumentTable().length; i++)
      {
        Argument argument = action.getInArgumentTable()[i];
        // try to set to default value
        if (argument.getValue() == null)
        {
          try
          {
            argument.setValue(argument.getRelatedStateVariable().getDefaultValue());
          } catch (Exception ex)
          {
          }
        }
        // if still null, check if stateVariable has an allowed value list
        try
        {
          if (argument.getValue() == null && argument.getRelatedStateVariable().getAllowedValueList() != null &&
            argument.getRelatedStateVariable().getAllowedValueList().length > 0)
          {
            // set to first element is allowed value list
            argument.setValue(argument.getRelatedStateVariable().getAllowedValueList()[0]);
          }
        } catch (Exception ex)
        {
        }
        try
        {
          SmoothValueButton button =
            new SmoothValueButton(new Dimension(GUIConstants.IN_ARGUMENT_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
              12,
              argument.getName() + ":",
              argument.getValueAsString(),
              argument.getName());
          button.setAutomaticTooltip(false);
          inArgumentButtons.add(button);
          inArguments.add(argument);
        } catch (Exception ex)
        {
          System.out.println(ex.getMessage());
        }
      }
    }
    if (action.getOutArgumentTable() != null)
    {
      // create out argument buttons
      for (int i = 0; i < action.getOutArgumentTable().length; i++)
      {
        Argument argument = action.getOutArgumentTable()[i];
        // clear value of out argument
        try
        {
          argument.setValue(null);
        } catch (Exception ex)
        {
        }
        SmoothValueButton button =
          new SmoothValueButton(new Dimension(GUIConstants.OUT_ARGUMENT_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
            12,
            argument.getName() + ":",
            "",
            argument.getName());
        button.setDisabledButtonColor(button.getButtonColor());
        button.setSelectable(false);
        button.setAutomaticTooltip(false);
        outArgumentButtons.add(button);
        outArguments.add(argument);
      }
    }
  }

  public void addActionListener(ActionListener listener)
  {
    actionButton.addActionListener(listener);
    invokeActionButton.addActionListener(listener);
    invokePersonalizedActionButton.addActionListener(listener);
    invokeSymmetricPersonalizedActionButton.addActionListener(listener);
    invokeSymmetricEncryptedPersonalizedActionButton.addActionListener(listener);
    invokeSignedActionButton.addActionListener(listener);
    invokeEncryptedActionButton.addActionListener(listener);
    for (int i = 0; i < inArgumentButtons.size(); i++)
    {
      getInArgumentButton(i).addActionListener(listener);
    }
    for (int i = 0; i < outArgumentButtons.size(); i++)
    {
      getOutArgumentButton(i).addActionListener(listener);
    }
  }

  /** Adds UPnP doc tooltips */
  public void upnpDocRead()
  {
    UPnPDoc upnpDoc = (UPnPDoc)action.getCPService().getCPDevice().getUPnPDocFromLanguageTable().get("de");
    if (upnpDoc != null)
    {
      UPnPDocEntry upnpDocEntry = upnpDoc.getUPnPDocEntry(action.getCPService().getServiceType(), action.getName());
      // add tooltip to action if possible
      if (upnpDocEntry != null)
      {
        actionButton.setToolTipText(upnpDocEntry.getDescription());

        for (int i = 0; i < inArgumentButtons.size(); i++)
        {
          SmoothButton button = getInArgumentButton(i);
          Argument argument = getInArgument(i);
          // add tooltip to in argument if possible
          if (upnpDocEntry.getArgumentDescription(argument.getName()) != null)
          {
            button.setToolTipText(upnpDocEntry.getArgumentDescription(argument.getName()));
          }
        }
        for (int i = 0; i < outArgumentButtons.size(); i++)
        {
          SmoothButton button = getOutArgumentButton(i);
          Argument argument = getOutArgument(i);
          // add tooltip to out argument if possible
          if (upnpDocEntry.getArgumentDescription(argument.getName()) != null)
          {
            button.setToolTipText(upnpDocEntry.getArgumentDescription(argument.getName()));
          }
        }
      }
    }
  }

  public SmoothButton getActionButton()
  {
    return actionButton;
  }

  public SmoothButton getInvokeActionButton()
  {
    return invokeActionButton;
  }

  public SmoothButton getInvokePersonalizedActionButton()
  {
    return invokePersonalizedActionButton;
  }

  /**
   * Retrieves the invokeSymmetricEncryptedPersonalizedActionButton.
   * 
   * @return The invokeSymmetricEncryptedPersonalizedActionButton
   */
  public SmoothButton getInvokeSymmetricEncryptedPersonalizedActionButton()
  {
    return invokeSymmetricEncryptedPersonalizedActionButton;
  }

  /**
   * Retrieves the invokeSymmetricPersonalizedActionButton.
   * 
   * @return The invokeSymmetricPersonalizedActionButton
   */
  public SmoothButton getInvokeSymmetricPersonalizedActionButton()
  {
    return invokeSymmetricPersonalizedActionButton;
  }

  public SmoothButton getInvokeSignedActionButton()
  {
    return invokeSignedActionButton;
  }

  public SmoothButton getInvokeEncryptedActionButton()
  {
    return invokeEncryptedActionButton;
  }

  public SmoothButton getErrorButton()
  {
    return errorButton;
  }

  /** Retrieves the number of in arguments. */
  public int getInArgumentCount()
  {
    return inArgumentButtons.size();
  }

  /** Retrieves a specific in argument button. */
  public SmoothValueButton getInArgumentButton(int index)
  {
    if (index >= 0 && index < inArgumentButtons.size())
    {
      return (SmoothValueButton)inArgumentButtons.elementAt(index);
    }

    return null;
  }

  /** Retrieves a specific in argument. */
  public Argument getInArgument(int index)
  {
    if (index >= 0 && index < inArgumentButtons.size())
    {
      return (Argument)inArguments.elementAt(index);
    }

    return null;
  }

  /** Retrieves the number of out arguments. */
  public int getOutArgumentCount()
  {
    return outArgumentButtons.size();
  }

  /** Retrieves a specific out argument button. */
  public SmoothValueButton getOutArgumentButton(int index)
  {
    if (index >= 0 && index < outArgumentButtons.size())
    {
      return (SmoothValueButton)outArgumentButtons.elementAt(index);
    }

    return null;
  }

  /** Retrieves a specific out argument. */
  public Argument getOutArgument(int index)
  {
    if (index >= 0 && index < outArgumentButtons.size())
    {
      return (Argument)outArguments.elementAt(index);
    }

    return null;
  }

  /** Retrieves the button for a certain argument. */
  public SmoothValueButton getInArgumentButton(Argument argument)
  {
    for (int i = 0; i < inArguments.size(); i++)
    {
      if ((Argument)inArguments.elementAt(i) == argument)
      {
        return getInArgumentButton(i);
      }
    }
    return null;
  }

  /** Updates the button for a specific argument. */
  public void updateArgumentButton(Argument argument)
  {
    for (int i = 0; i < inArguments.size(); i++)
    {
      if ((Argument)inArguments.elementAt(i) == argument && argument.getValue() != null)
      {
        // do not show escaped version of the string
        getInArgumentButton(i).setValue(StringHelper.escapedStringToXML(argument.getValueAsString()));
      }
    }
    for (int i = 0; i < outArguments.size(); i++)
    {
      if ((Argument)outArguments.elementAt(i) == argument && argument.getValue() != null)
      {
        getOutArgumentButton(i).setValue(argument.getValueAsString());
      }
    }
  }

  public CPAction getCPAction()
  {
    return action;
  }

}
