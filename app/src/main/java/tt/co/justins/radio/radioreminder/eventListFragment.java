package tt.co.justins.radio.radioreminder;

import android.app.Activity;
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

public class EventListFragment extends Fragment {
    private String tag = "ListFragment";

    private OnFragmentInteractionListener mListener;

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

            Log.d(tag, "Connected to service. Grabbing event list, and setting adapter.");
            eventList = mService.getEventList();

            if (eventList != null) {
                listAdapter.updateList(eventList);
                listAdapter.notifyDataSetChanged();
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
        Log.d(tag, "onAttach called.");
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        mActivity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(tag, "onCreate called.");
        setHasOptionsMenu(true);

        if(savedInstanceState == null) { //activity created for the first time
            //start the service, and tell it to set up it's state
            Log.d(tag, "SavedInstanceState is null. Starting radio service.");
            Intent intent = new Intent(getActivity(), RadioService.class);
            intent.setAction(RadioService.SERVICE_SETUP);
            getActivity().startService(intent);
        } else { //activity recreated, service should already be running
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(tag, "onCreateView called.");
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_list, container, false);

        // Set the listAdapter for the list view
        Log.d(tag, "Setting the list listAdapter with an empty list");
        ListView listView = (ListView) view.findViewById(R.id.event_list);
        listAdapter = new EventListAdapter(mActivity);
        listView.setAdapter(listAdapter);

        //set the text for an empty list
        TextView emptyText = (TextView) view.findViewById(R.id.empty_text);
        listView.setEmptyView(emptyText);

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
        Log.d(tag, "onResume called.");
    }

    @Override
    public void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            mActivity.unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void startEventActivity(int listPosition, Event event) {
        Log.d(tag, "Launching event activity.");
        Intent intent = new Intent(mActivity, EventActivity.class);
        intent.putExtra(RadioService.EXTRA_EVENT_POSITION, listPosition);
        intent.putExtra(RadioService.EXTRA_EVENT, event);
        startActivity(intent);
    }

    private void logEvent(Event e) {
        Log.d(tag, "-- Watch: " + e.watchAction);
        Log.d(tag, "-- Response: " + e.respondAction);
        if(e.waitAction != null)
            Log.d(tag, "-- Wait Action: " + e.waitAction);
        else if(e.waitInterval != 0)
            Log.d(tag, "-- Wait Interval: " + e.waitInterval + " mins.");
        else
            Log.d(tag, "-- No Wait Specified.");
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
            startEventActivity(RadioService.NEW_EVENT, null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
