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
package de.fraunhofer.fokus.upnp.core;

/**
 * This class represent the structure of icon element in the UPnP device description.
 * 
 * @author icu, Alexander Koenig
 * 
 */
public class DeviceIcon
{
  /**
   * mime type of the icon
   */
  private String mimetype;

  /**
   * width of the icon
   */
  private int    width;

  /**
   * height of the icon
   */
  private int    height;

  /**
   * depth of the icon
   */
  private int    depth;

  /**
   * URL of the icon
   */
  private String url;

  /**
   * Creates a device icon object.
   * 
   * @param mimetype
   *          mime type of the icon (e.g. bmp, jpeg etc.)
   * @param width
   *          width of the icon
   * @param height
   *          height of the icon
   * @param depth
   *          color depth of the icon
   * @param url
   *          the URL to get the icon
   */
  public DeviceIcon(String mimetype, int width, int height, int depth, String url)
  {
    this.mimetype = mimetype;
    this.width = width;
    this.height = height;
    this.depth = depth;
    this.url = url;
  }

  /**
   * Returns icon mime type
   * 
   * @return the mime type of the icon
   */
  public String getMimetype()
  {
    return mimetype;
  }

  /**
   * Return width
   * 
   * @return the width of the icon
   */
  public int getWidth()
  {
    return width;
  }

  /**
   * Returns height
   * 
   * @return the height of the icon
   */
  public int getHeight()
  {
    return height;
  }

  /**
   * Returns color depth
   * 
   * @return the color depth of the icon
   */
  public int getDepth()
  {
    return depth;
  }

  /**
   * Retrieves the url.
   * 
   * @return The url
   */
  public String getURL()
  {
    return url;
  }

}
