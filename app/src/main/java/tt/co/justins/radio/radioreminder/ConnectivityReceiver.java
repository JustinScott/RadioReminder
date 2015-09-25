package tt.co.justins.radio.radioreminder;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

public class ConnectivityReceiver extends BroadcastReceiver {
    private Context context = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context =  context;
        debugIntent(intent, "ConnectivityReceiver");
    }

    private void debugIntent(Intent intent, String tag) {
        String intentAction = intent.getAction();
        Bundle extras = intent.getExtras();

        Log.v(tag, "action: " + intentAction);
        Log.v(tag, "component: " + intent.getComponent());
        for (String key: extras.keySet()) {
            Log.v(tag, "key [" + key + "]: " + extras.get(key));
        }

        if(intentAction == ConnectivityManager.CONNECTIVITY_ACTION) {
            if (extras != null) {
                //there are two wifi disconnect broadcast, ignore the one that has this key
                if (!extras.containsKey("otherNetwork")) {
                    if (extras.containsKey("networkInfo")) {
                        NetworkInfo netInfo = (NetworkInfo) extras.get("networkInfo");
                        if (netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                            if (netInfo.getState() == NetworkInfo.State.CONNECTED) {
                                Log.d(tag, "WIFI CONNECTED");
                            } else if (netInfo.getState() == NetworkInfo.State.DISCONNECTED) {
                                Log.d(tag, "WIFI DISCONNECTED");
                                sendIntent(RadioService.ACTION_WIFI_OFF);
                            }
                        }
                    }
                }
            }
        }

        if(intentAction == BluetoothAdapter.ACTION_STATE_CHANGED) {
            if(extras.getInt(BluetoothAdapter.EXTRA_PREVIOUS_STATE) == BluetoothAdapter.STATE_TURNING_ON) {
                if(extras.getInt(BluetoothAdapter.EXTRA_STATE) == BluetoothAdapter.STATE_ON) {
                    Log.d(tag, "Bluetooth radio turned on.");
                    sendIntent(RadioService.ACTION_BLUETOOTH_ON);
                }
            }
        }

        if(intentAction == BluetoothAdapter.ACTION_STATE_CHANGED) {
            if(extras.getInt(BluetoothAdapter.EXTRA_PREVIOUS_STATE) == BluetoothAdapter.STATE_TURNING_OFF) {
                if(extras.getInt(BluetoothAdapter.EXTRA_STATE) == BluetoothAdapter.STATE_OFF) {
                    Log.d(tag, "Bluetooth radio turned off.");
                    sendIntent(RadioService.ACTION_BLUETOOTH_OFF);
                }
            }
        }

        if(intentAction == BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED) {
            if(extras.getInt(BluetoothAdapter.EXTRA_PREVIOUS_CONNECTION_STATE) == BluetoothAdapter.STATE_CONNECTED) {
                if(extras.getInt(BluetoothAdapter.EXTRA_CONNECTION_STATE) == BluetoothAdapter.STATE_DISCONNECTED) {
                    //send the extra device, so the service can determine which device disconnected
                    Log.d(tag, "Bluetooth device disconnected. Device: " + extras.getString(BluetoothDevice.EXTRA_DEVICE));
                    sendIntent(RadioService.ACTION_BLUETOOTH_DEVICE_DISCONNECT, extras);
                }
            }
        }

        if(intentAction == BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED) {
            if(extras.getInt(BluetoothAdapter.EXTRA_PREVIOUS_CONNECTION_STATE) == BluetoothAdapter.STATE_DISCONNECTED) {
                if(extras.getInt(BluetoothAdapter.EXTRA_CONNECTION_STATE) == BluetoothAdapter.STATE_CONNECTED) {
                    //send the extra device, so the service can determine which device connected
                    Log.d(tag, "Bluetooth device connected. Device: " + extras.getString(BluetoothDevice.EXTRA_DEVICE));
                    sendIntent(RadioService.ACTION_BLUETOOTH_DEVICE_CONNECT, extras);
                }
            }
        }

        if(intentAction == Intent.ACTION_POWER_CONNECTED) {
            Log.d(tag, "POWER CONNECTED");
            sendIntent(RadioService.ACTION_POWER_CONNECTED);
        }
    }

    private void sendIntent(String action, Bundle extras) {
        Intent intent = new Intent(context, RadioService.class);
        intent.setAction(action);
        intent.putExtras(extras);
        context.startService(intent);
    }

    private void sendIntent(String action) {
        Intent intent = new Intent(context, RadioService.class);
        intent.setAction(action);
        context.startService(intent);
    }
}
