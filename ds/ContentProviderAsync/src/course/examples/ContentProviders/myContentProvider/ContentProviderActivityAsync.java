package course.examples.ContentProviders.myContentProvider;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;

public class ContentProviderActivityAsync extends ListActivity 
                                     implements LoaderManager.LoaderCallbacks<Cursor>
{
    // The loader's unique id. Loader ids are specific to the Activity or
    // Fragment in which they reside.
    private static final int LOADER_ID = 0;

    // The callbacks through which we will interact with the LoaderManager.
    private LoaderManager.LoaderCallbacks<Cursor> mCallbacks;

    // The adapter that binds our data to the ListView
    private SimpleCursorAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ContentResolver cr = getContentResolver();
        
        ContentValues values = new ContentValues();
        Uri uri = null;

        values.put("data", "Record1");
        uri = cr.insert(MyContentProvider.CONTENT_URI, values);

        values.clear();
        values.put("data", "Record2");
        uri = cr.insert(MyContentProvider.CONTENT_URI, values);
        
        values.clear();
        values.put("data", "Record3");
        uri = cr.insert(MyContentProvider.CONTENT_URI, values);
        
        cr.delete(Uri.parse(MyContentProvider.CONTENT_URI + "/1"), (String) null, (String[]) null );
        
        values.clear();
        values.put("data", "Record4");
        cr.update(Uri.parse (MyContentProvider.CONTENT_URI + "/2"), values, (String) null, (String[]) null);

        String[] dataColumns = {"_id","data"};
        int[] viewIDs = {R.id.idString, R.id.data};

        // Initialize the adapter. Note that we pass a "null" Cursor
        // as the third argument. We will pass the adapter a Cursor
        // only when the data has finished loading for the first time
        // (i.e. when the LoaderManager delivers the data to
        // onLoadFinished). Also note that we have passed the "0" flag
        // as the last argument. This prevents the adapter from
        // registering a ContentObserver for the Cursor (the
        // CursorLoader will do this for us!).
        mAdapter = new SimpleCursorAdapter(this, R.layout.list_layout,
                                           null, dataColumns, viewIDs, 0);
 
        // Associate the (now empty) adapter with the ListView.
        setListAdapter(mAdapter);
 
        // The Activity (which implements the LoaderCallbacks<Cursor>
        // interface) is the callbacks object through which we will
        // interact with the LoaderManager. The LoaderManager uses
        // this object to instantiate the Loader and to notify the
        // client when data is made available/unavailable.
        mCallbacks = this;

        // Initialize the Loader with id "0" and callbacks
        // "mCallbacks".  If the loader doesn't already exist, one is
        // created. Otherwise, the already created Loader is
        // reused. In either case, the LoaderManager will manage the
        // Loader across the Activity/Fragment lifecycle, will receive
        // any new loads once they have completed, and will report
        // this new data back to the "mCallbacks" object.
        getLoaderManager().initLoader(LOADER_ID, null, mCallbacks);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Create a new CursorLoader with the following query parameters.
        return new CursorLoader(ContentProviderActivityAsync.this, MyContentProvider.CONTENT_URI,
                                null, null, null, null);
    }
 
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // A switch-case is useful when dealing with multiple Loaders/IDs
        switch (loader.getId()) {
        case LOADER_ID:
            // The asynchronous load is complete and the data is now
            // available for use. Only now can we associate the
            // queried Cursor with the SimpleCursorAdapter.
            mAdapter.swapCursor(cursor);
            break;
        }
        // The listview now displays the queried data.
    }
 
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // For whatever reason, the Loader's data is now unavailable.
        // Remove any references to the old data by replacing it with
        // a null Cursor.
        mAdapter.swapCursor(null);
    }
}
