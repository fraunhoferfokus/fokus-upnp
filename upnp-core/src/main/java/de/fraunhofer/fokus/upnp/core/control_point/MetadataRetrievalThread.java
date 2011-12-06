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

import de.fraunhofer.fokus.upnp.core.DeviceConstant;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.UPnPDoc;
import de.fraunhofer.fokus.upnp.core.templates.TemplateControlPoint;
import de.fraunhofer.fokus.upnp.core.templates.TemplateService;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;

/**
 * This class is used to retrieve additional infos found in common device services.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class MetadataRetrievalThread extends Thread
{

  private TemplateControlPoint controlPoint;

  private CPDevice             device;

  private CPService            attributeService;

  private CPService            translationService;

  private CPService            usageService;

  public MetadataRetrievalThread(TemplateControlPoint controlPoint, CPDevice device)
  {
    this.controlPoint = controlPoint;
    this.device = device;
    attributeService = device.getCPServiceByType(DeviceConstant.ATTRIBUTE_SERVICE_TYPE);
    translationService = device.getCPServiceByType(DeviceConstant.TRANSLATION_SERVICE_TYPE);
    usageService = device.getCPServiceByType(DeviceConstant.USAGE_SERVICE_TYPE);

    start();
  }

  public void run()
  {
    if (attributeService != null)
    {
      CPAction action = attributeService.getCPAction("GetAttributeList");
      if (action != null)
      {
        try
        {
          controlPoint.invokeAction(action);

          String attributes = action.getArgument("Result").getStringValue();

          AttributeParser attributeParser = new AttributeParser();
          attributeParser.parse(attributes);
          // link information to device
          device.setAttributeHashtable(attributeParser.getAttributeTable());

          // store information in cache
          if (attributeParser.getAttributeTable().size() > 0)
          {
            TemplateService.printMessage(translationService.toFriendlyNameString() + ": " +
              attributeParser.getAttributeTable().size() + " attributes read");

            if (controlPoint.getBasicControlPoint().getCPDeviceCache() != null)
            {
              controlPoint.getBasicControlPoint().getCPDeviceCache().storeAttributeServiceInformation(device,
                attributeParser.getAttributeTable());
            }
            // generate event for control point
            controlPoint.deviceEvent(device, UPnPConstant.DEVICE_EVENT_ATTRIBUTE_SERVICE_READ, null);
          }
        } catch (ActionFailedException afe)
        {
          System.out.println("AttributeServiceRetrieval: An error occured:" + afe.getMessage());
        } catch (Exception ex)
        {
          System.out.println("AttributeServiceRetrieval: An error occured:" + ex.getMessage());
        }
      }
    }
    if (translationService != null)
    {
      CPAction action = translationService.getCPAction("GetTranslationList");
      if (action != null)
      {
        try
        {
          action.getArgument("Language").setValue("de");
          controlPoint.invokeAction(action);

          String translations = action.getArgument("Result").getStringValue();

          TranslationParser translationParser = new TranslationParser();
          translationParser.parse(translations);

          TemplateService.printMessage(translationService.toFriendlyNameString() + ": " +
            translationParser.getTranslationTable().size() + " translations read");

          // link information to device
          device.setTranslationTable(translationParser.getTranslationTable());

          // generate event for control point
          controlPoint.deviceEvent(device, UPnPConstant.DEVICE_EVENT_TRANSLATION_SERVICE_READ, null);

        } catch (ActionFailedException afe)
        {
          System.out.println("TranslationServiceRetrieval: An error occured:" + afe.getMessage());
        } catch (Exception ex)
        {
          System.out.println("TranslationServiceRetrieval: An error occured:" + ex.getMessage());
        }
      }
    }
    if (usageService != null)
    {
      CPAction action = usageService.getCPAction("GetUPnPDocList");
      if (action != null)
      {
        try
        {
          action.getArgument("Language").setValue("de");
          controlPoint.invokeAction(action);

          String content = action.getArgument("Result").getStringValue();

          // System.out.println("Content is " + content);

          UPnPDoc upnpDoc = new UPnPDoc("de");
          upnpDoc.addUPnPDocFromString(content);

          device.getUPnPDocFromLanguageTable().put("de", upnpDoc);
        } catch (ActionFailedException afe)
        {
          System.out.println("UsageServiceRetrieval: An error occured:" + afe.getMessage());
        } catch (Exception ex)
        {
          System.out.println("UsageServiceRetrieval: An error occured:" + ex.getMessage());
        }
      }
    }
    controlPoint.metadataRetrievalFinished(this);
  }

}
