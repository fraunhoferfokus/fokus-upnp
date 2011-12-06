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
package de.fraunhofer.fokus.upnp.core.examples.sensors.internet_weather;

import java.net.URL;

import de.fraunhofer.fokus.lsf.core.IBinaryUPnPDevice;
import de.fraunhofer.fokus.upnp.core.device.Action;
import de.fraunhofer.fokus.upnp.core.device.DeviceService;
import de.fraunhofer.fokus.upnp.core.examples.sensors.SensorConstants;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateEntity;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;
import de.fraunhofer.fokus.upnp.soap.SOAPHeaderBuilder;
import de.fraunhofer.fokus.upnp.soap.SOAPMessageBuilder;
import de.fraunhofer.fokus.upnp.util.HighResTimerHelper;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.ThreadHelper;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;

/**
 * This class encapsulates temperature sensor UPnP devices.
 * 
 * @author Alexander Koenig
 */
public class InternetWeatherEntity extends TemplateEntity implements IBinaryUPnPDevice
{

  private TemplateDevice        internetWeatherDevice;

  /** Thread to regularly update the weather data */
  private InternetWeatherThread weatherThread;

  /** Creates a new instance of InternetTemperatureSensorEntity */
  public InternetWeatherEntity(UPnPStartupConfiguration startupConfiguration)
  {
    super(startupConfiguration);
    startupConfiguration = getStartupConfiguration();

    // request current weather data
    weatherThread = new InternetWeatherThread();

    // start weather device
    internetWeatherDevice =
      new InternetWeatherDevice(this,
        getStartupConfiguration(),
        this,
        weatherThread.getTemperatureProvider(),
        weatherThread.getWeatherProvider());

    // associated thread with appropriate device
    weatherThread.setInternetWeatherDevice(internetWeatherDevice);
    setTemplateDevice(internetWeatherDevice);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateEntity#terminate()
   */
  public void terminate()
  {
    weatherThread.terminate();
    super.terminate();
  }

  public static void main(String[] args)
  {
    InternetWeatherEntity internetWeatherEntity = new InternetWeatherEntity(null);
    testProcessingTime(internetWeatherEntity);
  }

  /** This method is used for performance evaluation, using SOAP caching or not */
  public static void testProcessingTime(InternetWeatherEntity internetWeatherEntity)
  {
    try
    {
      String serviceType = SensorConstants.TEMPERATURE_SENSOR_SERVICE_TYPE;
      DeviceService temperatureService = internetWeatherEntity.getTemplateDevice().getDeviceServiceByType(serviceType);

      Action tempAction = temperatureService.getAction("GetCurrentTemperature");

      String browseInnerBodyString = SOAPMessageBuilder.buildActionRequestInnerBody(serviceType, tempAction);
      String browseBodyString = SOAPMessageBuilder.buildEnvelope(browseInnerBodyString);

      byte[] browseBody = StringHelper.utf8StringToByteArray(browseBodyString);

      String browseHeader =
        SOAPHeaderBuilder.buildActionRequestHeader(new URL(temperatureService.getControlURL("localhost")),
          serviceType,
          tempAction,
          null,
          browseBody.length,
          false);

      HTTPMessageObject request =
        new HTTPMessageObject(browseHeader, browseBody, null, IPHelper.toSocketAddress("localhost:80"));

      int runCount = 1000;
      System.out.println("Wait for system to settle down");
      ThreadHelper.sleep(10000);

      System.out.println("Start uncached benchmark");
      tempAction.setCacheable(false);
      long time = System.currentTimeMillis();

      // time for the actual processing in microseconds. Removed from overall time to
      // get net time for XML parsing
      HighResTimerHelper.PERFORMANCE_CORRECTION = 0;

      for (int i = 0; i < runCount; i++)
      {
        internetWeatherEntity.getTemplateDevice().getHTTPMessageProcessor().processMessage(request);
      }
      long grossInvocationTime = System.currentTimeMillis() - time;
      long netInvocationTime = grossInvocationTime - HighResTimerHelper.PERFORMANCE_CORRECTION / 1000;
      System.out.println("Gross time for " + runCount + " runs is " + grossInvocationTime + " ms");
      System.out.println("Net time for " + runCount + " runs is " + netInvocationTime + " ms");

      System.out.println("Start cached benchmark");
      tempAction.setCacheable(true);
      // process one time to create cache entry
      internetWeatherEntity.getTemplateDevice().getHTTPMessageProcessor().processMessage(request);

      time = System.currentTimeMillis();

      // time for the actual processing in microseconds. Removed from overall time to
      // get net time for XML parsing
      HighResTimerHelper.PERFORMANCE_CORRECTION = 0;

      for (int i = 0; i < runCount; i++)
      {
        internetWeatherEntity.getTemplateDevice().getHTTPMessageProcessor().processMessage(request);
      }
      grossInvocationTime = System.currentTimeMillis() - time;
      netInvocationTime = grossInvocationTime - HighResTimerHelper.PERFORMANCE_CORRECTION / 1000;
      System.out.println("Gross time for " + runCount + " runs is " + grossInvocationTime + " ms");
      System.out.println("Net time for " + runCount + " runs is " + netInvocationTime + " ms");

    } catch (Exception e)
    {
      System.out.println("Error: " + e.getMessage());
    }
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.core.IBinaryUPnPDevice#getApplication()
   */
  public String getApplication()
  {
    return "Outdoors";
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.core.IBinaryUPnPDevice#getName()
   */
  public String getName()
  {
    return "InternetWeather";
  }

}
