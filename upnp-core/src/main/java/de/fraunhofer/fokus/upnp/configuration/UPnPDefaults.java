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
package de.fraunhofer.fokus.upnp.configuration;

/**
 * This class contains constants that change the behaviour of UPnP devices and control points.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class UPnPDefaults
{

  /** Maximum number of parallel device cache retrieval threads */
  public static final int     CP_DEVICE_CACHE_RETRIEVAL_THREAD_COUNT    = 5;

  /** Minimum timeout for devices that publish a lower max age in seconds */
  public static final int     CP_DEVICE_MIN_ACCEPTED_MAX_AGE            = 600;

  /** Maximum number of parallel device retrieval threads */
  public static final int     CP_DEVICE_RETRIEVAL_THREAD_COUNT          = 5;

  /** Flag to disable the device cache */
  public static boolean       CP_DISABLE_DEVICE_CACHE                   = false;

  /** Flag to disable metadata retrieval like attributes and translations */
  public static boolean       CP_DISABLE_METADATA_RETRIEVAL             = false;

  /** Flag to discover devices but without retrieving the descriptions */
  public static boolean       CP_DISCOVERY_ONLY                         = false;

  /** Flag to disable event processing */
  public static boolean       CP_DISABLE_EVENT_PROCESSING               = false;

  /** Timeout for invalid devices until a new discovery is possible (5 min) */
  public static final long    CP_INVALID_DEVICE_DISCOVERY_TIMEOUT       = 300;

  /** Time to sleep between two sent M_SEARCH messages */
  public static final int     CP_M_SEARCH_MESSAGE_DELAY                 = 5;     // 15

  /** Time to wait for M-SEARCH response messages */
  public static final long    CP_M_SEARCH_TIMEOUT                       = 120000;

  /** Time interval in which M-SEARCH requests should be answered */
  public static final int     CP_MX_VALUE                               = 10;

  /** Time to wait for a valid subscription ID before the initial event message is discarded */
  public static final int     CP_SUBSCRIBER_TEST_SLEEP_TIME             = 500;

  /** Requested subscription timeout by control points in seconds */
  public static final int     CP_SUBSCRIPTION_TIMEOUT                   = 2400;  // was 2400

  /** Add content hash for each service to device description */
  public static final boolean DEVICE_ADD_SERVICE_DESCRIPTION_HASH       = true;

  /**
   * Number of retries for consecutive event notify messages
   */
  public static final int     DEVICE_EVENT_NOTIFY_RETRIES               = 1;

  /** Time to wait before the first NOTIFY:alive is sent after device startup */
  public static long          DEVICE_INITIAL_NOTIFY_DELAY               = 500;

  /**
   * how many times the init subscription noty messages is send if the connection to the control
   * point can not be created the first time
   */
  public static final int     DEVICE_INITIAL_NOTIFY_RETRIES             = 3;

  /** True to increase the event collection interval with the number of subscribers */
  public static final boolean DEVICE_ADAPTIVE_EVENT_COLLECTION_INTERVAL = true;

  /** True to increase the event collection interval with the number of subscribers */
  public static final boolean DEVICE_FAULT_TOLERANT_EVENT_COLLECTION    = true;

  /** Time to sleep between two sent M_SEARCH response messages */
  public static final int     DEVICE_M_SEARCH_RESPONSE_MESSAGE_DELAY    = 0;     // 15

  /** Timeout for automatic device removal in seconds */
  public static final int     DEVICE_MAX_AGE                            = 1800;  // was 1800

  /** Time to sleep between two sent NOTIFY messages */
  public static final int     DEVICE_NOTIFY_MESSAGE_DELAY               = 0;     // 15

  /** Returned subscription timeout by device in seconds */
  public static final int     DEVICE_SUBSCRIPTION_TIMEOUT               = 1800;  // was 1800

  /** Flag to use relative URLs in device descriptions. */
  public static boolean       DEVICE_USE_RELATIVE_URLS                  = true;

  /** Number of times to transmit SSDP messages (2) */
  public static final int     UDP_SEND_COUNT                            = 2;     // 3

}
