package tt.co.justins.radio.radioreminder;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;


public class MyActivity extends FragmentActivity {
    public static List<Event> eventList = new ArrayList<Event>();

    private ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        //start the service, and tell it to set up it's state
        Intent intent = new Intent(this, RadioService.class);
        intent.setAction(RadioService.SERVICE_SETUP);
        startService(intent);

//        Event e = new Event();
//        e.waitInterval = 120;
//        e.waitAction = null;
//        e.watchAction = RadioService.ACTION_WIFI_OFF;
//        e.respondAction = RadioService.ACTION_WIFI_OFF;
//        eventList.add(e);

        pager = (ViewPager) findViewById(R.id.view_pager);
        FragmentManager manager = this.getSupportFragmentManager();
        FragmentAdapter fragmentAdapter = new FragmentAdapter(manager);
        pager.setAdapter(fragmentAdapter);
    }

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.new_event_button:
                Log.d("radioreminder", "New event added to list");
                Event e = new Event();
                eventList.add(e);
                pager.getAdapter().notifyDataSetChanged();
                break;
            case R.id.delete_event_button:
                int currentPosition = pager.getCurrentItem();

                eventList.remove(currentPosition);
                pager.getAdapter().notifyDataSetChanged();
                if(currentPosition > eventList.size())
                    pager.setCurrentItem(currentPosition - 1);
                else

                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
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
