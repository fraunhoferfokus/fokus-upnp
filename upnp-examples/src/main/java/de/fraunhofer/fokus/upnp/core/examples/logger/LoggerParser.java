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
package de.fraunhofer.fokus.upnp.core.examples.logger;

import java.util.Date;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.fraunhofer.fokus.upnp.util.DateTimeHelper;
import de.fraunhofer.fokus.upnp.util.SAXTemplateHandler;

/**
 * This class is used to parse saved data from a logging task.
 * 
 * @author Alexander Koenig
 * 
 */
public class LoggerParser extends SAXTemplateHandler
{
  private String description;

  private String deviceUDN;

  private String deviceFriendlyName;

  private String serviceType;

  private String shortenedServiceID;

  private String actionName;

  private Date   startTime;

  private Date   endTime;

  private long   interval;

  private Vector logEntryVector;

  private Date   date;

  private String value;

  /**
   * Creates a new instance of LoggerParser.
   * 
   * @param parser
   */
  public LoggerParser()
  {
    description = null;
    deviceUDN = null;
    deviceFriendlyName = null;
    serviceType = null;
    shortenedServiceID = null;
    actionName = null;
    startTime = null;
    endTime = null;
    interval = -1;
    logEntryVector = new Vector();
  }

  /**
   * Retrieves the logEntryVector.
   * 
   * @return The logEntryVector
   */
  public Vector getLogEntryVector()
  {
    return logEntryVector;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.util.SAXTemplateHandler#processStartElement(java.lang.String,
   *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  public void processStartElement(String uri, String name, String qName, Attributes atts) throws SAXException
  {
    if (getTagCount() == 3 && getCurrentTag().equalsIgnoreCase("entry"))
    {
      date = null;
      value = null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.xml.SAXTemplateHandler#processContentElement(java.lang.String)
   */
  public void processContentElement(String content)
  {
    if (getTagCount() == 2 && getCurrentTag().equalsIgnoreCase("description"))
    {
      description = content;
    }
    if (getTagCount() == 2 && getCurrentTag().equalsIgnoreCase("deviceUDN"))
    {
      deviceUDN = content;
    }
    if (getTagCount() == 2 && getCurrentTag().equalsIgnoreCase("deviceFriendlyName"))
    {
      deviceFriendlyName = content;
    }
    if (getTagCount() == 2 && getCurrentTag().equalsIgnoreCase("serviceType"))
    {
      serviceType = content;
    }
    if (getTagCount() == 2 && getCurrentTag().equalsIgnoreCase("shortenedServiceID"))
    {
      shortenedServiceID = content;
    }
    if (getTagCount() == 2 && getCurrentTag().equalsIgnoreCase("actionName"))
    {
      actionName = content;
    }
    if (getTagCount() == 2 && getCurrentTag().equalsIgnoreCase("startTime"))
    {
      startTime = DateTimeHelper.getDateFromUPnP(content);
    }
    if (getTagCount() == 2 && getCurrentTag().equalsIgnoreCase("endTime"))
    {
      endTime = DateTimeHelper.getDateFromUPnP(content);
    }
    if (getTagCount() == 2 && getCurrentTag().equalsIgnoreCase("interval"))
    {
      interval = Long.parseLong(content.trim());
    }
    if (getTagCount() == 4 && getCurrentTag().equalsIgnoreCase("date"))
    {
      date = DateTimeHelper.getDateFromUPnP(content);
    }
    if (getTagCount() == 4 && getCurrentTag().equalsIgnoreCase("value"))
    {
      value = content;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.util.SAXTemplateHandler#processEndElement(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public void processEndElement(String uri, String localName, String qName) throws SAXException
  {
    if (getTagCount() == 3 && getCurrentTag().equalsIgnoreCase("entry") && date != null && value != null)
    {
      LogEntry entry = new LogEntry(value, date);
      logEntryVector.add(entry);
    }
  }

  /**
   * Retrieves the actionName.
   * 
   * @return The actionName
   */
  public String getActionName()
  {
    return actionName;
  }

  /**
   * Retrieves the description.
   * 
   * @return The description
   */
  public String getDescription()
  {
    return description;
  }

  /**
   * Retrieves the deviceFriendlyName.
   * 
   * @return The deviceFriendlyName
   */
  public String getDeviceFriendlyName()
  {
    return deviceFriendlyName;
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

  /**
   * Retrieves the endTime.
   * 
   * @return The endTime
   */
  public Date getEndTime()
  {
    return endTime;
  }

  /**
   * Retrieves the interval.
   * 
   * @return The interval
   */
  public long getInterval()
  {
    return interval;
  }

  /**
   * Retrieves the serviceType.
   * 
   * @return The serviceType
   */
  public String getServiceType()
  {
    return serviceType;
  }

  /**
   * Retrieves the shortedServiceName.
   * 
   * @return The shortedServiceName
   */
  public String getShortenedServiceID()
  {
    return shortenedServiceID;
  }

  /**
   * Retrieves the startTime.
   * 
   * @return The startTime
   */
  public Date getStartTime()
  {
    return startTime;
  }

}
