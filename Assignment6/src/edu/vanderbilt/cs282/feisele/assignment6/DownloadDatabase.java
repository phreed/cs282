package edu.vanderbilt.cs282.feisele.assignment6;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import edu.vanderbilt.cs282.feisele.assignment6.DownloadContentProviderSchema.ImageTable;

public class DownloadDatabase extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "downloaded.db";
	private static final int DATABASE_VERSION = 2;

	private DownloadDatabase(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	public static DownloadDatabase newInstance(Context context) {
		return new DownloadDatabase(context, DATABASE_NAME, null,
				DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
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
		db.execSQL(builder.toString());
	}

	/**
	 * Upgrades are a simple drop and recreate. No effort is made to reclaim
	 * data from the previous build.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion >= newVersion)
			return;

		final StringBuilder builder = new StringBuilder("DROP TABLE IF EXISTS")
				.append(' ').append('"').append(ImageTable.NAME).append('"');
		db.execSQL(builder.toString());
		this.onCreate(db);
	}

}
