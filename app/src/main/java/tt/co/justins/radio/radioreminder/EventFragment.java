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
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

public class EventFragment extends Fragment implements AdapterView.OnItemSelectedListener, View.OnClickListener{
    private static final String ARG_PARAM1 = "param1";

    private Event mEvent;

    private Spinner watchSpinner;
    private Spinner respondSpinner;
    private Spinner waitEventSpinner;
    private Spinner waitIntervalSpinner;

    public static EventFragment newInstance(Event event) {
        EventFragment fragment = new EventFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, event);
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

        switch(getView().getId()) {
            //clicked the apply button
            //parse all the option selections and build an event object
            case R.id.save_button:
                Event myEvent = new Event();

                myEvent.serviceType = RadioService.SERVICE_WIFI;
                myEvent.watchAction = getSelectedSpinnerItemAsAction(watchSpinner);
                myEvent.respondAction = getSelectedSpinnerItemAsAction(respondSpinner);

                //determine which delay was selected
                RadioGroup buttonGroup = (RadioGroup) getView().findViewById(R.id.radioGroup);
                int buttonId = buttonGroup.getCheckedRadioButtonId();

                switch (buttonId) {
                    case R.id.delay_event_button:
                        myEvent.waitAction = getSelectedSpinnerItemAsAction(waitEventSpinner);
                        break;
                    case R.id.delay_interval_button:
                        //myEvent.waitInterval = Float.parseFloat(waitIntervalSpinner.toString());
                        String hours = (EditText) getView().findViewById(R.id.hour_text).getText();
                        break;
                    case R.id.delay_none_button:
                        //event class is initialized with values that indicate no delay
                        break;
                }

                //send event to the service
                Intent intent = new Intent(getView().getContext(), RadioService.class);
                intent.setAction(RadioService.SERVICE_EVENT);
                intent.putExtra(RadioService.EXTRA_EVENT, myEvent);
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
        } else {
            mEvent = new Event();
        }

        // Inflate the layout for this fragment
        View v =inflater.inflate(R.layout.fragment_event, container, false);

        // Find all the spinner IDs
        watchSpinner = (Spinner) v.findViewById(R.id.wifi_watch_spin);
        respondSpinner = (Spinner) v.findViewById(R.id.wifi_respond_spin);
        waitEventSpinner = (Spinner) v.findViewById(R.id.wifi_event_spin);
        //waitIntervalSpinner = (Spinner) v.findViewById(R.id.wifi_interval_spin);

        // Create the adapters that are needed to populate the spinners with values
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(v.getContext(), R.array.radio_state, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(v.getContext(), R.array.my_events, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(v.getContext(), R.array.intervals, android.R.layout.simple_spinner_item);
        //adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //populate spinners with data
        //and assign the method that is called when an item is selected
        //this class needs to implement the OnItemSelectedListener interface for this to work
        watchSpinner.setAdapter(adapter);
        // watchSpinner.setOnItemSelectedListener(this);
        respondSpinner.setAdapter(adapter);
        //respondSpinner.setOnItemSelectedListener(this);
        waitEventSpinner.setAdapter(adapter2);
        //waitEventSpinner.setOnItemSelectedListener(this);
        //waitIntervalSpinner.setAdapter(adapter3);
        //waitIntervalSpinner.setOnItemSelectedListener(this);

        waitEventSpinner.setSelection(getSpinnerSelectionFromAction(mEvent.waitAction));
        respondSpinner.setSelection(getSpinnerSelectionFromAction(mEvent.respondAction));
        watchSpinner.setSelection(getSpinnerSelectionFromAction(mEvent.watchAction));

        v.findViewById(R.id.delay_event_button).setOnClickListener(this);
        v.findViewById(R.id.delay_interval_button).setOnClickListener(this);
        v.findViewById(R.id.delay_none_button).setOnClickListener(this);
        v.findViewById(R.id.save_button).setOnClickListener(this);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
