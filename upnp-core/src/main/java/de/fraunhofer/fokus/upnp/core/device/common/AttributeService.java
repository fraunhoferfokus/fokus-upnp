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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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
 * This class provides an attribute service for UPnP devices. It allows additional property/value pairs for UPnP
 * devices.
 * 
 * @author Alexander Koenig
 */
public class AttributeService extends TemplateService
{

  private StateVariable nameList;

  private StateVariable updateID;

  private StateVariable A_ARG_TYPE_string;

  private Action        getNameList;

  private Action        getUpdateID;

  private Action        getAttributeList;

  private Action        getValue;

  private Action        addAttribute;

  private Action        removeAttribute;

  private Hashtable     attributes;

  /**
   * Creates a new instance of AttributeService. The standard attribute and attribute files in the working directory are
   * loaded automatically.
   */
  public AttributeService(TemplateDevice device)
  {
    super(device, DeviceConstant.ATTRIBUTE_SERVICE_TYPE, DeviceConstant.ATTRIBUTE_SERVICE_ID);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.core.templates.TemplateService#setupServiceVariables()
   */
  public void setupServiceVariables()
  {
    super.setupServiceVariables();
    attributes = new Hashtable();

    // try classloader first
    addDefaultAttributesViaClassloader();

    // overwrite from filesystem
    addDefaultAttributesFromFile();

    // try to add custom attributes
    if (getDevice().getDeviceStartupConfiguration().hasProperty("AttributeFile"))
    {
      addAttributesFromFile(getDevice().getDeviceStartupConfiguration().getProperty("AttributeFile"));
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.core.templates.TemplateService#initServiceContent()
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
    nameList = new StateVariable("NameList", "", true);
    updateID = new StateVariable("UpdateID", "", true);
    updateStateVariableValues();

    A_ARG_TYPE_string = new StateVariable("A_ARG_TYPE_string", "", false);

    StateVariable[] stateVariableList = {
        nameList, updateID, A_ARG_TYPE_string
    };
    setStateVariableTable(stateVariableList);

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // Actions //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    getNameList = new Action("GetNameList");
    getNameList.setArgumentTable(new Argument[] {
      new Argument("List", UPnPConstant.DIRECTION_OUT, nameList)
    });
    getUpdateID = new Action("GetUpdateID");
    getUpdateID.setArgumentTable(new Argument[] {
      new Argument("ID", UPnPConstant.DIRECTION_OUT, updateID)
    });
    getAttributeList = new Action("GetAttributeList");
    getAttributeList.setArgumentTable(new Argument[] {
      new Argument("Result", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_string)
    });
    addAttribute = new Action("AddAttribute");
    addAttribute.setArgumentTable(new Argument[] {
        new Argument("Name", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_string),
        new Argument("Value", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_string)
    });
    removeAttribute = new Action("RemoveAttribute");
    removeAttribute.setArgumentTable(new Argument[] {
      new Argument("Name", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_string)
    });
    getValue = new Action("GetValue");
    getValue.setArgumentTable(new Argument[] {
        new Argument("Name", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_string),
        new Argument("Value", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_string)
    });

    Action[] actionList = {
        getNameList, getUpdateID, getAttributeList, addAttribute, removeAttribute, getValue
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
  public void getNameList(Argument[] args) throws ActionFailedException
  {
    handleStateVariableRequest(args, nameList);
  }

  public void getUpdateID(Argument[] args) throws ActionFailedException
  {
    handleStateVariableRequest(args, updateID);
  }

  public void getAttributeList(Argument[] args) throws ActionFailedException
  {
    String result = "<AttributeList>" + CommonConstants.NEW_LINE;

    Enumeration keys = attributes.keys();
    while (keys.hasMoreElements())
    {
      String currentProperty = (String)keys.nextElement();
      String currentValue = (String)attributes.get(currentProperty);

      result += XMLHelper.createStartTag("Attribute") + CommonConstants.NEW_LINE;
      result += XMLHelper.createTag("Name", currentProperty);
      result += XMLHelper.createTag("Value", currentValue);
      result += XMLHelper.createEndTag("Attribute") + CommonConstants.NEW_LINE;
    }
    result += "</AttributeList>" + CommonConstants.NEW_LINE;

    try
    {
      args[0].setValue(StringHelper.xmlToEscapedString(result));
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void addAttribute(Argument[] args) throws ActionFailedException
  {
    String attribute = "";
    String value = "";
    try
    {
      attribute = args[0].getStringValue();
      value = args[1].getStringValue();
    } catch (Exception e)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    attributes.put(attribute, value);
    updateStateVariableValues();

    writeAttributesToFile(getDevice().getWorkingDirectory() + "attributes.txt");
  }

  public void removeAttribute(Argument[] args) throws ActionFailedException
  {
    String attribute = "";
    try
    {
      attribute = args[0].getStringValue();
    } catch (Exception e)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    attributes.remove(attribute);
    updateStateVariableValues();

    writeAttributesToFile(getDevice().getWorkingDirectory() + "attributes.txt");
  }

  public void getValue(Argument[] args) throws ActionFailedException
  {
    String currentAttribute = (String)args[0].getValue();
    Object value = attributes.get(currentAttribute);
    if (value == null)
    {
      throw new ActionFailedException(801, "Value not found");
    }
    try
    {
      args[1].setValue(value);
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void clearAttributes()
  {
    attributes.clear();
  }

  /**
   * Tries to load a default attribute file from the working directory. If there is no file, the standard attributes are
   * loaded.
   */
  protected void addDefaultAttributesFromFile()
  {
    // first add default values
    addAttributesFromFile(FileHelper.getResourceDirectoryNameFromPackage(this.getClass().getName()) + "attributes.txt");

    addAttributesFromFile(getDevice().getWorkingDirectory() + "attributes.txt");
  }

  /** Loads default attributes that are needed for most UPnP devices. */
  protected void addDefaultAttributesViaClassloader()
  {
    addAttributesViaClassloader(getDevice().getClass(),
      ResourceHelper.getResourcePathFromPackage(getClass().getName()) + "attributes.txt");

    addAttributesViaClassloader(getDevice().getClass(),
      ResourceHelper.getResourcePathFromPackage(getDevice().getClass().getName()) + "attributes.txt");
  }

  /** Adds attribute values for the service from a file. */
  public void addAttributesFromFile(String fileName)
  {
    if (new File(fileName).exists())
    {
      try
      {
        addAttributesFromInputStream(new FileInputStream(fileName));
      } catch (Exception ex)
      {
        printMessage("Error loading attributes: " + ex.getMessage());
      }
    } else
    {
      printMessage("Could not load attributes from " + fileName);
    }
  }

  /** Adds attributes via the class loader. */
  public void addAttributesViaClassloader(Class classInstance, String resourceName)
  {
    if (ResourceHelper.isAvailableViaClassloader(classInstance, resourceName))
    {
      try
      {
        addAttributesFromInputStream(classInstance.getResourceAsStream(resourceName));
      } catch (Exception ex)
      {
        printMessage("Error loading attributes: " + ex.getMessage());
      }
    } else
    {
      printMessage("Could not load attributes via class loader from " + resourceName);
    }
  }

  /** Adds attribute values for the service from a file. */
  private void addAttributesFromInputStream(InputStream inputStream)
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
        String attribute = tokens.nextToken().trim();
        String value = tokens.nextToken().trim();
        attributes.put(attribute, value);
        count++;
      } catch (Exception ex)
      {
        System.out.println("    Exception while loading attributes: " + ex.getMessage());
      }
    }
    printMessage(count + " attributes loaded");
  }

  /** Stores attribute values into a file. */
  private void writeAttributesToFile(String fileName)
  {
    try
    {
      writeAttributesToOutputStream(new FileOutputStream(fileName));
    } catch (Exception ex)
    {
      System.out.println("Error writing attributes: " + ex.getMessage());
    }
  }

  /** Stores attribute values into a stream. */
  private void writeAttributesToOutputStream(OutputStream outputStream)
  {
    String result = "";
    try
    {
      Enumeration keys = attributes.keys();
      while (keys.hasMoreElements())
      {
        String currentProperty = (String)keys.nextElement();
        String currentValue = (String)attributes.get(currentProperty);

        result += currentProperty + ":" + currentValue + "\r\n";
      }
      outputStream.write(StringHelper.utf8StringToByteArray(result));
      outputStream.close();
    } catch (Exception ex)
    {
      System.out.println("Error writing attributes: " + ex.getMessage());
    }
  }

  /**
   * Builds the attribute name list and updateID state variable after loading the attribute files or changes.
   */
  private void updateStateVariableValues()
  {
    String content = "";

    String attributeListString = "";
    Vector keys = CollectionHelper.getSortedKeyList(attributes);
    for (int i = 0; i < keys.size(); i++)
    {
      String currentAttribute = (String)keys.elementAt(i);
      String currentValue = (String)attributes.get(currentAttribute);

      attributeListString += (attributeListString.length() > 0 ? ", " : "") + currentAttribute;

      content += currentAttribute + ":" + currentValue + "\r\n";
    }
    try
    {
      nameList.setValue(attributeListString);
      updateID.setValue(DigestHelper.calculateSecurityIDForString(content));
    } catch (Exception ex)
    {
    }
  }

}
