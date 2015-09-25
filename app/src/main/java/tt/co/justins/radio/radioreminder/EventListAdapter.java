package tt.co.justins.radio.radioreminder;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class EventListAdapter extends BaseAdapter implements ListAdapter {

    List<Event> mEventList;
    Context mContext;
    private String tag = "EventListAdapter";

    EventListAdapter(Context context){
        mContext = context;
        mEventList = new ArrayList<>();
    }

    public void updateList(List<Event> eventList) {
        mEventList = new ArrayList<Event>(eventList);
    }

    @Override
    public int getCount() {
        return mEventList.size();
    }

    @Override
    public Object getItem(int i) {
        return mEventList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.view_event, viewGroup, false);
        }

        TextView textName = (TextView) view.findViewById(R.id.text_name);
        TextView textResponse = (TextView) view.findViewById(R.id.text_response_event);
        TextView textWatch = (TextView) view.findViewById(R.id.text_watch_event);
        TextView textWait = (TextView) view.findViewById(R.id.text_delay);

        Event event = mEventList.get(i);
        textName.setText("Name: " + event.name);
        textWatch.setText("Watch Event: " + event.watchAction.substring(event.watchAction.lastIndexOf('.')));
        textResponse.setText("Response Event: " + event.respondAction.substring(event.respondAction.lastIndexOf('.')));
        String wait = "Wait Event: ";
        if(event.waitAction.equals(""))
            wait += event.waitInterval + " minutes.";
        else
            wait += event.waitAction.substring(event.waitAction.lastIndexOf('.'));
        textWait.setText(wait);

        return view;
    }
}
