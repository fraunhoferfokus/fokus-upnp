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

import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * This class helps to manage file extensions.
 * 
 * @author Alexander Koenig
 * 
 */
public class FileExtensionHelper
{

  private static String    DELIMITERS              = ",;:";

  private static Hashtable imageExtensionHashtable = new Hashtable();

  private static Hashtable audioExtensionHashtable = new Hashtable();

  private static Hashtable videoExtensionHashtable = new Hashtable();

  static
  {
    fillHashtable(imageExtensionHashtable, CommonConstants.FILE_EXTENSIONS_IMAGE);
    fillHashtable(audioExtensionHashtable, CommonConstants.FILE_EXTENSIONS_AUDIO);
    fillHashtable(videoExtensionHashtable, CommonConstants.FILE_EXTENSIONS_VIDEO);
  }

  /** Fills a hashtable with file extensions. */
  private static void fillHashtable(Hashtable hashtable, String extensions)
  {
    StringTokenizer tokenizer = new StringTokenizer(extensions, DELIMITERS);
    while (tokenizer.hasMoreTokens())
    {
      String extension = tokenizer.nextToken().toUpperCase();
      hashtable.put(extension, extension);
    }
  }

  /**
   * Checks if a file extension is included in a hashtable.
   * 
   * @param hashtable
   *          The hash table
   * @param fileName
   *          The name of the file
   * 
   * @return True if the extension is included, false otherwise
   */
  private static boolean hasFileExtension(Hashtable hashtable, String fileName)
  {
    if (fileName == null || fileName.length() == 0)
    {
      return false;
    }

    int lastPeriod = fileName.lastIndexOf(".");
    if (lastPeriod != -1 && lastPeriod < fileName.length() - 1)
    {
      return hashtable.contains(fileName.substring(lastPeriod + 1).toUpperCase());
    }

    return false;
  }

  /**
   * Retrieves the file extension (e.g., jpg).
   * 
   * @param fileName
   *          The name of the file
   * 
   * @return The file extension without the dot
   */
  public static String getFileExtension(String fileName)
  {
    if (fileName == null || fileName.length() == 0)
    {
      return "";
    }

    int lastPeriod = fileName.lastIndexOf(".");
    if (lastPeriod != -1 && lastPeriod < fileName.length() - 1)
    {
      return fileName.substring(lastPeriod + 1);
    }

    return "";
  }

  /**
   * Retrieves the file name without extension.
   * 
   * @param fileName
   *          The name of the file
   * 
   * @return The simple name of the file
   */
  public static String getFileNameWithoutExtension(String fileName)
  {
    if (fileName == null || fileName.length() == 0)
    {
      return "";
    }

    int lastPeriod = fileName.lastIndexOf(".");
    if (lastPeriod != -1)
    {
      return fileName.substring(0, lastPeriod);
    }

    return fileName;
  }

  /**
   * Checks if a certain file represents an image.
   * 
   * @param fileName
   *          The name of the file
   * @return True if this is an image, false otherwise
   */
  public static boolean isImageFile(String fileName)
  {
    return hasFileExtension(imageExtensionHashtable, fileName);
  }

  /**
   * Checks if a certain file represents an audio file.
   * 
   * @param fileName
   *          The name of the file
   * @return True if this is an audio file, false otherwise
   */
  public static boolean isAudioFile(String fileName)
  {
    return hasFileExtension(audioExtensionHashtable, fileName);
  }

  /**
   * Checks if a certain file represents an video file.
   * 
   * @param fileName
   *          The name of the file
   * @return True if this is an video file, false otherwise
   */
  public static boolean isVideoFile(String fileName)
  {
    return hasFileExtension(videoExtensionHashtable, fileName);
  }

  /**
   * Retrieves the protocol info string for a certain file extension.
   * 
   * @param fileName
   *          The file name
   * 
   * @return The protocol info (e.g., http-get:*:image/png:*)
   */
  public static String getProtocolInfoByFileExtension(String fileName)
  {
    return "http-get:*:" + getMimeTypeByFileExtension(fileName) + ":*";
  }

  /**
   * Retrieves the protocol info string for a certain file extension.
   * 
   * @param fileName
   *          The file name
   * 
   * @return The protocol info (e.g., http-get:*:image/png:*)
   */

  public static String getMimeTypeByFileExtension(String fileName)
  {
    String mimeType = "text/xml";
    String fileExtension = FileExtensionHelper.getFileExtension(fileName).toLowerCase();
    if (FileExtensionHelper.isAudioFile(fileName))
    {
      mimeType = "audio/" + fileExtension;
      // handle special protocols
      if (fileExtension.equals(CommonConstants.EXTENSION_MP3))
      {
        mimeType = "audio/mpeg";
      }
    }
    if (FileExtensionHelper.isImageFile(fileName))
    {
      mimeType = "image/" + fileExtension;
      // handle special protocols
      if (fileExtension.equals(CommonConstants.EXTENSION_JPG))
      {
        mimeType = "image/jpeg";
      }

    }
    if (FileExtensionHelper.isVideoFile(fileName))
    {
      mimeType = "video/" + fileExtension;
      // handle special protocols
      if (fileExtension.equals(CommonConstants.EXTENSION_MPG))
      {
        mimeType = "video/mpeg";
      }
    }
    if (fileExtension.equals("css"))
    {
      mimeType = "text/css";
    }
    if (fileExtension.equals("js"))
    {
      mimeType = "application/x-javascript";
    }

    return mimeType;
  }

}
