package be.ana.nmct.multimania.data;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

/**
 * Created by Axel on 28/10/2014.
 */
public class ApiService<T> implements LoaderManager.LoaderCallbacks<List<T>> {

    private Context context;
    private String apiPath;
    private LoaderManager lm;
    private List<T> dataList;

    //nog niet helemaal async, nog eens over nadenken ;)
    public List<T> getData(){
        return dataList;
    }

    public ApiService(Context context, String apiPath, LoaderManager lm) {
        this.context = context;
        this.apiPath = apiPath;
        this.lm = lm;
        lm.initLoader(0, null, this);
    }

    @Override
    public Loader<List<T>> onCreateLoader(int i, Bundle bundle) {
        return new GsonLoader<T>(this.context, this.apiPath);
    }

    @Override
    public void onLoadFinished(Loader<List<T>> listLoader, List<T> objectList) {
        this.dataList = objectList;
    }

    @Override
    public void onLoaderReset(Loader<List<T>> listLoader) {

    }
}