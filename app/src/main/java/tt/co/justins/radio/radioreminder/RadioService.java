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

    public static final String EXTRA_PARAM1 = "tt.co.justins.radio.radioreminder.extra.PARAM1";
    public static final String EXTRA_PARAM2 = "tt.co.justins.radio.radioreminder.extra.PARAM2";

    public static final int SERVICE_WIFI = 0;

    private static final String tag = "radioreminder";


    private static int mId;
    private boolean waiting = false;

    private ConnectivityReceiver wifiReceiver;
    private ConnectivityReceiver batteryReceiver;

    private List<Event> eventList = new ArrayList<Event>();

    public RadioService() {
    }

    @Override
    //grab the Action from the intent and call the action's handler
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            switch(intent.getAction()) {
                case SERVICE_SETUP:
                    handleActionSetup();
                    break;
                case SERVICE_EXIT:
                    handleActionExit();
                    break;
                case SERVICE_EVENT:
                    Event event = (Event) intent.getSerializableExtra(EXTRA_EVENT);
                    addNewEvent(event);
                    break;
                case ACTION_WIFI_OFF:
                case ACTION_POWER_CONNECTED:
                    processAction(intent.getAction());
                    break;
                default:
            }
        }
        return START_STICKY;
    }

    private void addNewEvent(Event event) {
        for(Event e : eventList) {
            if(e.serviceType == event.serviceType) {
                eventList.remove(e);
                break;
            }
        }
        eventList.add(event);
        Log.d(tag, "Added event to list. Count: " + eventList.size());

        sendNotification("Service activated");

    }

    private void processAction(String action) {
        for(Event e : eventList) {
            if (e.watchAction.equals(action)) {
                if(e.state == Event.NOT_WAITING) {
                    executeEvent(e);
                    return;
                }
                else if(e.waitInterval != 0) {
                    //set timer then call execute event
                    return;
                }
            }
        }

        for(Event e : eventList) {
            if (e.waitAction.equals(action)) {
                if(e.state == Event.WAITING) {
                    executeEvent(e);
                    return;
                }
            }
        }
        //sendNotification("Waiting to get plugged in before restarting wifi");
        //Log.d(tag, "Notification sent");
        //waiting = true;
    }

    private void executeEvent(Event e) {
        switch(e.respondAction) {
            case ACTION_WIFI_OFF:
                disableWifi();
                break;
            case ACTION_WIFI_ON:
                enableWifi();
                break;
        }
    }

    private void disableWifi() {
        //turn off wifi
        WifiManager wifiMan = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiMan.setWifiEnabled(false);
        Log.d(tag, "WIFI Disabled");

    }

    private void enableWifi() {
        //turn on wifi
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
        mNotificationManager.notify(mId, mBuilder.build());
    }

    //This fucnction is called when the device is plugged in to a power source
    private void handleActionPowerConnected(String param1, String param2) {
        //remove notification
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(mId);
        Log.d(tag, "Notification removed");

        waiting = false;
    }

    //initialize all the receivers for the services that are being watched
    private void handleActionSetup() {
        wifiReceiver = new ConnectivityReceiver();
        batteryReceiver = new ConnectivityReceiver();

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
