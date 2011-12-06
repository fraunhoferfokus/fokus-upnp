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
package de.fraunhofer.fokus.upnp.util.gps;

import java.awt.geom.Point2D;

/** This class provides helper methods for GPS. */
public class GPSHelper
{

  /** Average earth radius in meters */
  public static double EARTH_RADIUS = 6372795;

  /**
   * Retrieves the distance in meters between two points.
   * 
   * 
   * @param centerLongitude
   *          First longitude in degrees
   * @param centerLatitude
   *          First latitude in degrees
   * @param pointLongitude
   *          Second longitude in degrees
   * @param pointLatitude
   *          Second latitude in degrees
   * 
   * @return The distance in meters between both points
   */
  public static double getDistance(double centerLongitude,
    double centerLatitude,
    double pointLongitude,
    double pointLatitude)
  {
    double centerLongitudeRadians = centerLongitude * Math.PI / 180.0;
    double centerLatitudeRadians = centerLatitude * Math.PI / 180.0;
    double pointLongitudeRadians = pointLongitude * Math.PI / 180.0;
    double pointLatitudeRadians = pointLatitude * Math.PI / 180.0;

    double longitudeDifferenceRadians = pointLongitudeRadians - centerLongitudeRadians;

    // use great circle distance
    double circleDistResult =
      Math.acos(Math.sin(centerLatitudeRadians) * Math.sin(pointLatitudeRadians) + Math.cos(centerLatitudeRadians) *
        Math.cos(pointLatitudeRadians) * Math.cos(longitudeDifferenceRadians)) *
        EARTH_RADIUS;

    return circleDistResult;
  }

  /**
   * Retrieves the distance between two latitudes.
   * 
   * @param centerLatitude
   * @param pointLatitude
   * @return
   */
  public static double getLatitudeDistance(double centerLatitude, double pointLatitude)
  {
    double difference = Math.abs(centerLatitude - pointLatitude);

    // calculate length of circle cord
    double circleCord = difference * EARTH_RADIUS * Math.PI / 180.0;

    // calculate length of circle arc from radius and circle cord
    double circleArc = 2 * EARTH_RADIUS * Math.asin(circleCord / (2 * EARTH_RADIUS));

    return circleArc;
  }

  /**
   * Returns a 2D vector between two GPS coordinates.
   * 
   * @param centerLongitude
   * @param centerLatitude
   * @param pointLongitude
   * @param pointLatitude
   * 
   * @return
   */
  public static Point2D.Double getVector(double centerLongitude,
    double centerLatitude,
    double pointLongitude,
    double pointLatitude)
  {
    // calculate distance
    double distance = getDistance(centerLongitude, centerLatitude, pointLongitude, pointLatitude);

    // calculate y distance
    double latitudeDistance = getLatitudeDistance(centerLatitude, pointLatitude);

    // calculate x distance (using pythagoras, will lead to errors for larger distances)
    double longitudeDistance = Math.sqrt(distance * distance - latitudeDistance * latitudeDistance);

    return new Point2D.Double(longitudeDistance, latitudeDistance);
  }

  /**
   * Checks if a point lies within a radius around a certain position.
   * 
   * 
   * @param centerLongitude
   * @param centerLatitude
   * @param pointLongitude
   * @param pointLatitude
   * @param radius
   * 
   * @return
   */
  public static boolean isPointInCircle(double centerLongitude,
    double centerLatitude,
    double pointLongitude,
    double pointLatitude,
    double radius)
  {
    return getDistance(centerLongitude, centerLatitude, pointLongitude, pointLatitude) < radius;
  }

}
