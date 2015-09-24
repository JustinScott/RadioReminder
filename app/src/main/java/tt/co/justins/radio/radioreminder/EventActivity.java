package tt.co.justins.radio.radioreminder;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class EventActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        // create fragment, add it to activity, pass bundle
        Bundle bundle = new Bundle(getIntent().getExtras());
        EventFragment eventFragment = EventFragment.newInstance(bundle);

        getFragmentManager()
            .beginTransaction()
            .add(R.id.activity_event_root, eventFragment)
            .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_event, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_save_event) {
            return false;
        }

        if (id == R.id.action_delete_event) {
            return false;
        }

        return super.onOptionsItemSelected(item);
    }
}
