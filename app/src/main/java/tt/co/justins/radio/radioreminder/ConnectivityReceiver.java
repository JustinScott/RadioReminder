package tt.co.justins.radio.radioreminder;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
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

        if(intentAction == WifiManager.NETWORK_STATE_CHANGED_ACTION) {
            NetworkInfo networkInfo = extras.getParcelable(WifiManager.EXTRA_NETWORK_INFO);
            if(networkInfo != null) {
                if(networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                    WifiInfo wifiInfo = extras.getParcelable(WifiManager.EXTRA_WIFI_INFO);
                    Log.v(tag, RadioAction.getNameFromAction(RadioAction.Action.WIFI_NETWORK_CONNECT)
                            + "<>" + wifiInfo.getSSID());
                }
            }
        }

        if(intentAction == WifiManager.NETWORK_STATE_CHANGED_ACTION) {
            NetworkInfo networkInfo = extras.getParcelable(WifiManager.EXTRA_NETWORK_INFO);
            if(networkInfo != null) {
                if(networkInfo.getState() == NetworkInfo.State.DISCONNECTED) {
                    Log.v(tag, RadioAction.getNameFromAction(RadioAction.Action.WIFI_NETWORK_DISCONNECT)
                            + "<>" + networkInfo.getExtraInfo());
                }
            }
        }

        if(intentAction == WifiManager.WIFI_STATE_CHANGED_ACTION) {
            if (extras.getInt(WifiManager.EXTRA_PREVIOUS_WIFI_STATE) == WifiManager.WIFI_STATE_ENABLED) {
                if (extras.getInt(WifiManager.EXTRA_WIFI_STATE) == WifiManager.WIFI_STATE_DISABLED) {
                    Log.d(tag, RadioAction.getNameFromAction(RadioAction.Action.WIFI_OFF));
                    sendIntent(RadioAction.Action.WIFI_OFF);
                }
            }
        }

        if(intentAction == WifiManager.WIFI_STATE_CHANGED_ACTION) {
            if (extras.getInt(WifiManager.EXTRA_PREVIOUS_WIFI_STATE) == WifiManager.WIFI_STATE_DISABLED) {
                if (extras.getInt(WifiManager.EXTRA_WIFI_STATE) == WifiManager.WIFI_STATE_ENABLED) {
                    Log.d(tag, RadioAction.getNameFromAction(RadioAction.Action.WIFI_ON));
                    sendIntent(RadioAction.Action.WIFI_ON);
                }
            }
        }

        if(intentAction == BluetoothAdapter.ACTION_STATE_CHANGED) {
            if(extras.getInt(BluetoothAdapter.EXTRA_PREVIOUS_STATE) == BluetoothAdapter.STATE_TURNING_ON) {
                if(extras.getInt(BluetoothAdapter.EXTRA_STATE) == BluetoothAdapter.STATE_ON) {
                    Log.d(tag, RadioAction.getNameFromAction(RadioAction.Action.BLUETOOTH_ON));
                    sendIntent(RadioAction.Action.BLUETOOTH_ON);
                }
            }
        }

        if(intentAction == BluetoothAdapter.ACTION_STATE_CHANGED) {
            if(extras.getInt(BluetoothAdapter.EXTRA_PREVIOUS_STATE) == BluetoothAdapter.STATE_TURNING_OFF) {
                if(extras.getInt(BluetoothAdapter.EXTRA_STATE) == BluetoothAdapter.STATE_OFF) {
                    Log.d(tag, RadioAction.getNameFromAction(RadioAction.Action.BLUETOOTH_OFF));
                    sendIntent(RadioAction.Action.BLUETOOTH_OFF);
                }
            }
        }

        if(intentAction == BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED) {
            if(extras.getInt(BluetoothAdapter.EXTRA_PREVIOUS_CONNECTION_STATE) == BluetoothAdapter.STATE_CONNECTED) {
                if(extras.getInt(BluetoothAdapter.EXTRA_CONNECTION_STATE) == BluetoothAdapter.STATE_DISCONNECTED) {
                    //send the extra device, so the service can determine which device disconnected
                    Log.d(tag, RadioAction.getNameFromAction(RadioAction.Action.BLUETOOTH_DEVICE_DISCONNECT)
                            + " Device: " + extras.getString(BluetoothDevice.EXTRA_DEVICE));
                    sendIntent(RadioAction.Action.BLUETOOTH_DEVICE_DISCONNECT, extras);
                }
            }
        }

        if(intentAction == BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED) {
            if(extras.getInt(BluetoothAdapter.EXTRA_PREVIOUS_CONNECTION_STATE) == BluetoothAdapter.STATE_DISCONNECTED) {
                if(extras.getInt(BluetoothAdapter.EXTRA_CONNECTION_STATE) == BluetoothAdapter.STATE_CONNECTED) {
                    //send the extra device, so the service can determine which device connected
                    Log.d(tag, RadioAction.getNameFromAction(RadioAction.Action.BLUETOOTH_DEVICE_CONNECT)
                            + " Device: " + extras.getString(BluetoothDevice.EXTRA_DEVICE));
                    sendIntent(RadioAction.Action.BLUETOOTH_DEVICE_CONNECT, extras);
                }
            }
        }

        if(intentAction == Intent.ACTION_POWER_CONNECTED) {
            Log.d(tag, RadioAction.getNameFromAction(RadioAction.Action.POWER_CONNECTED));
            sendIntent(RadioAction.Action.POWER_CONNECTED);
        }
    }

    private void sendIntent(RadioAction.Action action, Bundle extras) {
        Intent intent = new Intent(context, RadioService.class);
        intent.setAction(RadioAction.getKeyFromAction(action));
        intent.putExtras(extras);
        context.startService(intent);
    }

    private void sendIntent(RadioAction.Action action) {
        Intent intent = new Intent(context, RadioService.class);
        intent.setAction(RadioAction.getKeyFromAction(action));
        context.startService(intent);
    }
}
