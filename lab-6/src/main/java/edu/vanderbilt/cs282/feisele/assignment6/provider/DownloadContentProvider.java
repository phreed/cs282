package edu.vanderbilt.cs282.feisele.assignment6.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import edu.vanderbilt.cs282.feisele.assignment6.lifecycle.LLContentProvider;

/**
 * This content provider holds the images downloaded by the download service. It
 * does not store its images in the database itself, rather it stores meta-data
 * about the images in the database and the images are saved as files. These
 * files are accessed via the openFile() method. This is to accommodate the size
 * limitation on the cursor fields.
 * 
 * @author "Fred Eisele" <phreed@gmail.com>
 * 
 */
public class DownloadContentProvider extends LLContentProvider {
	static private final Logger logger = LoggerFactory
			.getLogger("class.provider.download");

	private DownloadContentProviderHelper impl = null;

	@Override
	public boolean onCreate() {
		super.onCreate();
		this.impl = DownloadContentProviderHelper
				.getInstance(this.getContext(), logger);
		return this.impl.onCreate();
	}

	@Override
	public String getType(Uri uri) {
		super.getType(uri);
		return this.impl.getType(uri);
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return this.impl.insert(uri, values);
	}

	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode) {
		return this.impl.openFile(uri, mode);
	}

	/**
	 * Used to obtain the meta data about the images.
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		return this.impl.query(uri, projection, selection, selectionArgs,
				sortOrder);
	}

	/**
	 * Not implemented.
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return this.impl.update(uri, values, selection, selectionArgs);
	}

	/**
	 * Used to delete the meta-data. The images are left untouched.
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		try {
			return this.impl.delete(uri, selection, selectionArgs);
		} catch (DownloadContentProviderException ex) {
			return super.delete(uri, selection, selectionArgs);
		}
	}
}
