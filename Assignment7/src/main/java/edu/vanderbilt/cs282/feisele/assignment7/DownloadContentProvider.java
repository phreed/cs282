package edu.vanderbilt.cs282.feisele.assignment7;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import edu.vanderbilt.cs282.feisele.assignment7.DownloadContentProviderSchema.ImageTable;

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

	private DownloadDatabaseHelper db = null;
	private File imageDirectory = null;

	@Override
	public boolean onCreate() {
		super.onCreate();
		this.db = DownloadDatabaseHelper.newInstance(this.getContext());

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
	 * Therefore the image is placed into a temporary file (see openFile) rather
	 * than in the database.
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		logger.debug("insert into=<{}> values=<{}>", uri, values);

		final SQLiteDatabase db = this.db.getWritableDatabase();
		int token = DownloadContentProviderSchema.URI_MATCHER.match(uri);
		switch (token) {
		case ImageTable.PATH_TOKEN: {
			long id = db.insert(ImageTable.NAME, null, values);
			this.getContext().getContentResolver().notifyChange(uri, null);
			return ContentUris.withAppendedId(ImageTable.CONTENT_URI, id);
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
			logger.info("image file mode=<{}> path=<{}>, uri=<{}>",
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

	/**
	 * Used to obtain the meta data about the images.
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		logger.debug("reading from=<{}> where=<{}> args=<{}> columns=<{}>",
				uri, selection, selectionArgs, projection);
		final SQLiteDatabase db = this.db.getReadableDatabase();
		final int match = DownloadContentProviderSchema.URI_MATCHER.match(uri);
		switch (match) {
		case ImageTable.PATH_TOKEN: {
			/**
			 * retrieve the image meta-data
			 */
			final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
			builder.setTables(ImageTable.NAME);
			return builder.query(db, projection, selection, selectionArgs,
					null, null, sortOrder);
		}
		default:
			return null;
		}
	}

	/**
	 * Not implemented.
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		logger.debug("updating from=<{}> where=<{}> args=<{}> values=<{}>",
				uri, selection, selectionArgs, values);
		throw new UnsupportedOperationException("update uri: " + uri
				+ " not supported.");
	}

	/**
	 * Used to delete the meta-data. The images are left untouched.
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		logger.debug("deleting from=<{}> where=<{}> args=<{}>", uri, selection,
				selectionArgs);
		final SQLiteDatabase db = this.db.getWritableDatabase();
		final int match = DownloadContentProviderSchema.URI_MATCHER.match(uri);
		switch (match) {
		case ImageTable.PATH_TOKEN:
			logger.trace("table=<{}>", ImageTable.NAME);
			return db.delete(ImageTable.NAME, selection, selectionArgs);
		default:
			return super.delete(uri, selection, selectionArgs);
		}
	}
}
