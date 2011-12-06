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

import org.apache.log4j.Logger;

/**
 * This class represent the structure of allowedValueRange element in the UPnP service description
 * 
 * @author tje, Alexander Koenig
 * @version 1.0
 */
public class AllowedValueRange
{

  /**
   * UPnP logger
   */
  private static Logger logger = Logger.getLogger("upnp");

  /**
   * minimal value of argument
   */
  private double        min;

  /**
   * maximal value of argument
   */
  private double        max;

  /**
   * step value between min and max value of argument
   */
  private double        step   = 0;

  /**
   * Creates an AllowedValueRange object with lower bound and upper bound.
   * 
   * @param min
   *          lower bound
   * @param max
   *          upper bound
   */
  public AllowedValueRange(double min, double max)
  {
    this.min = min;
    this.max = max;
  }

  /**
   * Creates an AllowedValueRange object with lower bound,upper bound and size of increment
   * operation.
   * 
   * @param min
   *          lower bound
   * @param max
   *          upper bound
   * @param step
   *          size of increment operation
   */
  public AllowedValueRange(double min, double max, double step)
  {
    this(min, max);
    this.step = step;
  }

  /**
   * Returns lower bound.
   * 
   * @return lower bound
   */
  public double getMin()
  {
    return min;
  }

  /**
   * Returns upper bound.
   * 
   * @return upper bound
   */
  public double getMax()
  {
    return max;
  }

  /**
   * Sets a new size of increment operation.
   * 
   * @param step
   *          size of increment operation
   */
  public void setStep(double step)
  {
    this.step = step;
  }

  /**
   * Tests if size of increment operation defined.
   * 
   * @return true if size of increment operation defined, false otherwise
   */
  public boolean hasStep()
  {
    if (Math.abs(step) > 0.00000001)
    {
      return true;
    }

    return false;
  }

  /**
   * Returns size of increment operation.
   * 
   * @return size of increment operation
   */
  public double getStep()
  {
    return step;
  }

  /**
   * Tests if value is within lower bound and upper bound.
   * 
   * @param value
   *          A Double or Long object value
   * @return true if value is in range, false otherwise
   */
  public boolean isInRange(Object value)
  {
    // check if object is type double
    if (value instanceof Double)
    {
      Double newValue = (Double)value;

      if (newValue.doubleValue() >= min && newValue.doubleValue() <= max)
      {
        return true;
      }
      logger.warn("Value not in allowed value range: " + newValue.doubleValue() + " [" + min + "," + max + "]");
      return false;
    }

    // check if object is type long
    if (value instanceof Long)
    {
      Long newValue = (Long)value;

      if (newValue.longValue() >= min && newValue.longValue() <= max)
      {
        return true;
      }
      logger.warn("Value not in allowed value range: " + newValue.longValue() + " [" + min + "," + max + "]");
      return false;
    }

    logger.warn("Unknown object type:" + value.getClass().toString());
    return false;
  }

  /**
   * Tests if value is within lower bound and upper bound.
   * 
   * @param value
   *          The string representation of a value
   * @return true if value is in range, false otherwise
   */
  public boolean isInRange(String value)
  {
    // cast as double
    try
    {
      double val = Double.valueOf(value).doubleValue();

      if (val >= min && val <= max)
      {
        return true;
      }
    } catch (Exception e)
    {
      e.printStackTrace();
      logger.warn("value = " + value + " is not in range from " + min + " " + max);
    }

    logger.warn("value = " + value + " is not in range from " + min + " " + max);

    return false;
  }

  /**
   * Increments the value by size of increment. If new value > upper bound, new value = upper bound.
   * 
   * @param value
   *          A Double or Long object value
   * @return incremented value
   */
  public Object increment(Object value)
  {
    if (value instanceof Double)
    {
      return new Double(increment(((Double)value).doubleValue()));
    }
    if (value instanceof Long)
    {
      return new Long(increment(((Long)value).longValue()));
    }

    return value;
  }

  /**
   * Increments the value by size of increment. If new value > upper bound, new value = upper bound.
   * 
   * @param value
   *          value
   * @return incremented value
   */
  public double increment(double value)
  {
    value = value + step;

    if (value > max)
    {
      return max;
    } else
    {
      return value;
    }
  }

  /**
   * Increments the value by size of increment. If new value > upper bound, new value = upper bound.
   * 
   * @param value
   *          value
   * @return incremented value
   */
  public long increment(long value)
  {
    value = (long)(value + step);

    if (value > max)
    {
      return (long)max;
    } else
    {
      return value;
    }
  }

  /**
   * Decrements the value by a size of increment. if new value < lower bound, new value = lower
   * bound.
   * 
   * @param value
   *          A Double or Long object value
   * @return decremented value
   */
  public Object decrement(Object value)
  {
    if (value instanceof Double)
    {
      return new Double(decrement(((Double)value).doubleValue()));
    }
    if (value instanceof Long)
    {
      return new Long(decrement(((Long)value).longValue()));
    }

    return value;
  }

  /**
   * Decrements the value by a size of increment. if new value < lower bound, new value = lower
   * bound.
   * 
   * @param value
   *          value
   * @return decremented value
   */
  public double decrement(double value)
  {
    value = value - step;

    if (value < min)
    {
      return min;
    } else
    {
      return value;
    }
  }

  /**
   * Decrements the value by a size of increment. if new value < lower bound, new value = lower
   * bound.
   * 
   * @param value
   *          value
   * @return decremented value
   */
  public long decrement(long value)
  {
    value = (long)(value - step);

    if (value < min)
    {
      return (long)min;
    } else
    {
      return value;
    }
  }

}
