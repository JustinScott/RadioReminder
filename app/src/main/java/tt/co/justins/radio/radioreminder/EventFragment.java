package tt.co.justins.radio.radioreminder;



import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

public class EventFragment extends Fragment implements AdapterView.OnItemSelectedListener, View.OnClickListener{
    private static final String ARG_PARAM1 = "param1";

    private Event mEvent;

    private Spinner wifiWatchSpin;
    private Spinner wifiRespondSpin;
    private Spinner wifiEventSpin;
    private Spinner wifiIntervalSpin;

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
        TextView saveText = (TextView) getView().findViewById(R.id.save_text);
        saveText.setText("Changes not saved");
        saveText.setVisibility(View.VISIBLE);

        switch(getView().getId()) {
            //clicked the apply button
            //parse all the option selections and build an event object
            case R.id.save_button:
                Event myEvent = new Event();

                myEvent.serviceType = RadioService.SERVICE_WIFI;

                if(wifiWatchSpin.getSelectedItem().toString().equals("Radio On"))
                    myEvent.watchAction = RadioService.ACTION_WIFI_ON;
                else if(wifiWatchSpin.getSelectedItem().toString().equals("Radio Off"))
                    myEvent.watchAction = RadioService.ACTION_WIFI_OFF;

                if(wifiRespondSpin.getSelectedItem().toString().equals("Radio On"))
                    myEvent.respondAction = RadioService.ACTION_WIFI_ON;
                else if(wifiRespondSpin.getSelectedItem().toString().equals("Radio Off"))
                    myEvent.respondAction = RadioService.ACTION_WIFI_OFF;

                //determine which delay was selected
                RadioGroup buttonGroup = (RadioGroup) getView().findViewById(R.id.radioGroup);
                int buttonId = buttonGroup.getCheckedRadioButtonId();

                switch (buttonId) {
                    case R.id.delay_event_button:
                        switch(wifiEventSpin.toString()) {
                            case "Plugged In":
                                myEvent.waitAction = RadioService.ACTION_POWER_CONNECTED;
                                break;
                        }
                        break;
                    case R.id.delay_interval_button:
                        //myEvent.waitInterval = Float.parseFloat(wifiIntervalSpin.toString());
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mEvent = (Event) getArguments().getSerializable(ARG_PARAM1);
            if(mEvent != null) {
                //populate screen
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =inflater.inflate(R.layout.fragment_event, container, false);

        wifiWatchSpin = (Spinner) v.findViewById(R.id.wifi_watch_spin);
        wifiRespondSpin = (Spinner) v.findViewById(R.id.wifi_respond_spin);
        wifiEventSpin = (Spinner) v.findViewById(R.id.wifi_event_spin);
        wifiIntervalSpin = (Spinner) v.findViewById(R.id.wifi_interval_spin);

        v.findViewById(R.id.delay_event_button).setOnClickListener(this);
        v.findViewById(R.id.delay_interval_button).setOnClickListener(this);
        v.findViewById(R.id.delay_none_button).setOnClickListener(this);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        //Create the adapter that are needed to populate the spinners with values
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getView().getContext(), R.array.radio_state, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getView().getContext(), R.array.my_events, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(getView().getContext(), R.array.intervals, android.R.layout.simple_spinner_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //populate spinners with data
        //and assign the method that is called when an item is selected
        //this class needs to implement the OnItemSelectedListener interface for this to work
        wifiWatchSpin.setAdapter(adapter);
        wifiWatchSpin.setOnItemSelectedListener(this);
        wifiRespondSpin.setAdapter(adapter);
        wifiRespondSpin.setOnItemSelectedListener(this);
        wifiEventSpin.setAdapter(adapter2);
        wifiEventSpin.setOnItemSelectedListener(this);
        wifiIntervalSpin.setAdapter(adapter3);
        wifiIntervalSpin.setOnItemSelectedListener(this);
    }
}
