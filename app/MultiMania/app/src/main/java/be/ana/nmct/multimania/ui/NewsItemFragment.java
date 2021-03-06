package be.ana.nmct.multimania.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.manuelpeinado.fadingactionbar.FadingActionBarHelper;
import com.manuelpeinado.fadingactionbar.FadingActionBarHelperBase;

import be.ana.nmct.multimania.R;
import be.ana.nmct.multimania.data.MultimaniaContract;
import be.ana.nmct.multimania.utils.Utility;

/**
 * NewsItemFragment shows the data for one NewsItem
 * The NewsItemFragment lives inside the NewsItemActivity
 * @see be.ana.nmct.multimania.ui.NewsItemActivity
 */
public class NewsItemFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String URI_KEY = "uri_key";
    private Uri mUri=null;
    private ImageView mNewsItemImg;
    private TextView mNewsItemTitle;
    private WebView mNewsItemInfo;
    private FadingActionBarHelperBase mFadingHelper;
    private TitleLoadListener mTitleLoadListener;
    private Cursor mData;

    public NewsItemFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getArguments() != null) {
            mUri = getArguments().getParcelable(URI_KEY);
        }
        getLoaderManager().initLoader(0, null, this);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = mFadingHelper.createView(inflater);
        mNewsItemImg = (ImageView) view.findViewById(R.id.img);
        mNewsItemTitle = (TextView) view.findViewById(R.id.title);
        mNewsItemInfo = (WebView) view.findViewById(R.id.txtNewsItemInfo);

        mNewsItemInfo.setBackgroundColor(0x00000000);
        if (Build.VERSION.SDK_INT >= 11) mNewsItemInfo.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);

        BindData(mData);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.news_item, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_share: {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, mNewsItemTitle.getText().toString() + ". " + getString(R.string.readmoreatmm)+ ". " + getString(R.string.hashtag_mm));
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.share_with)));
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mFadingHelper = new FadingActionBarHelper()
                .parallax(false)
                .actionBarBackground(R.drawable.ab_background)
                .headerLayout(R.layout.image_title)
                .contentLayout(R.layout.fragment_news_item);
        if(activity.getActionBar()!=null){
            mFadingHelper.initActionBar(activity);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),mUri,null,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mData = data;
        BindData(data);
        loader.abandon();
    }

    private void BindData(Cursor cursor) {
        if(cursor==null)return;
        if(cursor.moveToFirst()) {
            int titleCol = cursor.getColumnIndexOrThrow(MultimaniaContract.NewsItemEntry.TITLE);
            int imgCol = cursor.getColumnIndexOrThrow(MultimaniaContract.NewsItemEntry.IMAGE);
            int shortDescriptionCol = cursor.getColumnIndexOrThrow(MultimaniaContract.NewsItemEntry.SHORT_DESCRIPTION);
            int longDescriptionCol = cursor.getColumnIndexOrThrow(MultimaniaContract.NewsItemEntry.LONG_DESCRIPTION);

            Animation animFadein = AnimationUtils.loadAnimation(getActivity().getApplicationContext(),
                    R.anim.fade_in);

            String title = cursor.getString(titleCol);
            if (mTitleLoadListener != null) {
                mTitleLoadListener.onTitleloaded(title);
            }
            String shortDescription = cursor.getString(shortDescriptionCol);
            String longDescription = cursor.getString(longDescriptionCol);
            if (TextUtils.isEmpty(longDescription)) {
                longDescription = shortDescription;
            }
            String img = cursor.getString(imgCol);

            mNewsItemTitle.setText(title);

            String mime = "text/html";
            String encoding = "utf-8";
            String html = Utility.getHtml(longDescription);
            mNewsItemInfo.getSettings().setJavaScriptEnabled(true);
            mNewsItemInfo.loadDataWithBaseURL("file:///android_asset/", html, mime, encoding, null);
            Animation anim =Utility.getAlphaAnimation(0, 1, 400, 300);
            mNewsItemInfo.setAnimation(anim);
            anim.start();

            // holder.imgNews.setImageURI(Uri.parse(img));
            Ion.with(mNewsItemImg)
                    .smartSize(true)
                    .animateIn(animFadein)
                    .load(img);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void setTitleLoadListener(TitleLoadListener listener){
        mTitleLoadListener =listener;
    }

    public interface TitleLoadListener {
        public void onTitleloaded(String title);
    }
}
