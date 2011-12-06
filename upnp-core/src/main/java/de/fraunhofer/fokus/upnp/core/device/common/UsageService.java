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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.DeviceConstant;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.UPnPDoc;
import de.fraunhofer.fokus.upnp.core.UPnPDocEntry;
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
 * This service can be added to arbitrary devices to explain their functionality.
 * 
 * @author Alexander Koenig, Sebastian Nauck
 */
public class UsageService extends TemplateService
{

  private StateVariable languageIDList;

  private StateVariable updateID;

  private StateVariable A_ARG_TYPE_string;

  private Action        getLanguageIDList;

  private Action        getUpdateID;

  private Action        getUsage;

  private Action        getUPnPDoc;

  private Action        getUPnPDocList;

  /** Contains the usage for each language key. */
  private Hashtable     usageFromLanguageTable;

  /** Contains the UPnPDoc for each language key. */
  private Hashtable     upnpDocFromLanguageTable;

  /** Creates a new instance of UsageService. */
  public UsageService(TemplateDevice device)
  {
    super(device, DeviceConstant.USAGE_SERVICE_TYPE, DeviceConstant.USAGE_SERVICE_ID);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateService#setupServiceVariables()
   */
  public void setupServiceVariables()
  {
    super.setupServiceVariables();
    usageFromLanguageTable = new Hashtable();
    upnpDocFromLanguageTable = new Hashtable();

    // try classloader first
    addDefaultUsageViaClassloader();
    addDefaultUPnPDocViaClassloader();

    // overwrite from filesystem
    addDefaultUsageFromFile();
    addDefaultUPnPDocFromFile();
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
    getUsage = new Action("GetUsage");
    getUsage.setArgumentTable(new Argument[] {
        new Argument("Language", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_string),
        new Argument("Usage", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_string)
    });
    getUPnPDoc = new Action("GetUPnPDoc");
    getUPnPDoc.setArgumentTable(new Argument[] {
        new Argument("ServiceType", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_string),
        new Argument("Name", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_string),
        new Argument("Language", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_string),
        new Argument("UPnPDoc", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_string)
    });
    getUPnPDocList = new Action("GetUPnPDocList");
    getUPnPDocList.setArgumentTable(new Argument[] {
        new Argument("Language", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_string),
        new Argument("Result", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_string)
    });
    setActionTable(new Action[] {
        getLanguageIDList, getUpdateID, getUsage, getUPnPDoc, getUPnPDocList
    });

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

  public void getUsage(Argument[] args) throws ActionFailedException
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

    if (!usageFromLanguageTable.containsKey(language))
    {
      System.out.println("  Language not found: " + language);
      throw new ActionFailedException(802, "Language not found");
    }
    try
    {
      args[1].setValue(usageFromLanguageTable.get(language));
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void getUPnPDoc(Argument[] args) throws ActionFailedException
  {
    if (args.length != 4)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    String serviceType = (String)args[0].getValue();
    String name = (String)args[1].getValue();
    String language = (String)args[2].getValue();
    UPnPDoc upnpDoc = (UPnPDoc)upnpDocFromLanguageTable.get(language);
    if (upnpDoc == null)
    {
      throw new ActionFailedException(801, "Language not supported");
    }

    UPnPDocEntry upnpDocEntry = upnpDoc.getUPnPDocEntry(serviceType, name);
    if (upnpDocEntry == null)
    {
      System.out.println("  UPnPDoc not found: " + serviceType + "." + name);
      throw new ActionFailedException(802, "UPnPDoc not found");
    }
    try
    {
      args[3].setValue(StringHelper.xmlToEscapedString(upnpDocEntry.toXMLDescription()));
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void getUPnPDocList(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    String language = (String)args[0].getValue();
    UPnPDoc upnpDoc = (UPnPDoc)upnpDocFromLanguageTable.get(language);
    if (upnpDoc == null)
    {
      throw new ActionFailedException(801, "Language not supported");
    }

    try
    {
      args[1].setValue(StringHelper.xmlToEscapedString(upnpDoc.toXMLDescription()));
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Usage //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /** Loads the usage from a file. */
  public void addUsageFromFile(String fileName, String language)
  {
    if (new File(fileName).exists())
    {
      try
      {
        addUsageFromInputStream(new FileInputStream(fileName), language);
      } catch (Exception ex)
      {
        printMessage("Error loading usage: " + ex.getMessage());
      }
    } else
    {
      printMessage("Could not load usage from " + fileName);
    }
  }

  /** Adds usage via the class loader. */
  public void addUsageViaClassloader(Class classInstance, String resourceName, String language)
  {
    if (ResourceHelper.isAvailableViaClassloader(classInstance, resourceName))
    {
      try
      {
        addUsageFromInputStream(classInstance.getResourceAsStream(resourceName), language);
      } catch (Exception ex)
      {
        printMessage("Error loading usage: " + ex.getMessage());
      }
    } else
    {
      printMessage("Could not load usage via class loader from " + resourceName);
    }
  }

  /** Loads the usage from a file. */
  private void addUsageFromInputStream(InputStream inputStream, String language)
  {
    try
    {
      BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream, 256);
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      byte[] data = new byte[256];
      int readBytes = bufferedInputStream.read(data, 0, data.length);
      while (readBytes != -1)
      {
        byteArrayOutputStream.write(data, 0, readBytes);
        readBytes = bufferedInputStream.read(data, 0, data.length);
      }
      String result = StringHelper.byteArrayToUTF8String(byteArrayOutputStream.toByteArray());

      usageFromLanguageTable.put(language, result);
      inputStream.close();
      bufferedInputStream.close();
      byteArrayOutputStream.close();

    } catch (Exception ex)
    {
      ex.printStackTrace(System.out);
    }
    printMessage(usageFromLanguageTable.size() + " usage(s) loaded");
  }

  /** Loads the default usage from a file. */
  private void addDefaultUsageFromFile()
  {
    addUsageFromFile(getDevice().getWorkingDirectory() + "usage_de.txt", "de");
  }

  /** Loads default usage via the class loader. */
  protected void addDefaultUsageViaClassloader()
  {
    addUsageViaClassloader(getDevice().getClass(), ResourceHelper.getResourcePathFromPackage(getDevice().getClass()
      .getName()) +
      "usage_de.txt", "de");
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // UPnPDoc //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /** Adds UPnP documentation from a file. */
  public void addUPnPDocFromFile(String fileName, String language)
  {
    // create if needed
    UPnPDoc upnpDoc = (UPnPDoc)upnpDocFromLanguageTable.get(language);
    if (upnpDoc == null)
    {
      upnpDoc = new UPnPDoc(language);
      upnpDocFromLanguageTable.put(language, upnpDoc);
    }
    // load
    upnpDoc.addUPnPDocFromFile(fileName);
  }

  /** Adds UPnP documentation via the class loader. */
  public void addUPnPDocViaClassloader(Class classInstance, String resourceName, String language)
  {
    // create if needed
    UPnPDoc upnpDoc = (UPnPDoc)upnpDocFromLanguageTable.get(language);
    if (upnpDoc == null)
    {
      upnpDoc = new UPnPDoc(language);
      upnpDocFromLanguageTable.put(language, upnpDoc);
    }
    // load
    upnpDoc.addUPnPDocViaClassloader(classInstance, resourceName);
  }

  /** Loads the default UPnP doc from a file. */
  private void addDefaultUPnPDocFromFile()
  {
    addUPnPDocFromFile(FileHelper.getResourceDirectoryNameFromPackage(this.getClass().getName()) +
      "upnp_doc_attribute_service_de.xml", "de");
    addUPnPDocFromFile(getDevice().getWorkingDirectory() + "upnp_doc_de.xml", "de");
  }

  /** Loads the default UPnP doc via the class loader. */
  private void addDefaultUPnPDocViaClassloader()
  {
    addUPnPDocViaClassloader(getDevice().getClass(), ResourceHelper.getResourcePathFromPackage(this.getClass().getName()) +
      "upnp_doc_attribute_service_de.xml", "de");

    addUPnPDocViaClassloader(getDevice().getClass(), ResourceHelper.getResourcePathFromPackage(getDevice().getClass()
      .getName()) +
      "upnp_doc_de.xml", "de");
  }

  /** Builds the updateID state variable after loading the usage files. */
  private void updateStateVariableValues()
  {
    String content = "";

    Vector keys = CollectionHelper.getSortedKeyList(usageFromLanguageTable);
    for (int i = 0; i < keys.size(); i++)
    {
      String currentLanguage = (String)keys.elementAt(i);
      String currentUsage = (String)usageFromLanguageTable.get(currentLanguage);

      content += XMLHelper.createTag(currentLanguage, currentUsage) + CommonConstants.NEW_LINE;
    }
    keys = CollectionHelper.getSortedKeyList(upnpDocFromLanguageTable);
    for (int i = 0; i < keys.size(); i++)
    {
      String currentLanguage = (String)keys.elementAt(i);
      UPnPDoc currentDoc = (UPnPDoc)upnpDocFromLanguageTable.get(currentLanguage);

      content += currentDoc.toXMLDescription() + CommonConstants.NEW_LINE;
    }

    try
    {
      updateID.setValue(DigestHelper.calculateSecurityIDForString(content));
    } catch (Exception ex)
    {
    }
  }

}
