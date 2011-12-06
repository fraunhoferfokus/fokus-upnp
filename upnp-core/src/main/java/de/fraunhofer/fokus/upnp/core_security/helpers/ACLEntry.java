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
package de.fraunhofer.fokus.upnp.core_security.helpers;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Vector;

/**
 * This class holds one access permission for a specific subject. It is stored on the device to
 * verify action requests made by that subject (e.g. a control point)
 * 
 * @author Alexander Koenig
 */
public class ACLEntry
{

  private SecurityAwareObject subject;         // control point or group with granted rights

  private boolean             mayNotDelegate;

  private boolean             anySubject;

  private String              accessPermission;

  private String              notValidBefore;

  private String              notValidAfter;

  /** Creates a new instance of ACLEntry */
  public ACLEntry(SecurityAwareObject subject, boolean mayNotDelegate, String accessPermission)
  {
    this.subject = subject;
    this.mayNotDelegate = mayNotDelegate;
    this.accessPermission = accessPermission;
    notValidBefore = null;
    notValidAfter = null;
    anySubject = false;
  }

  /** Creates a new instance of a time limited ACLEntry */
  public ACLEntry(SecurityAwareObject subject,
    boolean mayNotDelegate,
    String accessPermission,
    String notValidBefore,
    String notValidAfter)
  {
    this.subject = subject;
    this.mayNotDelegate = mayNotDelegate;
    this.accessPermission = accessPermission;
    this.notValidBefore = notValidBefore;
    this.notValidAfter = notValidAfter;
    anySubject = false;
  }

  /** Creates a new instance of a time limited ACLEntry that is valid for all subjects */
  public ACLEntry(boolean mayNotDelegate, String accessPermission, String notValidBefore, String notValidAfter)
  {
    subject = null;
    this.mayNotDelegate = mayNotDelegate;
    this.accessPermission = accessPermission;
    this.notValidBefore = notValidBefore;
    this.notValidAfter = notValidAfter;
    anySubject = true;
  }

  /** Checks if this ACL entry grants rights to object */
  public boolean isAssociatedSubject(SecurityAwareObject object)
  {
    return anySubject || subject.equals(object);
  }

  /** Retrieves the subject associated with this entry */
  public SecurityAwareObject getSubject()
  {
    return subject;
  }

  /** Checks if the ACLEntry is valid right now */
  public boolean isValidTime()
  {
    return (notValidBefore == null || getCurrentTimeString().compareTo(notValidBefore) >= 0) &&
      (notValidAfter == null || getCurrentTimeString().compareTo(notValidAfter) <= 0);
  }

  /** Returns an XML description of this entry */
  public String toXMLDescription()
  {
    String result = "<entry>";
    if (anySubject)
    {
      result += "<subject><any/></subject>";
    } else
    {
      result += "<subject>" + subject.toXMLDescription() + "</subject>";
    }

    if (mayNotDelegate)
    {
      result += "<may-not-delegate/>";
    }
    result += "<access>" + accessPermission + "</access>";
    if (notValidAfter != null || notValidBefore != null)
    {
      result += "<valid>";
      if (notValidBefore != null)
      {
        result += "<not-before>" + notValidBefore + "</not-before>";
      }
      if (notValidAfter != null)
      {
        result += "<not-after>" + notValidAfter + "</not-after>";
      }
      result += "</valid>";
    }
    result += "</entry>";
    return result;
  }

  /** Creates a certificate that corresponds to this ACL entry */
  public AuthorizationCertificate toCertificate(SecurityAwareObject issuer,
    SecurityAwareObject target,
    String notValidBefore,
    String notValidAfter)
  {
    // Vector that holds only one permission
    Vector permissions = new Vector();
    permissions.add(accessPermission);
    return new AuthorizationCertificate(issuer, subject, target, permissions, notValidBefore, notValidAfter);
  }

  /**
   * Getter for property mayNotDelegate.
   * 
   * @return Value of property mayNotDelegate.
   */
  public boolean isMayNotDelegate()
  {
    return this.mayNotDelegate;
  }

  /**
   * Getter for property accessPermission.
   * 
   * @return Value of property accessPermission.
   */
  public String getAccessPermission()
  {
    return this.accessPermission;
  }

  /** Retrieves the current time as comparable string for validity */
  private String getCurrentTimeString()
  {
    Calendar calendar = Calendar.getInstance();
    String result = "";
    result += new DecimalFormat("0000").format(calendar.get(Calendar.YEAR)) + "-";
    result += new DecimalFormat("00").format(calendar.get(Calendar.MONTH)) + "-";
    result += new DecimalFormat("00").format(calendar.get(Calendar.DAY_OF_MONTH)) + "T";
    result += new DecimalFormat("00").format(calendar.get(Calendar.HOUR_OF_DAY)) + ":";
    result += new DecimalFormat("00").format(calendar.get(Calendar.MINUTE)) + ":";
    result += new DecimalFormat("00").format(calendar.get(Calendar.SECOND)) + "Z";

    return result;
  }

}
