package edu.vanderbilt.cs282.feisele.assignment6;

import java.util.HashMap;
import java.util.Map;

import android.content.UriMatcher;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * The schema for the downloaded resources.
 */
public enum DownloadContentProviderSchema {
	INSTANCE;

	/** the authority for the content provider */
	public static final String AUTHORITY = "edu.vanderbilt.cs282.feisele.lab6";
	/** the base uri (if more than one table is needed) */
	private static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);
	
	/** the uri matcher for selecting the appropriate table */
	public static final UriMatcher URI_MATCHER;
	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(AUTHORITY, ImageTable.PATH, ImageTable.PATH_TOKEN);
		URI_MATCHER.addURI(AUTHORITY, ImageTable.PATH_FOR_ID, ImageTable.PATH_FOR_ID_TOKEN);
	}

	/**
	 * The image table where the downloaded images are stored.
	 */
	public enum ImageTable {
		/** The unique primary key for the table */
		ID("INTEGER", BaseColumns._ID, "PRIMARY KEY AUTOINCREMENT"),
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

		private ImageTable(final String type, final String name, final String props) {
			this.type = type;
			this.title = name;
			this.props = props;
		}

		public static Map<String, ImageTable> byName = new HashMap<String, ImageTable>();
		static {
			for (ImageTable item : ImageTable.values()) {
				byName.put(item.title, item);
			}
		}
		
		public static final String NAME = "image";
		public static final String PATH = "images";
		public static final int PATH_TOKEN = 100;
		public static final String PATH_FOR_ID = "images/*";
		public static final int PATH_FOR_ID_TOKEN = 200;
		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.downloadimage.app";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.downloadimage.app";
	}

}
