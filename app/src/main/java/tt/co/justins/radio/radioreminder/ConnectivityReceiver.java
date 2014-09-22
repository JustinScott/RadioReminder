package tt.co.justins.radio.radioreminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

public class ConnectivityReceiver extends BroadcastReceiver {
    private static final String EXTRA_PARAM1 = "tt.co.justins.radio.radioreminder.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "tt.co.justins.radio.radioreminder.extra.PARAM2";

    public ConnectivityReceiver() {
    }

    private Context context = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context =  context;
        debugIntent(intent, "radioreminder");
    }

    private void debugIntent(Intent intent, String tag) {
        Log.v(tag, "action: " + intent.getAction());
        Log.v(tag, "component: " + intent.getComponent());
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if(extras.containsKey("networkInfo")) {
                NetworkInfo netInfo = (NetworkInfo) extras.get("networkInfo");
                if(netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    if(netInfo.getState() == NetworkInfo.State.CONNECTED) {
                        Log.d(tag, "WIFI CONNECTED");

                    }
                    else if(netInfo.getState() == NetworkInfo.State.DISCONNECTED) {
                        Log.d(tag, "WIFI DISCONNECTED");
                        sendIntent(RadioService.ACTION_WIFI_OFF, "", "");
                    }
                }
            }
            for (String key: extras.keySet()) {
                Log.v(tag, "key [" + key + "]: " + extras.get(key));
            }
        }

        if(intent.getAction() == Intent.ACTION_POWER_CONNECTED) {
            Log.d(tag, "POWER CONNECTED");
            sendIntent(RadioService.ACTION_POWER_CONNECTED, "", "");
        }
    }

    private void sendIntent(String action, String param1, String param2) {
        Intent intent = new Intent(context, RadioService.class);
        intent.setAction(action);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }
}
