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
package de.fraunhofer.fokus.upnp.core.examples.control_point.point_of_interest;

import java.awt.Image;

import de.fraunhofer.fokus.upnp.util.swing.SmoothButton;

/**
 * This class encapsulates one POI item.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class POIMessageEntry
{

  private String       genre;

  private String       title;

  private String       description;

  private String       infoURL;

  private String       imageURL;

  private Image        image;

  private float        longitude;

  private float        latitude;

  private SmoothButton button;

  /**
   * Creates a new instance of POIMessageEntry.
   * 
   * @param genre
   * @param title
   * @param description
   * @param infoURL
   * @param imageURL
   * @param longitude
   * @param latitude
   */
  public POIMessageEntry(String genre,
    String title,
    String description,
    String infoURL,
    String imageURL,
    float longitude,
    float latitude)
  {
    this.genre = genre;
    this.title = title;
    this.description = description;
    this.infoURL = infoURL;
    this.imageURL = imageURL;
    this.longitude = longitude;
    this.latitude = latitude;
    button = null;
  }

  /**
   * Retrieves the button.
   * 
   * @return The button
   */
  public SmoothButton getButton()
  {
    return button;
  }

  /**
   * Sets the button.
   * 
   * @param button
   *          The new value for button
   */
  public void setButton(SmoothButton button)
  {
    this.button = button;
  }

  /**
   * Retrieves the description.
   * 
   * @return The description
   */
  public String getDescription()
  {
    return description;
  }

  /**
   * Retrieves the genre.
   * 
   * @return The genre
   */
  public String getGenre()
  {
    return genre;
  }

  /**
   * Retrieves the imageURL.
   * 
   * @return The imageURL
   */
  public String getImageURL()
  {
    return imageURL;
  }

  /**
   * Retrieves the infoURL.
   * 
   * @return The infoURL
   */
  public String getInfoURL()
  {
    return infoURL;
  }

  /**
   * Retrieves the latitude.
   * 
   * @return The latitude
   */
  public float getLatitude()
  {
    return latitude;
  }

  /**
   * Retrieves the longitude.
   * 
   * @return The longitude
   */
  public float getLongitude()
  {
    return longitude;
  }

  /**
   * Retrieves the mapImage.
   * 
   * @return The mapImage
   */
  public Image getImage()
  {
    return image;
  }

  /**
   * Sets the image.
   * 
   * @param image
   *          The new value for image
   */
  public void setImage(Image image)
  {
    this.image = image;
  }

  /**
   * Retrieves the title.
   * 
   * @return The title
   */
  public String getTitle()
  {
    return title;
  }

}
