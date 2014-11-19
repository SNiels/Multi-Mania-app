package be.ana.nmct.multimania.ui;


import android.app.Fragment;
import android.app.LoaderManager;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bulletnoid.android.widget.StaggeredGridView.StaggeredGridView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import be.ana.nmct.multimania.R;
import be.ana.nmct.multimania.data.ApiActions;
import be.ana.nmct.multimania.data.MultimaniaContract;
import be.ana.nmct.multimania.model.Talk;
import be.ana.nmct.multimania.service.NotificationSender;
import be.ana.nmct.multimania.utils.GoogleCalUtil;
import be.ana.nmct.multimania.utils.SettingsUtil;
import be.ana.nmct.multimania.utils.Utility;
import be.ana.nmct.multimania.vm.ScheduleTalkVm;

public class ScheduleFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {
    public static final String TAG = ScheduleFragment.class.getSimpleName();
    public static final String DATE_KEY = "date_key";
    public static final String POSITION_KEY = "position_key";

    private StaggeredGridView mScheduleGrid;
    private ScheduleAdapter mAdapter;
    private Cursor mCursor;
    private String mDate;
    private int mPosition;
    private String mAccountName;
    private List<Object> mItems;

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

        mAccountName = new SettingsUtil(getActivity(), GoogleCalUtil.PREFERENCE_NAME).getStringPreference(GoogleCalUtil.PREFERENCE_ACCOUNTNAME);
        //setRetainInstance(true);
        mItems=new ArrayList<Object>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_schedule, container, false);
        mScheduleGrid = (StaggeredGridView) v.findViewById(R.id.scheduleGrid);
        mScheduleGrid.setItemMargin(Utility.dpToPx(getActivity(),8));
        // initialize your items array
        mAdapter = new ScheduleAdapter(getActivity(), mItems);

        mScheduleGrid.setAdapter(mAdapter);

        getLoaderManager().initLoader(MainActivity.LOADER_SCHEDULE_TALK_ID+mPosition, null, this);
        //BuildItems(mCursor);
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
        BuildItems(cursor);
    }

    private void BuildItems(Cursor cursor) {
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

        if(cursor.moveToFirst()){
            do{
                final ScheduleTalkVm vm = new ScheduleTalkVm();

                String dateFrom = cursor.getString(dateFromIndex);
                String dateUntil = cursor.getString(dateUntilIndex);

                vm.isKeynote = cursor.getInt(isKeynoteIndex)==1;
                vm.isFavorite = cursor.getInt(isFavoriteIndex)==1;
                String title = cursor.getString(titleIndex);
                String room = cursor.getString(roomIndex);
                final long talkId = cursor.getLong(idIndex);
                if(!dates.contains(dateFrom)){
                    try {
                        mItems.add(
                                Utility.getTimeString(dateFrom)
                                        + " - " +
                                Utility.getTimeString(dateUntil)
                        );
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    dates.add(dateFrom);
                }
                vm.title=title;
                vm.room=room;
                vm.id =talkId;

                getLoaderManager().initLoader(1000+(int)talkId,null,new LoaderManager.LoaderCallbacks<Cursor>() {
                    @Override
                    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                        return new CursorLoader(getActivity(),
                                ContentUris.appendId(MultimaniaContract.TagEntry.CONTENT_URI.buildUpon()
                                        .appendPath(MultimaniaContract.PATH_TALK), talkId).build()
                                ,null,null,null,null);
                    }

                    @Override
                    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                        vm.tags = "";
                        if(data.moveToFirst()){
                            final int nameIndex = data.getColumnIndex(MultimaniaContract.TagEntry.NAME);
                            do{
                                vm.tags +=data.getString(nameIndex)+", ";
                            }while(data.moveToNext());
                            if(vm.tags.lastIndexOf(", ")>-1)
                                vm.tags=vm.tags.substring(0,vm.tags.length()-2);
                        }
                        loader.abandon();
                    }

                    @Override
                    public void onLoaderReset(Loader<Cursor> loader) {

                    }
                });
                mItems.add(vm);
            }while(cursor.moveToNext());
        }
        if(mAdapter!=null){
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //mScheduleGrid.setAdapter(null);
        mCursor=null;
        mItems.clear();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object item =  mAdapter.getItem(position);
        if(item instanceof ScheduleTalkVm){
            ScheduleTalkVm vm = (ScheduleTalkVm) item;
            Uri uri = MultimaniaContract.TalkEntry.buildItemUri(vm.id);
            Intent intent = new Intent(getActivity(),TalkActivity.class);
            intent.setData(uri);
            startActivity(intent);
        }
    }

    private class ScheduleAdapter extends ArrayAdapter<Object> {

        private static final int SCHEDULE_GRID_HEADER_TYPE = 0;
        private static final int SCHEDULE_GRID_ITEM_TYPE = 1;
        private final LayoutInflater mInflater;

        public ScheduleAdapter(Context context, List<Object> objects) {
            super(context,0,objects);
            mInflater = LayoutInflater.from(getActivity());
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
            imgButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    item.isFavorite=!item.isFavorite;
                    int value = item.isFavorite?1:0;
                    imgButton.setImageResource(getStarDrawabale(item.isFavorite));

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
                        ApiActions.postFavoriteTalk(getActivity(),mAccountName, item.id);
                    }

                    //add/delete alarm when needed
                    Uri uri = MultimaniaContract.TalkEntry.buildItemUri(item.id);
                    SettingsUtil util = new SettingsUtil(getActivity(), SettingsFragment.PREFERENCE_NAME);
                    Talk talk = Utility.getTalkFromUri(getActivity(), uri);
                    NotificationSender notSender = new NotificationSender(getActivity());
                    if(util.getBooleanPreference(SettingsFragment.PREFERENCE_NOTIFY, true)){
                        if(item.isFavorite){
                            notSender.setAlarmForTalk(talk);
                        } else{
                            notSender.cancelAlarmForTalk(talk);
                        }
                    }

                }
            });
            imgButton.setImageResource(getStarDrawabale(item.isFavorite));
            ((TextView) view.findViewById(R.id.txtRoom)).setText(item.room);
            ((TextView) view.findViewById(R.id.txtTag)).setText(item.tags);

            StaggeredGridView.LayoutParams lp = new StaggeredGridView.LayoutParams(view.getLayoutParams());
            if(item.isKeynote){
                lp.span = mScheduleGrid.getColumnCount();
            }else{
                lp.span = 1;
            }
            view.setLayoutParams(lp);
        }

        private int getStarDrawabale(boolean isFavorite) {
            return  isFavorite  ? R.drawable.ic_action_important_green :  R.drawable.ic_action_not_important_green;
        }

        private void bindHeaderView(View convertView, String item) {
            ((TextView)convertView).setText(item);
            StaggeredGridView.LayoutParams lp = new StaggeredGridView.LayoutParams(convertView.getLayoutParams());
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
}
