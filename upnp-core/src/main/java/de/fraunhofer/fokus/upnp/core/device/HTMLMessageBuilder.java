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
package de.fraunhofer.fokus.upnp.core.device;

import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.AbstractDevice;
import de.fraunhofer.fokus.upnp.core.control_point.CPDevice;
import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.KeyValueVector;

/**
 * This class provides static methods to generate HTML code.
 * 
 * @author Alexander Koenig
 * 
 */
public class HTMLMessageBuilder
{

  private static int         rowNumber;

  private static int         PROPERTY_VALUE_X_POS = 250;

  public static final String CONFIGURATON         = "Konfiguration";

  public static final String CONFIGURATON_PAGE    = "config.html";

  public static final String CONTENT              = "Inhalt";

  public static final String CONTENT_PAGE         = "content.html";

  public static final String INFO                 = "Information";

  public static final String INFO_PAGE            = "info.html";

  /** Creates the header of an HTML page */
  public static String buildHeader(AbstractDevice device, String subtype)
  {
    return HTTPConstant.XML_UTF_8 + CommonConstants.NEW_LINE +

    HTTPConstant.DOCTYPE_4_01 + CommonConstants.NEW_LINE +

    "<html><head><title>" + device.getFriendlyName() + " - " + subtype + "</title>" + CommonConstants.NEW_LINE +

    HTTPConstant.META_CONTENT_TYPE_UTF_8 + CommonConstants.NEW_LINE +

    HTTPConstant.META_CONTENT_STYLE_TYPE + CommonConstants.NEW_LINE +

    HTTPConstant.HEADER_INCLUDE_STYLESHEET + CommonConstants.NEW_LINE +

    "</head>" + CommonConstants.NEW_LINE;
  }

  /** Builds the body for AJAX enabled pages */
  public static String buildSetResourceURLBodyTag(Vector urlList)
  {
    String urls = "";
    for (int i = 0; i < urlList.size(); i++)
    {
      urls += (i > 0 ? "," : "") + "'" + urlList.elementAt(i) + "'";
    }

    return "<body onLoad=\"" + "setResourceURLs(new Array(" + urls + "));\">";

  }

  /** Creates the header of an HTML page that uses java script. */
  public static String buildJavascriptHeader(AbstractDevice device, String subtype, String optionalScriptFile)
  {
    return HTTPConstant.XML_UTF_8 + CommonConstants.NEW_LINE +

    HTTPConstant.DOCTYPE_4_01 + CommonConstants.NEW_LINE +

    "<html><head><title>" + device.getFriendlyName() + " - " + subtype + "</title>" + CommonConstants.NEW_LINE +

    HTTPConstant.META_CONTENT_TYPE_UTF_8 + CommonConstants.NEW_LINE +

    HTTPConstant.META_CONTENT_STYLE_TYPE + CommonConstants.NEW_LINE +

    HTTPConstant.HEADER_INCLUDE_STYLESHEET + CommonConstants.NEW_LINE +

    HTTPConstant.HEADER_INCLUDE_JAVA_SCRIPT + CommonConstants.NEW_LINE +

    (optionalScriptFile == null ? "" :

    "<script src=\"" + optionalScriptFile + "\" type=\"text/javascript\"></script>") +

    "</head>" + CommonConstants.NEW_LINE;
  }

  /** Creates the header of an HTML page that reloads regularly. */
  public static String buildRefreshHeader(AbstractDevice device, String subtype, int refreshTimeout, String refreshURL)
  {
    return HTTPConstant.XML_UTF_8 + CommonConstants.NEW_LINE + HTTPConstant.DOCTYPE_4_01 + CommonConstants.NEW_LINE +
      "<html><head><title>" + device.getFriendlyName() + " - " + subtype + "</title>" +
      HTTPConstant.META_CONTENT_TYPE_UTF_8 + HTTPConstant.META_CONTENT_STYLE_TYPE +
      "<meta http-equiv=\"refresh\" content=\"" + refreshTimeout + ";URL=" + refreshURL + "\">" +
      HTTPConstant.HEADER_INCLUDE_STYLESHEET + "</head>" + CommonConstants.NEW_LINE;
  }

  /** Creates an incomplete body with links. */
  public static String buildIncompleteEmptyBody(AbstractDevice device, String subtype, KeyValueVector linkList)
  {
    return buildIncompleteEmptyBody(device, "<body>", subtype, linkList);
  }

  /** Creates an incomplete body with links. */
  public static String buildIncompleteEmptyBody(AbstractDevice device,
    String bodyTag,
    String subtype,
    KeyValueVector linkList)
  {
    String result =
      bodyTag + "<div class=\"title\">" + device.getFriendlyName() + " - " + subtype + "</div>" +
        CommonConstants.NEW_LINE + "<div class=\"copyright\"><p>(C) 2008</p><p>Fraunhofer FOKUS</p></div>" +
        CommonConstants.NEW_LINE;

    result += "<div class=\"top_container\" ></div>" + CommonConstants.NEW_LINE;
    result += "<div class=\"link_container\">" + CommonConstants.NEW_LINE;
    for (int i = 0; i < linkList.size(); i++)
    {
      String currentKey = (String)linkList.getKey(i);
      String currentLink = (String)linkList.getValue(i);
      if (currentKey.equals(subtype))
      {
        result +=
          "  <div class=\"active_link\"><a class=\"active\" href=\"" + currentLink + "\">" + currentKey + "</a></div>" +
            CommonConstants.NEW_LINE;
      } else
      {
        result +=
          "  <div class=\"link\"><a href=\"" + currentLink + "\">" + currentKey + "</a></div>" +
            CommonConstants.NEW_LINE;
      }
    }
    result += "</div>" + CommonConstants.NEW_LINE;

    return result;
  }

  /** Resets the row number. */
  public static void resetRowNumber()
  {
    rowNumber = 0;
  }

  /** Builds a link where the link text is equal to the link. */
  public static String buildLink(String link)
  {
    return "<a href=\"" + link + "\">" + link + "</a>";
  }

  /** Builds a link with a custom link text. */
  public static String buildLink(String linkTitle, String link)
  {
    return "<a href=\"" + link + "\">" + linkTitle + "</a>";
  }

  /** Builds the list with all service URLs. */
  public static String buildServiceLinks(AbstractDevice device, String serverAddress)
  {
    String result = CommonConstants.NEW_LINE;
    for (int i = 0; i < device.getAbstractServiceTable().length; i++)
    {
      if (device.isCPDevice())
      {
        result +=
          buildLink(((CPDevice)device).getCPServiceTable()[i].getSCPDURL().toString()) + "<br>" +
            CommonConstants.NEW_LINE;
      }
      // build service list for internal devices
      if (device.isInternalDevice())
      {
        result +=
          buildLink(((Device)device).getDeviceServiceTable()[i].getSCPDURL(serverAddress)) + "<br>" +
            CommonConstants.NEW_LINE;
      }
    }
    return result;
  }

  /** Builds a value for display. */
  public static String buildValue(String value)
  {
    rowNumber++;
    return "<div style=\"position: absolute; top: " + rowNumber * 20 + "px; left: 10px;\">" + value + "</div>" +
      CommonConstants.NEW_LINE;
  }

  /** Builds a property/value pair for display, using the current row. */
  public static String buildRelativeProperty(String propertyName, String value)
  {
    rowNumber++;
    return "<div style=\"position: absolute; top: " + rowNumber * 20 + "px; left: 10px;\">" + propertyName + "</div>" +
      "<div style=\"position: absolute; top: " + rowNumber * 20 + "px; left: " + PROPERTY_VALUE_X_POS + "px;\">" +
      value + "</div>" + CommonConstants.NEW_LINE;
  }

  /** Builds a property/value pair for display, using the current row. */
  public static String buildRelativeProperty(String propertyName, String propertyID, String value)
  {
    rowNumber++;
    return "<div style=\"position: absolute; top: " + rowNumber * 20 + "px; left: 10px;\">" + propertyName + "</div>" +
      "<div id=\"" + propertyID + "\" style=\"position: absolute; top: " + rowNumber * 20 + "px; left: " +
      PROPERTY_VALUE_X_POS + "px;\">" + value + "</div>" + CommonConstants.NEW_LINE;
  }

  /**
   * Builds a property/value pair for display.
   * 
   * 
   * @param property
   *          Property name
   * @param propertyValueXPos
   *          Position of property value
   * @param value
   *          Property value
   * 
   * @return
   */
  public static String buildProperty(String property, int propertyValueXPos, String value)
  {
    rowNumber++;
    return "<div style=\"position: absolute; top: " + rowNumber * 20 + "px; left: 10px; \">" + property + "</div>" +
      "<div style=\"position: absolute; top: " + rowNumber * 20 + "px; left: " + propertyValueXPos + "px; \">" + value +
      "</div>" + CommonConstants.NEW_LINE;
  }

  /** Builds a property/value pair that needs multiple rows. */
  public static String buildProperty(String property, String value, int rowOffset)
  {
    int propertyYPos = (rowNumber + 1) * 20;
    rowNumber += rowOffset;
    return "<div style=\"position: absolute; top: " + propertyYPos + "px; left: 10px;\">" + property + "</div>" +
      "<div style=\"position: absolute; top: " + propertyYPos + "px; left: " + PROPERTY_VALUE_X_POS + "px;\">" + value +
      "</div>" + CommonConstants.NEW_LINE;
  }

  /** Creates a basic HTML page */
  public static String buildContentPage(AbstractDevice device, KeyValueVector linkList)
  {
    return buildHeader(device, CONTENT) + buildIncompleteEmptyBody(device, CONTENT, linkList) +
      "<div class=\"content\">" + "Dieses Gerät bietet keine Funktionalität über den Browser an." +
      "</div></body></html>";
  }

  /** Creates an info HTML page */
  public static String buildInfoPage(AbstractDevice device, String serverAddress, KeyValueVector linkList)
  {
    rowNumber = 0;
    return buildHeader(device, INFO) + buildIncompleteEmptyBody(device, INFO, linkList) + "<div class=\"content\">" +
      CommonConstants.NEW_LINE + buildInfoBody(device, serverAddress) + "</div></body></html>";
  }

  /** Creates a table with common device infos. */
  public static String buildInfoBody(AbstractDevice device, String serverAddress)
  {
    String result = buildRelativeProperty("IP-Adresse", serverAddress);
    result += buildRelativeProperty("Gerätetyp", device.getDeviceType());
    result += buildRelativeProperty("UUID", device.getUDN());
    result += buildRelativeProperty("Gültigkeit", device.getMaxage() / 60 + " Minuten");

    result += buildValue("");

    if (device.isInternalDevice())
    {
      result +=
        buildRelativeProperty("Gerätebeschreibung", buildLink(((Device)device).getDeviceDescriptionURL(serverAddress)));
    }
    if (device.isCPDevice())
    {
      result +=
        buildRelativeProperty("Gerätebeschreibung", buildLink(((CPDevice)device).getDeviceDescriptionURL().toString()));
    }
    result +=
      buildProperty("Dienstbeschreibungen",
        buildServiceLinks(device, serverAddress),
        device.getAbstractServiceTable().length);

    result += buildValue("");

    result += buildRelativeProperty("Hersteller", device.getManufacturer());

    if (device.getManufacturerURL() != null)
    {
      result += buildRelativeProperty("Herstellerwebseite", device.getManufacturerURL().toString());
    }

    result += buildValue("");

    result += buildRelativeProperty("Modellname", device.getModelName());

    if (device.getModelDescription() != null)
    {
      result += buildRelativeProperty("Modellbeschreibung", device.getModelDescription());
    }

    if (device.getModelNumber() != null)
    {
      result += buildRelativeProperty("Modellnummer", device.getModelNumber());
    }

    if (device.getModelURL() != null)
    {
      result += buildRelativeProperty("Modellwebseite", device.getModelURL().toString());
    }

    if (device.getSerialNumber() != null)
    {
      result += buildRelativeProperty("Seriennummer", device.getSerialNumber());
    }

    if (device.getUPC() != null)
    {
      result += buildRelativeProperty("UniversalProductCode", device.getUPC());
    }

    return result;
  }

  /** Creates a config HTML page. */
  public static String buildConfigPage(AbstractDevice device, KeyValueVector linkList)
  {
    return buildHeader(device, "Konfiguration") + buildIncompleteEmptyBody(device, "Konfiguration", linkList) +
      "<div class=\"content\">" + "Dieses Gerät kann nicht konfiguriert werden." + "</div>" + CommonConstants.NEW_LINE +
      "</body></html>";
  }

}
