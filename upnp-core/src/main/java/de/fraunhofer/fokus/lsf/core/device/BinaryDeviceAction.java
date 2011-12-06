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
package de.fraunhofer.fokus.lsf.core.device;

import java.io.ByteArrayOutputStream;
import java.util.Vector;

import de.fraunhofer.fokus.lsf.core.BinaryUPnPConstants;
import de.fraunhofer.fokus.lsf.core.base.AbstractBinaryAction;
import de.fraunhofer.fokus.lsf.core.base.BinaryArgument;
import de.fraunhofer.fokus.upnp.util.StringHelper;

/**
 * This class encapsulates a local binary UPnP action.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class BinaryDeviceAction extends AbstractBinaryAction
{

  private BinaryDeviceService binaryDeviceService;

  /**
   * Creates a new instance of BinaryDeviceAction.java
   * 
   * @param actionName
   * @param actionID
   * @param argumentList
   */
  public BinaryDeviceAction(String actionName, int actionID, Vector argumentList)
  {
    super(actionName, actionID, argumentList);
  }

  /**
   * Retrieves the value of binaryDeviceService.
   * 
   * @return The value of binaryDeviceService
   */
  public BinaryDeviceService getBinaryDeviceService()
  {
    return binaryDeviceService;
  }

  /**
   * Sets the new value for binaryDeviceService.
   * 
   * @param binaryDeviceService
   *          The new value for binaryDeviceService
   */
  public void setBinaryDeviceService(BinaryDeviceService binaryDeviceService)
  {
    this.binaryDeviceService = binaryDeviceService;
  }

  /** Returns the byte array description for this action. */
  public byte[] toByteArrayForDescription()
  {
    try
    {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

      // calculate action size
      for (int i = 0; i < argumentList.size(); i++)
      {
        BinaryArgument currentArgument = (BinaryArgument)argumentList.elementAt(i);
        // add byte array description for argument
        byteArrayOutputStream.write(currentArgument.toByteArrayForDescription());
      }
      byte[] argumentDescriptions = byteArrayOutputStream.toByteArray();
      byte[] actionNameData = StringHelper.stringToByteArray(actionName);

      byteArrayOutputStream.reset();

      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeActionDescriptionContainer);
      byteArrayOutputStream.write(5 + actionNameData.length + argumentDescriptions.length);
      // add ID
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeActionID);
      byteArrayOutputStream.write(1);
      byteArrayOutputStream.write(actionID);
      // add name
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeActionName);
      byteArrayOutputStream.write(actionNameData.length);
      byteArrayOutputStream.write(actionNameData);
      // add all arguments
      byteArrayOutputStream.write(argumentDescriptions);

      return byteArrayOutputStream.toByteArray();
    } catch (Exception e)
    {

    }
    return null;
  }

}
