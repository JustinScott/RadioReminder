package tt.co.justins.radio.radioreminder;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

public class EventFragment extends Fragment implements AdapterView.OnItemSelectedListener, TextView.OnEditorActionListener {
    private static String tag = "EventFragment";

    private Event mEvent = null;
    private int mListPosition = -1;

    private Spinner watchSpinner = null;
    private Spinner respondSpinner = null;
    private Spinner waitEventSpinner = null;

    private EditText hoursEdit = null;
    private EditText minsEdit = null;
    private EditText editText = null;

    RadioButton delayEventButton = null;
    RadioButton delayIntervalButton = null;
    RadioButton delayNoneButton = null;

    private int lameSpinnerHack;

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
        //-1 means it's a new event that hasn't been added to the service yet, so just jump to
        //the list activity
        if(mListPosition != -1) {
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
        mEvent.name = editText.getText().toString();

        //determine which delay was selected
        RadioGroup buttonGroup = (RadioGroup) getView().findViewById(R.id.radioGroup);
        int buttonId = buttonGroup.getCheckedRadioButtonId();

        switch (buttonId) {
            case R.id.delay_event_button:
                mEvent.waitAction = getSelectedSpinnerItemAsAction(waitEventSpinner);
                mEvent.waitInterval = 0;
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
                mEvent.waitAction = "";
                break;
            case R.id.delay_none_button:
                mEvent.waitAction = "";
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

    private String getSelectedSpinnerItemAsAction(Spinner spinner) {
        String action = null;
        switch (spinner.getSelectedItem().toString()) {
            case "WIFI Radio On":
                action = RadioService.ACTION_WIFI_ON;
                break;

            case "WIFI Radio Off":
                action = RadioService.ACTION_WIFI_OFF;
                break;

            case "Power Plugged In":
                action = RadioService.ACTION_POWER_CONNECTED;
                break;

            case "Power Unplugged":
                action = RadioService.ACTION_POWER_DISCONNECTED;
			    break;

            case "Bluetooth Radio On":
                action = RadioService.ACTION_BLUETOOTH_ON;
			    break;

            case "Bluetooth Radio Off":
                action = RadioService.ACTION_BLUETOOTH_OFF;
			    break;

            case "Connect to WIFI network":
                action = RadioService.ACTION_WIFI_NETWORK_CONNECT;
			    break;

            case "Disconnect from WIFI network":
                action = RadioService.ACTION_WIFI_NETWORK_DISCONNECT;
			    break;

            case "Connect to Bluetooth device":
                action = RadioService.ACTION_BLUETOOTH_DEVICE_CONNECT;
			    break;

            case "Disconnect from Bluetooth device":
                action = RadioService.ACTION_BLUETOOTH_DEVICE_DISCONNECT;
			    break;
        }
        return action;
    }

    private int getSpinnerPositionFromAction(String action) {
        int position = 0;
        if(action != null) {
            switch (action) {
                case RadioService.ACTION_WIFI_ON:
                    position = 0;
                    break;
                case RadioService.ACTION_WIFI_OFF:
                    position = 1;
                    break;
                case RadioService.ACTION_POWER_CONNECTED:
                    position = 2;
                    break;
                case RadioService.ACTION_POWER_DISCONNECTED:
                    position = 3;
                    break;
                case RadioService.ACTION_BLUETOOTH_ON:
                    position = 4;
                    break;
                case RadioService.ACTION_BLUETOOTH_OFF:
                    position = 5;
                    break;
                case RadioService.ACTION_WIFI_NETWORK_CONNECT:
                    position = 6;
                    break;
                case RadioService.ACTION_WIFI_NETWORK_DISCONNECT:
                    position = 7;
                    break;
                case RadioService.ACTION_BLUETOOTH_DEVICE_CONNECT:
                    position = 8;
                    break;
                case RadioService.ACTION_BLUETOOTH_DEVICE_DISCONNECT:
                    position = 9;
                    break;
            }
        }
        return position;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        }

        setHasOptionsMenu(true);
        Log.d(tag, "onCreate called. Pos: " + mListPosition);
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
        Log.d(tag, "Inflating event state.");

        editText = (EditText) v.findViewById(R.id.text_name_edit);

        // Find all the spinner IDs
        watchSpinner = (Spinner) v.findViewById(R.id.wifi_watch_spin);
        respondSpinner = (Spinner) v.findViewById(R.id.wifi_respond_spin);
        waitEventSpinner = (Spinner) v.findViewById(R.id.wifi_event_spin);

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
        //watchSpinner.setOnItemSelectedListener(this);

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
        editText.setText(mEvent.name);

        watchSpinner.setSelection(getSpinnerPositionFromAction(mEvent.watchAction));
        respondSpinner.setSelection(getSpinnerPositionFromAction(mEvent.respondAction));

        if(!mEvent.waitAction.equals("")) {
            Log.v(tag, "Event wait action not empty.");
            //waitEventSpinner.setSelection(getSpinnerPositionFromAction(mEvent.waitAction));
            //delayEventButton.setChecked(true);
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
        Log.v(tag, "Wait event spinner selected. Pos: " + pos);
        if(lameSpinnerHack != 0) {
            delayEventButton.setChecked(true);
        }
        lameSpinnerHack++;
    }

    //part of OnItemSelected interface
    public void onNothingSelected(AdapterView av) {
    }
}
