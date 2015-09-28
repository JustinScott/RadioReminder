package tt.co.justins.radio.radioreminder;

import java.io.Serializable;

/*
This class describes the event to watch for and how to respond to that event
serviceType -
watchAction - the event to watch for
respondAction - the action that is sent in response to the watched action
waitAction - delays the response action until this action is detected
waitInterval - delays the response action until this time is reached
*/

public class Event implements Serializable {
    public static final int NOT_WAITING = 1;
    public static final int WAITING = 0;

    public int serviceType;
    public RadioAction.Action watchAction;
    public RadioAction.Action respondAction;
    public RadioAction.Action waitAction;
    public int waitInterval;
    public int state;
    public String name;
    public String netDev;

    public Event() {
        name = "new event";
        state = NOT_WAITING;
        waitInterval = 0;
        waitAction = null;
        watchAction = RadioAction.Action.WIFI_OFF;
        respondAction = RadioAction.Action.WIFI_OFF;
        netDev = "";
    }
}