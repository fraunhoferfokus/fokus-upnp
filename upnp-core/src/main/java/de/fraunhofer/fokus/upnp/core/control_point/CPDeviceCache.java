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
package de.fraunhofer.fokus.upnp.core.control_point;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.xml.sax.SAXException;

import de.fraunhofer.fokus.upnp.configuration.UPnPDefaults;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.templates.TemplateControlPoint;
import de.fraunhofer.fokus.upnp.http.HTTPClient;
import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.http.HTTPMessageBuilder;
import de.fraunhofer.fokus.upnp.http.HTTPParser;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.DateTimeHelper;
import de.fraunhofer.fokus.upnp.util.FileHelper;
import de.fraunhofer.fokus.upnp.util.SAXTemplateHandler;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.threads.IEventListener;

/**
 * This class is used to access cached device descriptions. This is used to accelerate the UPnP
 * discovery process.
 * 
 * 
 * @author Alexander Koenig
 * 
 */
public class CPDeviceCache implements IEventListener
{

  public static int     ACTIVE_THREADS                    = 0;

  public static boolean SERVICE_DESCRIPTION_SIZE_CHECKING = false;

  /** Timeout for offline devices */
  public static int     CP_DEVICE_CACHE_DEPRECATION       = 7;

  public static int     CACHED_DESCRIPTION_VALID          = 1;

  public static int     CACHED_DESCRIPTION_DEPRECATED     = 2;

  public static int     CACHED_DESCRIPTION_NOT_VERIFIED   = 3;

  /** Reference to associated control point */
  private ControlPoint  controlPoint;

  /** Directory used for cached information */
  private String        cacheDirectory;

  /** Vector with pending cache entries */
  private Vector        deviceCacheThreadList;

  /**
   * Creates a new instance of CPDeviceCache.
   * 
   */
  public CPDeviceCache(ControlPoint controlPoint, String cacheDirectory)
  {
    this.controlPoint = controlPoint;
    deviceCacheThreadList = new Vector();
    File directory = new File(cacheDirectory);
    // create directory if necessary
    if (!directory.exists())
    {
      directory.mkdir();
    }
    setCacheDirectory(cacheDirectory);
  }

  /**
   * Checks if a cached device or service description is still valid by comparing the size of the
   * cached file with the remote description and an optional ETAG value.
   * 
   * 
   */
  public static int getCachedDescriptionState(String fileName, URL descriptionURL, String etag)
  {
    // get file for description
    File descriptionFile = new File(fileName);

    // description file found
    if (descriptionFile.exists())
    {
      long fileLength = descriptionFile.length();

      // start HEAD request to description
      HTTPClient httpClient = new HTTPClient();
      HTTPMessageObject request =
        new HTTPMessageObject(HTTPMessageBuilder.createHEADRequest(descriptionURL.getPath(),
          descriptionURL.getHost(),
          descriptionURL.getPort(),
          ""), descriptionURL);

      // System.out.println("Send HEAD request to " + descriptionURL);

      httpClient.sendRequestAndWaitForResponse(request);
      HTTPParser responseParser = new HTTPParser();
      responseParser.parse(httpClient.getResponse());
      // check if response content is sufficient for content length checking
      if (responseParser.isHTTPOKResponse() && responseParser.hasField(CommonConstants.CONTENT_LENGTH))
      {
        int responseLength = -1;
        // compare size in description with length of file
        try
        {
          responseLength = Integer.parseInt(responseParser.getValue(CommonConstants.CONTENT_LENGTH));
        } catch (Exception e)
        {
        }
        boolean isValid = responseLength == fileLength;
        // check for optional etag
        if (isValid && etag != null && responseParser.hasField(HTTPConstant.ETAG_HEADER))
        {
          String receivedETag = responseParser.getValue(HTTPConstant.ETAG_HEADER).replaceAll("\"", "");
          // System.out.println("Compare etag " + etag + " with " + receivedETag);
          isValid &= etag.equals(receivedETag);

          if (!isValid)
          {
            // TemplateControlPoint.printMessage("Found description with correct size but incorrect
            // hash at " +
            // descriptionURL);

            return CACHED_DESCRIPTION_DEPRECATED;
          }
        }
        if (isValid)
        {
          return CACHED_DESCRIPTION_VALID;
        }
      }
      // if response is null, the remote device is probably unavailable
      if (httpClient.getResponse() == null || httpClient.getResponse().getHeader() == null)
      {
        return CACHED_DESCRIPTION_NOT_VERIFIED;
      }

      // HEAD did not work, but response was not null
      // try GET request to description
      httpClient = new HTTPClient();
      request =
        new HTTPMessageObject(HTTPMessageBuilder.createGETRequest(descriptionURL.getPath(),
          descriptionURL.getHost(),
          descriptionURL.getPort(),
          ""), descriptionURL);

      httpClient.sendRequestAndWaitForResponse(request);
      responseParser = new HTTPParser();
      responseParser.parse(httpClient.getResponse());
      byte[] responseBody = httpClient.getResponse().getBody();
      if (responseParser.isHTTPOKResponse() && responseBody != null)
      {
        // remove trailing \r\n etc.
        int xmlEndIndex = responseBody.length - 1;
        while (xmlEndIndex > 0 && responseBody[xmlEndIndex] != '>')
        {
          xmlEndIndex--;
        }
        if (xmlEndIndex < responseBody.length - 1)
        {
          byte[] temp = new byte[xmlEndIndex + 1];
          System.arraycopy(responseBody, 0, temp, 0, temp.length);
          responseBody = temp;
        }
        // compare description size
        boolean isValid = responseBody.length == fileLength;
        // check for optional etag
        if (isValid && etag != null && responseParser.hasField(HTTPConstant.ETAG_HEADER))
        {
          String receivedETag = responseParser.getValue(HTTPConstant.ETAG_HEADER).replaceAll("\"", "");
          // System.out.println("Compare etag " + etag + " with " + receivedETag);

          isValid &= etag.equals(receivedETag);
          if (!isValid)
          {
            // System.out.println("Found description with correct size but incorrect hash at " +
            // descriptionURL + ", rerequest description");

            return CACHED_DESCRIPTION_DEPRECATED;
          }
        }
        if (isValid)
        {
          return CACHED_DESCRIPTION_VALID;
        }
      }
    }
    return CACHED_DESCRIPTION_NOT_VERIFIED;
  }

  /** Retrieves the directory used for caching the description of one device */
  public String getCacheDirectoryForDevice(CPDevice device)
  {
    String separator = System.getProperty("file.separator");

    String socketAddress =
      device.getDeviceDescriptionURL().getHost() + ":" + device.getDeviceDescriptionURL().getPort();
    String udn = device.getUDN();

    String result =
      cacheDirectory + StringHelper.escapeDirectoryName(device.getDeviceDescriptionURL().getHost()) + separator +
        StringHelper.escapeDirectoryName(socketAddress + "_" + udn);

    return FileHelper.toValidDirectoryName(result);
  }

  /**
   * Stores the complete device description (device and services) in the cache directory.
   * 
   * @param device
   *          The device that should be cached
   * 
   */
  public void storeCompleteDeviceDescription(CPDevice device)
  {
    setupDirectories(device);

    // do not store descriptions for high ports cause these seem to change with every
    // device startup
    if (device.getDeviceDescriptionSocketAddress().getPort() > 20000)
    {
      return;
    }

    String separator = System.getProperty("file.separator");

    String escapedDeviceDescriptionPath = StringHelper.escapeDirectoryName(device.getDeviceDescriptionURL().getPath());
    String escapedDirectoryName = getCacheDirectoryForDevice(device);

    // store SSDP info
    updateSSDPInfo(device);

    File directory = new File(escapedDirectoryName);

    // store device and service descriptions
    if (directory.exists())
    {
      // store device description
      String deviceDescriptionFileName = escapedDirectoryName + separator + escapedDeviceDescriptionPath;
      File deviceDescriptionFile = new File(deviceDescriptionFileName);
      // overwrite older versions of the device description
      try
      {
        FileOutputStream deviceDescriptionOutputStream = new FileOutputStream(deviceDescriptionFile);
        byte[] deviceDescriptionData = StringHelper.utf8StringToByteArray(device.getDeviceDescription());
        deviceDescriptionOutputStream.write(deviceDescriptionData);
        deviceDescriptionOutputStream.close();
      } catch (Exception e)
      {
        System.out.println("Error: " + e.getMessage());
      }
      // store service descriptions
      CPService[] services = device.getCPServiceTable();
      for (int i = 0; i < services.length; i++)
      {
        String escapedServiceDescriptionPath = StringHelper.escapeDirectoryName(services[i].getSCPDURL().getPath());

        String serviceDescriptionFileName = escapedDirectoryName + separator + escapedServiceDescriptionPath;
        File serviceDescriptionFile = new File(serviceDescriptionFileName);
        // overwrite older versions of the service description
        try
        {
          FileOutputStream serviceDescriptionOutputStream = new FileOutputStream(serviceDescriptionFile);
          byte[] serviceDescriptionData = StringHelper.utf8StringToByteArray(services[i].getServiceDescription());
          serviceDescriptionOutputStream.write(serviceDescriptionData);
          serviceDescriptionOutputStream.close();
        } catch (Exception e)
        {
        }
      }
    }
  }

  /**
   * Creates the directory structure for this device.
   * 
   * @param device
   *          The device that should be cached
   * 
   */
  private void setupDirectories(CPDevice device)
  {
    // do not store descriptions for high ports cause these seem to change with every
    // device startup
    if (device.getDeviceDescriptionSocketAddress().getPort() > 20000)
    {
      return;
    }

    // use subdirectory for each device IP address
    String escapedIPDirectoryName =
      cacheDirectory + StringHelper.escapeDirectoryName(device.getDeviceDescriptionURL().getHost());

    File ipDirectory = new File(escapedIPDirectoryName);
    // create directory if necessary
    if (!ipDirectory.exists())
    {
      ipDirectory.mkdir();
    }

    String escapedDirectoryName = getCacheDirectoryForDevice(device);

    File directory = new File(escapedDirectoryName);
    // create directory if necessary
    if (!directory.exists())
    {
      directory.mkdir();
    }
  }

  /**
   * Stores the complete device description (device and services) in the cache directory.
   * 
   * @param device
   *          The device that should be cached
   * 
   */
  public void updateSSDPInfo(CPDevice device)
  {
    setupDirectories(device);

    // do not store descriptions for high ports cause these seem to change with every
    // device startup
    if (device.getDeviceDescriptionSocketAddress().getPort() > 20000)
    {
      return;
    }

    String separator = System.getProperty("file.separator");
    String escapedDirectoryName = getCacheDirectoryForDevice(device);
    File directory = new File(escapedDirectoryName);
    // store SSDP info
    if (directory.exists())
    {
      // create XML file with SSDP infos
      String ssdpFileName = escapedDirectoryName + separator + "ssdpInfo.xml";
      File ssdpFile = new File(ssdpFileName);
      // overwrite older versions of ssdpInfo.xml
      try
      {
        FileOutputStream ssdpOutputStream = new FileOutputStream(ssdpFile);
        ssdpOutputStream.write(StringHelper.stringToByteArray("<?xml version=\"1.0\"?>"));
        ssdpOutputStream.write(StringHelper.stringToByteArray("<DeviceInfo>"));
        // store max age
        ssdpOutputStream.write(StringHelper.stringToByteArray("<Cache-Control>" + device.getMaxage() +
          "</Cache-Control>"));
        ssdpOutputStream.write(StringHelper.stringToByteArray("<DeviceDescriptionPath>" +
          device.getDeviceDescriptionURL().getPath() + "</DeviceDescriptionPath>"));
        ssdpOutputStream.write(StringHelper.stringToByteArray("<Server>" + device.getServer() + "</Server>"));
        ssdpOutputStream.write(StringHelper.stringToByteArray("<LastDiscovery>" +
          DateTimeHelper.formatDateForUPnP(DateTimeHelper.getDate()) + "</LastDiscovery>"));
        ssdpOutputStream.write(StringHelper.stringToByteArray("<ETag>" + device.getDescriptionHashBase64() + "</ETag>"));
        ssdpOutputStream.write(StringHelper.stringToByteArray("</DeviceInfo>"));
      } catch (Exception e)
      {
        System.out.println("Error: " + e.getMessage());
      }
    }
  }

  /**
   * Stores additional information found in an attribute service.
   * 
   * @param device
   *          Associated device
   * @param information
   *          Hashtable with additional information
   * 
   */
  public void storeAttributeServiceInformation(CPDevice device, Hashtable information)
  {
    // do not store descriptions for high ports cause these seem to change with every
    // device startup
    if (device.getDeviceDescriptionSocketAddress().getPort() > 20000)
    {
      return;
    }

    String separator = System.getProperty("file.separator");

    // use subdirectory for each device IP address
    String escapedIPDirectoryName =
      cacheDirectory + StringHelper.escapeDirectoryName(device.getDeviceDescriptionURL().getHost());

    File ipDirectory = new File(escapedIPDirectoryName);
    // create directory if necessary
    if (!ipDirectory.exists())
    {
      ipDirectory.mkdir();
    }

    String escapedDirectoryName = getCacheDirectoryForDevice(device);

    File directory = new File(escapedDirectoryName);
    // create directory if necessary
    if (!directory.exists())
    {
      directory.mkdir();
    }
    // store device and service descriptions
    if (directory.exists())
    {
      // create XML file
      String infoFileName = escapedDirectoryName + separator + "descriptionService.xml";
      File infoFile = new File(infoFileName);
      if (!infoFile.exists())
      {
        try
        {
          FileOutputStream infoOutputStream = new FileOutputStream(infoFile);
          infoOutputStream.write(StringHelper.stringToByteArray("<?xml version=\"1.0\"?>"));
          infoOutputStream.write(StringHelper.stringToByteArray("<description>"));
          Enumeration keys = information.keys();
          while (keys.hasMoreElements())
          {
            String currentKey = (String)keys.nextElement();
            String content = (String)information.get(currentKey);
            infoOutputStream.write(StringHelper.utf8StringToByteArray("<" + currentKey + ">" + content + "</" +
              currentKey + ">"));
          }
          infoOutputStream.write(StringHelper.stringToByteArray("</description>"));
        } catch (Exception e)
        {
          System.out.println("Error: " + e.getMessage());
        }
      }
    }
  }

  /** Reads all devices found in the device cache */
  public void readDeviceCache()
  {
    File directory = new File(cacheDirectory);
    File[] ipDirectories = directory.listFiles();
    if (ipDirectories != null)
    {
      synchronized(deviceCacheThreadList)
      {
        for (int i = 0; i < ipDirectories.length; i++)
        {
          File[] deviceDirectories = ipDirectories[i].listFiles();
          if (deviceDirectories != null)
          {
            for (int j = 0; j < deviceDirectories.length; j++)
            {
              // create retrieval thread for each cached device
              deviceCacheThreadList.add(new CPDeviceCacheThread(deviceDirectories[j]));
            }
          }
        }
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.threads.IEventListener#triggerEvents()
   */
  public void triggerEvents()
  {
    if (ACTIVE_THREADS >= UPnPDefaults.CP_DEVICE_CACHE_RETRIEVAL_THREAD_COUNT)
    {
      return;
    }
    synchronized(deviceCacheThreadList)
    {
      // limit concurrently running threads
      for (int i = 0; i < deviceCacheThreadList.size() &&
        ACTIVE_THREADS < UPnPDefaults.CP_DEVICE_CACHE_RETRIEVAL_THREAD_COUNT; i++)
      {
        CPDeviceCacheThread currentDeviceCacheThread = (CPDeviceCacheThread)deviceCacheThreadList.elementAt(i);
        if (!currentDeviceCacheThread.isAlive())
        {
          // System.out.println("START " + currentDeviceCacheThread.toString());

          ACTIVE_THREADS++;
          currentDeviceCacheThread.start();
        }
      }
    }
  }

  /** Checks if a certain device description URL is currently processed in the device cache. */
  public boolean isKnownDeviceDescriptionURL(URL deviceURL)
  {
    synchronized(deviceCacheThreadList)
    {
      for (int i = 0; i < deviceCacheThreadList.size(); i++)
      {
        CPDeviceCacheThread currentThread = (CPDeviceCacheThread)deviceCacheThreadList.elementAt(i);
        if (currentThread.deviceDescriptionURL != null && currentThread.deviceDescriptionURL.equals(deviceURL))
        {
          return true;
        }
      }
    }
    return false;
  }

  /** Reads the cached attribute service information. */
  public void readCachedAttributeServiceInformation(CPDevice device)
  {
    String escapedDirectoryName = getCacheDirectoryForDevice(device);

    File directory = new File(escapedDirectoryName);
    if (directory.exists())
    {
      String separator = System.getProperty("file.separator");
      String attributeFileName = escapedDirectoryName + separator + "attributeService.xml";

      File attributeFile = new File(attributeFileName);
      if (attributeFile.exists())
      {
        AttributeParser attributeParser = new AttributeParser();
        try
        {
          attributeParser.parse(attributeFile);
          device.setAttributeHashtable(attributeParser.getAttributeTable());
        } catch (Exception e)
        {
        }
      }
    }
  }

  /**
   * Retrieves the cacheDirectory.
   * 
   * @return The cacheDirectory
   */
  public String getCacheDirectory()
  {
    return cacheDirectory;
  }

  /**
   * Sets the cacheDirectory.
   * 
   * @param directory
   *          The new value for cacheDirectory
   */
  public void setCacheDirectory(String directory)
  {
    cacheDirectory = FileHelper.toValidDirectoryName(directory);

  }

  /**
   * Stores a device description URL in a cache directory.
   * 
   * @param deviceDescription
   *          The device description that should be cached
   * 
   */
  public void storeManualDeviceDescriptionURL(String deviceDescription)
  {
    String separator = System.getProperty("file.separator");

    // use fixed directory
    String directoryName = FileHelper.getResourceDirectoryName() + "device_urls" + separator;
    File directory = new File(directoryName);
    // create directory if necessary
    if (!directory.exists())
    {
      directory.mkdir();
    }

    // store device and service descriptions
    if (directory.exists())
    {
      if (deviceDescription.toLowerCase().startsWith("http://"))
      {
        deviceDescription = deviceDescription.substring("http://".length());
      }

      String escapedFileName = directoryName + StringHelper.escapeDirectoryName(deviceDescription);
      // create XML file with SSDP infos
      try
      {
        FileOutputStream fileOutputStream = new FileOutputStream(escapedFileName);
        fileOutputStream.close();
      } catch (Exception e)
      {
        System.out.println("Error: " + e.getMessage());
      }
    }
  }

  /** Parses the content of the ssdpInfo.xml file */
  private SSDPInfoParser parseSSDPInfo(String fileName)
  {
    File ssdpInfoFile = new File(fileName);
    if (ssdpInfoFile.exists())
    {
      SSDPInfoParser ssdpInfoParser = new SSDPInfoParser();
      try
      {
        ssdpInfoParser.parse(ssdpInfoFile);

        return ssdpInfoParser;
      } catch (SAXException e)
      {
      }
    }
    return null;
  }

  /** Delete all files for one device cache entry. */
  private void removeDeviceCacheEntry(File deviceDirectory)
  {
    if (deviceDirectory != null)
    {
      File[] files = deviceDirectory.listFiles();
      for (int j = 0; files != null && j < files.length; j++)
      {
        files[j].delete();
      }
      deviceDirectory.delete();
    }
  }

  /** Private class to read one cached device. */
  private class CPDeviceCacheThread extends Thread
  {

    private File deviceDirectory;

    private URL  deviceDescriptionURL = null;

    public CPDeviceCacheThread(File deviceDirectory)
    {
      super("CPDeviceCacheThread (" + controlPoint.toString() + ")");
      this.deviceDirectory = deviceDirectory;

      // System.out.println("Add device cache thread for " + deviceDirectory.getName());
    }

    public String toString()
    {
      return "CPDeviceCacheThread (" + deviceDirectory + ")";
    }

    public void run()
    {
      String path = FileHelper.toValidDirectoryName(deviceDirectory.getAbsolutePath());
      // System.out.println("Escaped name is " + path);

      String escapedDirectoryName = deviceDirectory.getName();
      String directoryName = StringHelper.escapedDirectoryNameToString(escapedDirectoryName);

      int hypenIndex = directoryName.indexOf("_");
      // extract IP, port and uuid
      if (hypenIndex != -1 && hypenIndex < directoryName.length() - 1)
      {
        String hostSocketAddress = directoryName.substring(0, hypenIndex);
        String uuid = directoryName.substring(hypenIndex + 1);

        // System.out.println("Read cached device: " + uuid);

        // parse SSDP info
        SSDPInfoParser ssdpInfoParser = parseSSDPInfo(path + "ssdpInfo.xml");

        if (ssdpInfoParser == null)
        {
          removeDeviceCacheEntry(deviceDirectory);
          terminate();
          return;
        }

        String cacheControl = ssdpInfoParser.cacheControl;
        String deviceDescriptionPath = ssdpInfoParser.deviceDescriptionPath;
        String server = ssdpInfoParser.server;

        // get file for device description
        String deviceDescriptionFileName = path + StringHelper.escapeDirectoryName(deviceDescriptionPath);

        try
        {
          deviceDescriptionURL = new URL("http://" + hostSocketAddress + deviceDescriptionPath);
        } catch (Exception e)
        {
          System.out.println("Error during URLing:" + e.getMessage());
          terminate();
          return;
        }

        // check size and optional hash of cached device description
        int descriptionState =
          getCachedDescriptionState(deviceDescriptionFileName, deviceDescriptionURL, ssdpInfoParser.etag);

        // check if cached description is valid
        if (descriptionState == CACHED_DESCRIPTION_VALID)
        {
          // create simple discovery info with root device info
          CPDeviceDiscoveryInfo deviceDiscoveryInfo =
            new CPDeviceDiscoveryInfo(UPnPConstant.MAX_AGE_TAG + cacheControl,
              deviceDescriptionURL,
              server,
              UPnPConstant.UPNP_ROOTDEVICE,
              uuid + "::" + UPnPConstant.UPNP_ROOTDEVICE,
              "");

          if (deviceDiscoveryInfo == null)
          {
            System.out.println("CPDeviceCache: Could not create basic device discovery info");
          } else
          {
            synchronized(controlPoint.getDeviceInfoLock())
            {
              boolean descriptionURLKnown =
                controlPoint.getDiscoveryInfoFromDescriptionURLTable().containsKey(deviceDescriptionURL);

              boolean uuidKnown = controlPoint.getDeviceDescriptionURLFromUUIDTable().containsKey(uuid);

              if (descriptionURLKnown || uuidKnown)
              {
                TemplateControlPoint.printMessage(controlPoint.toString() +
                  ": ERROR: Another device with the same URL or UUID " + "already exists: URL is " +
                  deviceDescriptionURL + ", UUID is " + uuid);

                terminate();
                return;
              }

              controlPoint.getDiscoveryInfoFromDescriptionURLTable().put(deviceDescriptionURL, deviceDiscoveryInfo);

              controlPoint.getDeviceDescriptionURLFromUUIDTable().put(uuid, deviceDescriptionURL);

              TemplateControlPoint.printMessage(controlPoint.toString() + ": Add cached DEVICE INFO for URL " +
                deviceDescriptionURL + " with UUID " + uuid);
              // load device description from file
              new CPDeviceDescriptionRetrieval(controlPoint, path, deviceDiscoveryInfo, ssdpInfoParser.etag);
            }
          }
        }
        if (descriptionState == CACHED_DESCRIPTION_DEPRECATED)
        {
          TemplateControlPoint.printMessage(controlPoint.toString() + ": Device with URL " + deviceDescriptionURL +
            " is deprecated.");
          // this removes the thread from the pending thread list
          terminate();
          controlPoint.getTemplateControlPoint().addRemoteDevice(deviceDescriptionURL.toExternalForm());
          return;
        }
        if (descriptionState == CACHED_DESCRIPTION_NOT_VERIFIED)
        {
          if (ssdpInfoParser.lastDiscovery != null)
          {
            TemplateControlPoint.printMessage(controlPoint.toString() + ": Device with URL " + deviceDescriptionURL +
              " is offline since " + DateTimeHelper.formatDateForGermany(ssdpInfoParser.lastDiscovery));
          }
          Calendar deprecationBorder = Calendar.getInstance();
          // calculate date where devices will be deprecated
          deprecationBorder.add(Calendar.DATE, -CP_DEVICE_CACHE_DEPRECATION);
          if (ssdpInfoParser.lastDiscovery == null ||
            DateTimeHelper.dateToCalendar(ssdpInfoParser.lastDiscovery).before(deprecationBorder))
          {
            System.out.println("Remove device with URL " + deviceDescriptionURL +
              " from cache that is unavailable for " + CP_DEVICE_CACHE_DEPRECATION + " days.");
            removeDeviceCacheEntry(deviceDirectory);
          }
        }
      }
      terminate();
    }

    /** Terminates the thread for device cache retrieval. */
    public void terminate()
    {
      synchronized(deviceCacheThreadList)
      {
        ACTIVE_THREADS--;
        deviceCacheThreadList.remove(this);
      }
    }

  }

  /** Private class to parse the SSDP info file */
  private class SSDPInfoParser extends SAXTemplateHandler
  {
    public String cacheControl          = "";

    public String deviceDescriptionPath = "";

    public String server                = "";

    public Date   lastDiscovery         = null;

    public String etag                  = null;

    /*
     * (non-Javadoc)
     * 
     * @see de.fhg.fokus.magic.upnp.xml.SAXTemplateHandler#processContentElement(java.lang.String)
     */
    public void processContentElement(String content) throws SAXException
    {
      if (getTagCount() == 2 && getCurrentTag().equals("Cache-Control"))
      {
        cacheControl = content;
      }
      if (getTagCount() == 2 && getCurrentTag().equals("DeviceDescriptionPath"))
      {
        deviceDescriptionPath = content;
      }
      if (getTagCount() == 2 && getCurrentTag().equals("Server"))
      {
        server = content;
      }
      if (getTagCount() == 2 && getCurrentTag().equals("LastDiscovery"))
      {
        lastDiscovery = DateTimeHelper.getDateFromUPnP(content);
      }
      if (getTagCount() == 2 && getCurrentTag().equals("ETag"))
      {
        etag = content;
      }
    }

  }

}
