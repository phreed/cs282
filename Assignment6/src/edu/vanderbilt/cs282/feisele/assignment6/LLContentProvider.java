package edu.vanderbilt.cs282.feisele.assignment6;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

/**
 * An abstract activity which logs the life-cycle call backs. A decorator
 * pattern implemented via inheritance.
 * 
 * @author "Fred Eisele" <phreed@gmail.com>
 */
public abstract class LLContentProvider extends ContentProvider {
	static private final Logger logger = LoggerFactory.getLogger("class.provider.lifecycle");

	/**
	 * Display a notification about us starting. We put an icon in the status
	 * bar.
	 * @return 
	 */
	@Override
	public boolean onCreate() {
		logger.debug("onCreate: provider created");
		return true;
	}
	
	@Override
	public String getType(Uri uri) {
		logger.debug("getType: for {} ", uri);
		return "";
	}
		
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		logger.debug("insert: for {} {}", uri, values);
		return null;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		logger.debug("insert: for {} {} {} {} {}", uri, projection, selection, selectionArgs, sortOrder);
		return null;
	}
	
	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode) {
		logger.debug("openFile: for {} {}", uri, mode);
		return null;
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		logger.debug("update: for {} {} {} {}", uri, values, selection, selectionArgs);
		return 0;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		logger.debug("delete: for {} {} {}", uri, selection, selectionArgs);
		return 0;
	}


}
