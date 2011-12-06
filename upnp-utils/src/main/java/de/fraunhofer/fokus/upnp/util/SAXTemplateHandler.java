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
package de.fraunhofer.fokus.upnp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.util.Vector;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This is a template handler for SAX parsing. Descendant classes need merely to overwrite the process..() methods.
 * 
 * @author Alexander Koenig
 */
public class SAXTemplateHandler extends DefaultHandler
{

  private SAXParser    parser;

  private Vector       tags          = new Vector();

  // Buffer for text value parsing
  private StringBuffer contentBuffer = new StringBuffer();

  /**
   * Creates a new instance of SAXTemplateHandler with an outer SAX parser.
   * 
   * @param parser
   *          The associated parser
   * 
   */
  public SAXTemplateHandler(SAXParser parser)
  {
    this.parser = parser;
  }

  /**
   * Creates a new instance of SAXTemplateHandler with an inner SAX parser.
   * 
   */
  public SAXTemplateHandler()
  {
    try
    {
      SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
      parser = saxParserFactory.newSAXParser();
    } catch (Exception e)
    {
    }
  }

  /**
   * Parses a specific document.
   * 
   * @param xmlDocument
   *          The document that should be parsed
   * 
   * @throws SAXException
   */
  public void parse(String xmlDocument) throws SAXException
  {
    try
    {
      if (xmlDocument == null || xmlDocument.trim().length() == 0)
      {
        throw new IllegalArgumentException("Document is null or empty");
      }
      // remove trailing \0
      while (xmlDocument.charAt(xmlDocument.length() - 1) == 0)
      {
        xmlDocument = xmlDocument.substring(0, xmlDocument.length() - 1);
      }
      StringReader stringReader = new StringReader(xmlDocument);
      InputSource inputStream = new InputSource(stringReader);

      parser.parse(inputStream, this);
    } catch (Exception e)
    {
      System.out.println("Could not parse document: " + e.getMessage());
    }
  }

  /**
   * Parses a file.
   * 
   * @param file
   *          The file that should be parsed
   * 
   * @throws SAXException
   */
  public void parse(File file) throws SAXException
  {
    if (!file.exists())
    {
      System.out.println("File " + file.getAbsoluteFile() + " does not exist");
    }
    try
    {
      FileInputStream fileInputStream = new FileInputStream(file);
      InputSource inputStream = new InputSource(fileInputStream);

      parser.parse(inputStream, this);
    } catch (Exception e)
    {
      System.out.println("Could not parse file: " + e.getMessage());
    }
  }

  /**
   * Retrieves the associated parser.
   * 
   * @return The parser
   */
  public SAXParser getSAXParser()
  {
    return parser;
  }

  /** Retrieves the last parsed tag */
  public String getCurrentTag()
  {
    // return empty tag if there are no more parsed tags
    if (tags.size() < 1)
    {
      return "";
    }

    return (String)tags.elementAt(tags.size() - 1);
  }

  /**
   * Retrieves a specific tag.
   * 
   * @param index
   *          The 0-based index
   * 
   * @return The tag or null
   */
  public String getTag(int index)
  {
    if (index >= 0 && index < tags.size())
    {
      return (String)tags.elementAt(index);
    }

    return null;
  }

  /** Retrieves the total number of tags at the current state */
  public int getTagCount()
  {
    return tags.size();
  }

  /** Removes the current tag. May be useful for subparsers. */
  private void removeCurrentTag()
  {
    if (tags.size() > 0)
    {
      tags.remove(tags.size() - 1);
    }
  }

  /** This method processes a start element. */
  public void processStartElement(String uri, String name, String qName, Attributes atts) throws SAXException
  {
  }

  /** This method processes an end element. */
  public void processEndElement(String uri, String localName, String qName) throws SAXException
  {
  }

  /** This method processes a content element. */
  public void processContentElement(String content) throws SAXException
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String,
   *      java.lang.String, org.xml.sax.Attributes)
   */
  public void startElement(String uri, String name, String qName, Attributes atts) throws SAXException
  {
    contentBuffer.delete(0, contentBuffer.length());
    tags.add(qName);
    processStartElement(uri, name, qName, atts);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String,
   *      java.lang.String)
   */
  public void endElement(String uri, String localName, String qName) throws SAXException
  {
    // process last content element
    if (contentBuffer.length() != 0)
    {
      processContentElement(contentBuffer.toString());
      contentBuffer.delete(0, contentBuffer.length());
    }
    processEndElement(uri, localName, qName);
    if (getTagCount() > 0)
    {
      tags.remove(tags.size() - 1);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
   */
  public void characters(char[] ch, int start, int length) throws SAXException
  {
    for (int i = start; i < start + length; i++)
    {
      contentBuffer.append(ch[i]);
    }
  }

  /** Redirects SAX events to another template handler as result of a specific startElement. */
  public void redirectSAXEvents(SAXTemplateHandler newHandler, String uri, String name, String qName, Attributes atts) throws SAXException
  {
    // remove current tag, because the associated end element will be parsed by the
    // new parser
    removeCurrentTag();

    parser.getXMLReader().setContentHandler(newHandler);

    // retrigger current SAX event in new handler
    newHandler.startElement(uri, name, qName, atts);
  }

  /** Redirects SAX events back to this class */
  public void handleSAXEvents() throws SAXException
  {
    parser.getXMLReader().setContentHandler(this);
  }

}
