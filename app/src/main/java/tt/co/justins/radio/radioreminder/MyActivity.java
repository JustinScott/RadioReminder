package tt.co.justins.radio.radioreminder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;


public class MyActivity extends Activity implements AdapterView.OnItemSelectedListener {

    private Spinner wifiWatchSpin;
    private Spinner wifiRespondSpin;
    private Spinner wifiEventSpin;
    private Spinner wifiIntervalSpin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        //start the service, and tell it to set up it's state
        Intent intent = new Intent(this, RadioService.class);
        intent.setAction(RadioService.SERVICE_SETUP);
        intent.putExtra(RadioService.EXTRA_PARAM1, "");
        intent.putExtra(RadioService.EXTRA_PARAM2, "");
        startService(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        wifiWatchSpin = (Spinner) findViewById(R.id.wifi_watch_spin);
        wifiRespondSpin = (Spinner) findViewById(R.id.wifi_respond_spin);
        wifiEventSpin = (Spinner) findViewById(R.id.wifi_event_spin);
        wifiIntervalSpin = (Spinner) findViewById(R.id.wifi_interval_spin);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.radio_state, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.my_events, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(this, R.array.intervals, android.R.layout.simple_spinner_item);
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

    //part of OnItemSelected interface
    //fires when and item is selected in a spinner view
    public void onItemSelected(AdapterView av, View v, int pos, long id) {
        onClick(v);
    }

    //part of OnItemSelected interface
    public void onNothingSelected(AdapterView av) {

    }

    public void onClick(View v) {
        TextView saveText = (TextView) findViewById(R.id.save_text);
        saveText.setText("Changes not saved");
        saveText.setVisibility(View.VISIBLE);

        switch(v.getId()) {
            case R.id.event_button:
                break;
            case R.id.interval_button:
                break;
            //clicked the apply button
            //parse all the option selections and build an event object
            case R.id.save_button:
                Event myEvent = new Event();

                myEvent.serviceType = RadioService.SERVICE_WIFI;

                if(wifiWatchSpin.toString().equals("Radio On"))
                    myEvent.watchAction = RadioService.ACTION_WIFI_ON;
                else if(wifiWatchSpin.toString().equals("Radio Off"))
                    myEvent.watchAction = RadioService.ACTION_WIFI_OFF;

                if(wifiRespondSpin.toString().equals("Radio On"))
                    myEvent.respondAction = RadioService.ACTION_WIFI_ON;
                else if(wifiRespondSpin.toString().equals("Radio Off"))
                    myEvent.respondAction = RadioService.ACTION_WIFI_OFF;

                if(wifiEventSpin.toString().equals("Plugged In"))
                    myEvent.waitAction = RadioService.ACTION_POWER_CONNECTED;

                //myEvent.waitInterval = Float.parseFloat(wifiIntervalSpin.toString());

                //send event to the service
                Intent intent = new Intent(this, RadioService.class);
                intent.setAction(RadioService.SERVICE_EVENT);
                intent.putExtra(RadioService.EXTRA_EVENT, myEvent);
                startService(intent);

                saveText.setText("Changes saved");

                break;
            case R.id.checkbox:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
