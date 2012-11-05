package edu.vanderbilt.cs282.feisele.assignment6;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import edu.vanderbilt.cs282.feisele.assignment6.DownloadContentProviderSchema.ImageTable;
import edu.vanderbilt.cs282.feisele.assignment6.DownloadService.LocalBinder;

public class DownloadContentProvider extends LLContentProvider {
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
	private File imageDirectory = null;

	@Override
	public boolean onCreate() {
		super.onCreate();
		/*
		 * final Intent serviceIntent = new Intent(this.getContext(),
		 * DownloadService.class); this.getContext().bindService(serviceIntent,
		 * this.serviceConn, Context.BIND_AUTO_CREATE);
		 */
		this.db = DownloadDatabase.newInstance(this.getContext());

		this.imageDirectory = this.getContext().getCacheDir();
		return true;
	}

	@Override
	public String getType(Uri uri) {
		super.getType(uri);

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
		super.insert(uri, values);

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

	/**
	 * This method is used to receive images. It is possible to save the images
	 * in the sqlite database but this content provider places them in files.
	 * The problem with this approach is that it requires more effort to delete
	 * the files corresponding to the tuple.
	 */
	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode) {
		super.openFile(uri, mode);
		int imode = 0;
		try {
			if (mode.contains("w")) {
				imode |= ParcelFileDescriptor.MODE_WRITE_ONLY;
			}
			if (mode.contains("r")) {
				imode |= ParcelFileDescriptor.MODE_READ_ONLY;
			}
			if (mode.contains("+")) {
				imode |= ParcelFileDescriptor.MODE_APPEND;
			}
		} finally {
		}

		int token = DownloadContentProviderSchema.URI_MATCHER.match(uri);
		switch (token) {
		case ImageTable.PATH_FOR_ID_TOKEN: {
			final List<String> segments = uri.getPathSegments();
			final File imageFile = new File(this.imageDirectory,
					segments.get(1));
			logger.info("image file mode={} path={}, uri={}",
					Integer.toHexString(imode), imageFile, uri);
			try {
				if (!imageFile.exists()) {
					imageFile.createNewFile();
				}
				return ParcelFileDescriptor.open(imageFile, imode);
			} catch (FileNotFoundException ex) {
				logger.error("could not open file {}", imageFile, ex);
			} catch (IOException ex) {
				logger.error("could not create file {}", imageFile, ex);
			}
		}
			break;
		default: {
			throw new UnsupportedOperationException("URI: " + uri
					+ " not supported.");
		}
		}

		logger.error("could not open file=<{}>", uri);
		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		super.query(uri, projection, selection, selectionArgs, sortOrder);

		final SQLiteDatabase db = this.db.getReadableDatabase();
		final int match = DownloadContentProviderSchema.URI_MATCHER.match(uri);
		switch (match) {
		case ImageTable.PATH_TOKEN: {
			/**
			 * retrieve the image meta-data
			 */
			final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
			builder.setTables(ImageTable.NAME);
			return builder.query(db, null, null, null, null, null,
					ImageTable.ID.title + " DESC");
		}
		default:
			return null;
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return super.update(uri, values, selection, selectionArgs);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return super.delete(uri, selection, selectionArgs);
	}
}
