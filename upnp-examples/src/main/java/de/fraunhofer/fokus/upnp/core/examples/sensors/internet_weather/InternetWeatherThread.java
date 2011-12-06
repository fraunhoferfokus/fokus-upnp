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

import java.net.InetSocketAddress;
import java.util.StringTokenizer;

import org.xml.sax.SAXException;

import de.fraunhofer.fokus.upnp.core.device.interfaces.ITemperatureProvider;
import de.fraunhofer.fokus.upnp.core.examples.sensors.SensorConstants;
import de.fraunhofer.fokus.upnp.core.examples.sensors.temperature.TemperatureSensor;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;
import de.fraunhofer.fokus.upnp.http.HTTPClient;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.SAXTemplateHandler;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.ThreadHelper;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;

public class InternetWeatherThread extends Thread
{

  private String              condition;

  private String              airPressure;

  private String              airHumidity;

  private String              sunrise;

  private String              sunset;

  private String              temperature;

  private TemperatureProvider temperatureProvider;

  private WeatherProvider     weatherProvider;

  private boolean             terminateThread = false;

  private boolean             terminated      = false;

  private long                lastRequestTime = 0;

  /** Associated services */
  private TemperatureSensor   temperatureSensor;

  private WeatherService      weatherService;

  /**
   * Creates a new instance of InternetWeatherThread.
   * 
   */
  public InternetWeatherThread()
  {
    temperatureProvider = new TemperatureProvider();
    weatherProvider = new WeatherProvider();
    start();
  }

  public void run()
  {
    while (!terminateThread)
    {
      if (System.currentTimeMillis() - lastRequestTime > 600000)
      {
        lastRequestTime = System.currentTimeMillis();
        try
        {
          WeatherParser parser = generateRequest();
          // update data from parser
          condition = parser.getCondition();
          airPressure = parser.getAirPressure();
          airHumidity = parser.getAirHumidity();
          sunrise = parser.getSunrise();
          sunset = parser.getSunset();
          temperature = parser.getTemperature();

          updateUPnPServices();
        } catch (Exception e)
        {
          System.out.println("Generate Request failed");
        }
      }
      ThreadHelper.sleep(1000);
    }
    terminated = true;
  }

  public void terminate()
  {
    // prevent further state variable changes
    terminateThread = true;
    while (!terminated)
    {
      ThreadHelper.sleep(50);
    }
  }

  public WeatherParser generateRequest() throws ActionFailedException
  {

    HTTPClient httpClient = new HTTPClient();

    String requestHeader = "GET /weather/local/GMXX0007?unit=m&cc=* HTTP/1.1" + CommonConstants.NEW_LINE;
    requestHeader += "Host: xoap.weather.com" + CommonConstants.NEW_LINE;

    HTTPMessageObject request = new HTTPMessageObject(requestHeader);

    request.setDestinationAddress(new InetSocketAddress("xoap.weather.com", 80));
    httpClient.sendRequestAndWaitForResponse(request);

    if (httpClient.getResponse() == null || httpClient.getResponse().getHeader() == null ||
      httpClient.getResponse().getBody() == null)
    {
      throw new ActionFailedException(801, "Server not reachable");
    }
    String response = httpClient.getResponse().getBodyAsString();

    WeatherParser parser = new WeatherParser();
    try
    {
      parser.parse(response);
    } catch (SAXException e)
    {
      System.out.println("while data was parsing a error occured");
    }

    return parser;
  }

  /** This class is responsible for parsing the weather data */
  private class WeatherParser extends SAXTemplateHandler
  {

    private String airPressure;

    private String airHumidity;

    private String sunrise;

    private String sunset;

    private String temperature;

    private String condition;

    /*
     * (non-Javadoc)
     * 
     * @see de.fhg.fokus.magic.upnp.xml.SAXTemplateHandler#processContentElement(java.lang.String)
     */
    public void processContentElement(String content)
    {

      if (getTagCount() == 4 && getCurrentTag().equalsIgnoreCase("r"))
      {
        airPressure = content;
      }

      if (getTagCount() == 3 && getCurrentTag().equalsIgnoreCase("hmid"))
      {
        airHumidity = content;
      }

      if (getTagCount() == 3 && getCurrentTag().equalsIgnoreCase("sunr"))
      {
        sunrise = convertTimeFormat(content);
      }

      if (getTagCount() == 3 && getCurrentTag().equalsIgnoreCase("suns"))
      {
        sunset = convertTimeFormat(content);
      }

      if (getTagCount() == 3 && getCurrentTag().equalsIgnoreCase("tmp"))
      {
        temperature = content;
      }

      if (getTagCount() == 3 && getCurrentTag().equalsIgnoreCase("t"))
      {
        condition = content;
      }
    }

    public String getCondition()
    {
      return condition;
    }

    public String getAirPressure()
    {
      return airPressure;
    }

    public String getAirHumidity()
    {
      return airHumidity;
    }

    public String getSunrise()
    {
      return sunrise;
    }

    public String getSunset()
    {
      return sunset;
    }

    public String getTemperature()
    {
      return temperature;
    }

    /**
     * Converts the american time format to european time format.
     * 
     * @param time
     * @return converted time
     */
    public String convertTimeFormat(String time)
    {
      String newTimeFormatAsString;
      StringTokenizer timeSeparator = new StringTokenizer(time, " ");
      Object[] parties = new Object[2];
      int index = 0;

      while (timeSeparator.hasMoreElements())
      {
        parties[index] = timeSeparator.nextElement();
        index++;
      }

      String hoursAndMinutes = parties[0].toString();
      String appendant = parties[1].toString();

      if (appendant.matches("PM"))
      {
        int i = 0;
        StringTokenizer timeDecomposer = new StringTokenizer(hoursAndMinutes, ":");
        Object[] timeParties = new Object[2];

        while (timeDecomposer.hasMoreElements())
        {
          timeParties[i] = timeDecomposer.nextElement();
          i++;
        }

        String hours = timeParties[0].toString();
        String minutes = timeParties[1].toString();

        Integer hoursInteger = new Integer(hours);
        int hoursInt = hoursInteger.intValue() + 12;
        Integer newTimeFormatAsInteger = new Integer(hoursInt);
        newTimeFormatAsString = newTimeFormatAsInteger.toString() + ":" + minutes;

        return newTimeFormatAsString;

      } else
      {
        return hoursAndMinutes;
      }
    }
  }

  private class TemperatureProvider implements ITemperatureProvider
  {

    /*
     * (non-Javadoc)
     * 
     * @see de.fhg.fokus.magic.upnp.examples.sensors.ITemperatureProvider#getApplication()
     */
    public String getApplication()
    {
      return "WeatherProvider";
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fhg.fokus.magic.upnp.examples.sensors.ITemperatureProvider#getName()
     */
    public String getName()
    {
      return "Internet Weather";
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fhg.fokus.magic.upnp.examples.sensors.ITemperatureProvider#getTemperature()
     */
    public int getTemperature()
    {
      return StringHelper.stringToIntegerDef(temperature, 0) * 100;
    }

  }

  private class WeatherProvider implements IWeatherProvider
  {

    /*
     * (non-Javadoc)
     * 
     * @see de.fhg.fokus.magic.upnp.examples.sensors.internet.IWeatherProvider#getAirHuminity()
     */
    public String getAirHumidity()
    {
      return airHumidity;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fhg.fokus.magic.upnp.examples.sensors.internet.IWeatherProvider#getAirPressure()
     */
    public String getAirPressure()
    {
      return airPressure;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fhg.fokus.magic.upnp.examples.sensors.internet.IWeatherProvider#getSunRise()
     */
    public String getSunrise()
    {
      return sunrise;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fhg.fokus.magic.upnp.examples.sensors.internet.IWeatherProvider#getSunSet()
     */
    public String getSunset()
    {
      return sunset;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fhg.fokus.magic.upnp.examples.sensors.internet.IWeatherProvider#getTemperature()
     */
    public int getTemperature()
    {
      return StringHelper.stringToIntegerDef(temperature, 0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fhg.fokus.magic.upnp.examples.sensors.internet.IWeatherProvider#getTemperature()
     */
    public String getCondition()
    {
      return condition;

    }
  }

  public WeatherProvider getWeatherProvider()
  {
    return weatherProvider;
  }

  public TemperatureProvider getTemperatureProvider()
  {
    return temperatureProvider;
  }

  /**
   * Sets the internetWeatherDevice.
   * 
   * @param internetWeatherDevice
   *          The new value for internetWeatherDevice
   */
  public void setInternetWeatherDevice(TemplateDevice internetWeatherDevice)
  {
    this.temperatureSensor =
      (TemperatureSensor)internetWeatherDevice.getDeviceServiceByType(SensorConstants.TEMPERATURE_SENSOR_SERVICE_TYPE);
    this.weatherService =
      (WeatherService)internetWeatherDevice.getDeviceServiceByType(SensorConstants.WEATHER_SERVICE_TYPE);

    updateUPnPServices();
  }

  /** Sends the new values to the temperature and weather services. */
  private void updateUPnPServices()
  {
    if (temperatureSensor != null)
    {
      // TemperatureSensor holds temperature in 0.01 °C
      temperatureSensor.temperatureChanged(StringHelper.stringToIntegerDef(temperature, 0) * 100);
    }
    if (weatherService != null)
    {
      weatherService.conditionChanged(condition);
      weatherService.airHumidityChanged(airHumidity);
      weatherService.airPressureChanged(airPressure);
      weatherService.sunriseTimeChanged(sunrise);
      weatherService.sunsetTimeChanged(sunset);
      weatherService.temperatureChanged(StringHelper.stringToIntegerDef(temperature, 0));
    }
  }
}
