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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * This class can be used to remove non $GPRMC messages from GPS dumps.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class GPSCleaner
{

  /**
   * 
   * @param args
   */
  public static void main(String[] args)
  {
    if (args.length < 1)
    {
      System.out.println("Usage: GPSCleaner logFile");
      System.exit(1);
    }
    String logFileName = args[0];
    File logFile = new File(logFileName);
    try
    {
      System.out.println("Check " + logFile.getCanonicalPath());
    } catch (IOException e1)
    {
    }
    if (logFile.exists())
    {
      System.out.println("File or directory exists");
    }
    if (!logFile.exists() || !logFile.isFile())
    {
      System.out.println("Log file not found.");
      System.exit(1);
    }
    try
    {
      BufferedWriter bufferedWriter =
        new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile.getParentFile().getCanonicalPath() +
          "/Clean_" + logFile.getName())));

      BufferedReader reader = new BufferedReader(new FileReader(logFile));
      String line = reader.readLine();
      while (line != null)
      {
        if (line.length() > 0 && line.startsWith("$GPRMC"))
        {
          bufferedWriter.write(line + "\n");
        }
        line = reader.readLine();
      }
      reader.close();
      bufferedWriter.close();
      System.out.println("Successfully cleaned file");
    } catch (Exception e)
    {
      System.out.println("Error cleaning file: " + e.getMessage());
    }
  }

}
