package edu.vanderbilt.cs282.feisele.assignment6;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import edu.vanderbilt.cs282.feisele.assignment6.DownloadContentProviderSchema.ImageTable;
import edu.vanderbilt.cs282.feisele.assignment6.DownloadService.LocalBinder;

public class DownloadContentProvider extends ContentProvider {
	static private final Logger logger = LoggerFactory
			.getLogger("class.provider.download");

	private DownloadService service;
	private boolean isBound = false;

	/**
	 * If the content provider wishes to communicate with the service directly
	 * it can use the local service connection.
	 * <p>
	 * I am not sure which will perform better.
	 */
	private ServiceConnection serviceConn = new ServiceConnection() {
		final private DownloadContentProvider master = DownloadContentProvider.this;

		public void onServiceConnected(ComponentName name, IBinder service) {
			try {
				final LocalBinder binder = (LocalBinder) service;
				master.service = binder.getService();
				master.isBound = true;
			} catch (ClassCastException ex) {
				// Pass
			}
		}

		public void onServiceDisconnected(ComponentName name) {
			master.isBound = false;
		}
	};

	private DownloadDatabase db = null;
	private File cacheDir = null;

	@Override
	public boolean onCreate() {
		final Intent serviceIntent = new Intent(this.getContext(),
				DownloadService.class);
		this.getContext().bindService(serviceIntent, this.serviceConn,
				Context.BIND_AUTO_CREATE);
		this.db = DownloadDatabase.newInstance(this.getContext());

		this.cacheDir = this.getContext().getCacheDir();
		return true;
	}

	@Override
	public String getType(Uri uri) {
		final int match = DownloadContentProviderSchema.URI_MATCHER.match(uri);
		switch (match) {
		case ImageTable.PATH_TOKEN:
			return ImageTable.CONTENT_TYPE_DIR;
		case ImageTable.PATH_FOR_ID_TOKEN:
			return ImageTable.CONTENT_ITEM_TYPE;
		default:
			throw new UnsupportedOperationException("URI " + uri
					+ " is not supported.");
		}
	}

	/**
	 * The downloaded image file is large and may not fit in the cursor values.
	 * Therefore the image is placed into a temporary file (see openFile)
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		final SQLiteDatabase db = this.db.getWritableDatabase();
		int token = DownloadContentProviderSchema.URI_MATCHER.match(uri);
		switch (token) {
		case ImageTable.PATH_TOKEN: {
			long id = db.insert(ImageTable.NAME, null, values);
			getContext().getContentResolver().notifyChange(uri, null);
			return ImageTable.CONTENT_URI.buildUpon()
					.appendPath(String.valueOf(id)).build();
		}
		default: {
			throw new UnsupportedOperationException("URI: " + uri
					+ " not supported.");
		}
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		final SQLiteDatabase db = this.db.getReadableDatabase();
		final int match = DownloadContentProviderSchema.URI_MATCHER.match(uri);
		switch (match) {
		case ImageTable.PATH_TOKEN: {
			/**
			 * retrieve the image meta-data
			 */
			final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
			builder.setTables(ImageTable.NAME);
			return builder.query(db, null, null, null, null, null, null);
		}
		default:
			return null;
		}
	}

	/**
	 * This method is used to receive images. It is possible to save the images
	 * in the sqlite database but this content provider places them in files.
	 * The problem with this approach is that it requires more effort to delete
	 * the files corresponding to the tuple.
	 */
	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode) {
		File tempFile = null;
		try {
			
			tempFile = File.createTempFile("download", "tmp", this.cacheDir);

			int imode = 0;
			if (mode.contains("w")) {
				imode |= ParcelFileDescriptor.MODE_WRITE_ONLY;

			}
			if (mode.contains("r")) {
				imode |= ParcelFileDescriptor.MODE_READ_ONLY;
			}
			if (mode.contains("+")) {
				imode |= ParcelFileDescriptor.MODE_APPEND;
			}
			return ParcelFileDescriptor.open(tempFile, imode);

		} catch (IOException ex) {
			logger.error("could not write bitmap file {}", tempFile);
		}
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}
}
