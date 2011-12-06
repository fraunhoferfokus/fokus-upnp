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
package de.fraunhofer.fokus.upnp.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.templates.TemplateService;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.ResourceHelper;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.XMLHelper;

/**
 * This class contains the documentation for a UPnP device that supports the UsageService. This can
 * be used to realize JavaDoc-like documentation for each UPnP action.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class UPnPDoc
{
  private String    language;

  private String    deviceUDN;

  /** Hashtable containing a UPnP doc entry vector for each service type */
  private Hashtable docEntryFromServiceTypeTable = new Hashtable();

  /**
   * Creates a new instance of UPnPDoc.
   * 
   * @param language
   *          The UPnP doc language
   */
  public UPnPDoc(String language)
  {
    this.language = language;
  }

  /**
   * Creates a new instance of UPnPDoc.
   * 
   * @param deviceUDN
   * @param language
   *          The UPnP doc language
   */
  public UPnPDoc(String deviceUDN, String language)
  {
    super();
    this.deviceUDN = deviceUDN;
    this.language = language;
  }

  /**
   * Adds the UPnP documentation from a file.
   * 
   * @param fileName
   */
  public void addUPnPDocFromFile(String fileName)
  {
    File file = new File(fileName);
    try
    {
      addUPnPDocFromInputStream(new FileInputStream(file));
    } catch (FileNotFoundException e)
    {
      TemplateService.printMessage("Could not load UPnP doc from " + fileName);
    }
  }

  /** Adds the UPnP documentation via the class loader. */
  public void addUPnPDocViaClassloader(Class classInstance, String resourceName)
  {
    if (ResourceHelper.isAvailableViaClassloader(classInstance, resourceName))
    {
      try
      {
        addUPnPDocFromInputStream(classInstance.getResourceAsStream(resourceName));
      } catch (Exception ex)
      {
        TemplateService.printMessage("Error loading UPnP doc: " + ex.getMessage());
      }
    } else
    {
      TemplateService.printMessage("Could not load UPnP docs via class loader from " + resourceName);
    }
  }

  /**
   * Adds the UPnP documentation from a string.
   * 
   * @param content
   */
  public void addUPnPDocFromString(String content)
  {
    try
    {
      UPnPDocParser parser = new UPnPDocParser();
      parser.parse(content);

      docEntryFromServiceTypeTable.putAll(parser.getDocEntryFormServiceTypeTable());
    } catch (Exception ex)
    {
      System.out.println("Error parsing string: " + ex.getMessage());
    }
  }

  /**
   * Adds the UPnP documentation from an input stream.
   * 
   * @param description
   *          The description
   */
  private void addUPnPDocFromInputStream(InputStream inputStream)
  {
    try
    {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

      byte[] buffer = new byte[4096];

      int read = inputStream.read(buffer);
      while (read != -1)
      {
        outputStream.write(buffer, 0, read);
        read = inputStream.read(buffer);
      }

      String description = StringHelper.byteArrayToUTF8String(outputStream.toByteArray());

      addUPnPDocFromString(description);
    } catch (Exception ex)
    {
      System.out.println("Error reading input stream: " + ex.getMessage());
    }
  }

  /**
   * Retrieves the deviceUDN.
   * 
   * @return The deviceUDN
   */
  public String getDeviceUDN()
  {
    return deviceUDN;
  }

  /** Checks if the class contains valid UPnPDoc entries. */
  public boolean hasUPnPDocEntries()
  {
    return docEntryFromServiceTypeTable.size() > 0;
  }

  /**
   * Returns the documentation for a certain action or state variable.
   * 
   * 
   * @param serviceType
   * @param name
   * 
   * @return The associated {@link UPnPDocEntry} or null.
   */
  public UPnPDocEntry getUPnPDocEntry(String serviceType, String name)
  {
    Vector docEntryList = (Vector)docEntryFromServiceTypeTable.get(serviceType);
    if (docEntryList != null)
    {
      for (int i = 0; i < docEntryList.size(); i++)
      {
        UPnPDocEntry currentEntry = (UPnPDocEntry)docEntryList.elementAt(i);
        if (currentEntry.isAction() && currentEntry.getActionName().equalsIgnoreCase(name))
        {
          return currentEntry;
        }
        if (currentEntry.isStateVariable() && currentEntry.getStateVariableName().equalsIgnoreCase(name))
        {
          return currentEntry;
        }
      }
    }
    return null;
  }

  /**
   * Retrieves the language.
   * 
   * @return The language
   */
  public String getLanguage()
  {
    return language;
  }

  /** Returns the UPnP doc for this language as XML fragment. */
  public String toXMLDescription()
  {
    String result = XMLHelper.createStartTag("upnpDocList") + CommonConstants.NEW_LINE;

    // enumerate all service types
    Enumeration serviceTypes = docEntryFromServiceTypeTable.keys();
    while (serviceTypes.hasMoreElements())
    {
      String serviceType = (String)serviceTypes.nextElement();
      result +=
        XMLHelper.createAttributeStartTag("upnpDoc", "serviceType=\"" + serviceType + "\"") + CommonConstants.NEW_LINE;

      Vector docEntryList = (Vector)docEntryFromServiceTypeTable.get(serviceType);

      // build action table XML
      result += XMLHelper.createStartTag("actionList") + CommonConstants.NEW_LINE;
      for (int i = 0; i < docEntryList.size(); i++)
      {
        UPnPDocEntry currentEntry = (UPnPDocEntry)docEntryList.elementAt(i);
        if (currentEntry.isAction())
        {
          result += currentEntry.toXMLDescription();
        }
      }
      result += XMLHelper.createEndTag("actionList") + CommonConstants.NEW_LINE;

      // build state variable table XML
      result += XMLHelper.createStartTag("serviceStateTable") + CommonConstants.NEW_LINE;
      for (int i = 0; i < docEntryList.size(); i++)
      {
        UPnPDocEntry currentEntry = (UPnPDocEntry)docEntryList.elementAt(i);
        if (currentEntry.isStateVariable())
        {
          result += currentEntry.toXMLDescription();
        }
      }
      result += XMLHelper.createEndTag("serviceStateTable") + CommonConstants.NEW_LINE;
      result += XMLHelper.createEndTag("upnpDoc") + CommonConstants.NEW_LINE;
    }
    result += XMLHelper.createEndTag("upnpDocList");

    return result;
  }

}
