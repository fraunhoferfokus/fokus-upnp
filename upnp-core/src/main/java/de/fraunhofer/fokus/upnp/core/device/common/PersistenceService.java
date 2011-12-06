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
package de.fraunhofer.fokus.upnp.core.device.common;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Hashtable;

import org.xml.sax.SAXException;

import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.DeviceConstant;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.device.Action;
import de.fraunhofer.fokus.upnp.core.device.StateVariable;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateService;
import de.fraunhofer.fokus.upnp.util.Base64Helper;
import de.fraunhofer.fokus.upnp.util.SAXTemplateHandler;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;
import de.fraunhofer.fokus.upnp.util.network.FTPHelper;

/**
 * This class can be used to store data to different destinations, e.g., a file, a FTP account or a
 * data base.
 * 
 * @author Alexander Koenig
 */
public class PersistenceService extends TemplateService
{

  public static String  FTP  = "FTP";

  public static String  FILE = "File";

  // public static String DATA_BASE = "Database";

  private StateVariable defaultStorage;

  private StateVariable defaultMetadata;

  // private StateVariable A_ARG_TYPE_string;
  private StateVariable A_ARG_TYPE_base64;

  private Action        getDefaultStorage;

  private Action        setDefaultStorage;

  private Action        storeData;

  private Action        loadData;

  private Action        storeDataTo;

  private Action        loadDataFrom;

  private Hashtable     defaultMetadataProperties;

  /** Creates a new instance of PersistenceService. */
  public PersistenceService(TemplateDevice device)
  {
    super(device, DeviceConstant.PERSISTENCE_SERVICE_TYPE, DeviceConstant.PERSISTENCE_SERVICE_ID);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateService#initServiceContent()
   */
  public void initServiceContent()
  {
    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // State variables //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    // String variable
    defaultStorage = new StateVariable("DefaultStorage", "", true);
    defaultStorage.setAllowedValueList(new String[] {
        FTP, FILE
    });
    defaultMetadata = new StateVariable("DefaultMetadata", "", true);
    // A_ARG_TYPE_string = new StateVariable("A_ARG_TYPE_string", "", false);
    A_ARG_TYPE_base64 = new StateVariable("A_ARG_TYPE_base64", "bin.base64", "", false);

    StateVariable[] stateVariableList = {
        defaultStorage, defaultMetadata,
        /* A_ARG_TYPE_string, */A_ARG_TYPE_base64
    };
    setStateVariableTable(stateVariableList);

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // Actions //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    getDefaultStorage = new Action("GetDefaultStorage");
    getDefaultStorage.setArgumentTable(new Argument[] {
        new Argument("Storage", UPnPConstant.DIRECTION_OUT, defaultStorage),
        new Argument("Metadata", UPnPConstant.DIRECTION_OUT, defaultMetadata)
    });

    setDefaultStorage = new Action("SetDefaultStorage");
    setDefaultStorage.setArgumentTable(new Argument[] {
        new Argument("Storage", UPnPConstant.DIRECTION_IN, defaultStorage),
        new Argument("Metadata", UPnPConstant.DIRECTION_IN, defaultMetadata)
    });

    loadData = new Action("LoadData");
    loadData.setArgumentTable(new Argument[] {
      new Argument("Data", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_base64),
    });

    storeData = new Action("StoreData");
    storeData.setArgumentTable(new Argument[] {
      new Argument("Data", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_base64)
    });

    loadDataFrom = new Action("LoadDataFrom");
    loadDataFrom.setArgumentTable(new Argument[] {
        new Argument("Storage", UPnPConstant.DIRECTION_IN, defaultStorage),
        new Argument("Metadata", UPnPConstant.DIRECTION_IN, defaultMetadata),
        new Argument("Data", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_base64)
    });

    storeDataTo = new Action("StoreDataTo");
    storeDataTo.setArgumentTable(new Argument[] {
        new Argument("Storage", UPnPConstant.DIRECTION_IN, defaultStorage),
        new Argument("Metadata", UPnPConstant.DIRECTION_IN, defaultMetadata),
        new Argument("Data", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_base64)
    });

    Action[] actionList = {
        getDefaultStorage, setDefaultStorage, loadData, storeData, loadDataFrom, storeDataTo
    };
    setActionTable(actionList);
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Action implementation //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  public void getDefaultStorage(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    try
    {
      args[0].setValue(defaultStorage.getValue());
      args[1].setValue(defaultMetadata.getValue());
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void setDefaultStorage(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    try
    {
      setDefaultStorageLocally(args[0].getStringValue());
      setDefaultMetadataLocally(args[1].getStringValue());
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void loadData(Argument[] args) throws ActionFailedException
  {
    if (args.length != 1)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    byte[] result = loadDataLocally();
    if (result != null)
    {
      try
      {
        args[0].setValue(Base64Helper.byteArrayToBase64(result));

        return;
      } catch (Exception e)
      {
        System.out.println("Error loading data: " + e.getMessage());
      }
      throw new ActionFailedException(702, "Error loading data");
    }
    throw new ActionFailedException(402, "Invalid args");
  }

  public void loadDataFrom(Argument[] args) throws ActionFailedException
  {
    if (args.length != 3)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    try
    {
      String storage = args[0].getStringValue();
      String metadata = args[1].getStringValue();

      byte[] result = loadDataLocally(storage, metadata);
      if (result != null)
      {
        args[2].setValue(Base64Helper.byteArrayToBase64(result));

        return;
      }
    } catch (Exception e)
    {
      System.out.println("Error loading data: " + e.getMessage());
    }
    throw new ActionFailedException(402, "Invalid args");
  }

  public void storeData(Argument[] args) throws ActionFailedException
  {
    if (args.length != 1)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    try
    {
      String storage = args[0].getStringValue();
      String metadata = args[1].getStringValue();
      byte[] data = args[2].getBinBase64Value();

      if (data != null)
      {
        if (storeDataLocally(storage, metadata, data))
        {
          return;
        } else
        {
          throw new ActionFailedException(701, "Error storing data");
        }
      }
    } catch (Exception e)
    {
    }
    throw new ActionFailedException(402, "Invalid args");
  }

  public void storeDataTo(Argument[] args) throws ActionFailedException
  {
    if (args.length != 3)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    byte[] data = null;
    try
    {
      data = args[0].getBinBase64Value();
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
    if (data != null)
    {
      if (storeDataLocally(data))
      {
        return;
      } else
      {
        throw new ActionFailedException(701, "Error storing data");
      }
    }
    throw new ActionFailedException(402, "Invalid args");
  }

  /**
   * Loads data using the current storage and metadata. This method can be invoked by a local
   * service.
   */
  public byte[] loadDataLocally()
  {
    return loadDataLocally(null, null);
  }

  /** Loads data using certain storage and metadata. This method can be invoked by a local service. */
  public byte[] loadDataLocally(String storage, String metadata)
  {
    try
    {
      if (storage == null)
      {
        storage = defaultStorage.getStringValue();
      }

      Hashtable metadataProperties;
      if (metadata == null)
      {
        metadataProperties = defaultMetadataProperties;
      } else
      {
        MetadataParser parser = new MetadataParser();
        parser.parse(metadata);
        metadataProperties = parser.getProperties();
      }

      // check storage type
      if (storage.equals(FTP))
      {
        // check metadata for needed values
        if (metadataProperties.containsKey("Host") && metadataProperties.containsKey("User") &&
          metadataProperties.containsKey("Password") && metadataProperties.containsKey("Filename"))
        {
          return FTPHelper.downloadFile((String)metadataProperties.get("Host"),
            (String)metadataProperties.get("User"),
            (String)metadataProperties.get("Password"),
            (String)metadataProperties.get("Filename"));
        }
      }
      if (storage.equals(FILE))
      {
        // check metadata for needed values
        if (metadataProperties.containsKey("Filename"))
        {
          FileInputStream fileInputStream = new FileInputStream((String)metadataProperties.get("Filename"));
          ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
          byte[] buffer = new byte[4096];
          int bytesRead = fileInputStream.read(buffer);
          while (bytesRead >= 0)
          {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
            bytesRead = fileInputStream.read(buffer);
          }
          fileInputStream.close();
          return byteArrayOutputStream.toByteArray();
        }
      }

    } catch (Exception e)
    {
      System.out.println("Error downloading data: " + e.getMessage());
    }
    return null;
  }

  /**
   * Stores data using the current storage and metadata. This method can be invoked by a local
   * service.
   */
  public boolean storeDataLocally(byte[] data)
  {
    return storeDataLocally(null, null, data);
  }

  /** Stores data using certain storage and metadata. This method can be invoked by a local service. */
  public boolean storeDataLocally(String storage, String metadata, byte[] data)
  {
    try
    {
      if (storage == null)
      {
        storage = defaultStorage.getStringValue();
      }

      Hashtable metadataProperties;
      if (metadata == null)
      {
        metadataProperties = defaultMetadataProperties;
      } else
      {
        MetadataParser parser = new MetadataParser();
        parser.parse(metadata);
        metadataProperties = parser.getProperties();
      }

      // check storage type
      if (storage.equals(FTP))
      {
        // check metadata for needed values
        if (metadataProperties.containsKey("Host") && metadataProperties.containsKey("User") &&
          metadataProperties.containsKey("Password") && metadataProperties.containsKey("Filename"))
        {
          return FTPHelper.uploadFile((String)metadataProperties.get("Host"),
            (String)metadataProperties.get("User"),
            (String)metadataProperties.get("Password"),
            (String)metadataProperties.get("Filename"),
            data);
        }
      }
      if (storage.equals(FILE))
      {
        // check metadata for needed values
        if (metadataProperties.containsKey("Filename"))
        {
          FileOutputStream fileOutputStream = new FileOutputStream((String)metadataProperties.get("Filename"));
          fileOutputStream.write(data);
          fileOutputStream.close();
        }
      }
    } catch (Exception e)
    {
      System.out.println("Error storing data: " + e.getMessage());
    }
    return false;
  }

  /** Sets the storage type from a local service. */
  public void setDefaultStorageLocally(String storage)
  {
    try
    {
      defaultStorage.setValue(storage);
    } catch (Exception e)
    {
    }
  }

  /** Sets the meta data from a local service. */
  public void setDefaultMetadataLocally(String metadata)
  {
    try
    {
      defaultMetadata.setValue(metadata);
      MetadataParser parser = new MetadataParser();
      parser.parse(defaultMetadata.getStringValue());
      defaultMetadataProperties = parser.getProperties();
    } catch (Exception e)
    {
    }

  }

  private class MetadataParser extends SAXTemplateHandler
  {

    private Hashtable properties = new Hashtable();

    public void processContentElement(String content) throws SAXException
    {
      properties.put(getCurrentTag(), content);
    }

    /**
     * Retrieves the properties.
     * 
     * @return The properties
     */
    public Hashtable getProperties()
    {
      return properties;
    }

  }

}
