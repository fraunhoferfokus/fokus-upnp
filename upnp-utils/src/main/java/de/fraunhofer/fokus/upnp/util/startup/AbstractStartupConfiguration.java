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
package de.fraunhofer.fokus.upnp.util.startup;

import java.io.Serializable;
import java.util.Hashtable;

import de.fraunhofer.fokus.upnp.util.SAXTemplateHandler;
import de.fraunhofer.fokus.upnp.util.StringHelper;

/**
 * This is the base class for all startup configurations.
 * 
 * @author Alexander Koenig
 * 
 */
public class AbstractStartupConfiguration extends SAXTemplateHandler implements Serializable
{
  /**
   * 
   */
  private static final long  serialVersionUID      = 1L;

  /** Tag for friendly name */
  public static final String FRIENDLY_NAME         = "FriendlyName";

  /** Tag for ignored IP addresses */
  public static final String IGNORE_IP_ADDRESS     = "IgnoreIPAddress";

  /** Tag for preferred IP addresses */
  public static final String PREFERRED_IP_ADDRESS  = "PreferredIPAddress";

  /** Tag for flag to start a keyboard thread (optional, default = true) */
  public static final String START_KEYBOARD_THREAD = "StartKeyboardThread";

  /** Tag for working directory (optional) */
  public static final String WORKING_DIRECTORY     = "WorkingDirectory";

  protected Hashtable        propertyTable         = new Hashtable();

  protected boolean          valid                 = false;

  /**
   * Creates a new instance of AbstractStartupConfiguration.
   * 
   */
  public AbstractStartupConfiguration()
  {
    super();
    setupVariables();
  }

  /**
   * Creates a new instance of AbstractStartupConfiguration.
   * 
   * @param parentHandler
   */
  public AbstractStartupConfiguration(AbstractStartupConfiguration parentHandler)
  {
    super();
    setupVariables();
    valid = true;
  }

  /** Initializes variables prior to parsing */
  public void setupVariables()
  {

  }

  /**
   * Adds a property to the startup configuration.
   * 
   * 
   * @param name
   * @param value
   */
  public void addProperty(String name, String value)
  {
    propertyTable.put(name, value);
  }

  /**
   * Retrieves a specific boolean property.
   * 
   * @param property
   *          The requested property
   * 
   * @return True if the property exists and is in ["true", "yes", "1"], false otherwise
   */
  public boolean getBooleanProperty(String property)
  {
    String value = getProperty(property);
    if (value != null)
    {
      return StringHelper.stringToBoolean(value);
    }
    return false;
  }

  /**
   * Retrieves a specific numeric property.
   * 
   * @param property
   *          The requested property
   * 
   * @return The numeric property value or the default value
   */
  public int getNumericProperty(String property, int defaultValue)
  {
    if (propertyTable.containsKey(property))
    {
      return StringHelper.stringToIntegerDef((String)propertyTable.get(property), defaultValue);
    }

    return defaultValue;
  }

  /**
   * Retrieves a specific property.
   * 
   * @param property
   *          The requested property
   * 
   * @return The property value or null
   */
  public String getProperty(String property)
  {
    if (propertyTable.containsKey(property))
    {
      return (String)propertyTable.get(property);
    }
    return null;
  }

  /**
   * Retrieves a specific property.
   * 
   * @param property
   *          The requested property
   * @param defaultValue
   *          A default value
   * 
   * @return The property value or the default value
   */
  public String getProperty(String property, String defaultValue)
  {
    if (propertyTable.containsKey(property))
    {
      return (String)propertyTable.get(property);
    }

    return defaultValue;
  }

  /**
   * Checks if a property exists.
   * 
   * @param property
   *          The requested property
   * 
   * @return True if the property exists, false otherwise
   */
  public boolean hasProperty(String property)
  {
    return propertyTable.containsKey(property);
  }

  /**
   * Checks if this description could be parsed.
   * 
   * @return The valid.
   */
  public boolean isValid()
  {
    return valid;
  }

}
