package tt.co.justins.radio.radioreminder;

public class RadioAction {

    enum Action {
        WIFI_ON, WIFI_OFF, BLUETOOTH_ON, BLUETOOTH_OFF, BLUETOOTH_DEVICE_CONNECT,
        BLUETOOTH_DEVICE_DISCONNECT, POWER_CONNECTED, POWER_DISCONNECTED, WIFI_NETWORK_CONNECT,
        WIFI_NETWORK_DISCONNECT, LOCATION_ON, LOCATION_OFF, CELL_DATA_ON, CELL_DATA_OFF
    }

    private RadioAction() {
    }
    
    static String getNameFromAction(Action action) {
        String temp = "";
        switch(action) {
            case WIFI_ON:
                temp = "WIFI Radio On";
                break;
            case WIFI_OFF:
                temp = "WIFI Radio Off";
                break;
            case BLUETOOTH_ON:
                temp = "Bluetooth Radio On";
                break;
            case BLUETOOTH_OFF:
                temp = "Bluetooth Radio Off";
                break;
            case BLUETOOTH_DEVICE_CONNECT:
                temp = "Connect to Bluetooth device";
                break;
            case BLUETOOTH_DEVICE_DISCONNECT:
                temp = "Disconnect from Bluetooth device";
                break;
            case POWER_CONNECTED:
                temp = "Power Plugged In";
                break;
            case POWER_DISCONNECTED:
                temp = "Power Unplugged";
                break;
            case WIFI_NETWORK_CONNECT:
                temp = "Connect to WIFI network";
                break;
            case WIFI_NETWORK_DISCONNECT:
                temp = "Disconnect from WIFI network";
                break;
            case LOCATION_ON:
                temp = "GPS Radio On";
                break;
            case LOCATION_OFF:
                temp = "GPS Radio Off";
                break;
            case CELL_DATA_ON:
                temp = "Cellular Data On";
                break;
            case CELL_DATA_OFF:
                temp = "Cellular Data Off";
                break;
        }
        return temp;
    }

    static String getKeyFromAction(Action action) {
        String temp = "";
        switch(action) {
            case WIFI_ON:
                temp = "tt.co.justins.radio.radioreminder.action.WIFI_ON";
                break;
            case WIFI_OFF:
                temp = "tt.co.justins.radio.radioreminder.action.WIFI_OFF";
                break;
            case BLUETOOTH_ON:
                temp = "tt.co.justins.radio.radioreminder.action.BLUETOOTH_ON";
                break;
            case BLUETOOTH_OFF:
                temp = "tt.co.justins.radio.radioreminder.action.BLUETOOTH_OFF";
                break;
            case BLUETOOTH_DEVICE_CONNECT:
                temp = "tt.co.justins.radio.radioreminder.action.BLUETOOTH_DEVICE_CONNECT";
                break;
            case BLUETOOTH_DEVICE_DISCONNECT:
                temp = "tt.co.justins.radio.radioreminder.action.BLUETOOTH_DEVICE_DISCONNECT";
                break;
            case POWER_CONNECTED:
                temp = "tt.co.justins.radio.radioreminder.action.POWER_ON";
                break;
            case POWER_DISCONNECTED:
                temp = "tt.co.justins.radio.radioreminder.action.POWER_DISCONNECTED";
                break;
            case WIFI_NETWORK_CONNECT:
                temp = "tt.co.justins.radio.radioreminder.action.WIFI_NETWORK_CONNECT";
                break;
            case WIFI_NETWORK_DISCONNECT:
                temp = "tt.co.justins.radio.radioreminder.action.WIFI_NETWORK_DISCONNECT";
                break;
            case LOCATION_ON:
                temp = "tt.co.justins.radio.radioreminder.action.LOCATION_ON";
                break;
            case LOCATION_OFF:
                temp = "tt.co.justins.radio.radioreminder.action.LOCATION_OFF";
                break;
            case CELL_DATA_ON:
                temp = "tt.co.justins.radio.radioreminder.action.CELL_DATA_ON";
                break;
            case CELL_DATA_OFF:
                temp = "tt.co.justins.radio.radioreminder.action.CELL_DATA_OFF";
                break;
        }
        return temp;
    }
}