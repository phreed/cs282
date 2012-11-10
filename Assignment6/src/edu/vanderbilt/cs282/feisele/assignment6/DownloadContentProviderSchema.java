package edu.vanderbilt.cs282.feisele.assignment6;

import java.util.HashMap;
import java.util.Map;

import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * The schema for the downloaded resources.
 */
public enum DownloadContentProviderSchema {
	INSTANCE;

	/** the authority for the content provider */
	public static final String AUTHORITY = "edu.vanderbilt.cs282.feisele.assignment6.provider";
	/** the base uri (if more than one table is needed) */
	private static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

	/** the uri matcher for selecting the appropriate table */
	public static final UriMatcher URI_MATCHER;
	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(AUTHORITY, ImageTable.PATH, ImageTable.PATH_TOKEN);
		URI_MATCHER.addURI(AUTHORITY, ImageTable.PATH_FOR_ID,
				ImageTable.PATH_FOR_ID_TOKEN);
	}

	/**
	 * The image table where the downloaded images are stored.
	 */
	public enum ImageTable {
		/** The unique primary key for the table */
		ID("INTEGER", BaseColumns._ID, "PRIMARY KEY AUTOINCREMENT"),
		ORDINAL("INTEGER", "ordinal", null),
		/**
		 * This is the uri of the image in the table. There may be more than one
		 * image with the same type. This is the source of the image.
		 */
		URI("TEXT", "uri", null),
		/** The time (since the epoch, when the image was retrieved */
		TIMESTAMP("LONG", "timestamp", null);

		/** The data type of the image, */
		final public String type;
		/** The column name for the field */
		final public String title;
		/** Other properties of the field */
		final public String props;

		private ImageTable(final String type, final String name,
				final String props) {
			this.type = type;
			this.title = name;
			this.props = props;
		}

		public static Map<String, ImageTable> byName = new HashMap<String, ImageTable>();
		/**
		 * The default cursor is used when the default images are to be
		 * displayed. A null uri indicates the default cursor. The id field is
		 * used to select from the default bitmap images.
		 */
		public static Cursor DEFAULT_CURSOR;
		static {
			final ImageTable[] values = ImageTable.values();
			for (ImageTable item : values) {
				byName.put(item.title, item);
			}
			final MatrixCursor matrixCursor = new MatrixCursor(new String[] {
					ImageTable.ID.title, ImageTable.ORDINAL.title,
					ImageTable.URI.title,
					ImageTable.TIMESTAMP.title });
			matrixCursor.addRow(new Object[] { 1, 1, null, 0 });
			matrixCursor.addRow(new Object[] { 2, 2, null, 0 });
			DEFAULT_CURSOR = matrixCursor;
		}

		public static final String NAME = "image";
		public static final String PATH = "images";
		public static final int PATH_TOKEN = 100;
		public static final String PATH_FOR_ID = "images/#";
		public static final int PATH_FOR_ID_TOKEN = 200;
		public static final Uri CONTENT_URI = BASE_URI.buildUpon()
				.appendPath(PATH).build();
		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.downloadimage.app";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.downloadimage.app";

		static {

		}

	}

	/**
	 * Methods to help in filling out the selection clause.
	 */
	public enum Selection {
		/** select tuples matching the uri */
		BY_URI(ImageTable.URI.title + "=?"),
		/** select tuples matching the id */
		BY_ID(ImageTable.ID.title + "=?"),
		/** select all tuples */
		ALL(null);

		final String code;

		private Selection(String code) {
			this.code = code;
		}
	}

	/**
	 * Methods to help in filling out the orderBy clause.
	 */
	public enum Order {
		BY_URI(ImageTable.URI.title), BY_ID(ImageTable.ID.title);

		private final String code;

		public String ascending() {
			return this.code + " ASC ";
		}

		public String decending() {
			return this.code + " DESC ";
		}

		private Order(String code) {
			this.code = code;
		}
	}

}
