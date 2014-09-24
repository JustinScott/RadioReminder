package tt.co.justins.radio.radioreminder;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class FragmentAdapter extends FragmentStatePagerAdapter {
    public FragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment;
        fragment = EventFragment.newInstance(MyActivity.eventList.get(i), i);
        return fragment;
    }

    @Override
    public int getCount() {
        return MyActivity.eventList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "Event " + (position + 1);
    }
}
