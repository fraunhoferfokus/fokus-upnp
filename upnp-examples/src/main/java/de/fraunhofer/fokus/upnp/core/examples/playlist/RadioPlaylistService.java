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
package de.fraunhofer.fokus.upnp.core.examples.playlist;

import java.net.InetSocketAddress;
import java.util.Vector;

import org.xml.sax.SAXException;

import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.DeviceConstant;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.device.Action;
import de.fraunhofer.fokus.upnp.core.device.StateVariable;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateService;
import de.fraunhofer.fokus.upnp.http.HTTPClient;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.DateTimeHelper;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.XMLHelper;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.threads.IEventListener;

/**
 * 
 * @author Sebastian Nauck, Alexander Koenig
 * 
 */

public class RadioPlaylistService extends TemplateService implements IEventListener
{

  private Action        getPlaylist;

  private Action        getCurrentSong;

  private StateVariable A_ARG_TYPE_string;

  private StateVariable currentArtist;

  private StateVariable currentTitle;

  private long          lastRequest;

  private Vector        currentPlaylist;

  /**
   * Creates a new instance of PlayListService.
   * 
   * @param device
   */
  public RadioPlaylistService(TemplateDevice device)
  {
    super(device, DeviceConstant.RADIO_PLAYLIST_SERVICE_TYPE, DeviceConstant.RADIO_PLAYLIST_SERVICE_ID);
    device.getDeviceEventThread().register(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.threads.IEventListener#triggerEvents()
   */
  public void triggerEvents()
  {
    if (System.currentTimeMillis() - lastRequest > 120000)
    {
      lastRequest = System.currentTimeMillis();
      requestPlaylist();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.core.templates.TemplateService#initServiceContent()
   */
  public void initServiceContent()
  {

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // State variables // //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    A_ARG_TYPE_string = new StateVariable("A_ARG_TYPE_string", "", false);
    currentArtist = new StateVariable("CurrentArtist", "", true);
    currentTitle = new StateVariable("CurrentTitle", "", true);

    StateVariable[] stateVariableList = {
        A_ARG_TYPE_string, currentArtist, currentTitle
    };
    setStateVariableTable(stateVariableList);

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // Actions //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////

    getPlaylist = new Action("GetPlaylist");
    getPlaylist.setArgumentTable(new Argument[] {
      new Argument("Playlist", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_string)
    });
    getCurrentSong = new Action("GetCurrentSong");
    getCurrentSong.setArgumentTable(new Argument[] {
        new Argument("Artist", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_string),
        new Argument("Title", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_string)
    });

    setActionTable(new Action[] {
        getPlaylist, getCurrentSong
    });
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Action implementation //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  public void getPlaylist(Argument[] args) throws ActionFailedException
  {
    if (args.length != 1)
    {
      throw new ActionFailedException(402, "Invalid args");
    }

    try
    {
      if (currentPlaylist == null)
      {
        requestPlaylist();
      }
      String result = "<List>";
      for (int i = 0; i < currentPlaylist.size(); i++)
      {
        PlaylistEntry currentEntry = (PlaylistEntry)currentPlaylist.get(i);

        result += XMLHelper.createStartTag("Entry");
        result += XMLHelper.createTag("Time", DateTimeHelper.formatDateForUPnP(currentEntry.getTime()));
        result += XMLHelper.createTag("Artist", currentEntry.getArtist());
        result += XMLHelper.createTag("Title", currentEntry.getTitle());
        result += XMLHelper.createEndTag("Entry");
      }
      result += "</List>";
      args[0].setValue(StringHelper.xmlToEscapedString(result));
    } catch (Exception e)
    {
      System.out.println(e.getMessage());
      throw new ActionFailedException(402, "invalid args");
    }
  }

  public void getCurrentSong(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    try
    {
      if (currentPlaylist == null)
      {
        requestPlaylist();
      }
      if (currentPlaylist.size() < 1)
      {
        throw new ActionFailedException(701, "Playlist not available");
      }
      PlaylistEntry currentEntry = (PlaylistEntry)currentPlaylist.get(0);
      args[0].setValue(currentEntry.getArtist());
      args[1].setValue(currentEntry.getTitle());
    } catch (Exception e)
    {
      System.out.println(e.getMessage());
      throw new ActionFailedException(402, "invalid args");
    }
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Private methods //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////

  private void requestPlaylist()
  {
    currentPlaylist = loadPlayList();

    if (currentPlaylist.size() > 0)
    {
      PlaylistEntry currentEntry = (PlaylistEntry)currentPlaylist.elementAt(0);
      // if last song play time is shorter than 10 minutes, assume it is still playing
      try
      {
        if (System.currentTimeMillis() - currentEntry.getTime().getTime() < 600000)
        {
          currentArtist.setValue(currentEntry.getArtist());
          currentTitle.setValue(currentEntry.getTitle());
        } else
        {
          currentArtist.setValue("");
          currentTitle.setValue("");
        }
      } catch (Exception e)
      {
      }
    }
  }

  /**
   * Loads the HTML page from radioeins.funtip.de and parses it.
   * 
   */
  private Vector loadPlayList()
  {
    HTTPClient httpClient = new HTTPClient();

    String requestHeader = "GET /playList.do HTTP/1.1" + CommonConstants.NEW_LINE;
    requestHeader += "Host: radioeins.funtip.de" + CommonConstants.NEW_LINE;

    HTTPMessageObject request = new HTTPMessageObject(requestHeader);

    request.setDestinationAddress(new InetSocketAddress("radioeins.funtip.de", 80));
    httpClient.sendRequestAndWaitForResponse(request);
    byte[] response = httpClient.getResponse().getBody();

    if (response == null || response.length == 0)
    {
      return new Vector();
    }
    String result = StringHelper.byteArrayToUTF8String(response);

    // rip site apart
    int startIndex = result.indexOf("table id=\"tabelle_01\"");
    if (startIndex != -1)
    {
      result = result.substring(startIndex, result.length());
    }

    // search first tbody
    startIndex = result.indexOf("<tbody>");
    if (startIndex != -1)
    {
      result = result.substring(startIndex, result.length());
    }

    // search end of tbody
    int endIndex = result.indexOf("</tbody>");
    if (endIndex != -1)
    {
      result = result.substring(0, endIndex + "</tbody>".length());
    }

    result = result.replaceAll("&#039;", "'");
    result = result.replaceAll("&nbsp;", " ");
    // remove empty lines
    result = result.replaceAll("[\r\n]+\\s*[\r\n]+", "\r\n");
    // remove spaces
    result = result.replaceAll("[\r\n]+\\s*<", "\r\n<");

    // remove link to ram-file
    result = result.replaceAll("<td><a.*</td>", "");
    // remove unneeded links
    result = result.replaceAll("<td style.*</td>", "");

    // System.out.println("Page is " + result);

    RadioEinsPlaylistParser parser = new RadioEinsPlaylistParser();
    try
    {
      parser.parse(result);
    } catch (SAXException e)
    {
      System.out.println(e.getMessage());
    }
    return parser.getPlaylist();
  }
}
