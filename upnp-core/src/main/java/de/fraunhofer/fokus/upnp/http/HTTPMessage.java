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
package de.fraunhofer.fokus.upnp.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.configuration.HTTPDefaults;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageHelper;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;

/**
 * This class is used to read HTTP messages from an input socket.
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class HTTPMessage
{

  /**
   * HTTP logger
   */
  private static Logger logger = Logger.getLogger("upnp.http");

  /**
   * This method reads an incoming message.
   * 
   * @param message
   *          The object that holds the received message
   * @param inStream
   *          The stream that is read
   * @param returnAfterHeader
   *          The method returns immediately after the header was read.
   * @param expectHeaderOnly
   *          Flag that only a HTTP header is expected
   * 
   * @throws IOException
   *           if an error occurred reading the input
   * @throws HTTPParseException
   *           if the received message is empty or contains no body
   * 
   */
  public static void getServerMessage(HTTPMessageObject message, InputStream inStream) throws IOException,
    HTTPParseException
  {
    getMessage(message, inStream, false, false, true);
  }

  /**
   * This method reads an incoming message.
   * 
   * @param message
   *          The object that holds the received message
   * @param inStream
   *          The stream that is read
   * @param returnAfterHeader
   *          The method returns immediately after the header was read.
   * @param expectHeaderOnlyResponse
   *          Flag that this is a header only reponse
   * 
   * @throws IOException
   *           if an error occurred reading the input
   * @throws HTTPParseException
   *           if the received message is empty or contains no body
   * 
   */
  public static void getServerResponseMessage(HTTPMessageObject message,
    InputStream inStream,
    boolean returnAfterHeader,
    boolean expectHeaderOnlyResponse) throws IOException, HTTPParseException
  {
    getMessage(message, inStream, returnAfterHeader, expectHeaderOnlyResponse, false);
  }

  /**
   * This method reads an incoming message.
   * 
   * @param message
   *          The object that holds the received message
   * @param inStream
   *          The stream that is read
   * @param returnAfterHeader
   *          The method returns immediately after the header was read.
   * @param expectHeaderOnlyResponse
   *          Flag that this is a header only reponse
   * @param serverRequest
   *          Flag that this is a request received by a server
   * 
   * @throws IOException
   *           if an error occurred reading the input
   * @throws HTTPParseException
   *           if the received message is empty or contains no body
   * 
   */
  private static void getMessage(HTTPMessageObject message,
    InputStream inStream,
    boolean returnAfterHeader,
    boolean expectHeaderOnlyResponse,
    boolean serverRequest) throws IOException, HTTPParseException
  {
    ByteArrayOutputStream messageOutputStream = new ByteArrayOutputStream(CommonConstants.HTTP_BUFFER_READ_SIZE); // default
                                                                                                                  // is
                                                                                                                  // only
                                                                                                                  // 16
    byte[] buffer = new byte[CommonConstants.HTTP_BUFFER_READ_SIZE];
    String header = null;
    int bytesRead = 0;
    int remainingBodyBytes = -1;
    int headerEndIndex = -1;
    boolean finished = false;
    boolean headerFound = false;
    boolean chunkedEncoding = false;

    long lastReceiveTime = System.currentTimeMillis();

    // loop until whole message read or timeout
    while (!finished && System.currentTimeMillis() - lastReceiveTime < HTTPDefaults.TIMEOUT_FOR_RESEND * 1000)
    {
      bytesRead = 0;
      // try to read available bytes
      try
      {
        bytesRead = inStream.read(buffer);
        lastReceiveTime = System.currentTimeMillis();
      } catch (SocketTimeoutException e)
      {
      } catch (Exception e)
      {
        // an error occured
        finished = true;
      }
      // bytes were read
      if (bytesRead > 0)
      {
        // System.out.println("HTTPMessage.getMessage from " +
        // IPAddress.toString(message.getSourceAddress()) + ": " + bytesRead + " bytes read");
        // copy read bytes to output stream
        messageOutputStream.write(buffer, 0, bytesRead);

        // check for complete message
        if (remainingBodyBytes != -1)
        {
          remainingBodyBytes -= bytesRead;
          // gracefully handle overlong messages
          if (remainingBodyBytes <= 0)
          {
            logger.debug("Remaining bytes read");
            finished = true;
          }
        }

        // check for chunked encoding
        // search chunk with size 0
        if (chunkedEncoding)
        {
          byte[] partialBody = HTTPMessageHelper.getBody(messageOutputStream.toByteArray(), headerEndIndex);
          if (hasLastChunk(partialBody))
          {
            finished = true;
            logger.debug("Found last chunk");
          }
        }

        // try to find complete header
        if (!headerFound)
        {
          // build temporary byte array
          byte[] partialMessage = messageOutputStream.toByteArray();
          // search for header end
          headerEndIndex = HTTPMessageHelper.getHeaderEndIndex(partialMessage);
          // end of header found
          if (headerEndIndex != -1)
          {
            headerFound = true;
            // retrieve full header
            header = HTTPMessageHelper.getHeader(partialMessage);
            // save header to client
            message.setHeader(header);
            // convert to upper case for parsing
            String headerUpperCase = header.toUpperCase();

            // check for chunked encoding
            int encodingPos = headerUpperCase.indexOf(HTTPConstant.TRANSFER_ENCODING);
            int lineEnd = headerUpperCase.indexOf("\r\n", encodingPos);
            if (encodingPos != -1 && lineEnd != -1)
            {
              String encoding =
                headerUpperCase.substring(encodingPos + HTTPConstant.TRANSFER_ENCODING.length(), lineEnd).trim();
              // check if chunked
              chunkedEncoding = encoding.equals(HTTPConstant.CHUNKED);
              if (chunkedEncoding)
              {
                logger.warn("Found chunked encoding");
              }
            }

            // check for header only messages
            boolean isHeaderOnlyMessage = false;
            if (serverRequest)
            {
              isHeaderOnlyMessage = HTTPMessageHelper.isHeaderOnlyRequestMessage(headerUpperCase);
            } else
            {
              isHeaderOnlyMessage = expectHeaderOnlyResponse;
            }

            // if (isHeaderOnlyMessage)
            // System.out.println("Found header only message: [\n" + header + "]");

            // search content length
            int contentLengthPos = headerUpperCase.indexOf(CommonConstants.CONTENT_LENGTH);
            lineEnd = headerUpperCase.indexOf("\r\n", contentLengthPos);
            if (contentLengthPos != -1 && lineEnd != -1)
            {
              int contentLength =
                Integer.parseInt(headerUpperCase.substring(contentLengthPos + CommonConstants.CONTENT_LENGTH.length(),
                  lineEnd).trim());

              // calculate remaining bytes
              remainingBodyBytes = contentLength - (messageOutputStream.size() - (headerEndIndex + 4));
              if (remainingBodyBytes != 0)
              {
                logger.debug("Wait for " + remainingBodyBytes + " outstanding bytes:");
              }

              // check if message is already read completely
              if (remainingBodyBytes <= 0 || contentLength == 0)
              {
                finished = true;
              }
            } else
            {
              // check for HTTP error that would result in an header only response
              if (headerUpperCase.startsWith("HTTP/1.1 4"))
              {
                logger.debug("Received HTTP 404, message is header only");
                isHeaderOnlyMessage = true;
              }

              // no content length, check message type or chunked encoding
              if (!(chunkedEncoding || isHeaderOnlyMessage))
              {
                logger.warn("No content length found in http header");
                logger.warn("Partial message is[\n" + StringHelper.byteArrayToUTF8String(partialMessage) + "]");
              }
            }
            // check for messages that do not have a body
            if (isHeaderOnlyMessage)
            {
              finished = true;
            }
            // check for request for header only
            if (returnAfterHeader)
            {
              finished = true;
            }
          }
        }
      }
      // handle socket close
      if (bytesRead == -1)
      {
        finished = true;
      }
    }
    // try to build body with received message
    if (headerFound)
    {
      // handle chunked encoding only if message was read completely
      if (chunkedEncoding && !returnAfterHeader)
      {
        byte[] chunkedBody = HTTPMessageHelper.getBody(messageOutputStream.toByteArray(), headerEndIndex);
        if (hasLastChunk(chunkedBody))
        {
          byte[] dechunkedBody = decodeChunkedBody(chunkedBody);
          message.setBody(dechunkedBody);
          logger.debug("Decoded body is [\n" + StringHelper.byteArrayToString(dechunkedBody) + "]");

          // remove content-encoding: chunked header
          header = HTTPMessageHelper.removeHeaderLine(header, HTTPConstant.TRANSFER_ENCODING);
          // add content length header
          header = HTTPMessageHelper.addHeaderLine(header, CommonConstants.CONTENT_LENGTH, dechunkedBody.length + "");
          message.setHeader(header);
        }
      } else
      {
        // set body to complete message or to already received part for immediate return
        message.setBody(HTTPMessageHelper.getBody(messageOutputStream.toByteArray(), headerEndIndex));
      }
    }
    // no more data available
    if (System.currentTimeMillis() - lastReceiveTime >= HTTPDefaults.TIMEOUT_FOR_RESEND * 1000)
    {
      // no data was received within the timeout, return with incomplete message
      logger.warn("Wait timeout.");
      String incompleteMessage = StringHelper.byteArrayToString(messageOutputStream.toByteArray());
      // print start of received message to console
      System.out.println("Wait timeout while packet reception" +
        message.toString() +
        (incompleteMessage != null && incompleteMessage.length() > 0 ? "[\n" +
          incompleteMessage.substring(0, Math.min(500, incompleteMessage.length())) + "]" : ""));
    }
    // logger.debug("Received [\n" +
    // StringHelper.byteArrayToString(messageOutputStream.toByteArray()) + "]");

    if (messageOutputStream.size() == 0)
    {
      throw new HTTPParseException("Empty response");
    }
  }

  /**
   * Searches the last chunk in a chunked message.
   * 
   * @param body
   *          The chunked body.
   * 
   * @return True if the last chunk was found, false otherwise
   */
  private static boolean hasLastChunk(byte[] body)
  {
    int idx = 0;
    String buffer = "";
    while (idx < body.length)
    {
      final char c = (char)body[idx];
      idx++;
      // search for valid size
      if (c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a' && c <= 'f')
      {
        buffer += c;
      }
      if (c == '\n' || c == ';')
      {
        final int chunkBlockSize = Integer.parseInt(buffer, 16);
        buffer = "";
        if (chunkBlockSize == 0)
        {
          return true;
        }
        // search for line end if not already found
        if (c == ';')
        {
          while (idx < body.length && (char)body[idx] != '\n')
          {
            idx++;
          }

          idx++;
        }

        // skip data
        idx += chunkBlockSize;
        idx += 2; // skip added cr/lf
      }
    }
    return false;
  }

  /**
   * This method builds a normal message body from a chunked body.
   * 
   * @param body
   *          The chunked body
   * 
   * @return A dechunked body
   * 
   */
  private static byte[] decodeChunkedBody(byte[] body)
  {
    int idx = 0;
    String buffer = "";
    ByteArrayOutputStream decodedBody = new ByteArrayOutputStream();
    while (idx < body.length)
    {
      final char c = (char)body[idx];
      idx++;
      // search for valid size
      if (c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a' && c <= 'f')
      {
        buffer += c;
      }
      // search for end of size
      if (c == '\n' || c == ';')
      {
        final int chunkBlockSize = Integer.parseInt(buffer, 16);
        // found last chunk
        if (chunkBlockSize == 0)
        {
          return decodedBody.toByteArray();
        }
        buffer = "";
        // search for line end if not already found (possible optional stuff after ; )
        if (c == ';')
        {
          while (idx < body.length && (char)body[idx] != '\n')
          {
            idx++;
          }

          idx++;
        }
        // copy data
        for (int i = 0; i < chunkBlockSize; i++)
        {
          decodedBody.write(body[idx]);
          idx++;
        }
        idx += 2; // skip added cr/lf
      }
    }
    return null;
  }
}
