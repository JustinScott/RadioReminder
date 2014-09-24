package tt.co.justins.radio.radioreminder;



import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

public class EventFragment extends Fragment implements AdapterView.OnItemSelectedListener, View.OnClickListener{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private Event mEvent;
    private int mListPosition;

    private Spinner watchSpinner;
    private Spinner respondSpinner;
    private Spinner waitEventSpinner;

    private EditText hoursEdit;
    private EditText minsEdit;

    public static EventFragment newInstance(Event event, int listPosition) {
        EventFragment fragment = new EventFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, event);
        args.putInt(ARG_PARAM2, listPosition);
        fragment.setArguments(args);
        return fragment;
    }
    public EventFragment() {
        // Required empty public constructor
    }

    //part of OnItemSelected interface
    //fires when and item is selected in a spinner view
    public void onItemSelected(AdapterView av, View v, int pos, long id) {
        onClick(v);
    }

    //part of OnItemSelected interface
    public void onNothingSelected(AdapterView av) {

    }

    public void onClick(View v) {
        Log.d("radioreminder", "Onclick called with view: " + v.toString());
        TextView saveText = (TextView) getView().findViewById(R.id.save_text);
        saveText.setText("Changes not saved");
        saveText.setVisibility(View.VISIBLE);

        switch(v.getId()) {
            //clicked the apply button
            //parse all the option selections and build an event object
            case R.id.save_button:
                Log.d("radioreminder", "Saving settings and sending the event to radio service");
                mEvent.serviceType = RadioService.SERVICE_WIFI;
                mEvent.watchAction = getSelectedSpinnerItemAsAction(watchSpinner);
                mEvent.respondAction = getSelectedSpinnerItemAsAction(respondSpinner);

                //determine which delay was selected
                RadioGroup buttonGroup = (RadioGroup) getView().findViewById(R.id.radioGroup);
                int buttonId = buttonGroup.getCheckedRadioButtonId();

                switch (buttonId) {
                    case R.id.delay_event_button:
                        mEvent.waitAction = getSelectedSpinnerItemAsAction(waitEventSpinner);
                        mEvent.waitInterval = 0;
                        break;
                    case R.id.delay_interval_button:
                        int hours = Integer.parseInt(hoursEdit.getText().toString());
                        int mins = Integer.parseInt(minsEdit.getText().toString());
                        mEvent.waitInterval = hours * 60 + mins;
                        mEvent.waitAction = null;
                        break;
                    case R.id.delay_none_button:
                        mEvent.waitAction = null;
                        mEvent.waitInterval = 0;
                        break;
                }

                //send event to the service
                Intent intent = new Intent(getView().getContext(), RadioService.class);
                intent.setAction(RadioService.SERVICE_EVENT);
                intent.putExtra(RadioService.EXTRA_EVENT_POSITION, mListPosition);
                intent.putExtra(RadioService.EXTRA_EVENT, mEvent);
                getView().getContext().startService(intent);

                saveText.setText("Changes saved");
                break;
        }
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
            case "Plugged In":
                action = RadioService.ACTION_POWER_CONNECTED;
        }
        return action;
    }

    private int getSpinnerSelectionFromAction(String action) {
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
                    position = 0;
                    break;
            }
        }
        return position;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getArguments() != null) {
            mEvent = (Event) getArguments().getSerializable(ARG_PARAM1);
            mListPosition = getArguments().getInt(ARG_PARAM2);
        } else {
            //mEvent = new Event();
        }

        // Inflate the layout for this fragment
        View v =inflater.inflate(R.layout.fragment_event, container, false);

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
        //waitEventSpinner.setOnItemSelectedListener(this);
        //watchSpinner.setOnItemSelectedListener(this);

        //on click listeners
        RadioButton delayEventButton = (RadioButton) v.findViewById(R.id.delay_event_button);
        RadioButton delayIntervalButton = (RadioButton) v.findViewById(R.id.delay_interval_button);
        RadioButton delayNoneButton = (RadioButton) v.findViewById(R.id.delay_none_button);

        delayNoneButton.setOnClickListener(this);
        delayIntervalButton.setOnClickListener(this);
        delayEventButton.setOnClickListener(this);
        v.findViewById(R.id.save_button).setOnClickListener(this);

        //fill the controls with the values from the event
        watchSpinner.setSelection(getSpinnerSelectionFromAction(mEvent.watchAction));
        respondSpinner.setSelection(getSpinnerSelectionFromAction(mEvent.respondAction));

        if(mEvent.waitAction != null) {
            waitEventSpinner.setSelection(getSpinnerSelectionFromAction(mEvent.waitAction));
            delayEventButton.setChecked(true);
        }
        else if(mEvent.waitInterval != 0) {
            hoursEdit.setText((mEvent.waitInterval / 60) + "");
            minsEdit.setText((mEvent.waitInterval % 60) + "");
            delayIntervalButton.setChecked(true);
        }
        else {
            delayNoneButton.setChecked(true);
        }

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
