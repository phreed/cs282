package edu.vanderbilt.cs282.feisele.assignment6.provider;

import edu.vanderbilt.cs282.feisele.assignment6.provider.DownloadContentProviderSchema.ImageTable;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * This object is used to manage the lifecycle of the download database.
 * 
 * @author "Fred Eisele" <phreed@gmail.com>
 */
public class DownloadDatabaseHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "downloaded.db";
	private static final int DATABASE_VERSION = 3;

	private DownloadDatabaseHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	public static DownloadDatabaseHelper newInstance(Context context) {
		return new DownloadDatabaseHelper(context, DATABASE_NAME, null,
				DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DownloadDatabaseHelper.sqlCreateTable());
	}

	/**
	 * This method is provided to support the mock provider.
	 * 
	 * @return sql create table statement
	 */
	static public String sqlCreateTable() {
		final StringBuilder builder = new StringBuilder("CREATE TABLE")
				.append(' ').append('"').append(ImageTable.NAME).append('"')
				.append(' ').append('(');
		for (ImageTable field : ImageTable.values()) {
			builder.append("'").append(field.title).append("'").append(' ')
					.append(field.type).append(' ').append(field.props)
					.append(',').append(' ');
		}
		builder.append("UNIQUE (\"").append(ImageTable.ID.title)
				.append("\") ON CONFLICT REPLACE)");

		return builder.toString();
	}

	/**
	 * Upgrades are a simple drop and recreate. No effort is made to reclaim
	 * data from the previous build.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion >= newVersion)
			return;

		db.execSQL(DownloadDatabaseHelper.sqlDropTable());
		this.onCreate(db);
	}

	/**
	 * This method is provided to support the mock provider.
	 * 
	 * @return sql create table statement
	 */
	static public String sqlDropTable() {
		return new StringBuilder("DROP TABLE IF EXISTS").append(' ')
				.append('"').append(ImageTable.NAME).append('"').toString();
	}

}
