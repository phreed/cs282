package course.examples.ContentProviders.myContentProvider;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import android.util.Log;

public class MyContentProvider extends ContentProvider 
{
    public static final Uri CONTENT_URI = Uri
        .parse("content://course.examples.contentproviders.mycontentprovider/");
    public static final String _ID = "_id";
    public static final String Data = "data"; 
	
    private static final String[] columns = new String[] { _ID, Data };
    private static final Map<Integer, DataRecord> db = new HashMap<Integer, DataRecord>();
    private static final String contentTypeSingle = "vnd.android.cursor.item/mycontentprovider.data.text";
    private static final String contentTypeMultiple = "vnd.android.cursor.dir/mycontentprovider.data.text";
	
    @Override
    public synchronized int delete(Uri uri, String selection, String[] selectionArgs) {
            int recsRemoved=0; 
            String requestIdString = uri.getLastPathSegment();
            if (null == requestIdString) {
                for (DataRecord dr : db.values()) {
                    db.remove(dr.get_id());
                    recsRemoved++;
                }
            } else {
                Integer requestId = Integer.parseInt(requestIdString);
                if (db.containsKey(requestId)) {
                    db.remove(requestId);
                    recsRemoved++;
                }
            }
            return recsRemoved;
	}

    @Override
    public synchronized String getType(Uri uri) {
            String contentType; 
            if (null == uri.getLastPathSegment()) {
                contentType = contentTypeMultiple;
            } else {
                contentType = contentTypeSingle;
            }
            return contentType;
	}

    @Override
    public synchronized Uri insert(Uri uri, ContentValues values) {
            if (values.containsKey(Data)) {
                DataRecord tmp = new DataRecord(values.getAsString(Data));
                db.put(tmp.get_id(), tmp);
                return Uri.parse(CONTENT_URI + String.valueOf(tmp.get_id()));
            }
            return null;
	}

    @Override
    public synchronized Cursor query(Uri uri, String[] projection, String selection,
                                         String[] selectionArgs, String sortOrder) {
            String requestIdString = uri.getLastPathSegment();
            MatrixCursor cursor = new MatrixCursor(columns);
            if (null == requestIdString) {
                for (DataRecord dr : db.values()) {
                    cursor.addRow(new Object[] { dr.get_id(), dr.get_data() });
                }
            } else {
                Integer requestId = Integer.parseInt(requestIdString);
                if (db.containsKey(requestId)) {
                    DataRecord dr = db.get(requestId);
                    cursor.addRow(new Object[] { dr.get_id(), dr.get_data() });
                }
            }
            return cursor;
	}

    @Override
    public synchronized int update(Uri uri, ContentValues values, String selection,
                                       String[] selectionArgs) {
            String requestIdString = uri.getLastPathSegment();
            if (null != requestIdString)
                {
                    if (values.containsKey(Data)) {
                        Integer requestId = Integer.parseInt(requestIdString);
                        DataRecord tmp = new DataRecord(requestId, values.getAsString(Data));
                        db.put(tmp.get_id(), tmp);
                        return 1;
                    }
                }
            return 0;
	}

    @Override
    public boolean onCreate() {
        return false;
    }
}
