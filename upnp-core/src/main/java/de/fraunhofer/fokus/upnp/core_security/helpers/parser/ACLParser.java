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
package de.fraunhofer.fokus.upnp.core_security.helpers.parser;

import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.fraunhofer.fokus.upnp.core_security.helpers.ACLEntry;
import de.fraunhofer.fokus.upnp.core_security.helpers.SecurityAwareObject;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.SAXTemplateHandler;

/**
 * This classes parses a list with a single or multiple ACL entries.
 * 
 * @author Alexander Koenig
 */
public class ACLParser extends SAXTemplateHandler
{

  private final String ACCESS_TAG           = "access";

  private final String ACL_TAG              = "acl";

  private final String ALGORITHM_TAG        = "algorithm";

  private final String ENTRY_TAG            = "entry";

  private final String HASH_TAG             = "hash";

  private final String MAY_NOT_DELEGATE_TAG = "may-not-delegate";

  private final String NOT_AFTER_TAG        = "not-after";

  private final String NOT_BEFORE_TAG       = "not-before";

  private final String SUBJECT_TAG          = "subject";

  private final String VALID_TAG            = "valid";

  private final String VALUE_TAG            = "value";

  private boolean      mayNotDelegate       = false;

  private boolean      anySubject           = false;

  private String       access               = "";

  private String       notValidBefore       = null;

  private String       notValidAfter        = null;

  private String       algorithm            = "";

  private String       keyHash              = "";

  private Vector       aclList              = new Vector();

  private ACLEntry     aclEntry             = null;

  /** Retriesves the list of all parsed entries */
  public Vector getACLList()
  {
    return aclList;
  }

  /** Retrieves the ACL entry if parseText contained only a single entry */
  public ACLEntry getACLEntry()
  {
    return aclEntry;
  }

  /**
   * Template function for processing an start element
   */
  public void processStartElement(String uri, String name, String qName, Attributes atts) throws SAXException
  {
    if (getTagCount() == 3 && getTag(0).equals(ACL_TAG) && getTag(1).equals(ENTRY_TAG) &&
      getTag(2).equals(MAY_NOT_DELEGATE_TAG))
    {
      mayNotDelegate = true;
    }
    if (getTagCount() == 4 && getTag(0).equals(ACL_TAG) && getTag(1).equals(ENTRY_TAG) && getTag(2).equals(ACCESS_TAG))
    {
      access = "<" + getCurrentTag() + "/>";
    }
    // for single entries
    if (getTagCount() == 2 && getTag(0).equals(ENTRY_TAG) && getTag(1).equals(MAY_NOT_DELEGATE_TAG))
    {
      mayNotDelegate = true;
    }
    if (getTagCount() == 3 && getTag(0).equals(ENTRY_TAG) && getTag(1).equals(ACCESS_TAG))
    {
      access = "<" + getCurrentTag() + "/>";
    }
  }

  /**
   * Template function for processing an end element
   */
  public void processEndElement(String uri, String localName, String qName) throws SAXException
  {
    // end of one permission
    if (getTagCount() == 2 && getTag(0).equals(ACL_TAG) && getCurrentTag().equals(ENTRY_TAG) && access.length() != 0)
    {
      if (algorithm.length() != 0 && algorithm.equals(CommonConstants.SHA_1_UPNP) && keyHash.length() != 0)
      {
        SecurityAwareObject securityAwareObject = new SecurityAwareObject(CommonConstants.SHA_1_UPNP, keyHash);

        aclList.add(new ACLEntry(securityAwareObject, mayNotDelegate, access, notValidBefore, notValidAfter));
      }
      if (anySubject)
      {
        aclList.add(new ACLEntry(mayNotDelegate, access, notValidBefore, notValidAfter));
      }
      access = "";
      algorithm = "";
      keyHash = "";
      mayNotDelegate = false;
      anySubject = false;
      notValidBefore = null;
      notValidAfter = null;
    }
    // check for single entry
    if (getTagCount() == 1 && getCurrentTag().equals(ENTRY_TAG) && access.length() != 0)
    {
      if (algorithm.length() != 0 && algorithm.equals(CommonConstants.SHA_1_UPNP) && keyHash.length() != 0)
      {
        SecurityAwareObject securityAwareObject = new SecurityAwareObject(CommonConstants.SHA_1_UPNP, keyHash);

        aclEntry = new ACLEntry(securityAwareObject, mayNotDelegate, access, notValidBefore, notValidAfter);
      }
      if (anySubject)
      {
        aclEntry = new ACLEntry(mayNotDelegate, access, notValidBefore, notValidAfter);
      }
      access = "";
      algorithm = "";
      keyHash = "";
      mayNotDelegate = false;
      anySubject = false;
      notValidBefore = null;
      notValidAfter = null;
    }

  }

  /**
   * Template function for processing content
   */
  public void processContentElement(String content)
  {
    // time
    if (getTagCount() == 4 && getTag(0).equals(ACL_TAG) && getTag(1).equals(ENTRY_TAG) && getTag(2).equals(VALID_TAG))
    {
      if (getCurrentTag().equals(NOT_BEFORE_TAG))
      {
        notValidBefore = content;
      }

      if (getCurrentTag().equals(NOT_AFTER_TAG))
      {
        notValidAfter = content;
      }
    }
    // subject
    if (getTagCount() == 5 && getTag(0).equals(ACL_TAG) && getTag(1).equals(ENTRY_TAG) &&
      getTag(2).equals(SUBJECT_TAG) && getTag(3).equals(HASH_TAG))
    {
      if (getCurrentTag().equals(ALGORITHM_TAG))
      {
        algorithm = content;
      }
      if (getCurrentTag().equals(VALUE_TAG))
      {
        keyHash = content;
      }
    }
    // check for single entry
    // time
    if (getTagCount() == 3 && getTag(0).equals(ENTRY_TAG) && getTag(1).equals(VALID_TAG))
    {
      if (getCurrentTag().equals(NOT_BEFORE_TAG))
      {
        notValidBefore = content;
      }

      if (getCurrentTag().equals(NOT_AFTER_TAG))
      {
        notValidAfter = content;
      }
    }
    // subject
    if (getTagCount() == 4 && getTag(0).equals(ENTRY_TAG) && getTag(1).equals(SUBJECT_TAG) &&
      getTag(2).equals(HASH_TAG))
    {
      if (getCurrentTag().equals(ALGORITHM_TAG))
      {
        algorithm = content;
      }
      if (getCurrentTag().equals(VALUE_TAG))
      {
        keyHash = content;
      }
    }
  }
}
