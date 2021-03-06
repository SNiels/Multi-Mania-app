package be.ana.nmct.multimania.ui;


import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bulletnoid.android.widget.StaggeredGridView.BulletStaggeredGridView;
import com.koushikdutta.async.future.FutureCallback;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import be.ana.nmct.multimania.R;
import be.ana.nmct.multimania.data.ApiActions;
import be.ana.nmct.multimania.data.MultimaniaContract;
import be.ana.nmct.multimania.service.SyncAdapter;
import be.ana.nmct.multimania.utils.SettingsHelper;
import be.ana.nmct.multimania.utils.SettingsUtil;
import be.ana.nmct.multimania.utils.Utility;
import be.ana.nmct.multimania.vm.ScheduleTalkVm;

/**
 * The ScheduleFragment shows the schedule for a specific date
 */
public class ScheduleFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, BulletStaggeredGridView.OnItemClickListener {
    public static final String TAG = ScheduleFragment.class.getSimpleName();
    public static final String DATE_KEY = "date_key";
    public static final String POSITION_KEY = "position_key";

    private BulletStaggeredGridView mScheduleGrid;
    private ScheduleAdapter mAdapter;
    private Cursor mCursor;
    private String mDate;
    private int mPosition;
    private String mAccountName;
    private List<Object> mItems;
    private String mFilterTag;
    private SettingsHelper mSettingsHelper;

    public ScheduleFragment() {}

    public static ScheduleFragment newInstance(String date,int position) {
        ScheduleFragment fragmentFirst = new ScheduleFragment();
        Bundle args = new Bundle();
        args.putString(DATE_KEY, date);
        args.putInt(POSITION_KEY,position);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDate = getArguments().getString(DATE_KEY);
        mPosition = getArguments().getInt(POSITION_KEY);
        mSettingsHelper = new SettingsHelper(getActivity());
        mAccountName =  new SettingsUtil(getActivity().getApplicationContext(), MainActivity.PREFERENCE_NAME).getStringPreference(MainActivity.PREFERENCE_ACCOUNT);
        //setRetainInstance(true);
        mItems = new ArrayList<>();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SyncAdapter.SYNC_READY_BROADCAST);
        activity.registerReceiver(syncCompleteReceiver, intentFilter);
    }

    @Override
    public void onDetach() {
        getActivity().unregisterReceiver(syncCompleteReceiver);
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_schedule, container, false);
        mScheduleGrid = (BulletStaggeredGridView) v.findViewById(R.id.scheduleGrid);
        mScheduleGrid.setItemMargin(Utility.dpToPx(getActivity(), 8));
        // initialize your items array
        mAdapter = new ScheduleAdapter(getActivity(), mItems);

        mScheduleGrid.setAdapter(mAdapter);
        mScheduleGrid.setOnItemClickListener(this);

        getLoaderManager().initLoader(MainActivity.LOADER_SCHEDULE_TALK_ID+mPosition, null, this);

        //buildItems(mCursor);
        return v;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), MultimaniaContract.TalkEntry.CONTENT_URI,null,
                MultimaniaContract.TalkEntry.DATE_FROM + " LIKE ?"
                ,new String[]{mDate+"%"}, MultimaniaContract.TalkEntry.DATE_FROM);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursor = cursor;
        try {
            buildItems(cursor);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        loader.abandon();
    }

    /**
     * Builds all the ViewModels
     * @param cursor A Cursor with talk data
     * @throws ParseException
     */
    private void buildItems(Cursor cursor) throws ParseException {
        if(cursor==null||mItems==null)return;
        mItems.clear();

        List<String> dates = new ArrayList<String>();

        final int dateFromIndex     = cursor.getColumnIndex(MultimaniaContract.TalkEntry.DATE_FROM);
        final int dateUntilIndex    = cursor.getColumnIndex(MultimaniaContract.TalkEntry.DATE_UNTIL);
        final int isKeynoteIndex    = cursor.getColumnIndex(MultimaniaContract.TalkEntry.IS_KEYNOTE);
        final int isFavoriteIndex   = cursor.getColumnIndex(MultimaniaContract.TalkEntry.IS_FAVORITE);
        final int titleIndex        = cursor.getColumnIndex(MultimaniaContract.TalkEntry.TITLE);
        final int roomIndex         = cursor.getColumnIndex(MultimaniaContract.TalkEntry.ROOM_NAME);
        final int idIndex           = cursor.getColumnIndex(MultimaniaContract.TalkEntry._ID);
        final int calEventIdIndex   = cursor.getColumnIndex(MultimaniaContract.TalkEntry.CALEVENT_ID);
        final int descriptionIndex  = cursor.getColumnIndex(MultimaniaContract.TalkEntry.DESCRIPTION);
        final int tagsIndex         = cursor.getColumnIndex(MultimaniaContract.TalkEntry.TAGS);
        if(cursor.moveToFirst()){
            do{
                final ScheduleTalkVm vm = new ScheduleTalkVm();

                String dateFrom = cursor.getString(dateFromIndex);
                String dateUntil = cursor.getString(dateUntilIndex);

                vm.isKeynote = cursor.getInt(isKeynoteIndex)==1;
                vm.isFavorite = cursor.getInt(isFavoriteIndex)==1;
                vm.room = cursor.getString(roomIndex);
                vm.title = cursor.getString(titleIndex);
                vm.id = cursor.getLong(idIndex);
                vm.calEventId = cursor.getLong(calEventIdIndex);
                vm.description = cursor.getString(descriptionIndex);
                vm.from = Utility.convertStringToDate(dateFrom);
                vm.to = Utility.convertStringToDate(dateUntil);
                vm.tags = cursor.getString(tagsIndex);
                if(!dates.contains(dateFrom)){
                        mItems.add(
                                Utility.getTimeString(dateFrom)
                                        + " - " +
                                Utility.getTimeString(dateUntil)
                        );
                    dates.add(dateFrom);
                }
                mItems.add(vm);
            }while(cursor.moveToNext());
        }
        /*if(mAdapter!=null){
            mAdapter.notifyDataSetChanged();//bug in staggeredgridview with updates
        }*/
        if(mScheduleGrid!=null){
            mAdapter=new ScheduleAdapter(getActivity(),mItems);
            mScheduleGrid.setAdapter(mAdapter);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //mScheduleGrid.setAdapter(null);
        mCursor=null;
        mItems.clear();
        mScheduleGrid.setAdapter(mAdapter);
        //mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(BulletStaggeredGridView parent, View view, int position, long id) {
        Object item =  mAdapter.getItem(position);
        if(item instanceof ScheduleTalkVm){
            ScheduleTalkVm vm = (ScheduleTalkVm) item;
            Uri uri = MultimaniaContract.TalkEntry.buildItemUri(vm.id);
            Intent intent = new Intent(getActivity(),TalkActivity.class);
            intent.setData(uri);
            startActivity(intent);
        }
    }

    public void onFilterChanged(String tag) {
        mFilterTag = tag;
        if(mScheduleGrid!=null){
            mScheduleGrid.setAdapter(mAdapter);
        }
    }

    private class ScheduleAdapter extends ArrayAdapter<Object> {

        private static final int SCHEDULE_GRID_HEADER_TYPE = 0;
        private static final int SCHEDULE_GRID_ITEM_TYPE = 1;
        private final LayoutInflater mInflater;
        private final Animation mFadeOutAnim;
        private final Animation mFadeInAnim;

        public ScheduleAdapter(Context context, List<Object> objects) {
            super(context,0,objects);
            mInflater = LayoutInflater.from(getActivity());
            mFadeOutAnim = AnimationUtils.loadAnimation(getActivity(),R.anim.fade_out_partially);
            mFadeInAnim = AnimationUtils.loadAnimation(getActivity(),R.anim.fade_in_partially);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Object item = getItem(position);
            final int viewType = getItemViewType(position);
            if (convertView == null||!isCorrectType(viewType,convertView)) {
                convertView = newView(viewType,parent);
            }
            bindView(convertView,item,viewType);

            return convertView;
        }

        private boolean isCorrectType(final int viewType,View convertView) {
            Class cls = convertView.getClass();
            switch (viewType){
                case SCHEDULE_GRID_HEADER_TYPE:
                    return cls == TextView.class;
                case SCHEDULE_GRID_ITEM_TYPE:
                    return cls == RelativeLayout.class;
                default:
                    return false;
            }

        }

        private void bindView(View convertView, Object item,final int viewType) {
            switch (viewType){
                case SCHEDULE_GRID_HEADER_TYPE:
                    bindHeaderView(convertView,(String)item);
                    break;
                case SCHEDULE_GRID_ITEM_TYPE:
                    bindItemView(convertView, (ScheduleTalkVm) item);
                    break;
            }
        }

        private void bindItemView(View view,final ScheduleTalkVm item) {
            ((TextView)view.findViewById(R.id.txtTitle)).setText(item.title);
            final ImageButton imgButton = (ImageButton) view.findViewById(R.id.btnFavorite);
            Utility.enlargeTouchArea(view.findViewById(R.id.scheduleRowRoot), imgButton, 10);
            imgButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    item.isFavorite=!item.isFavorite;
                    int value = item.isFavorite?1:0;
                    imgButton.setImageResource(getStarDrawable(item.isFavorite));

                    ContentValues values = new ContentValues();
                    values.put(MultimaniaContract.TalkEntry.IS_FAVORITE,value);
                    AsyncQueryHandler handler = new ScheduleAsyncQueryHandler(getActivity().getContentResolver());
                    handler.startUpdate(
                            0,
                            null,
                            MultimaniaContract.TalkEntry.CONTENT_URI,
                            values,
                            MultimaniaContract.TalkEntry._ID+"=?",
                            new String[]{""+ item.id}
                    );
                    if(mAccountName!=null){
                        if( item.isFavorite){
                            ApiActions.postFavoriteTalk(getActivity(), mAccountName,item.id);
                        }else{
                            ApiActions.deleteFavoriteTalk(getActivity(), mAccountName, item.id).setCallback(new FutureCallback<String>() {
                                @Override
                                public void onCompleted(Exception e, String result) {
                                    Log.d(TAG,"Favorite deleted status:"+result);
                                }
                            });
                        }
                    }
                    mSettingsHelper.settingsHandler(item);
                }
            });
            imgButton.setImageResource(getStarDrawable(item.isFavorite));
            ((TextView) view.findViewById(R.id.txtRoom)).setText(item.room);
            ((TextView) view.findViewById(R.id.txtTag)).setText(item.tags);

            if(mFilterTag!=null){
                if(!item.tags.toLowerCase().contains(mFilterTag.toLowerCase())) {
                    view.setAnimation(mFadeOutAnim);
                }else{
                    view.setAnimation(mFadeInAnim);
                }
            }

            BulletStaggeredGridView.LayoutParams lp = new BulletStaggeredGridView.LayoutParams(view.getLayoutParams());
            if(item.isKeynote){
                lp.span = mScheduleGrid.getColumnCount();
            }else{
                lp.span = 1;
            }
            view.setLayoutParams(lp);
        }

        private int getStarDrawable(boolean isFavorite) {
            return  isFavorite  ? R.drawable.ic_action_important_green :  R.drawable.ic_action_not_important_green;
        }

        private void bindHeaderView(View convertView, String item) {
            ((TextView)convertView).setText(item);
            BulletStaggeredGridView.LayoutParams lp = new BulletStaggeredGridView.LayoutParams(convertView.getLayoutParams());
            lp.span=mScheduleGrid.getColumnCount();
            convertView.setLayoutParams(lp);
        }

        private View newView(final int viewType,ViewGroup parent) {
            View view = null;
            switch(viewType){
                case SCHEDULE_GRID_HEADER_TYPE:
                    view = mInflater.inflate(R.layout.row_schedule_header,parent,false);
                    break;
                case SCHEDULE_GRID_ITEM_TYPE:
                    view = mInflater.inflate(R.layout.row_schedule,parent,false);
                    break;
            }
            return view;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            Object item = getItem(position);
            if(item instanceof ScheduleTalkVm){
                return SCHEDULE_GRID_ITEM_TYPE;
            }else{
                return SCHEDULE_GRID_HEADER_TYPE;
            }
        }


    }
    private class ScheduleAsyncQueryHandler extends AsyncQueryHandler{

        public ScheduleAsyncQueryHandler(ContentResolver cr) {
            super(cr);
        }
    }

    private BroadcastReceiver syncCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Sync ready received");
            getLoaderManager().restartLoader(MainActivity.LOADER_SCHEDULE_TALK_ID,null,ScheduleFragment.this);
        }
    };
}
