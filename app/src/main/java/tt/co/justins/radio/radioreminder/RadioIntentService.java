package tt.co.justins.radio.radioreminder;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class RadioIntentService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_SETUP = "tt.co.justins.radio.radioreminder.action.SETUP";
    private static final String ACTION_SHUT_DOWN = "tt.co.justins.radio.radioreminder.action.SHUTDOWN";

    private static final String ACTION_WIFI_OFF = "tt.co.justins.radio.radioreminder.action.WIFI_OFF";
    private static final String ACTION_POWER_ON = "tt.co.justins.radio.radioreminder.action.POWER_ON";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "tt.co.justins.radio.radioreminder.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "tt.co.justins.radio.radioreminder.extra.PARAM2";

    private static final String tag = "radioremind";

    private static int mId;
    private boolean waiting = false;

    private ConnectivityReceiver wifiReceiver;
    private ConnectivityReceiver batteryReceiver;

    public static void startActionSetup(Context context, String param1, String param2) {
        Intent intent = new Intent(context, RadioIntentService.class);
        intent.setAction(ACTION_SETUP);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    public static void startActionShutdown(Context context, String param1, String param2) {
        Intent intent = new Intent(context, RadioIntentService.class);
        intent.setAction(ACTION_SHUT_DOWN);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    public RadioIntentService() {
        super("RadioIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SETUP.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionSetup(param1, param2);
            } else if (ACTION_SHUT_DOWN.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionShutdown(param1, param2);
            } else if (ACTION_WIFI_OFF.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionWifiOff(param1, param2);
            } else if (ACTION_POWER_ON.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionPowerOn(param1, param2);
            }
        }
    }

    @Override
    public void onDestroy() {
        handleActionShutdown("", "");
        super.onDestroy();
    }

    private void handleActionWifiOff(String param1, String param2) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("Radio Reminder")
                .setContentText("Waiting to get plugged in before restarting wifi");

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(mId, mBuilder.build());

        waiting = true;
    }

    private void handleActionPowerOn(String param1, String param2) {
        if(waiting) {
            //turn on wifi
            WifiManager wifiMan = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            wifiMan.setWifiEnabled(true);

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(mId);
            waiting = false;
        }
    }

    private void handleActionSetup(String param1, String param2) {
        wifiReceiver = new ConnectivityReceiver();
        batteryReceiver = new ConnectivityReceiver();

        registerReceiver(wifiReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        Log.v(tag, "Registered CONNECTIVITY_ACTION receiver");
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_POWER_CONNECTED));
        Log.v(tag, "Registered ACTION_POWER_CONNECTED receiver");
    }

    private void handleActionShutdown(String param1, String param2) {
        unregisterReceiver(wifiReceiver);
        Log.v(tag, "Unregistered CONNECTIVITY_ACTION receiver");
        unregisterReceiver(batteryReceiver);
        Log.v(tag, "Unregistered ACTION_POWER_CONNECTED receiver");
    }
}
