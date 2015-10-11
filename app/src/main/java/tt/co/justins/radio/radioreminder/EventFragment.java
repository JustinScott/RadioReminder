package tt.co.justins.radio.radioreminder;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EventFragment extends Fragment implements AdapterView.OnItemSelectedListener, TextView.OnEditorActionListener {
    private static final String BT_DEVICE_LIST = "Bluetooth device list";
    private static final String RADIO_PREFRENCES = "Radio Reminder Preferences";
    private static final String WIFI_DEVICE_LIST = "Wifi device list";

    private static final String tag = "EventFragment";

    private Event mEvent = null;
    private int mListPosition = RadioService.NEW_EVENT;

    private Spinner watchSpinner;
    private Spinner respondSpinner;
    private Spinner waitEventSpinner;
    private Spinner netDevSpinner1;
    private Spinner netDevSpinner2;

    private EditText hoursEdit;
    private EditText minsEdit;
    private EditText editText;

    private RadioButton delayEventButton;
    private RadioButton delayIntervalButton;
    private RadioButton delayNoneButton;

    private int lameSpinnerHack;
    private List<String> mPairedBluetoothDevices;
    private List<String> mPairedWifiNetworks;


    public static EventFragment newInstance(Bundle args) {
        EventFragment fragment = new EventFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public EventFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_event, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.v(tag, "Clicked actionbar item.");
        int id = item.getItemId();

        if (id == R.id.action_save_event) {
            saveEvent();
            return true;
        }

        if (id == R.id.action_delete_event) {
            deleteEvent();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void deleteEvent() {
        //if position equals new event, the event hasn't been added to the service yet
        //so skip adding it to the service
        if(mListPosition != RadioService.NEW_EVENT) {
            //send event to the service
            Intent intent = new Intent(getActivity(), RadioService.class);
            intent.setAction(RadioService.SERVICE_EVENT_DELETE);
            intent.putExtra(RadioService.EXTRA_EVENT_POSITION, mListPosition);
            getActivity().startService(intent);
        }
        startListActivity();
    }

    private void saveEvent() {
        Log.d(tag, "Saving settings and sending the event to radio service.");

        //todo fix service type
        mEvent.serviceType = RadioService.SERVICE_WIFI;
        mEvent.watchAction = getSelectedSpinnerItemAsAction(watchSpinner);
        mEvent.respondAction = getSelectedSpinnerItemAsAction(respondSpinner);

        Object netDev1 = netDevSpinner1.getSelectedItem();
        mEvent.netDev1 = (netDev1 == null) ? "" : netDev1.toString();

        //determine which delay was selected
        RadioGroup buttonGroup = (RadioGroup) getView().findViewById(R.id.radioGroup);
        int buttonId = buttonGroup.getCheckedRadioButtonId();

        switch (buttonId) {
            case R.id.delay_event_button:
                mEvent.waitAction = getSelectedSpinnerItemAsAction(waitEventSpinner);
                mEvent.waitInterval = 0;
                //getselecteditem returns null if empty
                Object netDev2 = netDevSpinner2.getSelectedItem();
                mEvent.netDev2 = (netDev2 == null) ? "" : netDev2.toString();
                break;
            case R.id.delay_interval_button:
                String sHour = hoursEdit.getText().toString();
                String sMin = minsEdit.getText().toString();
                int hours;
                int minutes;

                //parsing a string so check for empty string
                if(sHour.equals(""))
                    hours = 0;
                else
                    hours = Integer.parseInt(sHour);

                if(sMin.equals(""))
                    minutes = 0;
                else
                    minutes = Integer.parseInt(sMin);

                mEvent.waitInterval = hours * 60 + minutes;
                mEvent.waitAction = null;
                break;
            case R.id.delay_none_button:
                mEvent.waitAction = null;
                mEvent.waitInterval = 0;
                break;
        }

        //send event to the service
        Intent intent = new Intent(getActivity(), RadioService.class);
        intent.setAction(RadioService.SERVICE_EVENT);
        intent.putExtra(RadioService.EXTRA_EVENT_POSITION, mListPosition);
        intent.putExtra(RadioService.EXTRA_EVENT, mEvent);
        getActivity().startService(intent);

        startListActivity();
    }

    private void startListActivity() {
        //return to the list activity
        Intent intent = new Intent(getActivity(), ListActivity.class);
        getActivity().startActivity(intent);
    }

    private RadioAction.Action getSelectedSpinnerItemAsAction(Spinner spinner) {
        String actionName = spinner.getSelectedItem().toString();
        for(RadioAction.Action action : RadioAction.Action.values()) {
            if(RadioAction.getNameFromAction(action).equals(actionName))
                return action;
        }
        return null;
    }

    private int getSpinnerPositionFromAction(RadioAction.Action action, Spinner spinner) {
        String actionName = RadioAction.getNameFromAction(action);
        return getSpinnerPositionFromString(actionName, spinner);
    }

    private int getSpinnerPositionFromString(String spinnerItem, Spinner spinner) {
        ListAdapter adapter = (ListAdapter) spinner.getAdapter();
        for (int x = 0; x < adapter.getCount(); x++) {
            if (spinnerItem.equals(adapter.getItem(x).toString()))
                return x;
        }
        return 0;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(tag, "onCreate called. Pos: " + mListPosition);

        if(savedInstanceState != null) { //recreation
            mEvent = (Event) savedInstanceState.getSerializable(RadioService.EXTRA_EVENT);
            mListPosition = savedInstanceState.getInt(RadioService.EXTRA_EVENT_POSITION);
        } else { //first time creating the fragment
            if (getArguments() != null) {
                mListPosition = getArguments().getInt(RadioService.EXTRA_EVENT_POSITION, -1);
                //create an new empty event if position is magic valve NEW_EVENT
                if(mListPosition == RadioService.NEW_EVENT){
                    mEvent = new Event();
                } else {
                    mEvent = (Event) getArguments().getSerializable(RadioService.EXTRA_EVENT);
                }
            } else {
                throw new IllegalArgumentException("getArguments returns null.");
            }

            SharedPreferences preferences = getActivity().getSharedPreferences(RADIO_PREFRENCES, 0);
            if(preferences.contains(BT_DEVICE_LIST))
                mPairedBluetoothDevices = new ArrayList<>(preferences.getStringSet(BT_DEVICE_LIST,
                        new HashSet<String>()));
            if(preferences.contains(WIFI_DEVICE_LIST))
                mPairedBluetoothDevices = new ArrayList<>(preferences.getStringSet(BT_DEVICE_LIST,
                        new HashSet<String>()));
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(tag, "onCreateview called. Pos: " + mListPosition);

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_event, container, false);

        //set the state for all the views in the fragment
        inflateFragmentState(v);

        return v;
    }

    private void inflateFragmentState(View v) {
        Log.v(tag, "Inflating event state.");

        // Find all the spinner IDs
        watchSpinner = (Spinner) v.findViewById(R.id.watch_spin);
        respondSpinner = (Spinner) v.findViewById(R.id.respond_spin);
        waitEventSpinner = (Spinner) v.findViewById(R.id.wait_spin);
        netDevSpinner1 = (Spinner) v.findViewById(R.id.net_dev_spin1);
        netDevSpinner2 = (Spinner) v.findViewById(R.id.net_dev_spin2);

        hoursEdit = (EditText) v.findViewById(R.id.hour_edit);
        minsEdit = (EditText) v.findViewById(R.id.min_edit);

        // Create the adapters that are needed to populate the spinners with values
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(v.getContext(), R.array.radio_state, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(v.getContext(), R.array.my_events, android.R.layout.simple_spinner_item);
        //adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(v.getContext(), R.array.intervals, android.R.layout.simple_spinner_item);
        //adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //populate spinners with list of values
        watchSpinner.setAdapter(adapter);
        respondSpinner.setAdapter(adapter);
        waitEventSpinner.setAdapter(adapter);

        //this class needs to implement the OnItemSelectedListener interface for this to work
        //respondSpinner.setOnItemSelectedListener(this);
        waitEventSpinner.setOnItemSelectedListener(this);
        watchSpinner.setOnItemSelectedListener(this);
        netDevSpinner1.setOnItemSelectedListener(this);
        netDevSpinner2.setOnItemSelectedListener(this);

        netDevSpinner1.setEnabled(false);
        netDevSpinner2.setEnabled(false);
        lameSpinnerHack = 0;

        //on click listeners
        delayEventButton = (RadioButton) v.findViewById(R.id.delay_event_button);
        delayIntervalButton = (RadioButton) v.findViewById(R.id.delay_interval_button);
        delayNoneButton = (RadioButton) v.findViewById(R.id.delay_none_button);

        //delayNoneButton.setOnClickListener(this);
        //delayIntervalButton.setOnClickListener(this);
        //delayEventButton.setOnClickListener(this);

        hoursEdit.setOnEditorActionListener(this);
        minsEdit.setOnEditorActionListener(this);

        //fill the controls with the values from the event
        watchSpinner.setSelection(getSpinnerPositionFromAction(mEvent.watchAction, watchSpinner));
        //set device/network, if needed
        if(mEvent.watchAction == RadioAction.Action.BLUETOOTH_DEVICE_CONNECT ||
                mEvent.watchAction == RadioAction.Action.BLUETOOTH_DEVICE_DISCONNECT) {
            populateNetDevSpinnerBt(false, netDevSpinner1, mEvent.netDev1);
        }

        if(mEvent.watchAction == RadioAction.Action.WIFI_NETWORK_CONNECT ||
                mEvent.watchAction == RadioAction.Action.WIFI_NETWORK_DISCONNECT) {
            populateNetDevSpinnerWifi(netDevSpinner1, mEvent.netDev1);
        }


        respondSpinner.setSelection(getSpinnerPositionFromAction(mEvent.respondAction, respondSpinner));

        if(!(mEvent.waitAction == null)) {
            Log.v(tag, "Event wait action not empty.");
            waitEventSpinner.setSelection(getSpinnerPositionFromAction(mEvent.waitAction, waitEventSpinner));
            delayEventButton.setChecked(true);

            //check if the wait action is connecting/disconnecting from device/network
            if(mEvent.waitAction == RadioAction.Action.BLUETOOTH_DEVICE_CONNECT ||
                    mEvent.waitAction == RadioAction.Action.BLUETOOTH_DEVICE_DISCONNECT) {
                populateNetDevSpinnerBt(false, netDevSpinner2, mEvent.netDev2);
            }

            if(mEvent.waitAction == RadioAction.Action.WIFI_NETWORK_CONNECT ||
                    mEvent.waitAction == RadioAction.Action.WIFI_NETWORK_DISCONNECT) {
                populateNetDevSpinnerWifi(netDevSpinner2, mEvent.netDev2);
            }
        }
        else if(mEvent.waitInterval != 0) {
            hoursEdit.setText((mEvent.waitInterval / 60) + "");
            minsEdit.setText((mEvent.waitInterval % 60) + "");
            delayIntervalButton.setChecked(true);
        }
        else {
            delayNoneButton.setChecked(true);
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        delayIntervalButton.setChecked(true);
        return false;
    }

    //part of OnItemSelected interface
    //fires when and item is selected in a spinner view
    public void onItemSelected(AdapterView av, View v, int pos, long id) {
        switch(av.getId()) {
            case R.id.net_dev_spin2:
                Object data = av.getSelectedItem();
                Log.v(tag, "Net/Dev selected item: " + data.toString());
                break;
            case R.id.watch_spin:
                Log.v(tag, "Wait event spinner selected. Pos: " + pos);
                if(av.getSelectedItem().toString().equals("Connect to Bluetooth device") ||
                        av.getSelectedItem().toString().equals("Disconnect from Bluetooth device")) {
                    populateNetDevSpinnerBt(false, netDevSpinner1, mEvent.netDev1);
                    break;
                }

                if(av.getSelectedItem().toString().equals("Connect to WIFI network") ||
                        av.getSelectedItem().toString().equals("Disconnect from WIFI network")) {
                    populateNetDevSpinnerWifi(netDevSpinner1, mEvent.netDev1);
                    break;
                }

                netDevSpinner1.setEnabled(false);
                break;
            case R.id.wait_spin:
                Log.v(tag, "Wait event spinner selected. Pos: " + pos);
                //use this to skip detection of the first selection when the spinner is created
                if (lameSpinnerHack != 0) {
                    delayEventButton.setChecked(true);

                    if(av.getSelectedItem().toString().equals("Connect to Bluetooth device") ||
                            av.getSelectedItem().toString().equals("Disconnect from Bluetooth device")) {
                        populateNetDevSpinnerBt(false, netDevSpinner2, mEvent.netDev2);
                        break;
                    }

                    if(av.getSelectedItem().toString().equals("Connect to WIFI network") ||
                            av.getSelectedItem().toString().equals("Disconnect from WIFI network")) {
                        populateNetDevSpinnerWifi(netDevSpinner2, mEvent.netDev2);
                        break;
                    }

                    netDevSpinner2.setEnabled(false);
                }
                lameSpinnerHack++;
                break;
        }
    }

    //refreshList = true, forces a refresh, even if the list is available
    private void populateNetDevSpinnerBt(final boolean refreshList, final Spinner spinner, final String netDev) {
        boolean newData = false;
        //make this a background thread because it needs to sleep
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {

                //save this list so it only needs to be created once
                if(mPairedBluetoothDevices == null || refreshList == true) {
                    Log.d(tag, "Device list is null, or forcing refresh.");
                    mPairedBluetoothDevices = getBluetoothDevices();
                    SharedPreferences prefrences = getActivity().getSharedPreferences(RADIO_PREFRENCES, 0);
                    SharedPreferences.Editor editor = prefrences.edit();
                    editor.putStringSet(BT_DEVICE_LIST, new HashSet<>(mPairedBluetoothDevices));
                    editor.commit();
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //UI stuff gets posted back on the UI thread
                        ArrayAdapter adapter = new ArrayAdapter(getActivity(),
                                android.R.layout.simple_spinner_item, mPairedBluetoothDevices);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        spinner.setAdapter(adapter);
                        spinner.setSelection(getSpinnerPositionFromString(netDev, spinner));
                        spinner.setEnabled(true);
                    }
                });
            }

            private  List<String> getBluetoothDevices() {
                Log.v(tag, "Getting list of Bluetooth devices.");
                List<String> devices = new ArrayList<>();
                BluetoothManager btMan = (BluetoothManager) getActivity().getSystemService(getActivity().getBaseContext().BLUETOOTH_SERVICE);
                BluetoothAdapter adapter = btMan.getAdapter();

                //Bluetooth radio needs to be on to get list of paired devices
                boolean turnOff = false;
                if (!adapter.isEnabled()) {
                    turnOff = true;
                    Log.d(tag, "Turning on Bluetooth adapter.");
                    adapter.enable();
                }

                //Wait for radio to kick in
                Log.d(tag, "Sleeping for 2 seconds.");
                SystemClock.sleep(2000);

                Set<BluetoothDevice> btDevices = adapter.getBondedDevices();
                for (BluetoothDevice device : btDevices)
                    devices.add(device.getName());


                //Turn the Bluetooth device back off
                if (turnOff) {
                    Log.d(tag, "Turning off Bluetooth adapter.");
                    adapter.disable();
                }

                return devices;
            }
        }).start();
    }

    public void populateNetDevSpinnerWifi(Spinner spinner, String netDev) {
        if(mPairedWifiNetworks == null) {
            Log.d(tag, "Network list is null.");
            mPairedWifiNetworks = getWifiNetworks();
            SharedPreferences prefrences = getActivity().getSharedPreferences(RADIO_PREFRENCES, 0);
            SharedPreferences.Editor editor = prefrences.edit();
            editor.putStringSet(WIFI_DEVICE_LIST, new HashSet<>(mPairedWifiNetworks));
            editor.commit();
        }
        ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, mPairedWifiNetworks);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(getSpinnerPositionFromString(netDev, spinner));
        spinner.setEnabled(true);
    }

    public List<String> getWifiNetworks() {
        List<String> networks = new ArrayList<>();

        WifiManager wifiManager = (WifiManager) getActivity().getSystemService(getActivity().getBaseContext().WIFI_SERVICE);
        List<WifiConfiguration> configList = wifiManager.getConfiguredNetworks();
        for(WifiConfiguration wc : configList)
            networks.add(wc.SSID);

        return networks;
    }

    //part of OnItemSelected interface
    public void onNothingSelected(AdapterView av) {
    }
}
