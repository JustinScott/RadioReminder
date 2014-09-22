package tt.co.justins.radio.radioreminder;

import java.io.Serializable;

/**
 * Created by Justin on 9/21/2014.
 */

/*
This class describes the event to watch for and how to respond to that event
serviceType -
watchAction - the event to watch for
respondAction - the action that is sent in response to the watched action
afterAction - delays the response action until this action is detected
afterInterval - delays the response action until this time is reached
*/

public class Event implements Serializable {
    public int serviceType;
    public String watchAction;
    public String respondAction;
    public String afterAction;
    public float afterInterval;

    public Event() {
    }
}