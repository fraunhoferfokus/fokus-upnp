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
package de.fraunhofer.fokus.upnp.util.network.micro_device;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import de.fraunhofer.fokus.upnp.util.StringHelper;

/**
 * This class represents the firmware for the FOKUS sensor board.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class MC9S12NE64Firmware
{

  /** 64 K firmware memory stamp */
  private byte[]  firmware;

  private String  firmwareDate;

  private String  firmwareName;

  /** Flag that firmware is valid */
  private boolean valid = false;

  /**
   * Creates a new instance of MC9S12NE64Firmware.
   * 
   * @param firmwareFile
   */
  public MC9S12NE64Firmware(File firmwareFile)
  {
    firmware = new byte[65536];
    for (int i = 0; i < firmware.length; i++)
    {
      firmware[i] = (byte)0xFF;
    }
    firmwareDate = "";
    firmwareName = "";
    if (!firmwareFile.exists())
    {
      return;
    }
    try
    {
      FileInputStream inputStream = new FileInputStream(firmwareFile);
      BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
      String line;
      line = reader.readLine();
      while (line != null)
      {
        // make sure there wasn`t just an unnecessary newline at the end...
        // ignore comments
        if (line.length() > 0 && line.charAt(0) != '#')
        {
          String recordType = line.substring(0, 2);
          if (recordType.equalsIgnoreCase("s1"))
          {
            // System.out.println(line);
            // int payloadSize = Integer.parseInt(line.substring(2, 4), 16);
            int address = Integer.parseInt(line.substring(4, 8), 16);
            String payloadString = line.substring(8, line.length() - 2);

            byte[] payload = StringHelper.binHexToByteArray(payloadString);

            // System.out.println("Found 16-bit address record with " + (payloadSize - 3) + " data
            // bytes for address " +
            // address + "(0x" + Integer.toHexString(address) + ")");

            // search correct memory location
            int offset = calculateMemoryStampOffsetFromAddress(address);
            for (int i = 0; i < payload.length; i++)
            {
              firmware[offset + i] = payload[i];
            }
          }
          if (recordType.equalsIgnoreCase("s2"))
          {
            // int payloadSize = Integer.parseInt(line.substring(2, 4), 16);
            int address = Integer.parseInt(line.substring(4, 10), 16);
            String payloadString = line.substring(10, line.length() - 2);

            byte[] payload = StringHelper.binHexToByteArray(payloadString);

            // System.out.println("Found 24-bit address record with " + (payloadSize - 4) + " data
            // bytes for address " +
            // address + "(0x" + Integer.toHexString(address) + ")");

            // search correct memory location
            int offset = calculateMemoryStampOffsetFromAddress(address);
            for (int i = 0; i < payload.length; i++)
            {
              firmware[offset + i] = payload[i];
            }
          }
        }
        line = reader.readLine();
      }
      reader.close();
      valid = true;
      firmwareDate = parseFirmwareString(0x4000 - 0x100, 16);
      firmwareName = parseFirmwareString(0x4000 - 0x100 + 0x10, 32);
    } catch (Exception ex)
    {
      ex.printStackTrace(System.out);
    }
  }

  /**
   * Creates a new instance of MC9S12NE64Firmware.
   * 
   * @param firmware
   */
  public MC9S12NE64Firmware(byte[] firmware)
  {
    if (firmware != null && firmware.length == 65536)
    {
      this.firmware = firmware;
      firmwareDate = parseFirmwareString(0x4000 - 0x100, 16);
      firmwareName = parseFirmwareString(0x4000 - 0x100 + 0x10, 32);
      valid = true;
    }
  }

  /**
   * Retrieves the offset in the local memory stamp for a certain address.
   * 
   * 
   * @param absoluteAddress
   *          The sector address, e.g., 0x3E8000
   * @return An offset between 0 and 65536
   */
  public static int calculateMemoryStampOffsetFromAddress(int absoluteAddress)
  {
    // 16-bit addresses
    if (absoluteAddress >= 0x4000 && absoluteAddress < 0x8000)
    {
      return absoluteAddress - 0x4000;
    }
    if (absoluteAddress >= 0xC000 && absoluteAddress < 0x10000)
    {
      return 0x4000 + absoluteAddress - 0xC000;
    }

    // 24-bit addresses
    if (absoluteAddress >= 0x3C8000 && absoluteAddress < 0x3CC000)
    {
      return 0x8000 + absoluteAddress - 0x3C8000;
    }
    if (absoluteAddress >= 0x3D8000 && absoluteAddress < 0x3DC000)
    {
      return 0xC000 + absoluteAddress - 0x3D8000;
    }
    if (absoluteAddress >= 0x3E8000 && absoluteAddress < 0x3EC000)
    {
      return absoluteAddress - 0x3E8000;
    }
    if (absoluteAddress >= 0x3F8000 && absoluteAddress < 0x3FC000)
    {
      return 0x4000 + absoluteAddress - 0x3F8000;
    }
    return -1;
  }

  /**
   * Parses a part of the firmware as string.
   * 
   * 
   * @param offset
   *          An offset between 0 and 65536
   * @param length
   *          Number of bytes
   * @return
   */
  public String parseFirmwareString(int offset, int length)
  {
    byte[] data = new byte[length];
    System.arraycopy(firmware, offset, data, 0, data.length);

    return StringHelper.byteArrayToAsciiDebugString(data);
  }

  /**
   * Compares the firmware of a specific sector.
   * 
   * 
   * @param absoluteAddress
   *          The sector address, e.g., 0x3E8000
   * @param data
   *          The data to be compared (512 bytes)
   * @return
   */
  public boolean isEqualSector(int absoluteAddress, byte[] data)
  {
    if (data == null || data.length != 512)
    {
      return false;
    }

    int offset = calculateMemoryStampOffsetFromAddress(absoluteAddress);
    for (int i = 0; i < data.length; i++)
    {
      if (firmware[offset + i] != data[i])
      {
        return false;
      }
    }
    return true;
  }

  /**
   * Retrieves a specific firmware sector.
   * 
   * 
   * @param absoluteAddress
   *          The sector address, e.g., 0x3E8000
   * @return The firmware sector
   */
  public byte[] getFirmwareSector(int absoluteAddress)
  {
    int offset = calculateMemoryStampOffsetFromAddress(absoluteAddress);
    byte[] result = new byte[512];

    System.arraycopy(firmware, offset, result, 0, 512);

    return result;
  }

  /**
   * Retrieves the value of firmware.
   * 
   * @return The value of firmware
   */
  public byte[] getFirmware()
  {
    return firmware;
  }

  /**
   * Retrieves the value of firmwareDate.
   * 
   * @return The value of firmwareDate
   */
  public String getFirmwareDate()
  {
    return firmwareDate;
  }

  /**
   * Retrieves the value of firmwareName.
   * 
   * @return The value of firmwareName
   */
  public String getFirmwareName()
  {
    return firmwareName;
  }

  /**
   * Retrieves the value of valid.
   * 
   * @return The value of valid
   */
  public boolean isValid()
  {
    return valid;
  }

}
