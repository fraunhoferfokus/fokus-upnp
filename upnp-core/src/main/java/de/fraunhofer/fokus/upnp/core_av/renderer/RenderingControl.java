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
package de.fraunhofer.fokus.upnp.core_av.renderer;

import java.security.interfaces.RSAPublicKey;

import de.fraunhofer.fokus.upnp.core.AllowedValueRange;
import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.device.Action;
import de.fraunhofer.fokus.upnp.core.device.StateVariable;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateService;
import de.fraunhofer.fokus.upnp.core_av.UPnPAVConstant;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;

// import javazoom.jlgui.basicplayer.BasicPlayerException;

/**
 * This class represents a generic RenderingControl service.
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class RenderingControl extends TemplateService
{
  // private AudioRenderer audioRenderer = null;
  // private ImageRenderer imageRenderer = null;
  protected AllowedValueRange   valueRange;

  protected StateVariable       lastChange;

  protected StateVariable       presetNameList;

  protected StateVariable       brightness;             // optional

  protected StateVariable       contrast;               // optional

  protected StateVariable       sharpness;              // optional

  protected StateVariable       redVideoGain;           // optional

  protected StateVariable       greenVideoGain;         // optional

  protected StateVariable       blueVideoGain;          // optional

  protected StateVariable       redVideoBlackLevel;     // optional

  protected StateVariable       greenVideoBlackLevel;   // optional

  protected StateVariable       blueVideoBlackLevel;    // optional

  protected StateVariable       colorTemperature;       // optional

  protected StateVariable       horizontalKeystone;     // optional

  protected StateVariable       verticalKeystone;       // optional

  protected StateVariable       mute;                   // optional

  protected StateVariable       volume;                 // optional

  protected StateVariable       volumeDB;               // optional

  protected StateVariable       loudness;               // optional

  protected StateVariable       A_ARG_TYPE_Channel;

  protected StateVariable       A_ARG_TYPE_InstanceID;

  protected StateVariable       A_ARG_TYPE_PresetName;

  protected Action              listPresets;

  protected Action              selectPreset;

  protected Action              getBrightness;

  protected Action              setBrightness;

  protected Action              getContrast;

  protected Action              setContrast;

  protected Action              getSharpness;

  protected Action              setSharpness;

  protected Action              getRedVideoGain;

  protected Action              setRedVideoGain;

  protected Action              getGreenVideoGain;

  protected Action              setGreenVideoGain;

  protected Action              getBlueVideoGain;

  protected Action              setBlueVideoGain;

  protected Action              getRedVideoBlackLevel;

  protected Action              setRedVideoBlackLevel;

  protected Action              getGreenVideoBlackLevel;

  protected Action              setGreenVideoBlackLevel;

  protected Action              getBlueVideoBlackLevel;

  protected Action              setBlueVideoBlackLevel;

  protected Action              getColorTemperature;

  protected Action              setColorTemperature;

  protected Action              getHorizontalKeystone;

  protected Action              setHorizontalKeystone;

  protected Action              getVerticalKeystone;

  protected Action              setVerticalKeystone;

  protected Action              getMute;

  protected Action              setMute;

  protected Action              getVolume;

  protected Action              setVolume;

  protected Action              getVolumeDB;

  protected Action              setVolumeDB;

  protected Action              getVolumeDBRange;

  protected Action              getLoudness;

  protected Action              setLoudness;

  protected LastChangeCollector lastChangeCollector;

  private Short                 short0;

  private Short                 initShortVol;

  protected String[]            allowedPresetList;

  public RenderingControl(TemplateDevice device)
  {
    super(device, UPnPAVConstant.RENDERING_CONTROL_SERVICE_TYPE, UPnPAVConstant.RENDERING_CONTROL_SERVICE_ID);
  }

  public void setupServiceVariables()
  {
    super.setupServiceVariables();
    // lastChangeCollector = new LastChangeCollector(this);
    short0 = new Short("0");
    initShortVol = new Short((short)RendererConstants.VOLUME_INITIAL);
    allowedPresetList = new String[] {
        "FactoryDefaults", "InstallationDefaults"
    };
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
    lastChange = new StateVariable("LastChange", "", true);
    lastChangeCollector = new LastChangeCollector(lastChange, 200);
    getDevice().getDeviceEventThread().register(lastChangeCollector);

    presetNameList = new StateVariable("PresetNameList", allowedPresetList[0], false);
    brightness = new StateVariable("Brightness", "ui2", short0, false);
    brightness.setAllowedValueRange(0, 100, 1);
    contrast = new StateVariable("Contrast", "ui2", short0, false);
    contrast.setAllowedValueRange(0, 100, 1);
    sharpness = new StateVariable("Sharpness", "ui2", short0, false);
    sharpness.setAllowedValueRange(0, 100, 1);
    redVideoGain = new StateVariable("RedVideoGain", "ui2", short0, false);
    redVideoGain.setAllowedValueRange(0, 100, 1);
    greenVideoGain = new StateVariable("GreenVideoGain", "ui2", short0, false);
    greenVideoGain.setAllowedValueRange(0, 100, 1);
    blueVideoGain = new StateVariable("BlueVideoGain", "ui2", short0, false);
    blueVideoGain.setAllowedValueRange(0, 100, 1);
    redVideoBlackLevel = new StateVariable("RedVideoBlackLevel", "ui2", short0, false);
    redVideoBlackLevel.setAllowedValueRange(0, 100, 1);
    greenVideoBlackLevel = new StateVariable("GreenVideoBlackLevel", "ui2", short0, false);
    greenVideoBlackLevel.setAllowedValueRange(0, 100, 1);
    blueVideoBlackLevel = new StateVariable("BlueVideoBlackLevel", "ui2", short0, false);
    blueVideoBlackLevel.setAllowedValueRange(0, 100, 1);
    colorTemperature = new StateVariable("ColorTemperature", "ui2", short0, false);
    colorTemperature.setAllowedValueRange(0, 100, 1);
    horizontalKeystone = new StateVariable("HorizontalKeystone", "i2", short0, false);
    horizontalKeystone.setAllowedValueRange(-10, 10, 1);
    verticalKeystone = new StateVariable("VerticalKeystone", "i2", short0, false);
    verticalKeystone.setAllowedValueRange(-10, 10, 1);
    mute = new StateVariable("Mute", false, false);
    volume = new StateVariable("Volume", "ui2", initShortVol, false);
    volume.setAllowedValueRange(0, RendererConstants.VOLUME_MAX, 1);
    volume.setLastChangeCollector(lastChangeCollector);

    volumeDB = new StateVariable("VolumeDB", "i2", short0, false);
    volumeDB.setAllowedValueRange(0, 100, 0);
    loudness = new StateVariable("Loudness", false, false);
    A_ARG_TYPE_Channel = new StateVariable("A_ARG_TYPE_Channel", "", false);
    // A_ARG_TYPE_Channel.setAllowedValueList(new String[] {
    // "Master", "LF", "RF", "CF", "LFE", "LS", "RS", "LFC", "RFC",
    // "SD", "SL", "SR", "T", "B"
    // });
    A_ARG_TYPE_Channel.setAllowedValueList(new String[] {
      "Master"
    });
    A_ARG_TYPE_InstanceID = new StateVariable("A_ARG_TYPE_InstanceID", "ui4", new Long(0), false);
    A_ARG_TYPE_PresetName = new StateVariable("A_ARG_TYPE_PresetName", "", false);
    A_ARG_TYPE_PresetName.setAllowedValueList(allowedPresetList);

    StateVariable[] stateVariableList =
      {
          lastChange, presetNameList, brightness, contrast, sharpness, redVideoGain, greenVideoGain, blueVideoGain,
          redVideoBlackLevel, greenVideoBlackLevel, blueVideoBlackLevel, colorTemperature, horizontalKeystone,
          verticalKeystone, mute, volume, volumeDB, loudness, A_ARG_TYPE_Channel, A_ARG_TYPE_InstanceID,
          A_ARG_TYPE_PresetName
      };

    setStateVariableTable(stateVariableList);

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // Actions //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    listPresets = new Action("ListPresets");
    listPresets.setArgumentTable(new Argument[] {
        new Argument("InstanceID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("CurrentPresetNameList", UPnPConstant.DIRECTION_OUT, presetNameList)
    });
    selectPreset = new Action("SelectPreset");
    selectPreset.setArgumentTable(new Argument[] {
        new Argument("InstanceID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("PresetName", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_PresetName)
    });
    getBrightness = new Action("GetBrightness");
    getBrightness.setArgumentTable(new Argument[] {
        new Argument("InstanceID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("CurrentBrightness", UPnPConstant.DIRECTION_OUT, brightness)
    });
    setBrightness = new Action("SetBrightness");
    setBrightness.setArgumentTable(new Argument[] {
        new Argument("InstanceID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("DesiredBrightness", UPnPConstant.DIRECTION_IN, brightness)
    });
    getContrast = new Action("GetContrast");
    getContrast.setArgumentTable(new Argument[] {
        new Argument("InstanceID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("CurrentContrast", UPnPConstant.DIRECTION_OUT, contrast)
    });
    setContrast = new Action("SetContrast");
    setContrast.setArgumentTable(new Argument[] {
        new Argument("InstanceID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("DesiredContrast", UPnPConstant.DIRECTION_IN, contrast)
    });
    getSharpness = new Action("GetSharpness");
    getSharpness.setArgumentTable(new Argument[] {
        new Argument("InstanceID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("CurrentSharpness", UPnPConstant.DIRECTION_OUT, sharpness)
    });
    setSharpness = new Action("SetSharpness");
    setSharpness.setArgumentTable(new Argument[] {
        new Argument("InstanceID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("DesiredSharpness", UPnPConstant.DIRECTION_IN, sharpness)
    });
    getRedVideoGain = new Action("GetRedVideoGain");
    getRedVideoGain.setArgumentTable(new Argument[] {
        new Argument("InstanceID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("CurrentRedVideoGain", UPnPConstant.DIRECTION_OUT, redVideoGain)
    });
    setRedVideoGain = new Action("SetRedVideoGain");
    setRedVideoGain.setArgumentTable(new Argument[] {
        new Argument("InstanceID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("DesiredRedVideoGain", UPnPConstant.DIRECTION_IN, redVideoGain)
    });
    getGreenVideoGain = new Action("GetGreenVideoGain");
    getGreenVideoGain.setArgumentTable(new Argument[] {
        new Argument("InstanceID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("CurrentGreenVideoGain", UPnPConstant.DIRECTION_OUT, greenVideoGain)
    });
    setGreenVideoGain = new Action("SetGreenVideoGain");
    setGreenVideoGain.setArgumentTable(new Argument[] {
        new Argument("InstanceID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("DesiredGreenVideoGain", UPnPConstant.DIRECTION_IN, greenVideoGain)
    });
    getBlueVideoGain = new Action("GetBlueVideoGain");
    getBlueVideoGain.setArgumentTable(new Argument[] {
        new Argument("InstanceID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("CurrentBlueVideoGain", UPnPConstant.DIRECTION_OUT, blueVideoGain)
    });
    setBlueVideoGain = new Action("SetBlueVideoGain");
    setBlueVideoGain.setArgumentTable(new Argument[] {
        new Argument("InstanceID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("DesiredBlueVideoGain", UPnPConstant.DIRECTION_IN, blueVideoGain)
    });
    getRedVideoBlackLevel = new Action("GetRedVideoBlackLevel");
    getRedVideoBlackLevel.setArgumentTable(new Argument[] {
        new Argument("InstanceID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("CurrentRedVideoBlackLevel", UPnPConstant.DIRECTION_OUT, redVideoBlackLevel)
    });
    setRedVideoBlackLevel = new Action("SetRedVideoBlackLevel");
    setRedVideoBlackLevel.setArgumentTable(new Argument[] {
        new Argument("InstanceID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("DesiredRedVideoBlackLevel", UPnPConstant.DIRECTION_IN, redVideoBlackLevel)
    });
    getGreenVideoBlackLevel = new Action("GetGreenVideoBlackLevel");
    getGreenVideoBlackLevel.setArgumentTable(new Argument[] {
        new Argument("InstanceID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("CurrentGreenVideoBlackLevel", UPnPConstant.DIRECTION_OUT, greenVideoBlackLevel)
    });
    setGreenVideoBlackLevel = new Action("SetGreenVideoBlackLevel");
    setGreenVideoBlackLevel.setArgumentTable(new Argument[] {
        new Argument("InstanceID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("DesiredGreenVideoBlackLevel", UPnPConstant.DIRECTION_IN, greenVideoBlackLevel)
    });
    getBlueVideoBlackLevel = new Action("GetBlueVideoBlackLevel");
    getBlueVideoBlackLevel.setArgumentTable(new Argument[] {
        new Argument("InstanceID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("CurrentBlueVideoBlackLevel", UPnPConstant.DIRECTION_OUT, blueVideoBlackLevel)
    });
    setBlueVideoBlackLevel = new Action("SetBlueVideoBlackLevel");
    setBlueVideoBlackLevel.setArgumentTable(new Argument[] {
        new Argument("InstanceID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("DesiredBlueVideoBlackLevel", UPnPConstant.DIRECTION_IN, blueVideoBlackLevel)
    });
    getColorTemperature = new Action("GetColorTemperature");
    getColorTemperature.setArgumentTable(new Argument[] {
        new Argument("InstanceID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("CurrentColorTemperature", UPnPConstant.DIRECTION_OUT, colorTemperature)
    });
    setColorTemperature = new Action("SetColorTemperature");
    setColorTemperature.setArgumentTable(new Argument[] {
        new Argument("InstanceID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("DesiredColorTemperature", UPnPConstant.DIRECTION_IN, colorTemperature)
    });
    getHorizontalKeystone = new Action("GetHorizontalKeystone");
    getHorizontalKeystone.setArgumentTable(new Argument[] {
        new Argument("InstanceID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("CurrentHorizontalKeystone", UPnPConstant.DIRECTION_OUT, horizontalKeystone)
    });
    setHorizontalKeystone = new Action("SetHorizontalKeystone");
    setHorizontalKeystone.setArgumentTable(new Argument[] {
        new Argument("InstanceID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("DesiredHorizontalKeystone", UPnPConstant.DIRECTION_IN, horizontalKeystone)
    });
    getVerticalKeystone = new Action("GetVerticalKeystone");
    getVerticalKeystone.setArgumentTable(new Argument[] {
        new Argument("InstanceID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("CurrentVerticalKeystone", UPnPConstant.DIRECTION_OUT, verticalKeystone)
    });
    setVerticalKeystone = new Action("SetVerticalKeystone");
    setVerticalKeystone.setArgumentTable(new Argument[] {
        new Argument("InstanceID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("DesiredVerticalKeystone", UPnPConstant.DIRECTION_IN, verticalKeystone)
    });
    getMute = new Action("GetMute");
    getMute.setArgumentTable(new Argument[] {
        new Argument("InstanceID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("Channel", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_Channel),
        new Argument("CurrentMute", UPnPConstant.DIRECTION_OUT, mute)
    });
    setMute = new Action("SetMute");
    setMute.setArgumentTable(new Argument[] {
        new Argument("InstanceID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("Channel", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_Channel),
        new Argument("DesiredMute", UPnPConstant.DIRECTION_IN, mute)
    });
    getVolume = new Action("GetVolume");
    getVolume.setArgumentTable(new Argument[] {
        new Argument("InstanceID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("Channel", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_Channel),
        new Argument("CurrentVolume", UPnPConstant.DIRECTION_OUT, volume)
    });
    setVolume = new Action("SetVolume");
    setVolume.setArgumentTable(new Argument[] {
        new Argument("InstanceID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("Channel", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_Channel),
        new Argument("DesiredVolume", UPnPConstant.DIRECTION_IN, volume)
    });
    getVolumeDB = new Action("GetVolumeDB");
    getVolumeDB.setArgumentTable(new Argument[] {
        new Argument("InstanceID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("Channel", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_Channel),
        new Argument("CurrentVolume", UPnPConstant.DIRECTION_OUT, volumeDB)
    });
    setVolumeDB = new Action("SetVolumeDB");
    setVolumeDB.setArgumentTable(new Argument[] {
        new Argument("InstanceID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("Channel", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_Channel),
        new Argument("DesiredVolume", UPnPConstant.DIRECTION_IN, volumeDB)
    });
    getVolumeDBRange = new Action("GetVolumeDBRange");
    getVolumeDBRange.setArgumentTable(new Argument[] {
        new Argument("InstanceID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("Channel", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_Channel),
        new Argument("MinValue", UPnPConstant.DIRECTION_OUT, volumeDB),
        new Argument("MaxValue", UPnPConstant.DIRECTION_OUT, volumeDB)
    });
    getLoudness = new Action("GetLoudness");
    getLoudness.setArgumentTable(new Argument[] {
        new Argument("InstanceID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("Channel", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_Channel),
        new Argument("CurrentLoudness", UPnPConstant.DIRECTION_OUT, loudness)
    });
    setLoudness = new Action("SetLoudness");
    setLoudness.setArgumentTable(new Argument[] {
        new Argument("InstanceID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("Channel", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_Channel),
        new Argument("DesiredLoudness", UPnPConstant.DIRECTION_IN, loudness)
    });

    Action[] actionList =
      {
          listPresets, selectPreset, getBrightness, setBrightness, getContrast, setContrast, getSharpness,
          setSharpness, getRedVideoGain, setRedVideoGain, getGreenVideoGain, setGreenVideoGain, getBlueVideoGain,
          setBlueVideoGain, getRedVideoBlackLevel, setRedVideoBlackLevel, getGreenVideoBlackLevel,
          setGreenVideoBlackLevel, getBlueVideoBlackLevel, setBlueVideoBlackLevel, getColorTemperature,
          setColorTemperature, getHorizontalKeystone, setHorizontalKeystone, getVerticalKeystone, setVerticalKeystone,
          getMute, setMute, getVolume, setVolume, getVolumeDB, setVolumeDB, getVolumeDBRange, getLoudness, setLoudness
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
  // required
  public void listPresets(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
    try
    {
      args[1].setValue(getPresetList());
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void selectPreset(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
    String preset = "";
    try
    {
      preset = (String)args[1].getValue();
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    if (!isValidPreset(preset))
    {
      throw new ActionFailedException(701, "Invalid Name");
    }
  }

  // optional
  public void getBrightness(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
    throw new ActionFailedException(602, "Not implemented");
  }

  public void setBrightness(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
    throw new ActionFailedException(602, "Not implemented");
  }

  public void getContrast(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
    throw new ActionFailedException(602, "Not implemented");
  }

  public void setContrast(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
    throw new ActionFailedException(602, "Not implemented");
  }

  public void getSharpness(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
    throw new ActionFailedException(602, "Not implemented");
  }

  public void setSharpness(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
    throw new ActionFailedException(602, "Not implemented");
  }

  public void getRedVideoGain(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
    throw new ActionFailedException(602, "Not implemented");
  }

  public void setRedVideoGain(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
    throw new ActionFailedException(602, "Not implemented");
  }

  public void getGreenVideoGain(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
    throw new ActionFailedException(602, "Not implemented");
  }

  public void setGreenVideoGain(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
    throw new ActionFailedException(602, "Not implemented");
  }

  public void getBlueVideoGain(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
    throw new ActionFailedException(602, "Not implemented");
  }

  public void setBlueVideoGain(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
    throw new ActionFailedException(602, "Not implemented");
  }

  public void getRedVideoBlackLevel(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
    throw new ActionFailedException(602, "Not implemented");
  }

  public void setRedVideoBlackLevel(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
    throw new ActionFailedException(602, "Not implemented");
  }

  public void getGreenVideoBlackLevel(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
    throw new ActionFailedException(602, "Not implemented");
  }

  public void setGreenVideoBlackLevel(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
    throw new ActionFailedException(602, "Not implemented");
  }

  public void getBlueVideoBlackLevel(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
    throw new ActionFailedException(602, "Not implemented");
  }

  public void setBlueVideoBlackLevel(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
    throw new ActionFailedException(602, "Not implemented");
  }

  public void getColorTemperature(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
    throw new ActionFailedException(602, "Not implemented");
  }

  public void setColorTemperature(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
    throw new ActionFailedException(602, "Not implemented");
  }

  public void getHorizontalKeystone(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
    throw new ActionFailedException(602, "Not implemented");
  }

  public void setHorizontalKeystone(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
    throw new ActionFailedException(602, "Not implemented");
  }

  public void getVerticalKeystone(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
    throw new ActionFailedException(602, "Not implemented");
  }

  public void setVerticalKeystone(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
    throw new ActionFailedException(602, "Not implemented");
  }

  public void getMute(Argument[] args) throws ActionFailedException
  {
    if (args.length != 3)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
    throw new ActionFailedException(602, "Not implemented");
  }

  public void setMute(Argument[] args) throws ActionFailedException
  {
    if (args.length != 3)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
    throw new ActionFailedException(602, "Not implemented");
    /*
     * if (audioRenderer != null) { MP3LayerPlayer player = audioRenderer.avt.player;
     * 
     * if (player != null) { Argument arg = Action.getArgumentByName("DesiredMute", args); boolean
     * muted = checkBoolean(arg.getValue().toString());
     * 
     * try { player.setMute(muted); } catch (BasicPlayerException e) { e.printStackTrace(); }
     * 
     * mute.setValue(muted);
     * lastChangeCollector.changes(A_ARG_TYPE_InstanceID.getValue().toString(),mute.getName(),
     * mute.getValue().toString(), A_ARG_TYPE_Channel.getValue().toString()); } }
     */
  }

  public void getVolume(Argument[] args) throws ActionFailedException
  {
    if (args.length != 3)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
    try
    {
      args[2].setNumericValue(volume.getNumericValue());
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    /*
     * if (audioRenderer != null) { MP3LayerPlayer player = audioRenderer.avt.player;
     * 
     * if (player != null) { volume.setValue(player.getVolume()); } }
     */
  }

  public void setVolume(Argument[] args) throws ActionFailedException
  {
    if (args.length != 3)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
    try
    {
      volume.setNumericValue(args[2].getNumericValue());
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    /*
     * if (audioRenderer != null) { MP3LayerPlayer player = audioRenderer.avt.player;
     * 
     * if (player != null) { try { Argument arg = Action.getArgumentByName("DesiredVolume", args);
     * short vol = ((Short) arg.getValue()).shortValue(); player.setVolume(vol);
     * volume.setValue(player.getVolume());
     * 
     * lastChangeCollector.changes(A_ARG_TYPE_InstanceID.getValue() .toString(), volume.getName(),
     * volume.getValue().toString(), A_ARG_TYPE_Channel.getValue().toString()); } catch (Exception
     * e) { Argument arg = Action.getArgumentByName("DesiredVolume", args); System.err.println(
     * "False Value for DesiredVolume! Try to set Value=" + arg.getValue().toString());
     * System.err.println(e); } } }
     */
  }

  public void getVolumeDB(Argument[] args) throws ActionFailedException
  {
    if (args.length != 3)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
    throw new ActionFailedException(602, "Not implemented");
  }

  public void setVolumeDB(Argument[] args) throws ActionFailedException
  {
    if (args.length != 3)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
    throw new ActionFailedException(602, "Not implemented");
  }

  public void getVolumeDBRange(Argument[] args) throws ActionFailedException
  {
    if (args.length != 4)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
    throw new ActionFailedException(602, "Not implemented");
  }

  public void getLoudness(Argument[] args) throws ActionFailedException
  {
    if (args.length != 3)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
    throw new ActionFailedException(602, "Not implemented");
  }

  public void setLoudness(Argument[] args) throws ActionFailedException
  {
    if (args.length != 3)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
    throw new ActionFailedException(602, "Not implemented");
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Public methods //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.DeviceService#getInitialStateVariableValue(de.fhg.fokus.magic.upnp.StateVariable)
   */
  public Object getInitialStateVariableValue(StateVariable stateVariable, RSAPublicKey publicKey)
  {
    if (stateVariable.getName().equals(UPnPAVConstant.LAST_CHANGE))
    {
      // build message with all state variables
      String initialValue = UPnPAVConstant.EVENT_START;
      initialValue += "<" + UPnPAVConstant.ARG_INSTANCE_ID + " " + UPnPAVConstant.ATTR_VAL + "=\"0\">";

      // add all state variables
      initialValue += volume.toXMLAttributeDescription();

      initialValue += "</" + UPnPAVConstant.ARG_INSTANCE_ID + ">";
      initialValue += UPnPAVConstant.EVENT_END;

      return StringHelper.xmlToEscapedString(initialValue);
    }
    return super.getInitialStateVariableValue(stateVariable, publicKey);
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Private methods //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  private String getPresetList()
  {
    String result = "";
    for (int i = 0; i < allowedPresetList.length; i++)
    {
      result += (result.equals("") ? "" : ",") + allowedPresetList[i];
    }

    return result;
  }

  private boolean isValidPreset(String preset)
  {
    boolean result = false;
    for (int i = 0; i < allowedPresetList.length; i++)
    {
      result |= allowedPresetList[i].equals(preset);
    }

    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.DeviceService#terminate()
   */
  public void terminate()
  {
    getDevice().getDeviceEventThread().unregister(lastChangeCollector);
    super.terminate();
  }

}
