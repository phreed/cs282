package edu.vanderbilt.cs282.feisele.provider;

import android.net.Uri;
import android.provider.BaseColumns;
import android.content.ContentResolver;
import android.database.Cursor;

public abstract class AmmoMockSchemaBase {
  public static final String AUTHORITY = "edu.vu.isis.provider.aqsprovider";

  public static final String DATABASE_NAME = "aqs.db";

  /**
 see AmmoProviderSchema for details
   */
  public enum Disposition {
    REMOTE(0), LOCAL(1);

    private final int code;

    private Disposition(int code) {
      this.code = code;
    }

    public int toCode() {
      return this.code;
    }

    public static Disposition fromCode(final int code) {
      switch (code) {
        case 0: return REMOTE;
        case 1: return LOCAL;
      }
      return LOCAL;
    }

    @Override
    public String toString() {
      return this.name();
    }

    public static Disposition fromString(final String value) {
      try {
        return (value == null) ? Disposition.LOCAL 
            : (value.startsWith( "REMOTE" )) ? Disposition.REMOTE
                : Disposition.LOCAL;
      } catch (Exception ex) {
        return Disposition.LOCAL;
      }
    }
  }

  protected AmmoMockSchemaBase() {}

  public static final int FIELD_TYPE_NULL = 0;
  public static final int FIELD_TYPE_BOOL = 1;
  public static final int FIELD_TYPE_BLOB = 2;
  public static final int FIELD_TYPE_FLOAT = 3;
  public static final int FIELD_TYPE_INTEGER = 4;
  public static final int FIELD_TYPE_LONG = 5;
  public static final int FIELD_TYPE_TEXT = 6;
  public static final int FIELD_TYPE_REAL = 7;
  public static final int FIELD_TYPE_FK = 8;
  public static final int FIELD_TYPE_GUID = 9;
  public static final int FIELD_TYPE_EXCLUSIVE = 10;
  public static final int FIELD_TYPE_INCLUSIVE = 11;
  public static final int FIELD_TYPE_TIMESTAMP = 12;
  public static final int FIELD_TYPE_SHORT = 13;

  // BEGIN CUSTOM Aqs CONSTANTS
  // END   CUSTOM Aqs CONSTANTS

  public static class Meta {
    final public String name;
    final public int type;
    public Meta(String name, int type) { this.name = name; this.type = type; }
  }

  public static final Meta[] AMMO_CURSOR_COLUMNS = new Meta[] {
    new Meta(AmmoTableSchemaBase.A_FOREIGN_KEY_REF, FIELD_TYPE_FK)  ,
    new Meta(AmmoTableSchemaBase.AN_EXCLUSIVE_ENUMERATION, FIELD_TYPE_EXCLUSIVE)  ,
    new Meta(AmmoTableSchemaBase.AN_INCLUSIVE_ENUMERATION, FIELD_TYPE_INCLUSIVE)  
  };

  public static final String[] AMMO_KEY_COLUMNS = new String[] {
  };

  public static class AmmoTableSchemaBase implements BaseColumns {
    protected AmmoTableSchemaBase() {} // No instantiation.

    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI =
        Uri.parse("content://"+AUTHORITY+"/ammo");

    public static Uri getUri(Cursor cursor) {
      final Integer id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
      return  Uri.withAppendedPath(AmmoTableSchemaBase.CONTENT_URI, id.toString());
    }

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory
     */
    public static final String CONTENT_TYPE =
        ContentResolver.CURSOR_DIR_BASE_TYPE+"/vnd.edu.vu.isis.ammo";

    /**
     * A mime type used for publisher subscriber.
     */
    public static final String CONTENT_TOPIC =
        "ammo/edu.vu.isis.ammo";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single ammo entry.
     */
    public static final String CONTENT_ITEM_TYPE = 
        ContentResolver.CURSOR_ITEM_BASE_TYPE+"/vnd.edu.vu.isis.ammo";


    public static final String DEFAULT_SORT_ORDER = ""; //"modified_date DESC";

    // ========= Field Name and Type Constants ================

    /** 
     * Description: 
     * <P>Type: FK</P> 
     */
    public static final String A_FOREIGN_KEY_REF = "a_foreign_key_ref";

    /** 
     * Description: an exclusive enumeration list signifies that 
           only one value is allowed
     * <P>Type: EXCLUSIVE</P> 
     */
    public static final int AN_EXCLUSIVE_ENUMERATION_HIGH = 1;
    public static final int AN_EXCLUSIVE_ENUMERATION_MEDIUM = 2;
    public static final int AN_EXCLUSIVE_ENUMERATION_LOW = 3;

    public static final String AN_EXCLUSIVE_ENUMERATION = "an_exclusive_enumeration";

    /** 
     * Description: an inclusive enumeration list signifies that 
           any number of the values is allowed in a list.
     * <P>Type: INCLUSIVE</P> 
     */
    public static final int AN_INCLUSIVE_ENUMERATION_APPLE = 1;
    public static final int AN_INCLUSIVE_ENUMERATION_ORANGE = 2;
    public static final int AN_INCLUSIVE_ENUMERATION_PEAR = 3;

    public static final String AN_INCLUSIVE_ENUMERATION = "an_inclusive_enumeration";


    public static final String _DISPOSITION = "_disp"; 

    public static final String _RECEIVED_DATE = "_received_date";

    // BEGIN CUSTOM AMMO_SCHEMA PROPERTIES
    // END   CUSTOM AMMO_SCHEMA PROPERTIES
  } 
  public static final Meta[] QUICK_CURSOR_COLUMNS = new Meta[] {
    new Meta(QuickTableSchemaBase.A_SHORT_INTEGER, FIELD_TYPE_SHORT)  ,
    new Meta(QuickTableSchemaBase.AN_INTEGER, FIELD_TYPE_INTEGER)  ,
    new Meta(QuickTableSchemaBase.A_BOOLEAN, FIELD_TYPE_BOOL)  ,
    new Meta(QuickTableSchemaBase.A_LONG_INTEGER, FIELD_TYPE_LONG)  ,
    new Meta(QuickTableSchemaBase.A_ABSOLUTE_TIME, FIELD_TYPE_TIMESTAMP)  
  };

  public static final String[] QUICK_KEY_COLUMNS = new String[] {
  };

  public static class QuickTableSchemaBase implements BaseColumns {
    protected QuickTableSchemaBase() {} // No instantiation.

    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI =
        Uri.parse("content://"+AUTHORITY+"/quick");

    public static Uri getUri(Cursor cursor) {
      final Integer id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
      return  Uri.withAppendedPath(QuickTableSchemaBase.CONTENT_URI, id.toString());
    }

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory
     */
    public static final String CONTENT_TYPE =
        ContentResolver.CURSOR_DIR_BASE_TYPE+"/vnd.edu.vu.isis.quick";

    /**
     * A mime type used for publisher subscriber.
     */
    public static final String CONTENT_TOPIC =
        "ammo/edu.vu.isis.quick";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single quick entry.
     */
    public static final String CONTENT_ITEM_TYPE = 
        ContentResolver.CURSOR_ITEM_BASE_TYPE+"/vnd.edu.vu.isis.quick";


    public static final String DEFAULT_SORT_ORDER = ""; //"modified_date DESC";

    // ========= Field Name and Type Constants ================

    /** 
     * Description: 
     * <P>Type: SHORT</P> 
     */
    public static final String A_SHORT_INTEGER = "a_short_integer";

    /** 
     * Description: 
     * <P>Type: INTEGER</P> 
     */
    public static final String AN_INTEGER = "an_integer";

    /** 
     * Description: 
     * <P>Type: BOOL</P> 
     */
    public static final String A_BOOLEAN = "a_boolean";

    /** 
     * Description: 
     * <P>Type: LONG</P> 
     */
    public static final String A_LONG_INTEGER = "a_long_integer";

    /** 
     * Description: 
     * <P>Type: TIMESTAMP</P> 
     */
    public static final String A_ABSOLUTE_TIME = "a_absolute_time";


    public static final String _DISPOSITION = "_disp"; 

    public static final String _RECEIVED_DATE = "_received_date";

    // BEGIN CUSTOM QUICK_SCHEMA PROPERTIES
    // END   CUSTOM QUICK_SCHEMA PROPERTIES
  } 
  public static final Meta[] START_CURSOR_COLUMNS = new Meta[] {
    new Meta(StartTableSchemaBase.A_REAL, FIELD_TYPE_REAL)  ,
    new Meta(StartTableSchemaBase.A_GLOBALLY_UNIQUE_IDENTIFIER, FIELD_TYPE_GUID)  ,
    new Meta(StartTableSchemaBase.SOME_ARBITRARY_TEXT, FIELD_TYPE_TEXT)  ,
    new Meta(StartTableSchemaBase.A_BLOB, FIELD_TYPE_BLOB)  ,
    new Meta(StartTableSchemaBase.A_FILE_TYPE, FIELD_TYPE_TEXT), 
    new Meta(StartTableSchemaBase.A_FILE, FIELD_TYPE_BLOB)  
  };

  public static final String[] START_KEY_COLUMNS = new String[] {
    StartTableSchemaBase.A_GLOBALLY_UNIQUE_IDENTIFIER 
  };

  public static class StartTableSchemaBase implements BaseColumns {
    protected StartTableSchemaBase() {} // No instantiation.

    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI =
        Uri.parse("content://"+AUTHORITY+"/start");

    public static Uri getUri(Cursor cursor) {
      final Integer id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
      return  Uri.withAppendedPath(StartTableSchemaBase.CONTENT_URI, id.toString());
    }

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory
     */
    public static final String CONTENT_TYPE =
        ContentResolver.CURSOR_DIR_BASE_TYPE+"/vnd.edu.vu.isis.start";

    /**
     * A mime type used for publisher subscriber.
     */
    public static final String CONTENT_TOPIC =
        "ammo/edu.vu.isis.start";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single start entry.
     */
    public static final String CONTENT_ITEM_TYPE = 
        ContentResolver.CURSOR_ITEM_BASE_TYPE+"/vnd.edu.vu.isis.start";


    public static final String DEFAULT_SORT_ORDER = ""; //"modified_date DESC";

    // ========= Field Name and Type Constants ================

    /** 
     * Description: 
     * <P>Type: REAL</P> 
     */
    public static final String A_REAL = "a_real";

    /** 
     * Description: 
     * <P>Type: GUID</P> 
     */
    public static final String A_GLOBALLY_UNIQUE_IDENTIFIER = "a_globally_unique_identifier";

    /** 
     * Description: 
     * <P>Type: TEXT</P> 
     */
    public static final String SOME_ARBITRARY_TEXT = "some_arbitrary_text";

    /** 
     * Description: 
     * <P>Type: BLOB</P> 
     */
    public static final String A_BLOB = "a_blob";

    /** 
     * Description: 
     * <P>Type: FILE</P> 
     */
    public static final String A_FILE_TYPE = "a_file_type";
    public static final String A_FILE = "a_file";


    public static final String _DISPOSITION = "_disp"; 

    public static final String _RECEIVED_DATE = "_received_date";

    // BEGIN CUSTOM START_SCHEMA PROPERTIES
    // END   CUSTOM START_SCHEMA PROPERTIES
  } 


}
