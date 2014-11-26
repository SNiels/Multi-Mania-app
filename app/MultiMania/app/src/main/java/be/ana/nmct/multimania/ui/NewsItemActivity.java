package be.ana.nmct.multimania.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class NewsItemActivity extends Activity implements NewsItemFragment.TitleLoadListener {

    private Uri mUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Configuration configuration = getResources().getConfiguration();
        if ((configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE||
                (configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE){
            showAsPopup(this);
        }

        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mUri=intent.getData();

        if (savedInstanceState == null) {
            NewsItemFragment fragment = new NewsItemFragment();
            Bundle extras = new Bundle();
            extras.putParcelable(NewsItemFragment.URI_KEY,mUri);
            fragment.setArguments(extras);

            getFragmentManager().beginTransaction()
                    .add(android.R.id.content,fragment)
                    .commit();
            fragment.setTitleLoadListener(this);
        }else{
            NewsItemFragment fragment = (NewsItemFragment) getFragmentManager().findFragmentById(android.R.id.content);
            if(fragment!=null){
                fragment.setTitleLoadListener(this);
            }
        }
    }

    @Override
    public void onTitleloaded(String title) {
        ActionBar bar = getActionBar();
        if(bar!=null)
            bar.setTitle(title);
    }

    public static void showAsPopup(Activity activity) {
        //To show activity as dialog and dim the background, you need to declare android:theme="@style/PopupTheme" on for the chosen activity on the manifest
        activity.requestWindowFeature(Window.FEATURE_ACTION_BAR);
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        WindowManager.LayoutParams params = activity.getWindow().getAttributes();
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = 850; //fixed width
        params.alpha = 1.0f;
        params.dimAmount = 0.5f;
        activity.getWindow().setAttributes(params);
    }
}
