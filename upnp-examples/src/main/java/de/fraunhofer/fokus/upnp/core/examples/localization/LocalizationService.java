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
package de.fraunhofer.fokus.upnp.core.examples.localization;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.device.Action;
import de.fraunhofer.fokus.upnp.core.device.StateVariable;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateService;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;

/**
 * This class provides a localization service.
 * 
 * @author Alexander Koenig
 */
public class LocalizationService extends TemplateService
{

  public static final String UNKNOWN_LOCATION = "Unknown";

  /** List of known users */
  private Vector             userVector;

  private String             userListString;

  /** List of known locations */
  private Vector             locationVector;

  private String             locationListString;

  /** List of events */
  private Vector             eventVector;

  private ILocationProvider  locationProvider;

  // required state variables
  private StateVariable      locationList;

  private StateVariable      userList;

  private StateVariable      currentEventID;

  private StateVariable      active;

  private StateVariable      A_ARG_TYPE_EventID;

  private StateVariable      A_ARG_TYPE_EventIDs;

  private StateVariable      A_ARG_TYPE_EventType;

  private StateVariable      A_ARG_TYPE_Date;

  private StateVariable      A_ARG_TYPE_User;

  private StateVariable      A_ARG_TYPE_Location;

  private StateVariable      A_ARG_TYPE_Count;

  private StateVariable      A_ARG_TYPE_Index;

  private Action             getLocation;

  private Action             getEvent;

  private Action             getEventsForUser;

  private Action             getEventsForLocation;

  /** Creates a new instance of LocalizationService */
  public LocalizationService(TemplateDevice device, ILocationProvider locationProvider)
  {
    super(device, LocalizationConstant.LOCALIZATION_SERVICE_TYPE, LocalizationConstant.LOCALIZATION_SERVICE_ID, false);

    this.locationProvider = locationProvider;

    runDelayed();
  }

  public void setupServiceVariables()
  {
    super.setupServiceVariables();

    userVector = new Vector();
    userListString = locationProvider.getEntityIDList();
    StringTokenizer stringTokenizer = new StringTokenizer(userListString, ",");
    while (stringTokenizer.hasMoreTokens())
    {
      UserObject newUser = new UserObject(stringTokenizer.nextToken().trim());
      userVector.add(newUser);
    }

    locationVector = new Vector();
    locationListString = locationProvider.getLocationList();
    stringTokenizer = new StringTokenizer(locationListString, ",");
    while (stringTokenizer.hasMoreTokens())
    {
      locationVector.add(stringTokenizer.nextToken().trim());
    }

    eventVector = new Vector();
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
    // state Variables
    locationList = new StateVariable("LocationList", locationListString, true);
    userList = new StateVariable("UserList", userListString, true);
    currentEventID = new StateVariable("CurrentEventID", -1, true);
    active = new StateVariable("Active", false, true);
    A_ARG_TYPE_EventID = new StateVariable("A_ARG_TYPE_EventID", 0, false);
    A_ARG_TYPE_EventIDs = new StateVariable("A_ARG_TYPE_EventIDs", "", false);
    A_ARG_TYPE_EventType = new StateVariable("A_ARG_TYPE_EventType", "", false);
    A_ARG_TYPE_Date = new StateVariable("A_ARG_TYPE_Date", "", false);
    A_ARG_TYPE_User = new StateVariable("A_ARG_TYPE_User", "", false);
    A_ARG_TYPE_Location = new StateVariable("A_ARG_TYPE_Location", "", false);
    A_ARG_TYPE_Count = new StateVariable("A_ARG_TYPE_Count", 0, false);
    A_ARG_TYPE_Index = new StateVariable("A_ARG_TYPE_Index", 0, false);

    StateVariable[] stateVariables =
      {
          locationList, userList, currentEventID, active, A_ARG_TYPE_EventID, A_ARG_TYPE_EventIDs,
          A_ARG_TYPE_EventType, A_ARG_TYPE_Date, A_ARG_TYPE_User, A_ARG_TYPE_Location, A_ARG_TYPE_Count,
          A_ARG_TYPE_Index
      };
    setStateVariableTable(stateVariables);

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // Actions //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    getLocation = new Action("GetLocation");
    getLocation.setArgumentTable(new Argument[] {
        new Argument("User", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_User),
        new Argument("Location", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_Location)
    });

    getEvent = new Action("GetEvent");
    getEvent.setArgumentTable(new Argument[] {
        new Argument("EventID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_EventID),
        new Argument("User", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_User),
        new Argument("Location", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_Location),
        new Argument("Date", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_Date)
    });

    getEventsForUser = new Action("GetEventsForUser");
    getEventsForUser.setArgumentTable(new Argument[] {
        new Argument("User", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_User),
        new Argument("StartingIndex", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_Index),
        new Argument("RequestedCount", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_Count),
        new Argument("EventIDs", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_EventIDs),
        new Argument("NumberReturned", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_Count)
    });

    // getEventsForLocation
    getEventsForLocation = new Action("GetEventsForLocation");
    getEventsForLocation.setArgumentTable(new Argument[] {
        new Argument("Location", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_Location),
        new Argument("StartingIndex", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_Index),
        new Argument("RequestedCount", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_Count),
        new Argument("EventIDs", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_EventIDs),
        new Argument("NumberReturned", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_Count)
    });

    setActionTable(new Action[] {
        getLocation, getEvent, getEventsForUser, getEventsForLocation
    });
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Action implementation //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  public void getLocation(Argument[] args) throws ActionFailedException
  {
    // printMessage("GetLocation action invoked");
    String userName;
    // check argument
    try
    {
      userName = args[0].getStringValue();
      UserObject user = getUserByName(userName);
      if (user != null)
      {
        args[1].setValue(user.location);
        return;
      }
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    throw new ActionFailedException(801, "Unknown userID");
  }

  public void getEvent(Argument[] args) throws ActionFailedException
  {
    // printMessage("GetEvent action invoked");
    long eventID;
    // check argument
    try
    {
      eventID = args[0].getNumericValue();
      EventObject event = getEventByID((int)eventID);
      if (event != null)
      {
        // set result
        args[1].setValue(event.user);
        args[2].setValue(event.location);
        args[3].setValue(event.dateToString());

        return;
      }
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    throw new ActionFailedException(803, "Invalid eventID");
  }

  public void getEventsForUser(Argument[] args) throws ActionFailedException
  {
    printMessage("GetEventsForUser action invoked");
    String userName;
    UserObject user = null;
    int index;
    int count;
    int returned = 0;
    String result = "";
    // check arguments
    try
    {
      userName = args[0].getStringValue();
      user = getUserByName(userName);
      if (user != null)
      {
        index = (int)args[1].getNumericValue();
        if (index < 0)
        {
          index = 0;
        }

        count = (int)args[2].getNumericValue();
        if (count == 0)
        {
          count = eventVector.size();
        }

        // search all events for this user
        for (int i = index; i < eventVector.size() && returned < count; i++)
        {
          LocalizationService.EventObject currentEvent = (LocalizationService.EventObject)eventVector.elementAt(i);

          if (currentEvent.user.equals(userName))
          {
            result += (result.equals("") ? "" : ",") + Integer.toString(i);
            returned++;
          }
        }
        // set result
        args[3].setValue(result);
        args[4].setNumericValue(returned);
        return;
      }
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    throw new ActionFailedException(801, "Unknown userID");
  }

  public void getEventsForLocation(Argument[] args) throws ActionFailedException
  {
    printMessage("GetEventsForLocation action invoked");
    String location;
    int index;
    int count;
    int returned = 0;
    String result = "";
    // check arguments
    try
    {
      location = args[0].getStringValue();
      index = (int)args[1].getNumericValue();
      if (index < 0)
      {
        index = 0;
      }

      count = (int)args[2].getNumericValue();
      if (count == 0)
      {
        count = eventVector.size();
      }

      // search all events for this location
      for (int i = index; i < eventVector.size() && returned < count; i++)
      {
        LocalizationService.EventObject currentEvent = (LocalizationService.EventObject)eventVector.elementAt(i);

        if (currentEvent.location.equals(location))
        {
          result += (result.equals("") ? "" : ",") + Integer.toString(i);
          returned++;
        }
      }
      // set result
      args[3].setValue(result);
      args[4].setNumericValue(returned);
      if (location != null)
      {
        return;
      }
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    throw new ActionFailedException(802, "Invalid locationID");
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Event methods //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /** Event that the location for a user has changed */
  public void locationEvent(String user, String location)
  {
    System.out.println("Received location event for " + user + " at " + location);
    // update user object
    UserObject userObject = getUserByName(user);
    if (userObject != null)
    {
      if (!isKnownLocation(location))
      {
        location = UNKNOWN_LOCATION;
      }

      if (!userObject.location.equals(location))
      {
        userObject.location = location;
        try
        {
          // create event
          // increase event id
          int newEventID = (int)currentEventID.getNumericValue() + 1;
          EventObject newEvent = new EventObject(newEventID, user, location);
          eventVector.add(newEvent);

          // trigger event
          currentEventID.setNumericValue(newEventID);
        } catch (Exception e)
        {
          System.out.println("Error registering event. " + e.getMessage());
        }
      }
    }
  }

  /** Event that the location for a user has changed */
  public String getLocationForUser(String user)
  {
    // update user object
    UserObject userObject = getUserByName(user);
    if (userObject != null)
    {
      return userObject.location;
    }
    return UNKNOWN_LOCATION;
  }

  /** Event that the state of the location service has changed */
  public void setActive(boolean state)
  {
    try
    {
      active.setBooleanValue(state);
    } catch (Exception e)
    {
      System.out.println("Error registering event. " + e.getMessage());
    }
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Private methods //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  private UserObject getUserByName(String name)
  {
    for (int i = 0; i < userVector.size(); i++)
    {
      UserObject currentUser = (UserObject)userVector.elementAt(i);

      if (currentUser.name.equals(name))
      {
        return currentUser;
      }
    }
    return null;
  }

  private boolean isKnownLocation(String location)
  {
    return locationVector.contains(location);
  }

  private EventObject getEventByID(int id)
  {
    if (id >= 0 && id < eventVector.size())
    {
      return (LocalizationService.EventObject)eventVector.elementAt(id);
    }

    return null;
  }

  private class UserObject
  {
    String name;

    String location;

    public UserObject(String name)
    {
      this.name = name;
      this.location = UNKNOWN_LOCATION;
    }
  }

  private class EventObject
  {
    int    index;

    String user;

    String location;

    Date   date;

    public EventObject(int index, String user, String location)
    {
      this.index = index;
      this.user = user;
      this.location = location;
      this.date = Calendar.getInstance().getTime();
    }

    public String dateToString()
    {
      SimpleDateFormat formatter = new SimpleDateFormat();
      formatter.applyPattern("dd.MM.yyyy - HH:mm:ss.SSS");

      return formatter.format(date);
    }

    public String toString()
    {
      return "Event nr." + index + ": " + dateToString() + ": " + user + "appeared at " + location;
    }
  }

}
