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
package de.fraunhofer.fokus.upnp.core_av.examples.server.file_based;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLConnection;

import de.fraunhofer.fokus.upnp.core_av.didl.DIDLObject;
import de.fraunhofer.fokus.upnp.core_av.server.TransferStatus;
import de.fraunhofer.fokus.upnp.util.FileExtensionHelper;
import de.fraunhofer.fokus.upnp.util.URLHelper;

/**
 * This class is used to import a resource into the media server.
 * 
 * @author Alexander Koenig
 * 
 */
public class ImportResourceThread extends Thread
{

  /** Reference to media server */
  DirectoryMediaServerEntity directoryMediaServerEntity;

  /** ID for import */
  private int                transferID;

  /** Transfer status */
  private TransferStatus     transferStatus;

  /** Connection for resource retrieval */
  private URLConnection      connection;

  /** Object that is imported */
  private DIDLObject         didlObject;

  /**
   * Creates a new instance of ImportResourceThread.
   * 
   * @param directoryMediaServerEntity
   * @param didlObject
   * @param connection
   * @param transferID
   */
  public ImportResourceThread(DirectoryMediaServerEntity directoryMediaServerEntity,
    DIDLObject didlObject,
    URLConnection connection,
    int transferID)
  {
    this.directoryMediaServerEntity = directoryMediaServerEntity;
    this.connection = connection;
    this.didlObject = didlObject;
    this.transferID = transferID;
    start();
  }

  public void run()
  {
    String targetFileName = "";
    try
    {
      int transferTotal = connection.getContentLength();
      // System.out.println(transferID + ": Size of resource is " + transferTotal + " bytes");

      transferStatus = new TransferStatus("IN_PROGRESS", transferTotal, 0);

      InputStream inputStream = connection.getInputStream();
      String importURI = didlObject.getFirstResource().getImportURI();
      String importURIFileName = URLHelper.escapedURLToString(importURI);

      File importURIFile = new File(importURIFileName);
      targetFileName = importURIFile.getParent() + System.getProperty("file.separator") + didlObject.getTitle();

      String extension = "";
      // add file extension from source URL if necessary
      if (FileExtensionHelper.getFileExtension(targetFileName).length() == 0)
      {
        extension = "." + FileExtensionHelper.getFileExtension(connection.getURL().getPath());
        targetFileName += extension;
        didlObject.setTitle(didlObject.getTitle() + extension);
      }
      File targetFile = new File(targetFileName);

      FileOutputStream fileOutputStream = new FileOutputStream(targetFile);

      byte[] buffer = new byte[8192];
      int read = -1;
      while ((read = inputStream.read(buffer)) != -1)
      {
        transferStatus.setTransferLength(transferStatus.getTransferLength() + read);
        fileOutputStream.write(buffer, 0, read);
      }
      inputStream.close();
      fileOutputStream.close();

      // delete importURI object
      importURIFile.delete();

      // delete from hashtable
      directoryMediaServerEntity.removeIncompleteItemFromHashtable(importURI);

      transferStatus.setTransferStatus("VALUE_COMPLETED");
      // System.out.println(transferID + ": Successfully imported resource");

    } catch (Exception e)
    {
      transferStatus.setTransferStatus("ERROR");
      System.out.println(transferID + ": Could not import resource to " + targetFileName + ": " + e.getMessage());
    }
    // signal to media server
    // System.out.println("Finished transfer with ID: " + transferID);
    directoryMediaServerEntity.transferFinished(this);

    // hold result in memory for 30 seconds
    try
    {
      Thread.sleep(30000);
    } catch (Exception e)
    {
    }
    // signal to media server
    directoryMediaServerEntity.transferDeprecated(this);
  }

  /**
   * Retrieves the transferID.
   * 
   * @return The transferID.
   */
  public int getTransferID()
  {
    return transferID;
  }

  /**
   * Retrieves the transferStatus.
   * 
   * @return The transferStatus.
   */
  public TransferStatus getTransferStatus()
  {
    return transferStatus;
  }

}
