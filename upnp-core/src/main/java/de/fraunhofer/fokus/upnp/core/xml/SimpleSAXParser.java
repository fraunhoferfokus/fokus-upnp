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
package de.fraunhofer.fokus.upnp.core.xml;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class can be used to parse XML documents. This class is currently not used.
 * 
 * @author Alexander Koenig
 * 
 */
public class SimpleSAXParser
{

  private static final int       STATE_WHITE_SPACE = 1;

  private static final int       STATE_TAG         = 2;

  private static final int       STATE_END_TAG     = 3;

  // private static final int STATE_CONTENT = 4;
  // private static final int STATE_ATTRIBUTE = 5;

  private ISimpleSAXEventHandler eventHandler;

  private int                    currentState;

  private StringBuffer           buffer            = new StringBuffer(1024);

  public void parse(InputStream inputStream, ISimpleSAXEventHandler baseEventHandler) throws IOException
  {
    this.eventHandler = baseEventHandler;
    currentState = STATE_WHITE_SPACE;

    int currentChar = inputStream.read();
    boolean handled = false;
    while (currentChar != -1)
    {
      // process tag start
      if ((char)currentChar == '<')
      {
        if (currentState == STATE_WHITE_SPACE)
        {
          currentState = STATE_TAG;
          buffer.setLength(0);
          handled = true;
        }
      }
      // process end tag start
      if ((char)currentChar == '/')
      {
        if (currentState == STATE_TAG && buffer.length() == 0)
        {
          currentState = STATE_END_TAG;
          buffer.setLength(0);
          handled = true;
        }
      }
      // process tag end
      if ((char)currentChar == '>')
      {
        if (currentState == STATE_TAG && buffer.length() > 0)
        {
          currentState = STATE_WHITE_SPACE;
          if (eventHandler != null)
          {
            eventHandler.startElement(buffer.toString(), null);
          }

          buffer.setLength(0);
          handled = true;
        }
        if ((currentState == STATE_TAG || currentState == STATE_END_TAG) && buffer.length() > 0)
        {
          currentState = STATE_END_TAG;
          buffer.setLength(0);
          handled = true;
        }

      }

      if (!handled && currentState == STATE_TAG)
      {
        buffer.append((char)currentChar);
      }

      currentChar = inputStream.read();
    }
  }

  public void setContentHandler(ISimpleSAXEventHandler eventHandler)
  {
    this.eventHandler = eventHandler;
  }

}
