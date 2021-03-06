package be.ana.nmct.multimania.ui;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import be.ana.nmct.multimania.R;
import be.ana.nmct.multimania.data.MultimaniaContract;

/**
 * This fragment takes care of the PagerAdapter for My Schedule
 */
public class MySchedulesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String TAG = MySchedulesFragment.class.getSimpleName();

    public List<String> mDates = new ArrayList<String>();
    private List<MyScheduleFragment> mFragmentList = new ArrayList<MyScheduleFragment>();
    private SchedulesPagerAdapter mViewPagerAdapter;
    private ViewPager mPager;

    public MySchedulesFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(MainActivity.LOADER_SCHEDULE_DATES_ID,null,this);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_schedules, container, false);
        mPager = (ViewPager) v.findViewById(R.id.schedulesPager);
        mViewPagerAdapter = new SchedulesPagerAdapter(getFragmentManager());
        mPager.setAdapter(mViewPagerAdapter);
        setHasOptionsMenu(true);
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_my_schedule, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case R.id.action_timegap:
                if(!mFragmentList.get(mPager.getCurrentItem()).checkForTimeGaps()){
                    Toast.makeText(getActivity(), getString(R.string.no_timegap_found), Toast.LENGTH_LONG).show();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStop() {
        super.onStop();

        //clear the undobar if active
        for(int i = 0; i < mViewPagerAdapter.getCount(); i++){
            MyScheduleFragment frag = (MyScheduleFragment)mViewPagerAdapter.getItem(i);
            if(frag.mUndoBar != null){
                frag.mUndoBar.clear();
                break;
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),MultimaniaContract.TalkEntry.DATE_CONTENT_URI,null,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        addDates(cursor);
        loader.abandon();
    }

    private void addDates(Cursor cursor) {
        int dayIndex = cursor.getColumnIndex(MultimaniaContract.TalkEntry.DAY);
        mDates.clear();
        while(cursor.moveToNext()){
            String date = cursor.getString(dayIndex);
            mDates.add(date);
        }
        if(mViewPagerAdapter!=null)
            mViewPagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mDates.clear();
        mViewPagerAdapter.notifyDataSetChanged();
    }

    private class SchedulesPagerAdapter extends FragmentStatePagerAdapter {

        private final String mDayString;

        public SchedulesPagerAdapter(FragmentManager fm) {
            super(fm);
            mDayString = getString(R.string.day);
        }

        @Override
        public Fragment getItem(int i) {
            Log.d(TAG,"Date in schedules: "+mDates.get(i));
            MyScheduleFragment fragment = MyScheduleFragment.newInstance(mDates.get(i), i);
            mFragmentList.add(fragment);
            return fragment;
        }

        @Override
        public int getCount() {
            return mDates.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mDayString+ " "+(position+1);
        }
    }
}
