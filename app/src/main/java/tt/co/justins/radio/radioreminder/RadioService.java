package tt.co.justins.radio.radioreminder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RadioService extends Service{

    public static final String SERVICE_SETUP = "tt.co.justins.radio.radioreminder.action.SETUP";
    public static final String SERVICE_EXIT = "tt.co.justins.radio.radioreminder.action.EXIT";
    public static final String SERVICE_EVENT = "tt.co.justins.radio.radioreminder.action.EVENT";

    public static final String EXTRA_EVENT = "tt.co.justins.radio.radioreminder.extra.EVENT";
    public static final String EXTRA_EVENT_POSITION = "tt.co.justins.radio.radioreminder.extra.EVENT_POSITION";

    public static final String ACTION_WIFI_ON = "tt.co.justins.radio.radioreminder.action.WIFI_ON";
    public static final String ACTION_WIFI_OFF = "tt.co.justins.radio.radioreminder.action.WIFI_OFF";
    public static final String ACTION_BLUETOOTH_ON = "tt.co.justins.radio.radioreminder.action.BLUETOOTH_ON";
    public static final String ACTION_BLUETOOTH_OFF = "tt.co.justins.radio.radioreminder.action.BLUETOOTH_OFF";
    public static final String ACTION_BLUETOOTH_DEVICE_CONNECT = "tt.co.justins.radio.radioreminder.action.BLUETOOTH_DEVICE_CONNECT";
    public static final String ACTION_BLUETOOTH_DEVICE_DISCONNECT = "tt.co.justins.radio.radioreminder.action.BLUETOOTH_DEVICE_DISCONNECT";
    public static final String ACTION_POWER_CONNECTED = "tt.co.justins.radio.radioreminder.action.POWER_ON";

    public static final int SERVICE_WIFI = 0;
    public static final int NEW_EVENT = -1;

    private static final String tag = "RadioService";

    private static int notificationId;

    private ConnectivityReceiver wifiReceiver;
    private ConnectivityReceiver batteryReceiver;
    private ConnectivityReceiver bluetoothReceiver;

    private List<Event> eventList;
    private Timer timer;
    private boolean mStarted = false;

    public class RadioBinder extends Binder {
        RadioService getService() {
            return RadioService.this;
        }
    }

    private IBinder mBinder = new RadioBinder();

    //grab the Action from the intent and call the action's handler
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            Log.d(tag, "Started with action: " + action);
            switch(action) {
                case SERVICE_SETUP:
                    if(mStarted == false) {
                        Log.d(tag, "Setting up the service for the first time.");
                        eventList = new ArrayList<>();
                        registerBroadcastReceivers();
                        mStarted = true;
                    } else {
                        Log.d(tag, "Service already started. Ignoring setup.");
                    }
                    break;

                case SERVICE_EXIT:
                    unregisterBroadcastReceivers();
                    break;

                case SERVICE_EVENT:
                    //get the serialized event object and add it to the list
                    Event event = (Event) intent.getSerializableExtra(EXTRA_EVENT);
                    //if this extra is present, this event replaces an existing event in the list
                    int position = intent.getIntExtra(EXTRA_EVENT_POSITION, -1);

                    if(position == -1)
                        addNewEvent(event);
                    else
                        updateEvent(event, position);
                    break;

                case ACTION_BLUETOOTH_OFF:
                case ACTION_BLUETOOTH_ON:
                case ACTION_WIFI_OFF:
                case ACTION_WIFI_ON:
                case ACTION_POWER_CONNECTED:
                    processAction(action);
                    break;
                default:
            }
        }
        return START_STICKY;
    }

    //replace the event at position in the eventlist with this new event
    private void updateEvent(Event event, int position) {
        eventList.set(position, event);
        Log.d(tag, "Updated event at position " + position + " in the event list.");
        logEvent(event);
    }

    //add event to the list of events the service watches for
    //only allows one event per service type
    private void addNewEvent(Event event) {
//        todo allow multipule events of the same type
//        for(Event e : eventList) {
//            if(e.serviceType == event.serviceType) {
//                eventList.remove(e);
//                break;
//            }
//        }
        eventList.add(event);
        Log.d(tag, "Added event to list. Count: " + eventList.size());
        logEvent(event);
    }

    public List<Event> getEventList() {
        Log.d(tag, "Sending event list to application. Size: " + eventList.size());
        return eventList;
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
                        executeDelayedEvent(e);
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

    private class WaitTask extends TimerTask {
        Event e;

        private WaitTask(Event e) {
            this.e = e;
        }

        @Override
        public void run() {
            executeEvent(e);
        }
    }

    private void executeDelayedEvent(Event e) {
        Log.d(tag, "Scheduling event (" + eventList.indexOf(e) + ") to execute in " + e.waitInterval + " mins.");
        timer = new Timer();
        timer.schedule(new WaitTask(e), (e.waitInterval * 60 * 1000));
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
            case ACTION_BLUETOOTH_OFF:
                disableBluetooth();
                break;
            case ACTION_BLUETOOTH_ON:
                enableBluetooth();
                break;
        }
        //todo implement rule lifetime
        //removeEvent(e);
    }

    private void removeEvent(Event e) {
        Log.d(tag, "Removing event (" + eventList.indexOf(e) + ") from list.");
        logEvent(e);
        eventList.remove(e);
    }

    private void logEvent(Event e) {
        Log.d(tag, "-- Watch: " + e.watchAction);
        Log.d(tag, "-- Response: " + e.respondAction);
        if(e.waitAction != null)
            Log.d(tag, "-- Wait Action: " + e.waitAction);
        else if(e.waitInterval != 0)
            Log.d(tag, "-- Wait Interval: " + e.waitInterval + " mins.");
        else
            Log.d(tag, "-- No Wait Specified.");
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

    private void disableBluetooth() {
        BluetoothManager btMan = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btMan.getAdapter().disable();
        Log.d(tag, "Bluetooth Disabled");
    }

    private void enableBluetooth() {
        BluetoothManager btMan = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btMan.getAdapter().enable();
        Log.d(tag, "Bluetooth Enabled");
    }

    //creates a notification and sends it to the notification drawer
    private void sendNotification(String message) {
        Notification.Builder mBuilder = new Notification.Builder(this)
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
    private void registerBroadcastReceivers() {
        wifiReceiver = new ConnectivityReceiver();
        batteryReceiver = new ConnectivityReceiver();
        bluetoothReceiver = new ConnectivityReceiver();

        registerReceiver(wifiReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        Log.d(tag, "Registered CONNECTIVITY_ACTION receiver");
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_POWER_CONNECTED));
        Log.d(tag, "Registered ACTION_POWER_CONNECTED receiver");
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_POWER_DISCONNECTED));
        Log.d(tag, "Registered ACTION_POWER_DISCONNECTED receiver");
        registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED));
    }

    //destroy all the receivers
    private void unregisterBroadcastReceivers() {
        unregisterReceiver(wifiReceiver);
        Log.d(tag, "Unregistered CONNECTIVITY_ACTION receiver");
        unregisterReceiver(batteryReceiver);
        Log.d(tag, "Unregistered ACTION_POWER_CONNECTED receiver");
    }

    //allow binding so the service can provide the event list
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
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
