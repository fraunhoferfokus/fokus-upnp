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
package de.fraunhofer.fokus.upnp.core_av.control_point;

import java.util.Hashtable;

import de.fraunhofer.fokus.upnp.core.AllowedValueRange;
import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.control_point.CPAction;
import de.fraunhofer.fokus.upnp.core.control_point.CPService;
import de.fraunhofer.fokus.upnp.core.control_point.CPStateVariable;
import de.fraunhofer.fokus.upnp.core.templates.TemplateControlPoint;
import de.fraunhofer.fokus.upnp.core_av.UPnPAVConstant;

/**
 * This class represents a remote view on an RenderingControl service.
 * 
 * @author Alexander Koenig
 * 
 */
public class RenderingControlCPService implements ILastChangeEventListener
{

  private CPService                                cpService;

  private TemplateControlPoint                     controlPoint;

  private Hashtable                                hashtableFromInstanceIDHashtable = new Hashtable();

  private ICPRenderingControlStateVariableListener stateVariableListener;

  public RenderingControlCPService(MediaRendererCPDevice mediaRendererCPDevice,
    ICPRenderingControlStateVariableListener stateVariableListener)
  {
    this.cpService =
      mediaRendererCPDevice.getCPDevice().getCPServiceByType(UPnPAVConstant.RENDERING_CONTROL_SERVICE_TYPE);
    this.controlPoint = mediaRendererCPDevice.getTemplateControlPoint();
    this.stateVariableListener = stateVariableListener;
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Rendering control //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  public String listPresets(long instanceID)
  {
    CPAction action = cpService.getCPAction(UPnPAVConstant.ACTION_LIST_PRESETS);
    try
    {
      action.getInArgument(UPnPAVConstant.ARG_INSTANCE_ID).setValueFromString(instanceID + "");

      controlPoint.invokeAction(action);

      return action.getOutArgument(UPnPAVConstant.ARG_CURRENT_PRESET_NAMELIST).getStringValue();

    } catch (Exception e1)
    {
      errorInAVTAction(action, e1);
    }
    return null;
  }

  public void selectPreset(long instanceID, String presetName)
  {
    CPAction action = cpService.getCPAction(UPnPAVConstant.ACTION_SELECT_PRESET);
    try
    {
      action.getInArgument(UPnPAVConstant.ARG_INSTANCE_ID).setValueFromString(instanceID + "");
      action.getInArgument(UPnPAVConstant.ARG_PRESET_NAME).setValueFromString(presetName);

      controlPoint.invokeAction(action);
    } catch (Exception e1)
    {
      errorInAVTAction(action, e1);
    }
  }

  public short getBrightness(long instanceID)
  {
    CPAction action = cpService.getCPAction(UPnPAVConstant.ACTION_GET_BRIGHTNESS);

    try
    {
      action.getInArgument(UPnPAVConstant.ARG_INSTANCE_ID).setValueFromString(instanceID + "");

      controlPoint.invokeAction(action);

      return ((Short)action.getOutArgument("CurrentBrightness").getValue()).shortValue();

    } catch (Exception e1)
    {
      errorInAVTAction(action, e1);
    }

    return 0;
  }

  public void setBrightness(long instanceID, short desiredBrightness)
  {
    CPAction action = cpService.getCPAction(UPnPAVConstant.ACTION_SET_BRIGHTNESS);

    try
    {
      action.getInArgument(UPnPAVConstant.ARG_INSTANCE_ID).setValueFromString(instanceID + "");
      action.getInArgument(UPnPAVConstant.ARG_DESIRED_BRIGHTNESS).setValueFromString(desiredBrightness + "");

      controlPoint.invokeAction(action);
    } catch (Exception e1)
    {
      errorInAVTAction(action, e1);
    }
  }

  public short getContrast(long instanceID)
  {
    CPAction action = cpService.getCPAction(UPnPAVConstant.ACTION_GET_CONTRAST);

    try
    {
      action.getInArgument(UPnPAVConstant.ARG_INSTANCE_ID).setValueFromString(instanceID + "");

      controlPoint.invokeAction(action);

      return ((Short)action.getOutArgument("CurrentContrast").getValue()).shortValue();

    } catch (Exception e1)
    {
      errorInAVTAction(action, e1);
    }

    return 0;
  }

  public void setContrast(long instanceID, short desiredContrast)
  {
    CPAction action = cpService.getCPAction(UPnPAVConstant.ACTION_SET_CONTRAST);

    try
    {
      action.getInArgument(UPnPAVConstant.ARG_INSTANCE_ID).setValueFromString(instanceID + "");
      action.getInArgument(UPnPAVConstant.ARG_DESIRED_CONTRAST).setValueFromString(desiredContrast + "");

      controlPoint.invokeAction(action);
    } catch (Exception e1)
    {
      errorInAVTAction(action, e1);
    }
  }

  public boolean getMute(long instanceID, String channel)
  {
    CPAction action = cpService.getCPAction(UPnPAVConstant.ACTION_GET_MUTE);

    try
    {
      action.getInArgument(UPnPAVConstant.ARG_INSTANCE_ID).setValueFromString(instanceID + "");
      action.getInArgument(UPnPAVConstant.ARG_CHANNEL).setValueFromString(channel);

      controlPoint.invokeAction(action);

      return ((Boolean)action.getOutArgument("CurrentMute").getValue()).booleanValue();

    } catch (Exception e1)
    {
      errorInAVTAction(action, e1);
    }

    return false;
  }

  public void setMute(long instanceID, String channel, boolean desiredMute)
  {
    CPAction action = cpService.getCPAction(UPnPAVConstant.ACTION_SET_MUTE);

    try
    {
      action.getInArgument(UPnPAVConstant.ARG_INSTANCE_ID).setValueFromString(instanceID + "");
      action.getInArgument(UPnPAVConstant.ARG_CHANNEL).setValueFromString(channel);
      action.getInArgument(UPnPAVConstant.ARG_MUTE).setValueFromString(desiredMute + "");

      controlPoint.invokeAction(action);
    } catch (Exception e1)
    {
      errorInAVTAction(action, e1);
    }
  }

  public short getVolume(long instanceID, String channel)
  {
    CPAction action = cpService.getCPAction(UPnPAVConstant.ACTION_GET_VOLUME);

    try
    {
      action.getInArgument(UPnPAVConstant.ARG_INSTANCE_ID).setValueFromString(instanceID + "");
      action.getInArgument(UPnPAVConstant.ARG_CHANNEL).setValueFromString(channel);

      controlPoint.invokeAction(action);

      return ((Short)action.getOutArgument("CurrentVolume").getValue()).shortValue();

    } catch (Exception e1)
    {
      errorInAVTAction(action, e1);
    }

    return 0;
  }

  public void setVolume(long instanceID, String channel, short desiredVolume)
  {
    System.out.println("Invoke set volume for " + channel + " to " + desiredVolume);
    CPAction action = cpService.getCPAction(UPnPAVConstant.ACTION_SET_VOLUME);

    try
    {
      action.getInArgument(UPnPAVConstant.ARG_INSTANCE_ID).setNumericValue(instanceID);
      action.getInArgument(UPnPAVConstant.ARG_CHANNEL).setValue(channel);
      action.getInArgument(UPnPAVConstant.ARG_DESIRED_VOLUME).setNumericValue(desiredVolume);

      controlPoint.invokeAction(action);

      System.out.println("Successfully invoked set volume");
    } catch (Exception e1)
    {
      System.out.println("Error: " + e1.getMessage());
      errorInAVTAction(action, e1);
    }
  }

  public short getMaxBrightness()
  {
    try
    {
      CPStateVariable brightness = cpService.getCPStateVariable("Brightness");
      if (brightness == null)
      {
        return 0;
      }
      AllowedValueRange avr = brightness.getAllowedValueRange();
      return Short.valueOf(avr.getMax() + "").shortValue();
    } catch (Exception ex)
    {
      CPService.logger.error(ex.getMessage());
    }
    return 0;
  }

  public short getMaxVolume()
  {
    try
    {
      CPStateVariable volume = cpService.getCPStateVariable("Volume");
      if (volume == null)
      {
        return 0;
      }
      AllowedValueRange avr = volume.getAllowedValueRange();
      return Short.valueOf(avr.getMax() + "").shortValue();
    } catch (Exception ex)
    {
      CPService.logger.error(ex.getMessage());
    }
    return 0;
  }

  public short getMaxContrast()
  {
    try
    {
      CPStateVariable contrast = cpService.getCPStateVariable("Contrast");
      if (contrast == null)
      {
        return 0;
      }
      AllowedValueRange avr = contrast.getAllowedValueRange();
      return Short.valueOf(avr.getMax() + "").shortValue();
    } catch (Exception ex)
    {
      CPService.logger.error(ex.getMessage());
    }
    return 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ILastChangeEventListener#stateVariableChanged(de.fhg.fokus.magic.upnp.CPService,
   *      long, java.lang.String, java.lang.String)
   */
  public void stateVariableChanged(CPService cpService, long instanceID, String name, String value)
  {
    if (cpService == this.cpService)
    {
      // System.out.println("RenderingControl: " + name + " changed to " + value);

      Hashtable hashtable = getHashtable(instanceID);
      // hash table not found, create new one
      if (hashtable == null)
      {
        hashtable = new Hashtable();
        Long instanceIDObject = new Long(instanceID);
        hashtableFromInstanceIDHashtable.put(instanceIDObject, hashtable);
      }
      // hash table found or newly created
      if (hashtable != null)
      {
        // this effectively overwrites any previously stored value
        hashtable.put(name, value);

        // signal to listener
        if (stateVariableListener != null)
        {
          try
          {
            if (name.equals(UPnPAVConstant.BRIGHTNESS))
            {
              stateVariableListener.brightnessChanged(instanceID, Short.parseShort(value));
            }

          } catch (Exception e)
          {
          }
        }
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ILastChangeEventListener#channelStateVariableChanged(de.fhg.fokus.magic.upnp.CPService,
   *      long, java.lang.String, java.lang.String, java.lang.String)
   */
  public void channelStateVariableChanged(CPService cpService,
    long instanceID,
    String name,
    String channel,
    String value)
  {
    if (cpService == this.cpService)
    {
      Hashtable instanceIDHashtable = getHashtable(instanceID);
      // hash table not found, create new one
      if (instanceIDHashtable == null)
      {
        instanceIDHashtable = new Hashtable();
        Long instanceIDObject = new Long(instanceID);
        hashtableFromInstanceIDHashtable.put(instanceIDObject, instanceIDHashtable);
      }
      // channel hash table found or newly created
      if (instanceIDHashtable != null)
      {
        // try to get hashtable for channel
        Hashtable channelHashtable = null;
        if (instanceIDHashtable.containsKey(channel))
        {
          channelHashtable = (Hashtable)instanceIDHashtable.get(channel);
        } else
        {
          channelHashtable = new Hashtable();
          instanceIDHashtable.put(channel, channelHashtable);
        }
        // value hash table found or newly created
        if (channelHashtable != null)
        {
          // this effectively overwrites any previously stored value
          channelHashtable.put(name, value);

          try
          {
            if (name.equals(UPnPAVConstant.VOLUME))
            {
              stateVariableListener.volumeChanged(instanceID, channel, new Short(value).shortValue());
            }

          } catch (Exception e)
          {
          }
        }
      }
    }

  }

  /**
   * Retrieves the state variable hash table for an instanceID.
   * 
   * @param instanceID
   * @return
   */
  private Hashtable getHashtable(long instanceID)
  {
    Long instanceIDObject = new Long(instanceID);
    if (hashtableFromInstanceIDHashtable.containsKey(instanceIDObject))
    {
      return (Hashtable)hashtableFromInstanceIDHashtable.get(instanceIDObject);
    }

    return null;
  }

  /**
   * Retrieves the value for a specific state variable.
   * 
   * @param instanceID
   * @param name
   * @return
   */
  public String getStateVariableValue(long instanceID, String name)
  {
    Hashtable hashtable = getHashtable(instanceID);
    if (hashtable != null && hashtable.containsKey(name))
    {
      return (String)hashtable.get(name);
    }

    return null;
  }

  /**
   * Retrieves the value for a specific state variable with a channel.
   * 
   * @param instanceID
   * @param name
   * @param channel
   * 
   * @return
   */
  public String getChannelStateVariableValue(long instanceID, String name, String channel)
  {
    Hashtable instanceIDHashtable = getHashtable(instanceID);
    if (instanceIDHashtable != null && instanceIDHashtable.containsKey(channel))
    {
      Hashtable channelHashtable = (Hashtable)instanceIDHashtable.get(channel);
      if (channelHashtable.containsKey(name))
      {
        return (String)channelHashtable.get(name);
      }
    }
    return null;
  }

  /**
   * Retrieves the cpService.
   * 
   * @return The cpService.
   */
  public CPService getCPService()
  {
    return cpService;
  }

  /**
   * Logs arguments for a failed action.
   * 
   * @param action
   * @param e
   *          The exception
   */
  private void errorInAVTAction(CPAction action, Exception e)
  {
    if (action != null)
    {
      Argument[] argument = action.getArgumentTable();

      CPService.logger.error("--------------------------------");
      CPService.logger.error("Device '" + cpService.getCPDevice().getFriendlyName() + "': " + action);
      for (int i = 0; i < argument.length; ++i)
      {
        Argument a = argument[i];
        CPService.logger.error("arg[" + i + "](" + a.getName() + ") ='" + a.getValue() + "'");
      }
    } else
    {
      CPService.logger.error("Action not available");
    }
    CPService.logger.error(e.toString());
    CPService.logger.error("--------------------------------");
  }

}
