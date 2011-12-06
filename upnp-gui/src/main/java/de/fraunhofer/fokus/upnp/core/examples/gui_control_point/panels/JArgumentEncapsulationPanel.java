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
package de.fraunhofer.fokus.upnp.core.examples.gui_control_point.panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.toedter.calendar.JDateChooser;

import de.fraunhofer.fokus.upnp.core.AbstractStateVariable;
import de.fraunhofer.fokus.upnp.core.AllowedValueRange;
import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.util.DateTimeHelper;

/**
 * <p>
 * Title: ArgumentEncapsulationPanel
 * </p>
 * <p>
 * Description: Displays an Argument in an appropriate graphical component: JLabel, JComboBox,
 * JFormattedTextField or JSpinner <br>
 * 
 * @author Jan Hauer (habakuk@cs.tu-berlin.de), Alexander Koenig
 * 
 */
public class JArgumentEncapsulationPanel extends JPanel implements
  ActionListener,
  ChangeListener,
  DocumentListener,
  PropertyChangeListener
{

  private static final long       serialVersionUID   = 1L;

  // constants for the components
  public static final int         COMPONENT_UNKNOWN  = 0;

  public static final int         COMPONENT_LABEL    = 1;

  public static final int         COMPONENT_COMBOBOX = 2;

  public static final int         COMPONENT_SPINNER  = 3;

  public static final int         COMPONENT_EDITOR   = 4;

  // constants for value-types
  public static final int         TYPE_URI           = 1;

  public static final int         TYPE_Date          = 2;

  public static final int         TYPE_Character     = 3;

  public static final int         TYPE_Boolean       = 4;

  public static final int         TYPE_Double        = 5;

  public static final int         TYPE_Long          = 6;

  public static final int         TYPE_Unknown       = 8;

  public static final int         TYPE_String        = 9;

  // possible components declaration
  protected JLabel                labelComponent;

  protected JComboBox             comboBoxComponent;

  protected JTextField            textFieldComponent;

  protected JSpinner              spinnerComponent;

  protected JDateChooser          dateChooser;

  protected int                   dataType;               // one of constants for value-types

  protected Object                value;

  protected AbstractStateVariable stateVariable;

  public JArgumentEncapsulationPanel(Argument argument) throws Exception
  {
    value = argument.getValue();
    stateVariable = argument.getRelatedStateVariable();

    setLayout(new FlowLayout(FlowLayout.LEFT, 10, 1));
    dataType = determineType(stateVariable);

    if (value == null)
    {
      throw new NullPointerException("Argument value is null");
    }

    // display Argument as Label
    if (stateVariable.getAllowedValueRange() == null && stateVariable.getAllowedValueList() != null &&
      stateVariable.getAllowedValueList().length == 1 || stateVariable.getAllowedValueRange() != null &&
      stateVariable.getAllowedValueRange().getMax() == stateVariable.getAllowedValueRange().getMin())
    {
      labelComponent = new JLabel(value.toString());
      labelComponent.setBackground(Color.WHITE);
      add(labelComponent);

      return;
    }

    // display Argument in TextEditor
    if (stateVariable.getAllowedValueList() == null &&
      (dataType == TYPE_URI || dataType == TYPE_String || dataType == TYPE_Character))
    {
      textFieldComponent = new JTextField(value.toString());

      if (textFieldComponent.getPreferredSize().width < 250 && (dataType == TYPE_URI || dataType == TYPE_String))
      {
        textFieldComponent.setPreferredSize(new Dimension(250, textFieldComponent.getPreferredSize().height));
      }
      if (dataType == TYPE_Character)
      {
        textFieldComponent.setPreferredSize(new Dimension(20, textFieldComponent.getPreferredSize().height));
      }
      textFieldComponent.getDocument().addDocumentListener(this);
      add(textFieldComponent);

      return;
    }

    // display Argument in ComboBox
    if (stateVariable.getAllowedValueList() != null && stateVariable.getAllowedValueList().length <= 8)
    {
      comboBoxComponent = new JComboBox(stateVariable.getAllowedValueList());
      comboBoxComponent.addActionListener(this);
      comboBoxComponent.setActionCommand(argument.getName());
      add(comboBoxComponent);

      boolean validValue = false;
      for (int i = 0; i < comboBoxComponent.getItemCount(); i++)
      {
        if (comboBoxComponent.getItemAt(i).equals(value))
        {
          validValue = true;
          comboBoxComponent.setSelectedIndex(i);
          break;
        }
      }
      if (!validValue)
      {
        throw new Exception("Argument value is not in allowed value list");
      }

      return;
    }

    // display Argument in Calendar
    if (dataType == TYPE_Date)
    {
      textFieldComponent = new JTextField(DateTimeHelper.formatTimeForGermany(argument.getDateValue()));
      textFieldComponent.setPreferredSize(new Dimension(100, textFieldComponent.getPreferredSize().height));
      textFieldComponent.getDocument().addDocumentListener(this);

      dateChooser = new JDateChooser();
      dateChooser.setLocale(Locale.GERMANY);
      dateChooser.setDate(argument.getDateValue());
      dateChooser.setPreferredSize(new Dimension(150, dateChooser.getPreferredSize().height));
      dateChooser.addPropertyChangeListener(this);

      add(dateChooser);
      add(textFieldComponent);

      return;
    }

    // display Argument in Spinner
    SpinnerModel spinnerModel = null;
    AllowedValueRange avr = stateVariable.getAllowedValueRange();

    if (stateVariable.getAllowedValueList() != null)
    { // get values from ValueList
      spinnerModel = new SpinnerListModel(stateVariable.getAllowedValueList());
    } else
    {
      try
      { // no value List available

        switch (dataType)
        {
        case TYPE_Long:
          // we go for int because spinnerNumberModel does not support long
          // this will only be a problem for very large unsigned ints
          int maximalNumericValue =
            (int)Math.min(Integer.MAX_VALUE, stateVariable.getDataType().getMaximalNumericValue());

          spinnerModel =
            new SpinnerNumberModel((int)argument.getNumericValue(), (int)stateVariable.getDataType()
              .getMinimalNumericValue(), maximalNumericValue, (int)(avr == null ? 1 : Math.round(avr.getStep())));
          break;

        case TYPE_Double:

          spinnerModel =
            new SpinnerNumberModel(((Double)argument.getValue()).doubleValue(), avr == null ? -Double.MAX_VALUE
              : avr.getMin(), avr == null ? Double.MAX_VALUE : avr.getMax(), avr == null ? 1 : avr.getStep());
          break;

        case TYPE_Date:
          spinnerModel = new SpinnerDateModel((Date)argument.getValue(), null, null, java.util.Calendar.DAY_OF_MONTH);

          break;

        case TYPE_Boolean:
          spinnerModel = new SpinnerListModel(new Boolean[] {
              Boolean.FALSE, Boolean.TRUE
          });

          break;
        } // of switch
      } catch (java.lang.ClassCastException e)
      {
        e.printStackTrace();
        labelComponent = new JLabel("unknown");
        labelComponent.setBackground(Color.WHITE);
        add(labelComponent);

        return;
      }
    }
    spinnerComponent = new JSpinner(spinnerModel);
    spinnerComponent.addChangeListener(this);
    spinnerComponent.setPreferredSize(new Dimension(80, spinnerComponent.getPreferredSize().height));
    try
    {
      spinnerComponent.setValue(value);
    } catch (Exception e)
    {
      System.out.println("Could not set predefined value to spinner: " + e.getMessage());
    }
    add(spinnerComponent);
  }

  /**
   * Returns the value, which is shown by the component.
   * 
   * @return Value, shown by component.
   */
  public Object getValue()
  {
    return value;
  }

  /**
   * Determines type of StateVariable. The type is a constant like
   * StateVariableEncapsulationPanel.TYPE_Short.
   * 
   * @param stateVariable
   *          The StateVariable to find the type of.
   * @return Type of the StateVariable as StateVariableEncapsulationPanel-constant.
   */
  public static int determineType(AbstractStateVariable stateVariable)
  {
    if (stateVariable.getDataType().isNumericValue())
    {
      return TYPE_Long;
    }

    if (stateVariable.getDataType().isDoubleValue())
    {
      return TYPE_Double;
    }

    if (stateVariable.getDataType().isBooleanValue())
    {
      return TYPE_Boolean;
    }

    if (stateVariable.getDataType().isCharValue())
    {
      return TYPE_Character;
    }

    if (stateVariable.getDataType().isDateTimeValue())
    {
      return TYPE_Date;
    }

    if (stateVariable.getDataType().isBinBase64Value() || stateVariable.getDataType().isBinHexValue() ||
      stateVariable.getDataType().isStringValue() || stateVariable.getDataType().isUUIDValue())
    {
      return TYPE_String;
    }
    if (stateVariable.getDataType().isURIValue())
    {
      return TYPE_URI;
    }

    return TYPE_Unknown;
  }

  /**
   * Action Listener for ComboBox.
   * 
   * @param e
   */
  public void actionPerformed(ActionEvent e)
  {
    try
    {
      if (e.getSource() instanceof JComboBox)
      {
        JComboBox cb = (JComboBox)e.getSource();
        value = new String((String)cb.getSelectedItem());
      }
    } catch (Exception ex)
    {
    }
  }

  // origin must be a Spinner
  public void stateChanged(ChangeEvent e)
  {
    if (spinnerComponent != null)
    {
      try
      {
        value = spinnerComponent.getValue();
        if (value instanceof Integer)
        {
          value = new Long(((Integer)value).intValue());
        }
      } catch (Exception ex)
      {
      }
    }
  }

  public void changedUpdate(DocumentEvent e)
  {
    if (textFieldComponent != null)
    {
      try
      {
        switch (dataType)
        {
        case TYPE_Character:

          if (textFieldComponent.getText().length() != 1)
          {
            textFieldComponent.setText((String)value);
            return;
          }

        case TYPE_String:
        case TYPE_URI:
          value = textFieldComponent.getText();
          break;
        case TYPE_Date:
          value = buildDate();
          break;

        }
      } catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }

  }

  public void insertUpdate(DocumentEvent e)
  {
    changedUpdate(null);
  }

  public void removeUpdate(DocumentEvent e)
  {
    changedUpdate(null);
  }

  public void propertyChange(PropertyChangeEvent evt)
  {
    if (dataType == TYPE_Date)
    {
      value = buildDate();
    }
  }

  /** Builds the date from the date and the time field */
  private Date buildDate()
  {
    Date currentTime = DateTimeHelper.getTimeFromGermanLocale(textFieldComponent.getText());

    if (currentTime != null)
    {
      Calendar dateCalendar = DateTimeHelper.dateToCalendar(dateChooser.getDate());
      long time1 = dateCalendar.getTimeInMillis();

      Calendar timeCalendar = DateTimeHelper.dateToCalendar(currentTime);
      long time2 =
        timeCalendar.getTimeInMillis() + timeCalendar.get(Calendar.ZONE_OFFSET) + timeCalendar.get(Calendar.DST_OFFSET);

      Calendar calendar = Calendar.getInstance();
      calendar.setTimeInMillis(time1 + time2);

      return calendar.getTime();
    }
    return null;
  }

}
