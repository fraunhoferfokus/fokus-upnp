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
package de.fraunhofer.fokus.upnp.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * This class provides access to the current date and time.
 * 
 * @author tje, Alexander Koenig
 */
public class DateTimeHelper
{
  private static SimpleDateFormat rfc1123        = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.US);

  private static SimpleDateFormat upnpDate       = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

  private static SimpleDateFormat fullDate       = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);

  private static SimpleDateFormat germanDate     = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);

  private static SimpleDateFormat germanDateTime = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.GERMANY);

  private static SimpleDateFormat germanTime     = new SimpleDateFormat("HH:mm:ss", Locale.GERMANY);

  private static SimpleDateFormat debugDate      = new SimpleDateFormat("HH:mm:ss.SSS", Locale.GERMANY);

  /**
   * Creates a date time value from a string.
   * 
   */
  public synchronized static Date getTimeFromGermanLocale(String timeString)
  {
    try
    {
      return germanTime.parse(timeString);
    } catch (Exception e)
    {
      // System.out.println("Error parsing date: " + e.getMessage());
    }
    return null;
  }

  /**
   * Creates a date time value from a string appropriate for the German locale (e.g., 27.10.1978 12:00:00).
   * 
   */
  public synchronized static Date getDateTimeFromGermanLocale(String dateString)
  {
    try
    {
      return germanDateTime.parse(dateString);
    } catch (Exception e)
    {
      // System.out.println("Error parsing date: " + e.getMessage());
    }
    return null;
  }

  /**
   * Converts a Date object into a Calendar object.
   * 
   */
  public synchronized static Calendar dateToCalendar(Date date)
  {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    return calendar;
  }

  /**
   * Creates a date time value from a string appropriate for UPnP (e.g., 2006-10-27 12:00:00).
   * 
   */
  public synchronized static Date getDateFromUPnP(String dateString)
  {
    try
    {
      return upnpDate.parse(dateString);
    } catch (Exception e)
    {
      // System.out.println("Error parsing date: " + e.getMessage());
    }
    return null;
  }

  /**
   * Converts a UPnP date string in a date string suitable for the German locale.
   * 
   */
  public synchronized static String upnpDateStringToGermanLocale(String dateString)
  {
    try
    {
      return formatDateTimeForGermany(upnpDate.parse(dateString));
    } catch (Exception e)
    {
      System.out.println("Error parsing date: " + e.getMessage());
    }
    return null;
  }

  /**
   * Returns the current date time.
   * 
   */
  public synchronized static Date getDate()
  {
    return Calendar.getInstance().getTime();
  }

  /**
   * Retrieves the current date suitable for RFC 1123
   * 
   * @return the current date suitable for the RFC 1123 standard as a string (e.g., Sun, 06 Nov 1994 08:49:37 CEST)
   */
  public synchronized static String getRFC1123Date()
  {
    return rfc1123.format(Calendar.getInstance().getTime());
  }

  /**
   * Retrieves the current date suitable for UPnP
   * 
   * @return the current date suitable for UPnP as a string (e.g., 2004-11-23 11:43:22)
   */
  public synchronized static String getUPnPDate()
  {
    return upnpDate.format(Calendar.getInstance().getTime());
  }

  /**
   * Formats a date suitable for RFC 1123.
   * 
   * @return The given date suitable for the RFC 1123 standard (e.g., Sun, 06 Nov 1994 08:49:37 CEST)
   */
  public synchronized static String formatDateForRFC1123(Date date)
  {
    return rfc1123.format(date);
  }

  /**
   * Formats a date suitable for UPnP.
   * 
   * @return the given date suitable for UPnP as a string (e.g., 2004-11-23 11:43:22)
   */
  public synchronized static String formatDateForUPnP(Date date)
  {
    return upnpDate.format(date);
  }

  /**
   * Formats a date holding all provided info.
   * 
   * @return the given date suitable for UPnP as a string (e.g., 2004-11-23 11:43:22)
   */
  public synchronized static String formatDateForCompleteInfo(Date date)
  {
    return fullDate.format(date);
  }

  /**
   * Formats a date suitable for the german locale.
   * 
   * @return The given date suitable for Germany (e.g., 12.4.2006 11:43:22)
   */
  public synchronized static String formatDateTimeForGermany(Date date)
  {
    return germanDateTime.format(date);
  }

  /**
   * Formats a date suitable for the german locale.
   * 
   * @return The given date suitable for Germany (e.g., 12.4.2006)
   */
  public synchronized static String formatDateForGermany(Date date)
  {
    return germanDate.format(date);
  }

  /**
   * Formats a time suitable for the german locale.
   * 
   * @return The given time suitable for Germany (e.g., 11:43:22)
   */
  public synchronized static String formatTimeForGermany(Date date)
  {
    return germanTime.format(date);
  }

  /**
   * Formats the current date and time for debugging purposes.
   * 
   * @return The current date suitable for debug (e.g., 11:43:22.454)
   */
  public synchronized static String formatCurrentDateForDebug()
  {
    return debugDate.format(Calendar.getInstance().getTime());
  }

  /** Converts seconds into h:mm:ss. */
  public static String secondsToTimeString(long seconds)
  {
    String result = "";
    if (seconds >= 3600)
    {
      result += seconds / 3600 + ":";
      seconds = seconds % 3600;
    }
    if (seconds >= 60)
    {
      long temp = seconds / 60;
      result += (temp < 10 ? "0" : "") + temp + ":";
      seconds = seconds % 60;
    }
    result += (seconds < 10 ? "0" : "") + seconds;
    return result;
  }

}
