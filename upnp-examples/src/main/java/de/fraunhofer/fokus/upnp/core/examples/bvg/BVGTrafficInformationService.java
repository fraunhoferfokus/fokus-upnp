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
package de.fraunhofer.fokus.upnp.core.examples.bvg;

import java.net.InetSocketAddress;
import java.util.StringTokenizer;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.device.Action;
import de.fraunhofer.fokus.upnp.core.device.StateVariable;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateService;
import de.fraunhofer.fokus.upnp.http.HTTPClient;
import de.fraunhofer.fokus.upnp.http.HTTPMessageBuilder;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;

/**
 * This class implements a BVG traffic information service.
 * 
 * @author Alexander Koenig
 */
public class BVGTrafficInformationService extends TemplateService
{

  private StateVariable                  A_ARG_TYPE_stop;

  private StateVariable                  A_ARG_TYPE_result;

  private Action                         getStopInfo;

  //  private BVGWebsiteParser               bvgWebsiteParser;

  protected BVGTrafficInformationService service;

  /** Creates a new instance of BVGTrafficInformationService */
  public BVGTrafficInformationService(TemplateDevice device)
  {
    super(device,
      BVGTrafficInformationConstant.BVG_TRAFFIC_INFORMATION_SERVICE_TYPE,
      BVGTrafficInformationConstant.BVG_TRAFFIC_INFORMATION_SERVICE_ID);
  }

  public void setupServiceVariables()
  {
    super.setupServiceVariables();
    service = this;
    //    bvgWebsiteParser = new BVGWebsiteParser();
  }

  /** Initializes service specific properties */
  public void initServiceContent()
  {
    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // State variables //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    A_ARG_TYPE_stop = new StateVariable("A_ARG_TYPE_stop", "ui4", 0, false);
    A_ARG_TYPE_result = new StateVariable("A_ARG_TYPE_result", "", false);

    StateVariable[] stateVariableList = {
        A_ARG_TYPE_stop, A_ARG_TYPE_result
    };
    setStateVariableTable(stateVariableList);

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // Actions //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    getStopInfo = new Action("GetStopInfo");
    getStopInfo.setArgumentTable(new Argument[] {
        new Argument("StopID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_stop),
        new Argument("Result", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_result)
    });

    Action[] actionList = {
      getStopInfo
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
  public void getStopInfo(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    int stopID = -1;
    try
    {
      stopID = (int)args[0].getNumericValue();
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }

    // generate request to web site
    HTTPClient httpClient = new HTTPClient();

    HTTPMessageObject request =
      new HTTPMessageObject(HTTPMessageBuilder.createGETRequest("/IstAbfahrtzeiten/index/wap?input=" + stopID,
        "www.fahrinfo-berlin.de",
        80,
        null));

    request.setDestinationAddress(new InetSocketAddress("www.fahrinfo-berlin.de", 80));

    httpClient.sendRequestAndWaitForResponse(request);

    if (httpClient.getResponse() == null || httpClient.getResponse().getHeader() == null ||
      httpClient.getResponse().getBody() == null)
    {
      throw new ActionFailedException(801, "Server not reachable");
    }
    // remove ballast
    String response = httpClient.getResponse().getBodyAsString();

    if (response.indexOf("</anchor>") != -1)
    {
      response = response.substring(response.indexOf("</anchor>") + "</anchor>".length());
    }
    if (response.indexOf("<do type") != -1)
    {
      response = response.substring(0, response.indexOf("<do type"));
    }
    response = response.replaceAll("<small>&#160;</small>", " ");
    response = response.replaceAll("&#160;", " ");
    response = response.replaceAll("&#x20;", " ");
    response = response.replaceAll("<br />", " ");
    response = response.replaceAll("[ ]{2,}", " ");

    Vector timetableList = new Vector();

    StringTokenizer tokenizer = new StringTokenizer(response, "\r\n");
    String time = "";
    while (tokenizer.hasMoreTokens())
    {
      String token = tokenizer.nextToken().trim();
      if (token.length() > 0)
      {
        if (time.length() == 0)
        {
          time = token;
        } else
        {
          if (token.length() > 6)
          {
            int spaceIndex = token.indexOf(' ', 5);
            if (spaceIndex != -1)
            {
              String line = token.substring(0, spaceIndex);
              String direction = token.substring(spaceIndex + 1);
              timetableList.add(new TimetableEntry(time, line, direction));
            }
          }
          time = "";
        }
      }
    }

    String result = "<TrafficInfo>";
    for (int i = 0; i < timetableList.size(); i++)
    {
      TimetableEntry currentEntry = (TimetableEntry)timetableList.elementAt(i);
      result +=
        "<Vehicle line=\"" + currentEntry.line + "\" direction=\"" + currentEntry.direction + "\">" +
          currentEntry.time + "</Vehicle>";
    }
    result += "</TrafficInfo>";
    try
    {
      args[1].setValue(StringHelper.xmlToEscapedString(result));

      return;
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  private class TimetableEntry
  {
    public String time;

    public String line;

    public String direction;

    /**
     * Creates a new instance of TimetableEntry.
     * 
     * @param time
     * @param line
     * @param direction
     */
    public TimetableEntry(String time, String line, String direction)
    {
      super();
      this.time = time;
      this.line = line;
      this.direction = direction;
    }

  }

  //  private class BVGWebsiteParser extends SAXTemplateHandler
  //  {
  //
  //    public Vector timetableList = new Vector();
  //
  //    public String time;
  //
  //    public String line;
  //
  //    public String direction;
  //
  //    public int    entryCount    = 0;
  //
  //    public void processStartElement(String uri, String name, String name2, Attributes atts) throws SAXException
  //    {
  //      if (getTagCount() == 1)
  //      {
  //        timetableList.clear();
  //      }
  //      if (getTagCount() == 2)
  //      {
  //        time = null;
  //        line = null;
  //        direction = null;
  //        entryCount = 0;
  //      }
  //    }
  //
  //    /*
  //     * (non-Javadoc)
  //     * 
  //     * @see de.fraunhofer.fokus.upnp.util.SAXTemplateHandler#processContentElement(java.lang.String)
  //     */
  //    public void processContentElement(String content) throws SAXException
  //    {
  //      if (getTagCount() == 3)
  //      {
  //        if (entryCount == 0)
  //        {
  //          time = content.trim();
  //        }
  //        if (entryCount == 1)
  //        {
  //          line = content.trim();
  //        }
  //        entryCount++;
  //      }
  //      if (getTagCount() == 4)
  //      {
  //        direction = content.trim();
  //      }
  //
  //    }
  //
  //    /*
  //     * (non-Javadoc)
  //     * 
  //     * @see de.fraunhofer.fokus.upnp.util.SAXTemplateHandler#processEndElement(java.lang.String,
  //     *      java.lang.String, java.lang.String)
  //     */
  //    public void processEndElement(String uri, String localName, String name) throws SAXException
  //    {
  //      if (getTagCount() == 2 && time != null && line != null && direction != null)
  //      {
  //        timetableList.add(new TimetableEntry(time, line, direction));
  //      }
  //    }
  //  }

}
