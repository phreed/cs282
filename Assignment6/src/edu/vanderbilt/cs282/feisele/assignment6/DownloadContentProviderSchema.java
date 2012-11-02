package edu.vanderbilt.cs282.feisele.assignment6;

import java.util.HashMap;
import java.util.Map;

import android.provider.BaseColumns;

public enum DownloadContentProviderSchema {
	
	ID("INTEGER", BaseColumns._ID, " AUTOINCREMENT"),
	URI("TEXT", "uri", null),
	TIMESTAMP("LONG", "timestamp", null);
	
	final public String type;
	final public String name;
	final public String props;
	
	private DownloadContentProviderSchema(final String type, final String name, final String props) {
		this.type = type;
		this.name = name;
		this.props = props;
	}
	
	public static Map<String, DownloadContentProviderSchema> byName = new HashMap<String, DownloadContentProviderSchema>();
	static {
		for (DownloadContentProviderSchema item : DownloadContentProviderSchema.values()) {
			byName.put(item.name, item);
		}
	}

}
