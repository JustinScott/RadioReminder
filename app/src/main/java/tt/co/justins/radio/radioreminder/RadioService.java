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
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RadioService extends Service{

    public static final String SERVICE_EXIT = "tt.co.justins.radio.radioreminder.action.EXIT";
    public static final String SERVICE_EVENT = "tt.co.justins.radio.radioreminder.action.EVENT";
    public static final String SERVICE_EVENT_DELETE = "tt.co.justins.radio.radioreminder.action.EVENT_DELETE";

    public static final String EXTRA_EVENT = "tt.co.justins.radio.radioreminder.extra.EVENT";
    public static final String EXTRA_EVENT_POSITION = "tt.co.justins.radio.radioreminder.extra.EVENT_POSITION";
    public static final String EXTRA_NETWORK_DEVICE = "tt.co.justins.radio.radioreminder.extra.NETWORK_DEVICE";

    public static final int SERVICE_WIFI = 0;
    public static final int NEW_EVENT = -1;

    private static final String tag = "RadioService";

    private static int sServiceNotificationId = 0;
    private int sServiceNotificationColor = 0x4c99;
    private boolean mServiceNotificationSet = false;
    private Notification mServiceNotification;

    private ConnectivityReceiver radioBroadcastReceiver;
    private ConnectivityReceiver batteryReceiver;
    private ConnectivityReceiver bluetoothReceiver;

    private List<Event> eventList;
    private Timer timer;
    private boolean mServiceInitialized = false;
    private String mSaveFileName = "eventlist.save";

    public class RadioBinder extends Binder {
        RadioService getService() {
            return RadioService.this;
        }
    }

    private IBinder mBinder = new RadioBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(tag, "onCreate called.");

        //readListFromFile();

        if(eventList == null || eventList.size() == 0) {
            //call this incase the service was killed without calling onDestroy
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancelAll();
        } else {
            setServiceNotification();
        }
    }

    //grab the Action from the intent and call the action's handler
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            Bundle extras = intent.getExtras();
            String netDev = "";
            if(extras != null) {
                netDev = extras.getString(EXTRA_NETWORK_DEVICE, "");
            }
            int position;
            Log.d(tag, "Service started with action: " + action);
            switch(action) {
                case SERVICE_EXIT:
                    stopSelf();
                    break;

                case SERVICE_EVENT_DELETE:
                    position = intent.getIntExtra(EXTRA_EVENT_POSITION, -1);
                    removeEvent(position);

                    if(eventList.size() > 0)
                        setServiceNotification();
                    else
                        removeServiceNotification();

                    writeListToFile();
                    break;

                case SERVICE_EVENT:
                    if(mServiceInitialized == false) {
                        Log.d(tag, "Initializing event list.");
                        //todo kind of broken, check to see if oncreate restored the list
                        if(eventList == null)
                            eventList = new ArrayList<>();
                        registerBroadcastReceivers();
                        mServiceInitialized = true;
                    } else {
                        Log.d(tag, "Service already started. Ignoring setup.");
                    }

                    //get the serialized event object and add it to the list
                    Event event = (Event) intent.getSerializableExtra(EXTRA_EVENT);
                    //if this extra is present, this event replaces an existing event in the list
                    position = intent.getIntExtra(EXTRA_EVENT_POSITION, -1);

                    if(position == -1) {
                        addNewEvent(event);
                        setServiceNotification();
                    } else
                        updateEvent(event, position);

                    writeListToFile();
                    break;

                //processAction should ignore any unknown actions, so there is no need to have a
                //case for each action dropping through to process action.
                default:
                    processAction(action, netDev);
                    break;
            }
        }
        return START_STICKY;
    }

    private void removeEvent(int position) {
        Log.v(tag, "RemoveEvent called.");
        if(eventList !=  null) {
            if (position < 0 || position >= eventList.size()) {
                Log.d(tag, "Bad position value in delete event.");
            } else {
                eventList.remove(position);
                Log.d(tag, "Removed element (" + position + ") in event list.");
            }
        }
    }

    //replace the event at position in the eventlist with this new event
    private void updateEvent(Event event, int position) {
        eventList.set(position, event);
        Log.d(tag, "Updated event at position " + position + " in the event list.");
        logEvent(event);
    }

    //add event to the list of events the service watches for
    private void addNewEvent(Event event) {
        eventList.add(event);
        Log.v(tag, "Added event to list. Count: " + eventList.size());
        logEvent(event);
    }

    public List<Event> getEventList() {
        String size = (eventList != null) ? eventList.size() + "" : "null";
        Log.v(tag, "Sending event list to application. Size: " + size);
        return eventList;
    }

    public int getEventListSize() {
        int size = 0;
        if(eventList != null)
            size = eventList.size();
        Log.v(tag, "Sending event list size to application. Size: " + size);
        return size;
    }

    public boolean isServiceStarted() {
        return mServiceInitialized;
    }

    //if match is found check to see if the event needs to wait for some time or another event
    //if not execute the event
    private void processAction(String actionKey, String netDev) {
        //cycle through the event list to see if any of the events are looking for this watch action
        Log.v(tag, "Processing action: " + actionKey);
        for(Event e : eventList) {
            if (RadioAction.getKeyFromAction(e.watchAction).equals(actionKey)) {
                //check if its a device network action, and compair name
                if((e.watchAction == RadioAction.Action.BLUETOOTH_DEVICE_CONNECT ||
                        e.watchAction == RadioAction.Action.BLUETOOTH_DEVICE_DISCONNECT ||
                        e.watchAction == RadioAction.Action.WIFI_NETWORK_CONNECT ||
                        e.watchAction == RadioAction.Action.WIFI_NETWORK_DISCONNECT) &&
                        !e.netDev.equals(netDev)) {
                    Log.d(tag, "Network / Device mismatch, ignoring action. (" + e.netDev + ") (" + netDev + ")");
                    break;
                }
                //mark the event so it knows the watch action has occured
                Log.d(tag, "Watch action for event (" + eventList.indexOf(e) + ") detected: " + actionKey);
                if (e.waitAction != null) {
                    e.state = Event.WAITING;
                    Log.d(tag, "Putting the event in the waiting STATE");
                }
                //mark the event so it knows the watch action has occured
                else if (e.waitInterval != 0) {
                    //set timer then call execute event
                    executeDelayedEvent(e);
                }
                //event doesn't require a delay, so execute immediately
                else {
                    executeEvent(e);
                }
            }
        }

        //cycle through the list and see if any of the events are waiting for this action
        for(Event e : eventList) {
            if (RadioAction.getKeyFromAction(e.waitAction).equals(actionKey)) {
                //check if its a device network action, and the values match
                if((e.watchAction == RadioAction.Action.BLUETOOTH_DEVICE_CONNECT ||
                        e.watchAction == RadioAction.Action.BLUETOOTH_DEVICE_DISCONNECT ||
                        e.watchAction == RadioAction.Action.WIFI_NETWORK_CONNECT ||
                        e.watchAction == RadioAction.Action.WIFI_NETWORK_DISCONNECT) &&
                                !e.netDev.equals(netDev)) {
                    Log.d(tag, "Network / Device mismatch, ignoring action. (" + e.netDev + ") (" + netDev + ")");
                    break;
                }

                if (e.state == Event.WAITING) {
                    Log.d(tag, "Wait action for event (" + eventList.indexOf(e) + ") detected: " + actionKey);
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
        Log.d(tag, "Scheduling event (" + eventList.indexOf(e) + ") to execute in " + e.waitInterval + " min(s).");
        timer = new Timer();
        timer.schedule(new WaitTask(e), (e.waitInterval * 60 * 1000));
    }

    private void executeEvent(Event e) {
        Log.d(tag, "Executing event (" + eventList.indexOf(e) + ") with action:" + e.respondAction);
        switch(e.respondAction) {
            case WIFI_ON:
                disableWifi();
                break;
            case WIFI_OFF:
                enableWifi();
                break;
            case BLUETOOTH_ON:
                enableBluetooth();
                break;
            case BLUETOOTH_OFF:
                disableBluetooth();
                break;
            case LOCATION_ON:
                break;
            case LOCATION_OFF:
                break;
            case CELL_DATA_ON:
                break;
            case CELL_DATA_OFF:
                break;
        }
        //todo implement rule lifetime
        //removeEvent(e);
    }

    private void logEvent(Event e) {
        Log.v(tag, "-- Watch: " + e.watchAction);
        Log.v(tag, "-- Response: " + e.respondAction);
        if(e.waitAction != null)
            Log.v(tag, "-- Wait Action: " + e.waitAction);
        else if(e.waitInterval != 0)
            Log.v(tag, "-- Wait Interval: " + e.waitInterval + " mins.");
        else
            Log.v(tag, "-- No Wait Specified.");
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

    //creates a notification and makes it a foreground service
    private void setServiceNotification() {
        String message = "Tracking " + eventList.size() + " event(s). Touch to view them.";
        Notification.Builder mBuilder = new Notification.Builder(this)
                .setContentTitle("Radio Reminder")
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_notification)
                .setOngoing(true)
                .setColor(sServiceNotificationColor);

        Intent activityIntent = new Intent(this, ListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);
        mServiceNotification = mBuilder.build();

        Log.d(tag, "Foreground started, and notification set.");
        startForeground(sServiceNotificationId, mServiceNotification);
        mServiceNotificationSet = true;
    }

    private void removeServiceNotification() {
        if(mServiceNotificationSet) {
            stopForeground(true);
            Log.d(tag, "Foreground stopped, and Notification removed.");
            mServiceNotificationSet = false;
        }
    }

    private void writeListToFile() {
        try {
            FileOutputStream fileOut = openFileOutput(mSaveFileName, Context.MODE_PRIVATE);
            ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
            objOut.writeObject(eventList);
            objOut.close();
            fileOut.close();
            Log.d(tag, "Saved eventlist to file.");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readListFromFile() {
        try {
            FileInputStream fileIn = openFileInput(mSaveFileName);
            ObjectInputStream objIn = new ObjectInputStream(fileIn);
            eventList = (List<Event>) objIn.readObject();
            fileIn.close();
            objIn.close();
            Log.d(tag, "Restored eventList from save file. Size: " + eventList.size());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    //initialize all the receivers for the services that are being watched
    private void registerBroadcastReceivers() {
        radioBroadcastReceiver = new ConnectivityReceiver();

        registerReceiver(radioBroadcastReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
        registerReceiver(radioBroadcastReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
        Log.v(tag, "Registered WIFI broadcast receiver");
        registerReceiver(radioBroadcastReceiver, new IntentFilter(Intent.ACTION_POWER_CONNECTED));
        registerReceiver(radioBroadcastReceiver, new IntentFilter(Intent.ACTION_POWER_DISCONNECTED));
        Log.v(tag, "Registered power broadcast receiver");
        registerReceiver(radioBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        registerReceiver(radioBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED));
    }

    //destroy all the receivers
    private void unregisterBroadcastReceivers() {
        unregisterReceiver(radioBroadcastReceiver);
        Log.d(tag, "Unregistered broadcast receiver.");
    }

    //allow binding so the service can provide the event list
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        Log.v(tag, "onDestroy called.");
        if(mServiceNotificationSet)
            removeServiceNotification();
        if(mServiceInitialized)
            unregisterBroadcastReceivers();
        super.onDestroy();
    }
}
