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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.event.WebServerListenerResponse;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;
import de.fraunhofer.fokus.upnp.core.xml.SOAPActionArgument;
import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.http.HTTPHeaderBuilder;
import de.fraunhofer.fokus.upnp.http.HTTPMessageBuilder;
import de.fraunhofer.fokus.upnp.http.HTTPParser;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.DateTimeHelper;
import de.fraunhofer.fokus.upnp.util.FileExtensionHelper;
import de.fraunhofer.fokus.upnp.util.ResourceHelper;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.URLHelper;
import de.fraunhofer.fokus.upnp.util.KeyValueVector;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageHelper;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;

/**
 * This class handles the processing and response of an incoming GET or HEAD message to retrieve
 * UPnP XML descriptions or resources.
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class DeviceGetMessageProcessor
{

  /**
   * UPnP logger
   */
  protected static Logger logger = Logger.getLogger("upnp.desc");

  /**
   * Processes a GET or HEAD request.
   * 
   * @param requestParser
   *          Associated parser
   * @param device
   *          Device that received the request
   * 
   * @return Response message object (HTTP error or requested data)
   * 
   */
  public static HTTPMessageObject processMessage(HTTPParser requestParser, Device device)
  {
    String responseString = null;
    byte[] responseByteArray = null;
    String responseContentType = HTTPConstant.CONTENT_TYPE_TEXT_XML_UTF8;
    Vector optionalHeaderLines = new Vector();

    // prevent path traversal
    if (requestParser.getHostPath().indexOf("../") != -1)
    {
      // remove any links to parent directories to prevent security hole
      requestParser.setHostPath(requestParser.getHostPath().replaceAll("\\x2E\\x2E/", "./"));
      TemplateDevice.printMessage("Tried to access a protected parent path. Changed path is " +
        requestParser.getHostPath());
    }
    // add connection close header if local HTTP server is set up to accept only single connections
    if (requestParser.getHTTPMessageObject().isCloseConnection())
    {
      optionalHeaderLines.add(HTTPHeaderBuilder.buildHeader(HTTPConstant.CONNECTION_HEADER, "close"));
    }

    // check for device or service description
    responseString = getDescription(requestParser.getHost(), requestParser.getHostPath(), device);
    // convert to byte array
    if (responseString != null)
    {
      responseByteArray = StringHelper.utf8StringToByteArray(responseString);
      // add description hash value to allow client to check for changed
      // descriptions
      optionalHeaderLines.add(HTTPHeaderBuilder.buildHeader(HTTPConstant.ETAG_HEADER, "\"" +
        device.getDescriptionHashBase64() + "\""));
    }

    // check for REST
    if (responseString == null)
    {
      responseString = getRESTResource(requestParser.getHost(), requestParser.getHostPath(), device);
      if (responseString != null)
      {
        responseContentType = HTTPConstant.CONTENT_TYPE_TEXT_PLAIN;
        responseByteArray = StringHelper.utf8StringToByteArray(responseString);
      }
    }

    // handle requests to internal web server
    if (responseString == null && device.getWebServerListener() != null)
    {
      WebServerListenerResponse response = device.getWebServerListener().processRequest(requestParser, device);
      // check if successfull
      if (response != null)
      {
        responseContentType = response.getContentType();
        responseByteArray = response.getBody();
      }
    }
    HTTPMessageObject response = null;
    // return description or web site if found
    if (responseByteArray != null)
    {
      // check request type
      if (requestParser.isGETRequest())
      {
        // GET request
        logger.info("GET message is valid & a GET response message will be created and sent");
        response =
          new HTTPMessageObject(HTTPMessageBuilder.createGETorHEADResponse(requestParser.isHTTP_1_0_Request(),
            HTTPConstant.DEFAULT_LANGUAGE,
            String.valueOf(responseByteArray.length),
            responseContentType,
            DateTimeHelper.getRFC1123Date(),
            optionalHeaderLines), responseByteArray, requestParser.getHTTPMessageObject().getDestinationAddress());
      }
      if (requestParser.isHEADRequest())
      {
        // HEAD request, do not return body
        logger.info("HEAD message is valid & a HEAD response message will be created and sent");
        response =
          new HTTPMessageObject(HTTPMessageBuilder.createGETorHEADResponse(requestParser.isHTTP_1_0_Request(),
            HTTPConstant.DEFAULT_LANGUAGE,
            String.valueOf(responseByteArray.length),
            responseContentType,
            DateTimeHelper.getRFC1123Date(),
            optionalHeaderLines), requestParser.getHTTPMessageObject().getDestinationAddress());
      }
    }
    if (response == null)
    {
      // System.out.println("Try to find file resource: " + getParser.getURL());

      // check for resources
      response = getResource(requestParser, device, optionalHeaderLines);
    }
    if (response == null)
    {
      // nothing found, return error
      logger.warn("device or service description or resource cannot be created or found.");
      response =
        new HTTPMessageObject(HTTPConstant.HTTP_ERROR_404, requestParser.getHTTPMessageObject().getDestinationAddress());
    }
    // set close flag if requested by client
    response.setCloseConnection(requestParser.hasConnectionCloseTag());

    return response;
  }

  /**
   * Tests if the URL points to a resource on the filesystem or is in the class path.
   * 
   * @param httpParser
   *          Associated parser
   * @param device
   *          Device that received the request
   * 
   * @return Response message object (HTTP error or requested data)
   */
  protected static HTTPMessageObject getResource(HTTPParser httpParser, Device device, Vector optionalHeaderLines)
  {
    // this URL is escaped, retrieve filename
    String unescapedName = URLHelper.escapedURLToString(httpParser.getHostPath());
    // remove leading "/"
    if (unescapedName.startsWith("/"))
    {
      unescapedName = unescapedName.substring(1);
    }

    HTTPMessageObject result = getResourceViaFilesystem(httpParser, device, unescapedName, optionalHeaderLines);
    if (result != null)
    {
      return result;
    }

    result = getResourceViaClassLoader(httpParser, device, unescapedName, optionalHeaderLines);
    if (result != null)
    {
      return result;
    }

    return null;
  }

  /**
   * Tries to load a resource from the filesystem.
   * 
   * @param httpParser
   *          Associated parser
   * @param device
   *          Device that received the request
   * 
   * @return Response message object (HTTP error or requested data)
   */
  protected static HTTPMessageObject getResourceViaFilesystem(HTTPParser httpParser,
    Device device,
    String fileName,
    Vector optionalHeaderLines)
  {
    // check for requests for device file directory
    if (device.getWebServerDirectoryList().size() > 0)
    {
      System.out.println(device.toString() + ": Try to find file: " + fileName);

      // search file in all registered web server directories
      File file = null;
      boolean found = false;
      for (int i = 0; !found && i < device.getWebServerDirectoryList().size(); i++)
      {
        char separator = System.getProperty("file.separator").charAt(0);
        String absoluteFileName = (String)device.getWebServerDirectoryList().elementAt(i) + fileName;
        absoluteFileName = absoluteFileName.replace('\\', separator);
        absoluteFileName = absoluteFileName.replace('/', separator);

        file = new File(absoluteFileName);
        found = file.exists();
      }
      if (found)
      {
        logger.info("File found");
        // System.out.println(device.toString() +": Deliver requested file: " + fileName);

        try
        {
          return processResource(fileName,
            new FileInputStream(file),
            (int)file.length(),
            httpParser,
            device,
            optionalHeaderLines);
        } catch (FileNotFoundException e)
        {
        }
      }
    }
    return null;
  }

  /**
   * Tries to load a resource via the class loader.
   * 
   * @param httpParser
   *          Associated parser
   * @param device
   *          Device that received the request
   * 
   * @return Response message object (HTTP error or requested data)
   */
  protected static HTTPMessageObject getResourceViaClassLoader(HTTPParser httpParser,
    Device device,
    String resourceName,
    Vector optionalHeaderLines)
  {
    // check for requests for device file directory
    if (device.getClassloaderResourceDirectoryList().size() > 0)
    {
      // System.out.println(device.toString() +": Try to find resource: " + resourceName);
      String absoluteResourceName = "";

      // search file in all registered directories
      boolean found = false;
      for (int i = 0; !found && i < device.getClassloaderResourceDirectoryList().size(); i++)
      {
        absoluteResourceName = (String)device.getClassloaderResourceDirectoryList().elementAt(i) + resourceName;
        // TemplateDevice.printMessage(device.toString() + ": Check in: " + absoluteResourceName);

        found = ResourceHelper.isAvailableViaClassloader(device.getClass(), absoluteResourceName);
      }

      if (found)
      {
        byte[] resourceData = ResourceHelper.loadByteArrayViaClassloader(device.getClass(), absoluteResourceName);
        InputStream resourceStream = device.getClass().getResourceAsStream(absoluteResourceName);

        if (resourceData != null && resourceStream != null)
        {
          return processResource(absoluteResourceName,
            resourceStream,
            resourceData.length,
            httpParser,
            device,
            optionalHeaderLines);
        }
      }
    }

    return null;
  }

  /**
   * Tests if the URL points to a resource on the filesystem or is in the class path.
   * 
   * @param httpParser
   *          Associated parser
   * @param device
   *          Device that received the request
   * 
   * @return Response message object (HTTP error or requested data)
   */
  protected static HTTPMessageObject processResource(String resourceName,
    InputStream resourceStream,
    int resourceSize,
    HTTPParser httpParser,
    Device device,
    Vector optionalHeaderLines)
  {
    logger.info("Resource found");
    // System.out.println(device.toString() +": Deliver requested file: " + fileName);
    // check file type
    String mimetype = FileExtensionHelper.getMimeTypeByFileExtension(resourceName);

    // check for HEAD request
    if (httpParser.isHEADRequest())
    {
      String responseHeader =
        HTTPMessageBuilder.createGETorHEADResponse(httpParser.isHTTP_1_0_Request(),
          HTTPConstant.DEFAULT_LANGUAGE,
          resourceSize + "",
          mimetype,
          DateTimeHelper.getRFC1123Date(),
          null);

      logger.info("HEAD message is valid & a HEAD response message will be created and sent");
      return new HTTPMessageObject(responseHeader, httpParser.getHTTPMessageObject().getDestinationAddress());
    }
    // check for partial GET request
    if (httpParser.hasField(CommonConstants.RANGE))
    {
      long[] range = HTTPMessageHelper.getRange(httpParser.getValue(CommonConstants.RANGE));
      if (range != null)
      {
        System.out.println("Received partial GET request from " + range[0] + " to " + range[1]);
        if (range[1] == -1)
        {
          range[1] = resourceSize;
        }
        try
        {
          // copy resource to byte array
          byte[] buffer = new byte[4096];
          ByteArrayOutputStream contentArray = new ByteArrayOutputStream();
          // seek to start
          long currentIndex = 0;
          while (currentIndex < range[0])
          {
            long skipped = resourceStream.skip(Math.min(Integer.MAX_VALUE, range[0] - currentIndex));
            currentIndex += skipped;
          }
          // read all bytes in range
          int requestCount = (int)Math.min(buffer.length, range[1] - currentIndex);
          int read = 0;
          while ((read = resourceStream.read(buffer, 0, requestCount)) != -1 && currentIndex < range[1])
          {
            contentArray.write(buffer, 0, read);
            currentIndex += read;
            requestCount = (int)Math.min(buffer.length, range[1] - currentIndex);
          }
          System.out.println("Read " + contentArray.size() + " bytes");
          // build header for partial GET
          String responseHeader =
            HTTPMessageBuilder.createPartialGETResponse(HTTPConstant.DEFAULT_LANGUAGE,
              contentArray.size() + "",
              mimetype,
              DateTimeHelper.getRFC1123Date(),
              range[0] + "-" + (currentIndex - 1) + "/" + resourceSize,
              optionalHeaderLines);

          // build response message
          logger.info("Partial GET message is valid & a partial GET response message will be created and sent");
          return new HTTPMessageObject(responseHeader, contentArray.toByteArray(), httpParser.getHTTPMessageObject()
            .getDestinationAddress());
        } catch (Exception ex)
        {
        }
      }
    }
    // normal GET request, return complete resource
    try
    {
      // copy resource to byte array
      byte[] buffer = new byte[4096];
      ByteArrayOutputStream contentArray = new ByteArrayOutputStream();
      int read = 0;
      while ((read = resourceStream.read(buffer)) != -1)
      {
        contentArray.write(buffer, 0, read);
      }
      String responseHeader =
        HTTPMessageBuilder.createGETorHEADResponse(httpParser.isHTTP_1_0_Request(),
          HTTPConstant.DEFAULT_LANGUAGE,
          contentArray.size() + "",
          mimetype,
          DateTimeHelper.getRFC1123Date(),
          null);

      // build response message
      // check request type
      if (httpParser.isGETRequest())
      {
        // GET request
        logger.info("GET message is valid & a GET response message will be created and sent");
        return new HTTPMessageObject(responseHeader, contentArray.toByteArray(), httpParser.getHTTPMessageObject()
          .getDestinationAddress());
      }
    } catch (Exception ex)
    {
    }
    return null;
  }

  /**
   * Tests if the message parameterURL is valid and return the requested description
   * 
   * @param serverAddress
   *          Server that received the request
   * @param urlPath
   *          Path of URL to be checked against description URL
   * @param device
   *          Device that received the request
   * 
   * @return A device or service description, null if description is not found
   * 
   */
  protected static String getDescription(String serverAddress, String urlPath, Device device)
  {
    // check if path is for DeviceDescription or DeviceService
    if (urlPath.equals(device.getRelativeDeviceDescriptionURL()))
    {
      logger.info("GET device description identified");

      try
      {
        return device.toXMLDescription(serverAddress);
      } catch (Exception ex)
      {
        return null;
      }
    } else
    {
      DeviceService[] services = device.getDeviceServiceTable();

      for (int i = 0; services != null && i < services.length; i++)
      {
        if (urlPath.equals(services[i].getRelativeSCPDURL()))
        {
          logger.info("GET service description identified");

          try
          {
            return services[i].toXMLDescription();
          } catch (Exception ex)
          {
            return null;
          }
        }
      }
    }

    Device[] embeddedDevices = device.getEmbeddedDeviceTable();

    for (int i = 0; embeddedDevices != null && i < embeddedDevices.length; i++)
    {
      String description = getDescription(serverAddress, urlPath, embeddedDevices[i]);

      if (description != null)
      {
        return description;
      }
    }
    // nothing found
    return null;
  }

  /**
   * Tests if the URL is a valid REST request and return the requested values
   * 
   * @param serverAddress
   *          Server that received the request
   * @param urlPath
   *          Path of URL to be checked against REST URL
   * @param device
   *          Device that received the request
   * 
   * @return A value or null
   * 
   */
  protected static String getRESTResource(String serverAddress, String urlPath, Device device)
  {
    // search service for specified path
    DeviceService service = device.getServiceByRelativeURL(urlPath, UPnPConstant.SUFFIX_REST);

    if (service != null)
    {
      // parse action name
      try
      {
        // separate path and parameters with the help of an URL
        URL url = new URL("http://localhost" + urlPath);
        String actionName = url.getPath();
        String restPath = device.getRelativeServiceURL(service, UPnPConstant.SUFFIX_REST);
        if (actionName.length() > restPath.length())
        {
          actionName = actionName.substring(restPath.length());
        }
        Action action = service.getAction(actionName);
        if (action != null)
        {
          KeyValueVector argumentPairs = URLHelper.parseURLQuery(url.getQuery());
          // build SOAPArgument list from vector pairs
          Vector argumentList = new Vector();
          for (int i = 0; argumentPairs != null && i < argumentPairs.size(); i++)
          {
            argumentList.add(new SOAPActionArgument((String)argumentPairs.getKey(i), (String)argumentPairs.getValue(i)));
          }
          // check number of input arguments for action
          action = DeviceControlMessageProcessor.processReceivedArguments(action, argumentList);
          // try to invoke action on local device
          if (service.invokeLocalAction(action))
          {
            String result = "";
            Argument[] outArgumentTable = action.getOutArgumentTable();
            for (int i = 0; i < outArgumentTable.length; i++)
            {
              // TODO: Remove new lines in responses and escape responses
              result +=
                outArgumentTable[i].getName() + "=" + outArgumentTable[i].getValueAsString() + CommonConstants.NEW_LINE;
            }
            return result;
          }
          if (action.processingError())
          {
            return action.getErrorCode() + ":" + action.getErrorDescription();
          }
          return UPnPConstant.SOAP_ERROR_501 + ":" + UPnPConstant.SOAP_ERROR_501_DESCRIPTION;
        }
      } catch (Exception e)
      {
      }
    }

    // check embedded devices
    Device[] embeddedDevices = device.getEmbeddedDeviceTable();
    for (int i = 0; embeddedDevices != null && i < embeddedDevices.length; i++)
    {
      String result = getRESTResource(serverAddress, urlPath, embeddedDevices[i]);
      if (result != null)
      {
        return result;
      }
    }
    // nothing found
    return null;
  }
}
