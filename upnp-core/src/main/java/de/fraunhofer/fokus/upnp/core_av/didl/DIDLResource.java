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
package de.fraunhofer.fokus.upnp.core_av.didl;

import java.util.Vector;

/**
 * This class describes a DIDL resource with its arguments.
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class DIDLResource
{

  private String protocolInfo;

  private String size;

  private String duration;

  private String value;

  private String importURI;

  public DIDLResource()
  {
  }

  public DIDLResource(String protocolInfo, String value)
  {
    this.protocolInfo = protocolInfo;
    this.value = value;
  }

  public DIDLResource(DIDLResource sourceResource)
  {
    this.protocolInfo = sourceResource.getProtocolInfo();
    this.size = sourceResource.getSize();
    this.duration = sourceResource.getDuration();
    this.value = sourceResource.getValue();
  }

  public void setProtocolInfo(String protInfo)
  {
    this.protocolInfo = protInfo;
  }

  public void setSize(String size)
  {
    this.size = size;
  }

  public void setDuration(String duration)
  {
    this.duration = duration;
  }

  /** Sets the URL for this resource. Must be properly escaped. */
  public void setValue(String value)
  {
    this.value = value;
    // remove leading '/'
    if (this.value != null && this.value.startsWith("/"))
    {
      this.value = this.value.substring(1);
    }
  }

  /** Sets the import URL for this resource. Must be properly escaped. */
  public void setImportURI(String value)
  {
    this.importURI = value;
    // remove leading '/'
    if (importURI != null && importURI.startsWith("/"))
    {
      importURI = importURI.substring(1);
    }
  }

  /** Retrieves the protocol of this resource (e.g. http-get:*:image/jpg:* ) */
  public String getProtocolInfo()
  {
    return protocolInfo;
  }

  /** Retrieves the protocol of this resource (e.g. http-get) */
  public String getProtocol()
  {
    if (protocolInfo == null || protocolInfo.length() == 0)
    {
      return null;
    }

    if (protocolInfo.indexOf(':') != -1)
    {
      return protocolInfo.substring(0, protocolInfo.indexOf(':'));
    }

    return null;
  }

  /** Retrieves the protocol extension of this resource (e.g. jpg) */
  public String getProtocolExtension()
  {
    if (protocolInfo == null || protocolInfo.length() == 0)
    {
      return null;
    }

    try
    {
      // remove last *
      String resourceExtension = protocolInfo.substring(0, protocolInfo.lastIndexOf(":"));
      // remove leading part
      resourceExtension = resourceExtension.substring(resourceExtension.lastIndexOf(":") + 1);
      // remove type
      return resourceExtension.substring(resourceExtension.indexOf("/") + 1).toLowerCase();
    } catch (Exception e)
    {
    }

    return null;
  }

  public String getSize()
  {
    return size;
  }

  public String getDuration()
  {
    return duration;
  }

  /**
   * Retrieves the URI of this resource (e.g. http://www.anywhere.de/anything.jpg)
   */
  public String getValue()
  {
    return value;
  }

  public String getImportURI()
  {
    return importURI;
  }

  /** Retrieves the resource type for the resource, e.g., audio or video. */
  public int getMediaType()
  {
    // extract image from "http-get:*:image/jpg:*"
    try
    {
      // remove last *
      String resourceType = protocolInfo.substring(0, protocolInfo.lastIndexOf(":"));
      // remove leading part
      resourceType = resourceType.substring(resourceType.lastIndexOf(":") + 1);
      // remove subtype
      resourceType = resourceType.substring(0, resourceType.indexOf("/")).toUpperCase();
      if (resourceType.equals("IMAGE"))
      {
        return DIDLConstants.RES_TYPE_IMAGE;
      }
      if (resourceType.equals("AUDIO"))
      {
        return DIDLConstants.RES_TYPE_AUDIO;
      }
      if (resourceType.equals("VIDEO"))
      {
        return DIDLConstants.RES_TYPE_VIDEO;
      }
    } catch (Exception e)
    {
    }

    return DIDLConstants.RES_TYPE_UNKNOWN;
  }

  /**
   * Retrieves the XML fragment for this resource
   * 
   * @param showAll
   *          Flag to include all attributes in the description
   * @param filterList
   *          The properties that should be included in the description
   * @param absoluteServerPath
   *          The path that should be used for relative URLs (e.g. http://192.168.1.5:80/resources/)
   * 
   * @return The XML fragment for this resource
   * 
   */
  public String toXMLDescription(boolean showAll, Vector filterList, String absoluteServerPath)
  {
    // check for relative ImportURI
    String absoluteImportURI = importURI;
    if (importURI != null && importURI.length() > 0 && !importURI.toUpperCase().startsWith("HTTP://") &&
      absoluteServerPath != null)
    {
      absoluteImportURI = absoluteServerPath + importURI;
    }
    // check for relative URL
    String absoluteResValue = value;
    if (value != null && value.length() > 0 && !value.toUpperCase().startsWith("HTTP://") && absoluteServerPath != null)
    {
      absoluteResValue = absoluteServerPath + value;
    }
    String result =
      "<res protocolInfo=\"" + protocolInfo + "\" " +
        (importURI != null ? "importUri=\"" + absoluteImportURI + "\" " : "") +
        (size != null ? "size=\"" + size + "\" " : "") + (duration != null ? "duration=\"" + duration + "\" " : "") +
        ">";
    // do not add null values
    result += absoluteResValue != null ? absoluteResValue : "";
    result += "</res>";

    return result;
  }

  /**
   * This method parses resource attributes.
   * 
   * @param attribute
   *          The attribute
   * @param value
   *          The attribute value
   * 
   * @return True if the attribute was handled, false otherwise
   * 
   */
  public boolean handleAttribute(String attribute, String value)
  {
    boolean handled = false;

    if (attribute.equals(DIDLConstants.RES_PROTOCOL_INFO))
    {
      setProtocolInfo(value);
    }
    if (attribute.equals(DIDLConstants.RES_IMPORT_URI))
    {
      setImportURI(value);
    }
    if (attribute.equals(DIDLConstants.RES_SIZE))
    {
      setSize(value);
    }
    if (attribute.equals(DIDLConstants.RES_DURATION))
    {
      setDuration(value);
    }

    return handled;
  }

}
