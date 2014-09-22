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

import java.security.PublicKey;

public class RadioService extends Service {

    private static final String ACTION_SETUP = "tt.co.justins.radio.radioreminder.action.SETUP";
    private static final String ACTION_EXIT = "tt.co.justins.radio.radioreminder.action.EXIT";

    public static final String ACTION_WIFI_ON = "tt.co.justins.radio.radioreminder.action.WIFI_ON";
    public static final String ACTION_WIFI_OFF = "tt.co.justins.radio.radioreminder.action.WIFI_OFF";
    public static final String ACTION_POWER_CONNECTED = "tt.co.justins.radio.radioreminder.action.POWER_ON";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "tt.co.justins.radio.radioreminder.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "tt.co.justins.radio.radioreminder.extra.PARAM2";

    public static final int SERVICE_WIFI = 0;

    private static final String tag = "radioreminder";

    private static int mId;
    private boolean waiting = false;

    private ConnectivityReceiver wifiReceiver;
    private ConnectivityReceiver batteryReceiver;

    public RadioService() {
    }

    public static class Event {
        public int serviceType;
        public String watchAction;
        public String respondAction;
        public String afterAction;
        public int afterInterval;

        public Event() {
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SETUP.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionSetup(param1, param2);
            } else if (ACTION_EXIT.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionExit(param1, param2);
            } else if (ACTION_WIFI_OFF.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionWifiOff(param1, param2);
            } else if (ACTION_POWER_CONNECTED.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionPowerConnected(param1, param2);
            }
        }
        return START_STICKY;
    }

    private void handleActionWifiOff(String param1, String param2) {
        sendNotification("Waiting to get plugged in before restarting wifi");
        Log.d(tag, "Notification sent");
        waiting = true;
    }

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

    private void handleActionPowerConnected(String param1, String param2) {
        if(waiting) {
            //turn on wifi
            WifiManager wifiMan = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            wifiMan.setWifiEnabled(true);
            Log.d(tag, "WIFI Enabled");

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(mId);
            Log.d(tag, "Notification removed");

            waiting = false;
        }
    }

    private void handleActionSetup(String param1, String param2) {
        wifiReceiver = new ConnectivityReceiver();
        batteryReceiver = new ConnectivityReceiver();

        registerReceiver(wifiReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        Log.d(tag, "Registered CONNECTIVITY_ACTION receiver");
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_POWER_CONNECTED));
        Log.d(tag, "Registered ACTION_POWER_CONNECTED receiver");
    }

    private void handleActionExit(String param1, String param2) {
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
