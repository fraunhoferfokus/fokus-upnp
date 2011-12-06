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
package de.fraunhofer.fokus.lsf.core.control_point;

import java.io.ByteArrayOutputStream;
import java.util.Vector;

import de.fraunhofer.fokus.lsf.core.BinaryUPnPConstants;
import de.fraunhofer.fokus.lsf.core.base.AbstractBinaryAction;
import de.fraunhofer.fokus.lsf.core.base.BinaryArgument;
import de.fraunhofer.fokus.upnp.util.CollectionHelper;
import de.fraunhofer.fokus.upnp.util.KeyValueVector;
import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;
import de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject;

/**
 * This class encapsulates a remote view on a binary UPnP action.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class BinaryCPAction extends AbstractBinaryAction
{

  /** Associated service. */
  private BinaryCPService binaryCPService;

  /**
   * Creates a new instance of BinaryCPAction.java
   * 
   * @param actionName
   * @param actionID
   * @param argumentList
   */
  public BinaryCPAction(String actionName, int actionID, Vector argumentList)
  {
    super(actionName, actionID, argumentList);
  }

  /**
   * Retrieves the binaryCPService.
   * 
   * @return The binaryCPService
   */
  public BinaryCPService getBinaryCPService()
  {
    return binaryCPService;
  }

  /**
   * Sets the binaryCPService.
   * 
   * @param binaryCPService
   *          The new value for binaryCPService
   */
  public void setBinaryCPService(BinaryCPService binaryCPService)
  {
    this.binaryCPService = binaryCPService;
  }

  /** Sends this action to the remote binary UPnP device. */
  public void invokeAction() throws ActionFailedException
  {
    ActionFailedException actionFailedException = null;
    try
    {
      byte[] data = toByteArrayForAction();

      BinaryMessageObject response =
        getBinaryCPService().getBinaryCPDevice().getBinaryControlPoint().invokeAction(this, data);

      // Portable.println(DateTimeHelper.formatCurrentDateForDebug() + ":" +
      // BinaryUPnPConstants.toDebugString(response.getBody()));

      BinaryCPValueMessageParser messageParser = new BinaryCPValueMessageParser();
      messageParser.parse(response);
      if (messageParser.isOkActionResponse())
      {
        // store out arguments
        KeyValueVector outArguments =
          CollectionHelper.getPersistentElementList(messageParser.getOutArgumentValueTable());
        for (int i = 0; i < outArguments.size(); i++)
        {
          Integer currentArgumentIDKey = (Integer)outArguments.getKey(i);
          byte[] currentArgumentValue = (byte[])outArguments.getValue(i);
          int currentArgumentID = currentArgumentIDKey.intValue();

          BinaryArgument currentArgument = getArgumentByID(currentArgumentID);
          if (currentArgument == null)
          {
            actionFailedException = new ActionFailedException(BinaryUPnPConstants.ResultTypeInvalidArgumentID, "");
            throw actionFailedException;
          }
          if (!currentArgument.trySetValueFromByteArray(currentArgumentValue))
          {
            actionFailedException = new ActionFailedException(BinaryUPnPConstants.ResultTypeInvalidArgumentValue, "");
            throw actionFailedException;
          }
        }
        return;
      }
      actionFailedException =
        BinaryUPnPConstants.createActionFailedException(BinaryUPnPConstants.ResultTypeNoResponseMessage);
      throw actionFailedException;
    } catch (Exception e)
    {
      Portable.println("Exception: " + e.getMessage());
    }
    if (actionFailedException != null)
    {
      throw actionFailedException;
    }
    throw new ActionFailedException(501, "Action failed");
  }

  /** Returns the byte array description for this action, but without the EndOfPacket tupel. */
  public byte[] toByteArrayForAction()
  {
    try
    {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

      // calculate action size
      for (int i = 0; i < argumentList.size(); i++)
      {
        BinaryArgument currentArgument = (BinaryArgument)argumentList.elementAt(i);
        if (currentArgument.isInArgument())
        {
          // add byte array description for in argument
          byteArrayOutputStream.write(currentArgument.toByteArrayForAction());
        }
      }
      byte[] argumentDescriptions = byteArrayOutputStream.toByteArray();
      byteArrayOutputStream.reset();

      long deviceID = getBinaryCPService().getBinaryCPDevice().getDeviceID();
      int serviceID = getBinaryCPService().getServiceID();
      // add prefix
      byte[] prefix =
        new byte[] {
            BinaryUPnPConstants.UnitTypeInvokeAction, 0, BinaryUPnPConstants.UnitTypeDeviceID, 4,
            (byte)(deviceID >> 24 & 0xFF), (byte)(deviceID >> 16 & 0xFF), (byte)(deviceID >> 8 & 0xFF),
            (byte)(deviceID >> 0 & 0xFF), BinaryUPnPConstants.UnitTypeServiceID, 1, (byte)(serviceID & 0xFF),
            BinaryUPnPConstants.UnitTypeActionContainer, (byte)(3 + argumentDescriptions.length),
            BinaryUPnPConstants.UnitTypeActionID, 1, (byte)(actionID & 0xFF),
        };
      // combine prefix and arguments
      byteArrayOutputStream.write(prefix);
      byteArrayOutputStream.write(argumentDescriptions);
      //      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeEndOfPacket);

      return byteArrayOutputStream.toByteArray();
    } catch (Exception e)
    {

    }
    return null;
  }

}
