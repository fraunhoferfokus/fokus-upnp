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

import de.fraunhofer.fokus.upnp.util.DateTimeHelper;

/**
 * This class is used to store one log entry.
 * 
 * @author Alexander Koenig
 * 
 */
public class LogEntry
{

  private String value;

  private Date   date;

  /**
   * Creates a new instance of LogEntry.
   * 
   * @param value
   *          The value that should be stored
   * @param date
   *          The date when the value has been recorded
   */
  public LogEntry(String value, Date date)
  {
    this.value = value;
    this.date = date;
  }

  /** Retrieves the XML description for this log entry. */
  public String toXMLDescription()
  {
    return "<entry>" + "<date>" + DateTimeHelper.formatDateForUPnP(date) + "</date>" + "<value>" + value + "</value>" +
      "</entry>\n";
  }

  /**
   * Retrieves the date.
   * 
   * @return The date
   */
  public Date getDate()
  {
    return date;
  }

  /**
   * Retrieves the value.
   * 
   * @return The value
   */
  public String getValue()
  {
    return value;
  }

}
