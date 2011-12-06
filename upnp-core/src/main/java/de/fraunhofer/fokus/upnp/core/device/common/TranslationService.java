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
package de.fraunhofer.fokus.upnp.core.device.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.DeviceConstant;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.device.Action;
import de.fraunhofer.fokus.upnp.core.device.StateVariable;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateService;
import de.fraunhofer.fokus.upnp.util.CollectionHelper;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.FileHelper;
import de.fraunhofer.fokus.upnp.util.ResourceHelper;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.XMLHelper;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;
import de.fraunhofer.fokus.upnp.util.security.DigestHelper;

/**
 * This class encapsulates a translation service that can be used to translate action names etc.
 * 
 * @author Alexander Koenig
 */
public class TranslationService extends TemplateService
{

  private StateVariable languageIDList;

  private StateVariable updateID;

  private StateVariable A_ARG_TYPE_string;

  private Action        getLanguageIDList;

  private Action        getUpdateID;

  private Action        getTranslation;

  private Action        getTranslationList;

  /** Table holding all known translations */
  private Hashtable     translationTable;

  /**
   * Creates a new instance of TranslationService. Standard translations and translation files in the working directory
   * are loaded automatically.
   */
  public TranslationService(TemplateDevice device)
  {
    super(device, DeviceConstant.TRANSLATION_SERVICE_TYPE, DeviceConstant.TRANSLATION_SERVICE_ID);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateService#setupServiceVariables()
   */
  public void setupServiceVariables()
  {
    super.setupServiceVariables();
    translationTable = new Hashtable();

    // try classloader first
    addDefaultTranslationsViaClassloader();

    // overwrite from file system
    addDefaultTranslationsFromFile();
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
    // String variable
    languageIDList = new StateVariable("LanguageIDList", "de", true);
    updateID = new StateVariable("UpdateID", "", true);
    A_ARG_TYPE_string = new StateVariable("A_ARG_TYPE_string", "", false);

    updateStateVariableValues();

    StateVariable[] stateVariableList = {
        languageIDList, updateID, A_ARG_TYPE_string
    };
    setStateVariableTable(stateVariableList);

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // Actions //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    getLanguageIDList = new Action("GetLanguageIDList");
    getLanguageIDList.setArgumentTable(new Argument[] {
      new Argument("IDList", UPnPConstant.DIRECTION_OUT, languageIDList)
    });
    getUpdateID = new Action("GetUpdateID");
    getUpdateID.setArgumentTable(new Argument[] {
      new Argument("ID", UPnPConstant.DIRECTION_OUT, updateID)
    });
    getTranslation = new Action("GetTranslation");
    getTranslation.setArgumentTable(new Argument[] {
        new Argument("Text", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_string),
        new Argument("Language", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_string),
        new Argument("Translation", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_string)
    });
    getTranslation.setCacheable(true);
    getTranslationList = new Action("GetTranslationList");
    getTranslationList.setArgumentTable(new Argument[] {
        new Argument("Language", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_string),
        new Argument("Result", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_string)
    });
    getTranslationList.setCacheable(true);

    Action[] actionList = {
        getLanguageIDList, getUpdateID, getTranslation, getTranslationList
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
  public void getLanguageIDList(Argument[] args) throws ActionFailedException
  {
    handleStateVariableRequest(args, languageIDList);
  }

  public void getUpdateID(Argument[] args) throws ActionFailedException
  {
    handleStateVariableRequest(args, updateID);
  }

  /** Returns a single translation */
  public void getTranslation(Argument[] args) throws ActionFailedException
  {
    if (args.length != 3)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    String text = (String)args[0].getValue();
    String language = (String)args[1].getValue();
    if (!language.equals("de"))
    {
      throw new ActionFailedException(801, "Language not supported");
    }
    // System.out.println("Try to get translation for: " + text);

    if (!translationTable.containsKey(text))
    {
      System.out.println("  Translation not found: " + text);
      throw new ActionFailedException(802, "No translation found");
    }
    try
    {
      args[2].setValue(translationTable.get(text));
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  /** Returns a XML document with all known translations */
  public void getTranslationList(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    String language = (String)args[0].getValue();
    if (!language.equals("de"))
    {
      throw new ActionFailedException(801, "Language not supported");
    }

    String result = XMLHelper.createStartTag("TranslationList") + CommonConstants.NEW_LINE;
    Enumeration translations = translationTable.keys();

    while (translations.hasMoreElements())
    {
      String currentName = (String)translations.nextElement();
      String currentTranslation = (String)translationTable.get(currentName);

      result += XMLHelper.createStartTag("Item") + CommonConstants.NEW_LINE;
      result += XMLHelper.createTag("Name", currentName);
      result += XMLHelper.createTag("Translation", currentTranslation);
      result += XMLHelper.createEndTag("Item") + CommonConstants.NEW_LINE;
    }
    result += XMLHelper.createEndTag("TranslationList") + CommonConstants.NEW_LINE;

    try
    {
      args[1].setValue(StringHelper.xmlToEscapedString(result));
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  /** Removes all known translations */
  public void clearTranslation()
  {
    translationTable.clear();
  }

  /** Tries to find a translation */
  public String getTranslation(String text)
  {
    Object translation = translationTable.get(text);
    if (translation == null)
    {
      return text;
    }
    return (String)translation;
  }

  /** Loads default translations that are needed for most UPnP devices. */
  protected void addDefaultTranslationsFromFile()
  {
    addTranslationsFromFile(FileHelper.getResourceDirectoryNameFromPackage(this.getClass().getName()) +
      "translations.txt");

    addTranslationsFromFile(getDevice().getWorkingDirectory() + "translations.txt");
  }

  /** Loads default translations that are needed for most UPnP devices via the class loader. */
  protected void addDefaultTranslationsViaClassloader()
  {
    addTranslationsViaClassloader(getDevice().getClass(),
      ResourceHelper.getResourcePathFromPackage(getClass().getName()) + "translations.txt");

    addTranslationsViaClassloader(getDevice().getClass(),
      ResourceHelper.getResourcePathFromPackage(getDevice().getClass().getName()) + "translations.txt");
  }

  /** Adds translations from the file system. */
  public void addTranslationsFromFile(String fileName)
  {
    if (new File(fileName).exists())
    {
      try
      {
        addTranslationsFromInputStream(new FileInputStream(fileName));
      } catch (Exception ex)
      {
        printMessage("Error loading translations: " + ex.getMessage());
      }
    } else
    {
      printMessage("Could not load translations from " + fileName);
    }
  }

  /** Adds translations via the class loader. */
  public void addTranslationsViaClassloader(Class classInstance, String resourceName)
  {
    if (ResourceHelper.isAvailableViaClassloader(classInstance, resourceName))
    {
      try
      {
        addTranslationsFromInputStream(classInstance.getResourceAsStream(resourceName));
      } catch (Exception ex)
      {
        printMessage("Error loading translations: " + ex.getMessage());
      }
    } else
    {
      printMessage("Could not load translations via class loader from " + resourceName);
    }
  }

  /** Adds translations from an input stream. */
  private void addTranslationsFromInputStream(InputStream inputStream)
  {
    int count = 0;
    Vector result = ResourceHelper.loadStringListFromInputStream(inputStream);
    for (int i = 0; i < result.size(); i++)
    {
      String line = (String)result.elementAt(i);
      // get individual config items
      StringTokenizer tokens = new StringTokenizer(line, ":");
      try
      {
        String text = tokens.nextToken();
        String translation = tokens.nextToken();
        translationTable.put(text, translation);
        count++;
      } catch (Exception ex)
      {
        System.out.println("    Exception while loading translations: " + ex.getMessage());
      }
    }
    printMessage(count + " translations loaded");
  }

  /** Builds the updateID state variable after loading the translation files. */
  private void updateStateVariableValues()
  {
    String content = "";

    Vector keys = CollectionHelper.getSortedKeyList(translationTable);
    for (int i = 0; i < keys.size(); i++)
    {
      String currentText = (String)keys.elementAt(i);
      String currentTranslation = (String)translationTable.get(currentText);

      content += currentText + ":" + currentTranslation + "\r\n";
    }
    try
    {
      updateID.setValue(DigestHelper.calculateSecurityIDForString(content));
    } catch (Exception ex)
    {
    }
  }

}
