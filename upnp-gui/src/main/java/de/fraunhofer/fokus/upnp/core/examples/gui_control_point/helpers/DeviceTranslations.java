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

import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.control_point.CPDevice;
import de.fraunhofer.fokus.upnp.util.swing.SmoothButton;
import de.fraunhofer.fokus.upnp.util.swing.SmoothValueButton;

/**
 * This class is responsible for translating device and service dependent strings, e.g. action
 * names, variables etc.
 * 
 * @author Alexander Koenig
 */
public class DeviceTranslations
{

  private static final int TEXT_ID             = 1;

  private static final int VALUE_ID            = 2;

  private static final int TOOLTIP_ID          = 3;

  private CPDevice         device;

  private Vector           pendingTranslations = new Vector();

  private Object           lock                = new Object();

  /** Creates a new instance of DeviceGUIContext */
  public DeviceTranslations(CPDevice device)
  {
    this.device = device;
  }

  public CPDevice getDevice()
  {
    return device;
  }

  /** Retrieves a certain translation. */
  public String getTranslation(String text)
  {
    synchronized(lock)
    {
      // check for translation
      Object result = device.getTranslationTable().get(text);
      if (result != null)
      {
        return (String)result;
      }

      return text;
    }
  }

  /** Translates text for a button */
  public void setTranslationForButton(SmoothButton button, String text)
  {
    setTranslationForButton(button, text, "");
  }

  /**
   * Translates text for a button.
   * 
   * @param button
   *          The button
   * @param text
   *          The text that should be translated
   * @param afterTranslation
   *          Text to be added after the translation
   * 
   */
  public void setTranslationForButton(SmoothButton button, String text, String afterTranslation)
  {
    setTranslationForButton(button, text, "", afterTranslation);
  }

  /** Translates text for a button */
  public void setTranslationForButton(SmoothButton button,
    String text,
    String beforeTranslation,
    String afterTranslation)
  {
    setTranslation(button, text, beforeTranslation, afterTranslation, TEXT_ID);
  }

  /** Translates text for a button value */
  public void setTranslationForButtonValue(SmoothValueButton button,
    String text,
    String beforeTranslation,
    String afterTranslation)
  {
    setTranslation(button, text, beforeTranslation, afterTranslation, VALUE_ID);
  }

  /** Translates text for a button tooltip */
  public void setTranslationForTooltip(SmoothButton button, String text)
  {
    setTranslation(button, text, "", "", TOOLTIP_ID);
  }

  /** Translates text for a button, either text, value or tooltip */
  private void setTranslation(SmoothButton button,
    String text,
    String beforeTranslation,
    String afterTranslation,
    int targetID)
  {
    // do not translate numbers
    try
    {
      Double.parseDouble(text);

      if (targetID == TEXT_ID)
      {
        button.setText(beforeTranslation + text + afterTranslation);
      }
      if (targetID == TOOLTIP_ID)
      {
        button.setToolTipText(beforeTranslation + text + afterTranslation);
      }
      if (targetID == VALUE_ID && button instanceof SmoothValueButton)
      {
        ((SmoothValueButton)button).setValue(beforeTranslation + text + afterTranslation);
      }

      return;
    } catch (Exception e)
    {
    }

    synchronized(lock)
    {
      // check if translations were already read
      if (device.getTranslationTable() == null)
      {
        // System.out.println("Translations are pending, add to list");

        pendingTranslations.add(new TranslationEntry(button, text, beforeTranslation, afterTranslation, targetID));

        return;
      }

      // check for translation
      String result = (String)device.getTranslationTable().get(text);
      if (result != null)
      {
        if (targetID == TEXT_ID)
        {
          button.setText(beforeTranslation + result + afterTranslation);
        }
        if (targetID == TOOLTIP_ID)
        {
          button.setToolTipText(beforeTranslation + result + afterTranslation);
        }
        if (targetID == VALUE_ID && button instanceof SmoothValueButton)
        {
          ((SmoothValueButton)button).setValue(beforeTranslation + result + afterTranslation);
        }

        return;
      }

      // translation not found, do not translate
      if (targetID == TEXT_ID)
      {
        button.setText(beforeTranslation + text + afterTranslation);
      }
      if (targetID == TOOLTIP_ID)
      {
        button.setToolTipText(beforeTranslation + text + afterTranslation);
      }
      if (targetID == VALUE_ID && button instanceof SmoothValueButton)
      {
        ((SmoothValueButton)button).setValue(beforeTranslation + text + afterTranslation);
      }
    }
  }

  /** Event that the translations have been read. */
  public void translationsRead()
  {
    // System.out.println("Translate pending buttons");
    synchronized(lock)
    {
      // check if translations are now available
      if (pendingTranslations.size() > 0 && device.getTranslationTable() != null)
      {
        for (int i = 0; i < pendingTranslations.size(); i++)
        {
          TranslationEntry currentEntry = (TranslationEntry)pendingTranslations.elementAt(i);
          setTranslation(currentEntry.button,
            currentEntry.text,
            currentEntry.beforeTranslation,
            currentEntry.afterTranslation,
            currentEntry.targetID);
        }
        pendingTranslations.clear();
      }
    }
  }

  /** This class holds one pending translation */
  private class TranslationEntry
  {
    public SmoothButton button;

    /** Original text */
    public String       text;

    public String       beforeTranslation;

    public String       afterTranslation;

    public int          targetID;

    /**
     * Creates a new instance of TranslationEntry.
     * 
     * @param button
     * @param text
     * @param beforeTranslation
     * @param afterTranslation
     * @param targetID
     * 
     */
    public TranslationEntry(SmoothButton button,
      String text,
      String beforeTranslation,
      String afterTranslation,
      int targetID)
    {
      this.button = button;
      this.text = text;
      this.beforeTranslation = beforeTranslation;
      this.afterTranslation = afterTranslation;
      this.targetID = targetID;
    }
  }

}
