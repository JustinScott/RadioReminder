package tt.co.justins.radio.radioreminder;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class RadioService extends Service {

    public static final String SERVICE_SETUP = "tt.co.justins.radio.radioreminder.action.SETUP";
    public static final String SERVICE_EXIT = "tt.co.justins.radio.radioreminder.action.EXIT";
    public static final String SERVICE_EVENT = "tt.co.justins.radio.radioreminder.action.EVENT";

    public static final String EXTRA_EVENT = "tt.co.justins.radio.radioreminder.extra.EVENT";

    public static final String ACTION_WIFI_ON = "tt.co.justins.radio.radioreminder.action.WIFI_ON";
    public static final String ACTION_WIFI_OFF = "tt.co.justins.radio.radioreminder.action.WIFI_OFF";
    public static final String ACTION_POWER_CONNECTED = "tt.co.justins.radio.radioreminder.action.POWER_ON";

    public static final int SERVICE_WIFI = 0;

    private static final String tag = "radioreminder";

    private static int notificationId;

    private ConnectivityReceiver wifiReceiver;
    private ConnectivityReceiver batteryReceiver;

   private List<Event> eventList;

    //grab the Action from the intent and call the action's handler
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            Log.d(tag, "Service started with action: " + action);
            switch(action) {
                case SERVICE_SETUP:
                    handleActionSetup();
                    break;
                case SERVICE_EXIT:
                    handleActionExit();
                    break;
                case SERVICE_EVENT:
                    //get the serialized event object and add it to the list
                    Event event = (Event) intent.getSerializableExtra(EXTRA_EVENT);
                    addNewEvent(event);
                    break;
                case ACTION_WIFI_OFF:
                case ACTION_POWER_CONNECTED:
                    processAction(action);
                    break;
                default:
            }
        }
        return START_STICKY;
    }

    //add event to the list of events the service watches for
    //only allows one event per service type
    private void addNewEvent(Event event) {
        for(Event e : eventList) {
            if(e.serviceType == event.serviceType) {
                eventList.remove(e);
                break;
            }
        }
        eventList.add(event);
        Log.d(tag, "Added event to list. Count: " + eventList.size());
    }

    //if match is found check to see if the event needs to wait for some time or another event
    //if not execute the event
    private void processAction(String action) {
        //cycle through the event list to see if any of the events are looking for this watch action
        Log.d(tag, "Processing action: " + action);
        for(Event e : eventList) {
            if (e.watchAction.equals(action)) {
                //mark the event so it knows the watch action has occured
                Log.d(tag, "Watch action for event (" + eventList.indexOf(e) + ") detected: " + action);
                if(e.waitAction != null) {
                    e.state = Event.WAITING;
                    Log.d(tag, "Putting the event in the waiting STATE");
                }
                //mark the event so it knows the watch action has occured
                else if(e.waitInterval != 0) {
                    if(e.state == Event.NOT_WAITING) {
                        //set timer then call execute event
                    }
                    e.state = Event.WAITING;
                    return;
                }
                //event doesn't require a delay, so execute immediately
                else {
                    executeEvent(e);
                }
            }
        }

        //cycle through the list and see if any of the events are waiting for this action
        for(Event e : eventList) {
            if (e.waitAction.equals(action)) {
                if(e.state == Event.WAITING) {
                    Log.d(tag, "Wait action for event (" + eventList.indexOf(e) + ") detected: " + action);
                    executeEvent(e);
                    return;
                }
            }
        }
    }

    private void executeEvent(Event e) {
        Log.d(tag, "Executing event (" + eventList.indexOf(e) + ") with action:" + e.respondAction);
        switch(e.respondAction) {
            case ACTION_WIFI_OFF:
                disableWifi();
                break;
            case ACTION_WIFI_ON:
                enableWifi();
                break;
        }
        removeEvent(e);
    }

    private void removeEvent(Event e) {
        Log.d(tag, "Removing event (" + eventList.indexOf(e) + ") from list.");
        Log.d(tag, "-- Watch: " + e.watchAction);
        Log.d(tag, "-- Response: " + e.respondAction);
        if(e.watchAction != null)
            Log.d(tag, "-- Wait Action: " + e.watchAction);
        else if(e.waitInterval != 0)
            Log.d(tag, "-- Wait Interval: " + e.waitInterval);
        else
            Log.d(tag, "-- No Wait Specified");

        eventList.remove(e);
    }

    private void disableWifi() {
        WifiManager wifiMan = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiMan.setWifiEnabled(false);
        Log.d(tag, "WIFI Disabled");
    }

    private void enableWifi() {
        WifiManager wifiMan = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiMan.setWifiEnabled(true);
        Log.d(tag, "WIFI Enabled");
    }

    //creates a notification and sends it to the notification drawer
    private void sendNotification(String message) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("Radio Reminder")
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_launcher);

        Intent emptyIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, emptyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(notificationId, mBuilder.build());
    }

    private void removeNotification(int notificationId) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(notificationId);
        Log.d(tag, "Notification removed");
    }

    //initialize all the receivers for the services that are being watched
    private void handleActionSetup() {
        wifiReceiver = new ConnectivityReceiver();
        batteryReceiver = new ConnectivityReceiver();
        eventList = MyActivity.eventList;

        registerReceiver(wifiReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        Log.d(tag, "Registered CONNECTIVITY_ACTION receiver");
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_POWER_CONNECTED));
        Log.d(tag, "Registered ACTION_POWER_CONNECTED receiver");
    }

    //destroy all the receivers
    private void handleActionExit() {
        unregisterReceiver(wifiReceiver);
        Log.d(tag, "Unregistered CONNECTIVITY_ACTION receiver");
        unregisterReceiver(batteryReceiver);
        Log.d(tag, "Unregistered ACTION_POWER_CONNECTED receiver");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
