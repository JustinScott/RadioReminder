package tt.co.justins.radio.radioreminder;

import android.app.Activity;
import android.app.ListFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class EventListFragment extends ListFragment {
    private String tag = "ListFragment";

    private static List<Event> eventList = null;
    EventListAdapter listAdapter;
    private Activity mActivity;

    private RadioService mService;
    private boolean mBound;
    private boolean mStarted;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            RadioService.RadioBinder binder = (RadioService.RadioBinder) iBinder;
            mService = binder.getService();
            mBound = true;

            Log.d(tag, "Connected to service. Grabbing event list.");
            eventList = mService.getEventList();

            if (eventList != null) {
                Log.d(tag, "Updating list in adapter, and invalidating old list data.");
                listAdapter.updateList(eventList);
                listAdapter.notifyDataSetChanged();
            }

            //kill the service if it is running and has an empty event list
            if(eventList == null || eventList.size() == 0)
            {
                Log.d(tag, "Service running in background with empty list. Turning it off.");
                Intent intent = new Intent(getActivity(), RadioService.class);
                getActivity().stopService(intent);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
            mBound = false;
        }
    };

    public EventListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        Log.v(tag, "onAttach called.");
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(tag, "onCreate called.");
        setHasOptionsMenu(true);

        if(savedInstanceState == null) { //activity created for the first time
        } else { //activity recreated, service should already be running
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(tag, "onCreateView called.");
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_list, container, false);

        // Set the listAdapter for the list view
        Log.d(tag, "Setting the list listAdapter with an empty list");
        //ListView listView = (ListView) view.findViewById(R.id.event_list);
        listAdapter = new EventListAdapter(mActivity);
        setListAdapter(listAdapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(tag, "onStart called. Binding to service.");
        Intent intent = new Intent(mActivity, RadioService.class);
        mActivity.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(tag, "onResume called.");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.v(tag, "onStop called.");
        if (mBound) {
            Log.v(tag, "Unbind from service.");
                    mActivity.unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void startEventActivity(int listPosition) {
        Log.d(tag, "Launching event activity.");
        Event event;

        if(listPosition < 0 || listPosition >= eventList.size())
            event = null;
        else
            event = eventList.get(listPosition);

        Intent intent = new Intent(mActivity, EventActivity.class);
        intent.putExtra(RadioService.EXTRA_EVENT_POSITION, listPosition);
        intent.putExtra(RadioService.EXTRA_EVENT, event);
        startActivity(intent);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_list_event, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_event) {
            startEventActivity(RadioService.NEW_EVENT);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        startEventActivity(position);
    }
}
