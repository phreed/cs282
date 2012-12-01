package edu.vanderbilt.cs282.feisele.provider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import android.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.test.mock.MockContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.BaseColumns;
import android.text.TextUtils;

import edu.vu.isis.ammo.provider.AmmoMockSchemaBase.Meta;
import edu.vu.isis.ammo.provider.AmmoMockSchemaBase.AmmoTableSchemaBase;
import edu.vu.isis.ammo.provider.AmmoMockSchemaBase.QuickTableSchemaBase;
import edu.vu.isis.ammo.provider.AmmoMockSchemaBase.StartTableSchemaBase;



// BEGIN CUSTOM AmmoMock IMPORTS
// END   CUSTOM  AmmoMock IMPORTS

public abstract class AmmoMockProviderBase extends MockContentProvider {
    public final static Logger clogger = LoggerFactory.getLogger("trial.mock.provider.base");

    // Table definitions 
    public interface Tables {
        public static final String AMMO_TBL = "ammo";
        public static final String QUICK_TBL = "quick";
        public static final String START_TBL = "start";

    }


    private static final String AMMO_KEY_CLAUSE;
    static {
        AMMO_KEY_CLAUSE = new StringBuilder()
        .toString();
    }; 

    private static final String QUICK_KEY_CLAUSE;
    static {
        QUICK_KEY_CLAUSE = new StringBuilder()
        .toString();
    }; 

    private static final String START_KEY_CLAUSE;
    static {
        START_KEY_CLAUSE = new StringBuilder()
        .append(StartTableSchemaBase.A_GLOBALLY_UNIQUE_IDENTIFIER).append("=?") 
        .toString();
    }; 


    protected AmmoMockProviderBase( Context context ) {
        super(context);
    }

    // Views.
    public interface Views {
        // Nothing to put here yet.
    }

    public final static Logger hlogger = LoggerFactory.getLogger("trial.mock.provider.helper");
    protected class AmmoMockDatabaseHelper extends SQLiteOpenHelper {
        // ===========================================================
        // Constants
        // ===========================================================

        // ===========================================================
        // Fields
        // ===========================================================

        /** Nothing to put here */


        // ===========================================================
        // Constructors
        // ===========================================================
        public AmmoMockDatabaseHelper(Context context, String name, int version) {
            super(context, name, null, version);
        }

        /**
         * Pass through to grand parent.
         */
        public AmmoMockDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }


        // ===========================================================
        // SQLiteOpenHelper Methods
        // ===========================================================

        @Override
        public synchronized void onCreate(SQLiteDatabase db) {
            hlogger.info( "Bootstrapping database");
            try {

                /** 
                 * Table Name: ammo <P>
                 */
                db.execSQL("CREATE TABLE \"" + Tables.AMMO_TBL + "\" (" 
                        + "\""+AmmoTableSchemaBase.A_FOREIGN_KEY_REF + "\" INTEGER, " 
                        + "\""+AmmoTableSchemaBase.AN_EXCLUSIVE_ENUMERATION + "\" INTEGER, " 
                        + "\""+AmmoTableSchemaBase.AN_INCLUSIVE_ENUMERATION + "\" INTEGER, " 
                        + "\""+AmmoTableSchemaBase._ID + "\" INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "\""+AmmoTableSchemaBase._RECEIVED_DATE + "\" LONG, "
                        + "\""+AmmoTableSchemaBase._DISPOSITION + "\" TEXT );" ); 
                /** 
                 * Table Name: quick <P>
                 */
                db.execSQL("CREATE TABLE \"" + Tables.QUICK_TBL + "\" (" 
                        + "\""+QuickTableSchemaBase.A_SHORT_INTEGER + "\" SMALLINT, " 
                        + "\""+QuickTableSchemaBase.AN_INTEGER + "\" INTEGER, " 
                        + "\""+QuickTableSchemaBase.A_BOOLEAN + "\" INTEGER, " 
                        + "\""+QuickTableSchemaBase.A_LONG_INTEGER + "\" INTEGER, " 
                        + "\""+QuickTableSchemaBase.A_ABSOLUTE_TIME + "\" INTEGER, " 
                        + "\""+QuickTableSchemaBase._ID + "\" INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "\""+QuickTableSchemaBase._RECEIVED_DATE + "\" LONG, "
                        + "\""+QuickTableSchemaBase._DISPOSITION + "\" TEXT );" ); 
                /** 
                 * Table Name: start <P>
                 */
                db.execSQL("CREATE TABLE \"" + Tables.START_TBL + "\" (" 
                        + "\""+StartTableSchemaBase.A_REAL + "\" REAL, " 
                        + "\""+StartTableSchemaBase.A_GLOBALLY_UNIQUE_IDENTIFIER + "\" TEXT, " 
                        + "\""+StartTableSchemaBase.SOME_ARBITRARY_TEXT + "\" TEXT, " 
                        + "\""+StartTableSchemaBase.A_BLOB + "\" BLOB, " 
                        + "\""+StartTableSchemaBase.A_FILE_TYPE + "\" TEXT, "
                        + "\""+StartTableSchemaBase.A_FILE + "\" TEXT, " 
                        + "\""+StartTableSchemaBase._ID + "\" INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "\""+StartTableSchemaBase._RECEIVED_DATE + "\" LONG, "
                        + "\""+StartTableSchemaBase._DISPOSITION + "\" TEXT );" ); 

                preloadTables(db);
                createViews(db);
                createTriggers(db);

            } catch (SQLiteException ex) {
                hlogger.error("problem creating database", ex);
            }
        }

        @Override
        public synchronized void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            hlogger.warn( "Upgrading database from version {} to {} which will destroy all old data",
                    oldVersion, newVersion);
            db.execSQL("DROP TABLE IF EXISTS \"" + Tables.AMMO_TBL + "\";");
            db.execSQL("DROP TABLE IF EXISTS \"" + Tables.QUICK_TBL + "\";");
            db.execSQL("DROP TABLE IF EXISTS \"" + Tables.START_TBL + "\";");

            onCreate(db);
        }

        // ===========================================================
        // Database Creation Helper Methods
        // ===========================================================

        /**
         * Can be overriden to cause tables to be loaded
         */
        protected void preloadTables(SQLiteDatabase db) { }

        /** View creation */
        protected void createViews(SQLiteDatabase db) { }

        /** Trigger creation */
        protected void createTriggers(SQLiteDatabase db) { }
    }

    // ===========================================================
    // Constants
    // ===========================================================
    public final static Logger logger = LoggerFactory.getLogger("AmmoMockProviderBase");

    // ===========================================================
    // Fields
    // ===========================================================
    /** Projection Maps */
    protected static String[] ammoProjectionKey;
    protected static HashMap<String, String> ammoProjectionMap;

    protected static String[] quickProjectionKey;
    protected static HashMap<String, String> quickProjectionMap;

    protected static String[] startProjectionKey;
    protected static HashMap<String, String> startProjectionMap;


    /** Uri Matcher tags */
    protected static final int AMMO_BLOB = 10;
    protected static final int AMMO_SET = 11;
    protected static final int AMMO_ID = 12;
    protected static final int AMMO_SERIAL = 13;
    protected static final int AMMO_DESERIAL = 14;
    protected static final int AMMO_META = 15;

    private static final MatrixCursor ammoFieldTypeCursor;

    protected static final int QUICK_BLOB = 20;
    protected static final int QUICK_SET = 21;
    protected static final int QUICK_ID = 22;
    protected static final int QUICK_SERIAL = 23;
    protected static final int QUICK_DESERIAL = 24;
    protected static final int QUICK_META = 25;

    private static final MatrixCursor quickFieldTypeCursor;

    protected static final int START_BLOB = 30;
    protected static final int START_SET = 31;
    protected static final int START_ID = 32;
    protected static final int START_SERIAL = 33;
    protected static final int START_DESERIAL = 34;
    protected static final int START_META = 35;

    private static final MatrixCursor startFieldTypeCursor;


    /** Uri matcher */
    protected static final UriMatcher uriMatcher;

    /** Database helper */
    protected AmmoMockDatabaseHelper openHelper;
    protected abstract boolean createDatabaseHelper();

    /**
     * In support of cr.openInputStream
     */
    private static final UriMatcher blobUriMatcher;
    static {
        blobUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        blobUriMatcher.addURI(AmmoMockSchemaBase.AUTHORITY, Tables.AMMO_TBL+"/#/*", AMMO_BLOB);

        blobUriMatcher.addURI(AmmoMockSchemaBase.AUTHORITY, Tables.QUICK_TBL+"/#/*", QUICK_BLOB);

        blobUriMatcher.addURI(AmmoMockSchemaBase.AUTHORITY, Tables.START_TBL+"/#/*", START_BLOB);

    }

    /**
     * Examines uri's from clients:
     *  long fkId = cursor.getLong(cursor.getColumnIndex(Table.FK));
     *    Drawable icon = null;
     *    Uri fkUri = ContentUris.withAppendedId(TableSchema.CONTENT_URI, fkId);
     *  // then the fkUri can be used to get a tuple using a query.
     *    Cursor categoryCursor = this.managedQuery(categoryUri, null, null, null, null);
     *  // ...or the fkUri can be used to get a file descriptor.
     *    Uri iconUri = Uri.withAppendedPath(categoryUri, CategoryTableSchema.ICON);
     *  InputStream is = this.getContentResolver().openInputStream(iconUri);
     *  Drawableicon = Drawable.createFromStream(is, null);
     *  
     *  It is expected that the uri passed in will be of the form <content_uri>/<table>/<id>/<column>
     *  This is simple enough that a UriMatcher is not needed and 
     *  a simple uri.getPathSegments will suffice to identify the file.
     */

    // ===========================================================
    // Content Provider Overrides
    // ===========================================================

    /**
     * This is used to get fields which are too large to store in the
     * database or would exceed the Binder data size limit of 1MiB.
     * The blob matcher expects a URI post-pended with 
     */

    @Override
    public synchronized ParcelFileDescriptor openFile (Uri uri, String mode) {
        int imode = 0;
        if (mode.contains("w")) imode |= ParcelFileDescriptor.MODE_WRITE_ONLY;
        if (mode.contains("r")) imode |= ParcelFileDescriptor.MODE_READ_ONLY;
        if (mode.contains("+")) imode |= ParcelFileDescriptor.MODE_APPEND;

        final List<String> pseg = uri.getPathSegments();
        final SQLiteDatabase db = this.openHelper.getReadableDatabase();

        final int match = blobUriMatcher.match(uri);
        switch (match) {

            case AMMO_BLOB:
                if (pseg.size() < 3)
                    return null;

                try {
                    final String tuple = pseg.get(1);
                    final String field = pseg.get(2);
                    Log.e("FOO", "open file error tuple=["+tuple+"] field=["+field+"]");
                    Log.e("BAR", "  pseg=["+pseg+"]");
                    final String selection = new StringBuilder()
                    .append(AmmoTableSchemaBase._ID).append("=?")
                    .toString();
                    final String[] selectArgs = new String[]{tuple}; 

                    final Cursor blobCursor = db
                            .query(Tables.AMMO_TBL, new String[]{field}, 
                                    selection, selectArgs, 
                                    null, null, null);
                    if (blobCursor.getCount() < 1) return null;
                    blobCursor.moveToFirst();

                    final File filePath = new File(blobCursor.getString(0));
                    blobCursor.close();

                    if (0 != (imode & ParcelFileDescriptor.MODE_WRITE_ONLY)) {
                        logger.trace("candidate blob file {}", filePath);
                        try {
                            final File newFile = receiveFile(Tables.AMMO_TBL, tuple, filePath);
                            logger.trace("new blob file {}", newFile);

                            final ContentValues cv = new ContentValues();
                            cv.put(field, newFile.getCanonicalPath());
                            db.update(Tables.AMMO_TBL, cv, selection, selectArgs);

                            return ParcelFileDescriptor.open(newFile, imode | ParcelFileDescriptor.MODE_CREATE);

                        } catch (FileNotFoundException ex) {
                            logger.error("could not open file {}\n {}", 
                                    ex.getLocalizedMessage(), ex.getStackTrace());
                            return null;
                        } catch (IOException ex) {
                            return null;
                        }
                    }
                    return ParcelFileDescriptor.open(filePath, imode);

                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                } 
                break;


            case QUICK_BLOB:
                if (pseg.size() < 3)
                    return null;

                try {
                    final String tuple = pseg.get(1);
                    final String field = pseg.get(2);
                    Log.e("FOO", "open file error tuple=["+tuple+"] field=["+field+"]");
                    Log.e("BAR", "  pseg=["+pseg+"]");
                    final String selection = new StringBuilder()
                    .append(QuickTableSchemaBase._ID).append("=?")
                    .toString();
                    final String[] selectArgs = new String[]{tuple}; 

                    final Cursor blobCursor = db
                            .query(Tables.QUICK_TBL, new String[]{field}, 
                                    selection, selectArgs, 
                                    null, null, null);
                    if (blobCursor.getCount() < 1) return null;
                    blobCursor.moveToFirst();

                    final File filePath = new File(blobCursor.getString(0));
                    blobCursor.close();

                    if (0 != (imode & ParcelFileDescriptor.MODE_WRITE_ONLY)) {
                        logger.trace("candidate blob file {}", filePath);
                        try {
                            final File newFile = receiveFile(Tables.QUICK_TBL, tuple, filePath);
                            logger.trace("new blob file {}", newFile);

                            final ContentValues cv = new ContentValues();
                            cv.put(field, newFile.getCanonicalPath());
                            db.update(Tables.QUICK_TBL, cv, selection, selectArgs);

                            return ParcelFileDescriptor.open(newFile, imode | ParcelFileDescriptor.MODE_CREATE);

                        } catch (FileNotFoundException ex) {
                            logger.error("could not open file {}\n {}", 
                                    ex.getLocalizedMessage(), ex.getStackTrace());
                            return null;
                        } catch (IOException ex) {
                            return null;
                        }
                    }
                    return ParcelFileDescriptor.open(filePath, imode);

                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                } 
                break;


            case START_BLOB:
                if (pseg.size() < 3)
                    return null;

                try {
                    final String tuple = pseg.get(1);
                    final String field = pseg.get(2);
                    Log.e("FOO", "open file error tuple=["+tuple+"] field=["+field+"]");
                    Log.e("BAR", "  pseg=["+pseg+"]");
                    final String selection = new StringBuilder()
                    .append(StartTableSchemaBase._ID).append("=?")
                    .toString();
                    final String[] selectArgs = new String[]{tuple}; 

                    final Cursor blobCursor = db
                            .query(Tables.START_TBL, new String[]{field}, 
                                    selection, selectArgs, 
                                    null, null, null);
                    if (blobCursor.getCount() < 1) return null;
                    blobCursor.moveToFirst();

                    final File filePath = new File(blobCursor.getString(0));
                    blobCursor.close();

                    if (0 != (imode & ParcelFileDescriptor.MODE_WRITE_ONLY)) {
                        logger.trace("candidate blob file {}", filePath);
                        try {
                            final File newFile = receiveFile(Tables.START_TBL, tuple, filePath);
                            logger.trace("new blob file {}", newFile);

                            final ContentValues cv = new ContentValues();
                            cv.put(field, newFile.getCanonicalPath());
                            db.update(Tables.START_TBL, cv, selection, selectArgs);

                            return ParcelFileDescriptor.open(newFile, imode | ParcelFileDescriptor.MODE_CREATE);

                        } catch (FileNotFoundException ex) {
                            logger.error("could not open file {}\n {}", 
                                    ex.getLocalizedMessage(), ex.getStackTrace());
                            return null;
                        } catch (IOException ex) {
                            return null;
                        }
                    }
                    return ParcelFileDescriptor.open(filePath, imode);

                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                } 
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        return null;
    }

    @Override
    public synchronized boolean onCreate() {
        this.createDatabaseHelper();
        return true;
    }

    @Override
    public synchronized Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {

        final SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        final SQLiteDatabase db = this.openHelper.getReadableDatabase();

        // Switch on the path in the uri for what we want to query.
        final SQLiteCursor cursor;
        final List<String> psegs = uri.getPathSegments();

        switch (uriMatcher.match(uri)) {
            case AMMO_META:
                logger.debug("meta","provide AMMO meta data {}",uri);
                return ammoFieldTypeCursor;

            case AMMO_SET:
                qb.setTables(Tables.AMMO_TBL);
                qb.setProjectionMap(ammoProjectionMap);

                cursor = (SQLiteCursor) qb.query(db, projection, selection, selectionArgs, null, null, 
                        (! TextUtils.isEmpty(sortOrder)) ? sortOrder
                                : AmmoTableSchemaBase.DEFAULT_SORT_ORDER);

                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;

            case AMMO_ID:
                qb.setTables(Tables.AMMO_TBL);
                qb.setProjectionMap(ammoProjectionMap);
                qb.appendWhere(AmmoTableSchemaBase._ID + "="
                        + uri.getPathSegments().get(1));

                cursor = (SQLiteCursor) qb.query(db, projection, selection, selectionArgs, null, null, 
                        (! TextUtils.isEmpty(sortOrder)) ? sortOrder
                                : AmmoTableSchemaBase.DEFAULT_SORT_ORDER);

                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;

            case AMMO_SERIAL:
            {
                qb.setTables(Tables.AMMO_TBL);
                qb.setProjectionMap(ammoProjectionMap);

                qb.appendWhere(AmmoTableSchemaBase._ID + " = " + uri.getPathSegments().get(1));

                final List<String> projectionList = new ArrayList<String>();
                for (final Meta columnMeta : AmmoMockSchemaBase.AMMO_CURSOR_COLUMNS) {
                    switch (columnMeta.type) {
                        case AmmoMockSchemaBase.FIELD_TYPE_BLOB: break;
                        default: 
                            projectionList.add(columnMeta.name);
                    }
                }
                final String[] projectionArray = projectionList.toArray(new String[projectionList.size()]);
                cursor = (SQLiteCursor) qb.query(db, projectionArray, null, null, null, null, null);
                if (1 > cursor.getCount()) {
                    logger.info("no data of type AMMO_ID"); 
                    cursor.close();
                    return null;
                }
                if (psegs.size() < 2) {
                    return cursor;
                }
                return this.customQueryAmmoTableSchema(psegs, cursor);
            }
            case AMMO_BLOB:
            {
                final List<String> fieldNameList = new ArrayList<String>();

                for (Meta columnMeta : AmmoMockSchemaBase.AMMO_CURSOR_COLUMNS) {
                    switch (columnMeta.type) {
                        case AmmoMockSchemaBase.FIELD_TYPE_BLOB:
                            fieldNameList.add(columnMeta.name);
                            break;
                        default:
                    }
                }
                if (fieldNameList.size() < 1) return null;

                final String[] fieldNameArray = fieldNameList.toArray(new String[fieldNameList.size()]);
                final String bselect = AmmoTableSchemaBase._ID + "=?";
                final String[] bselectArgs = new String[]{ uri.getPathSegments().get(1) };
                return db.query(Tables.AMMO_TBL, fieldNameArray, 
                        bselect, bselectArgs, null, null, null);
            }


            case QUICK_META:
                logger.debug("meta","provide QUICK meta data {}",uri);
                return quickFieldTypeCursor;

            case QUICK_SET:
                qb.setTables(Tables.QUICK_TBL);
                qb.setProjectionMap(quickProjectionMap);

                cursor = (SQLiteCursor) qb.query(db, projection, selection, selectionArgs, null, null, 
                        (! TextUtils.isEmpty(sortOrder)) ? sortOrder
                                : QuickTableSchemaBase.DEFAULT_SORT_ORDER);

                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;

            case QUICK_ID:
                qb.setTables(Tables.QUICK_TBL);
                qb.setProjectionMap(quickProjectionMap);
                qb.appendWhere(QuickTableSchemaBase._ID + "="
                        + uri.getPathSegments().get(1));

                cursor = (SQLiteCursor) qb.query(db, projection, selection, selectionArgs, null, null, 
                        (! TextUtils.isEmpty(sortOrder)) ? sortOrder
                                : QuickTableSchemaBase.DEFAULT_SORT_ORDER);

                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;

            case QUICK_SERIAL:
            {
                qb.setTables(Tables.QUICK_TBL);
                qb.setProjectionMap(quickProjectionMap);

                qb.appendWhere(QuickTableSchemaBase._ID + " = " + uri.getPathSegments().get(1));

                final List<String> projectionList = new ArrayList<String>();
                for (final Meta columnMeta : AmmoMockSchemaBase.QUICK_CURSOR_COLUMNS) {
                    switch (columnMeta.type) {
                        case AmmoMockSchemaBase.FIELD_TYPE_BLOB: break;
                        default: 
                            projectionList.add(columnMeta.name);
                    }
                }
                final String[] projectionArray = projectionList.toArray(new String[projectionList.size()]);
                cursor = (SQLiteCursor) qb.query(db, projectionArray, null, null, null, null, null);
                if (1 > cursor.getCount()) {
                    logger.info("no data of type QUICK_ID"); 
                    cursor.close();
                    return null;
                }
                if (psegs.size() < 2) {
                    return cursor;
                }
                return this.customQueryQuickTableSchema(psegs, cursor);
            }
            case QUICK_BLOB:
            {
                final List<String> fieldNameList = new ArrayList<String>();

                for (Meta columnMeta : AmmoMockSchemaBase.QUICK_CURSOR_COLUMNS) {
                    switch (columnMeta.type) {
                        case AmmoMockSchemaBase.FIELD_TYPE_BLOB:
                            fieldNameList.add(columnMeta.name);
                            break;
                        default:
                    }
                }
                if (fieldNameList.size() < 1) return null;

                final String[] fieldNameArray = fieldNameList.toArray(new String[fieldNameList.size()]);
                final String bselect = QuickTableSchemaBase._ID + "=?";
                final String[] bselectArgs = new String[]{ uri.getPathSegments().get(1) };
                return db.query(Tables.QUICK_TBL, fieldNameArray, 
                        bselect, bselectArgs, null, null, null);
            }


            case START_META:
                logger.debug("meta","provide START meta data {}",uri);
                return startFieldTypeCursor;

            case START_SET:
                qb.setTables(Tables.START_TBL);
                qb.setProjectionMap(startProjectionMap);

                cursor = (SQLiteCursor) qb.query(db, projection, selection, selectionArgs, null, null, 
                        (! TextUtils.isEmpty(sortOrder)) ? sortOrder
                                : StartTableSchemaBase.DEFAULT_SORT_ORDER);

                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;

            case START_ID:
                qb.setTables(Tables.START_TBL);
                qb.setProjectionMap(startProjectionMap);
                qb.appendWhere(StartTableSchemaBase._ID + "="
                        + uri.getPathSegments().get(1));

                cursor = (SQLiteCursor) qb.query(db, projection, selection, selectionArgs, null, null, 
                        (! TextUtils.isEmpty(sortOrder)) ? sortOrder
                                : StartTableSchemaBase.DEFAULT_SORT_ORDER);

                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;

            case START_SERIAL:
            {
                qb.setTables(Tables.START_TBL);
                qb.setProjectionMap(startProjectionMap);

                qb.appendWhere(StartTableSchemaBase._ID + " = " + uri.getPathSegments().get(1));

                final List<String> projectionList = new ArrayList<String>();
                for (final Meta columnMeta : AmmoMockSchemaBase.START_CURSOR_COLUMNS) {
                    switch (columnMeta.type) {
                        case AmmoMockSchemaBase.FIELD_TYPE_BLOB: break;
                        default: 
                            projectionList.add(columnMeta.name);
                    }
                }
                final String[] projectionArray = projectionList.toArray(new String[projectionList.size()]);
                cursor = (SQLiteCursor) qb.query(db, projectionArray, null, null, null, null, null);
                if (1 > cursor.getCount()) {
                    logger.info("no data of type START_ID"); 
                    cursor.close();
                    return null;
                }
                if (psegs.size() < 2) {
                    return cursor;
                }
                return this.customQueryStartTableSchema(psegs, cursor);
            }
            case START_BLOB:
            {
                final List<String> fieldNameList = new ArrayList<String>();

                for (Meta columnMeta : AmmoMockSchemaBase.START_CURSOR_COLUMNS) {
                    switch (columnMeta.type) {
                        case AmmoMockSchemaBase.FIELD_TYPE_BLOB:
                            fieldNameList.add(columnMeta.name);
                            break;
                        default:
                    }
                }
                if (fieldNameList.size() < 1) return null;

                final String[] fieldNameArray = fieldNameList.toArray(new String[fieldNameList.size()]);
                final String bselect = StartTableSchemaBase._ID + "=?";
                final String[] bselectArgs = new String[]{ uri.getPathSegments().get(1) };
                return db.query(Tables.START_TBL, fieldNameArray, 
                        bselect, bselectArgs, null, null, null);
            }


            default: 
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    private Cursor customQueryAmmoTableSchema(final List<String> psegs, final SQLiteCursor cursor) {
        logger.info("no custom cursor {}", psegs); 
        return cursor;
    }

    private Cursor customQueryQuickTableSchema(final List<String> psegs, final SQLiteCursor cursor) {
        logger.info("no custom cursor {}", psegs); 
        return cursor;
    }

    private Cursor customQueryStartTableSchema(final List<String> psegs, final SQLiteCursor cursor) {
        logger.info("no custom cursor {}", psegs); 
        return cursor;
    }


    @Override
    public synchronized Uri insert(Uri uri, ContentValues assignedValues) {

	logger.trace("insert: uri=[{}]  cv=[{}]", uri, assignedValues);

	logger.trace("   uri match = {}", uriMatcher.match(uri));

        /** Validate the requested uri and do default initialization. */
        switch (uriMatcher.match(uri)) {
            case AMMO_SET:
                try {
                    final ContentValues values = this.initializeAmmoWithDefaults(assignedValues);
                    if ( AmmoMockSchemaBase.AMMO_KEY_COLUMNS.length < 1 ) {
                        final SQLiteDatabase db = openHelper.getWritableDatabase();
                        final long rowID = db.insert(Tables.AMMO_TBL, AmmoTableSchemaBase.A_FOREIGN_KEY_REF, values);
                        if (rowID < 1) {
                            throw new SQLiteException("Failed to insert row into " + uri);
                        }
                        final Uri playerURI = ContentUris.withAppendedId(AmmoTableSchemaBase.CONTENT_URI, rowID);
                        getContext().getContentResolver().notifyChange(uri, null);
                        return playerURI;
                    }

                    final List<String> selectArgsList = new ArrayList<String>();
                    for (String item : AmmoMockSchemaBase.AMMO_KEY_COLUMNS) {
                        selectArgsList.add(values.getAsString(item));
                    } 
                    final String[] selectArgs = selectArgsList.toArray(new String[0]);
                    final SQLiteDatabase db = openHelper.getWritableDatabase();

                    final long rowID;
                    final int count = db.update(Tables.AMMO_TBL, values, 
                            AmmoMockProviderBase.AMMO_KEY_CLAUSE, selectArgs);
                    if ( count < 1 ) {
                        rowID = db.insert(Tables.AMMO_TBL, AmmoTableSchemaBase.A_FOREIGN_KEY_REF, values);
                        if (rowID < 1) {
                            throw new SQLiteException("Failed to insert row into " + uri);
                        }
                    }
                    else {
                        final Cursor cursor = db.query(Tables.AMMO_TBL, null, 
                                AmmoMockProviderBase.AMMO_KEY_CLAUSE, selectArgs, 
                                null, null, null);
                        cursor.moveToFirst();
                        rowID = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
                        cursor.close();
                    }
                    final Uri playerURI = ContentUris.withAppendedId(AmmoTableSchemaBase.CONTENT_URI, rowID);
                    getContext().getContentResolver().notifyChange(uri, null);
                    return playerURI;
                } catch (SQLiteException ex) {
                    logger.warn("bad column set {}", ex.getLocalizedMessage());
                }
                return null;

                /**
     Receive messages from the distributor and deserialize into the content provider.
                 */
            case AMMO_DESERIAL:
                try {
                    final ContentValues values = this.initializeAmmoWithDefaults(assignedValues);
                    final String json = assignedValues.getAsString("_serial");
                    final JSONObject input = (JSONObject) new JSONTokener(json).nextValue();

                    final ContentValues cv = new ContentValues();
                    for (int ix=0; ix <  AmmoMockSchemaBase.AMMO_CURSOR_COLUMNS.length; ix++) {
                        switch (AmmoMockSchemaBase.AMMO_CURSOR_COLUMNS[ix].type) {
                            case AmmoMockSchemaBase.FIELD_TYPE_BLOB:
                                continue;
                        }
                        final String key = AmmoMockSchemaBase.AMMO_CURSOR_COLUMNS[ix].name;
                        try {
                            final String value = input.getString(key);
                            cv.put(key, value);
                        } catch (JSONException ex) {
                            logger.error("could not extract from json {} {}", 
                                    ex.getLocalizedMessage(), ex.getStackTrace());
                        }
                    }

                    final List<String> selectArgsList = new ArrayList<String>();
                    for (String item : AmmoMockSchemaBase.AMMO_KEY_COLUMNS) {
                        selectArgsList.add(values.getAsString(item));
                    } 
                    final String[] selectArgs = selectArgsList.toArray(new String[0]);
                    final SQLiteDatabase db = this.openHelper.getWritableDatabase();
                    final long rowID;
                    final int count = db.update(Tables.AMMO_TBL, values, 
                            AmmoMockProviderBase.AMMO_KEY_CLAUSE, selectArgs);
                    if ( count < 1 ) {
                        rowID = db.insert(Tables.AMMO_TBL, AmmoTableSchemaBase.A_FOREIGN_KEY_REF, values);
                        if (rowID < 1) {
                            throw new SQLiteException("Failed to insert row into " + uri);
                        }
                    }
                    else {
                        final Cursor cursor = db.query(Tables.AMMO_TBL, null, 
                                AmmoMockProviderBase.AMMO_KEY_CLAUSE, selectArgs, 
                                null, null, null);
                        cursor.moveToFirst();
                        rowID = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
                        cursor.close();
                    }
                    final Uri playerURI = ContentUris.withAppendedId(AmmoTableSchemaBase.CONTENT_URI, rowID);
                    getContext().getContentResolver().notifyChange(uri, null);
                    return playerURI;
                } catch (JSONException ex) {
                    logger.error("could not parse json {} {}", 
                            ex.getLocalizedMessage(), ex.getStackTrace());
                }
                return null;

            case QUICK_SET:
                try {
                    final ContentValues values = this.initializeQuickWithDefaults(assignedValues);
                    if ( AmmoMockSchemaBase.QUICK_KEY_COLUMNS.length < 1 ) {
                        final SQLiteDatabase db = openHelper.getWritableDatabase();
                        final long rowID = db.insert(Tables.QUICK_TBL, QuickTableSchemaBase.A_SHORT_INTEGER, values);
                        if (rowID < 1) {
                            throw new SQLiteException("Failed to insert row into " + uri);
                        }
                        final Uri playerURI = ContentUris.withAppendedId(QuickTableSchemaBase.CONTENT_URI, rowID);
                        getContext().getContentResolver().notifyChange(uri, null);
                        return playerURI;
                    }

                    final List<String> selectArgsList = new ArrayList<String>();
                    for (String item : AmmoMockSchemaBase.QUICK_KEY_COLUMNS) {
                        selectArgsList.add(values.getAsString(item));
                    } 
                    final String[] selectArgs = selectArgsList.toArray(new String[0]);
                    final SQLiteDatabase db = openHelper.getWritableDatabase();

                    final long rowID;
                    final int count = db.update(Tables.QUICK_TBL, values, 
                            AmmoMockProviderBase.QUICK_KEY_CLAUSE, selectArgs);
                    if ( count < 1 ) {
                        rowID = db.insert(Tables.QUICK_TBL, QuickTableSchemaBase.A_SHORT_INTEGER, values);
                        if (rowID < 1) {
                            throw new SQLiteException("Failed to insert row into " + uri);
                        }
                    }
                    else {
                        final Cursor cursor = db.query(Tables.QUICK_TBL, null, 
                                AmmoMockProviderBase.QUICK_KEY_CLAUSE, selectArgs, 
                                null, null, null);
                        cursor.moveToFirst();
                        rowID = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
                        cursor.close();
                    }
                    final Uri playerURI = ContentUris.withAppendedId(QuickTableSchemaBase.CONTENT_URI, rowID);
                    getContext().getContentResolver().notifyChange(uri, null);
                    return playerURI;
                } catch (SQLiteException ex) {
                    logger.warn("bad column set {}", ex.getLocalizedMessage());
                }
                return null;

                /**
     Receive messages from the distributor and deserialize into the content provider.
                 */
            case QUICK_DESERIAL:
                try {
                    final ContentValues values = this.initializeQuickWithDefaults(assignedValues);
                    final String json = assignedValues.getAsString("_serial");
                    final JSONObject input = (JSONObject) new JSONTokener(json).nextValue();

                    final ContentValues cv = new ContentValues();
                    for (int ix=0; ix <  AmmoMockSchemaBase.QUICK_CURSOR_COLUMNS.length; ix++) {
                        switch (AmmoMockSchemaBase.QUICK_CURSOR_COLUMNS[ix].type) {
                            case AmmoMockSchemaBase.FIELD_TYPE_BLOB:
                                continue;
                        }
                        final String key = AmmoMockSchemaBase.QUICK_CURSOR_COLUMNS[ix].name;
                        try {
                            final String value = input.getString(key);
                            cv.put(key, value);
                        } catch (JSONException ex) {
                            logger.error("could not extract from json {} {}", 
                                    ex.getLocalizedMessage(), ex.getStackTrace());
                        }
                    }

                    final List<String> selectArgsList = new ArrayList<String>();
                    for (String item : AmmoMockSchemaBase.QUICK_KEY_COLUMNS) {
                        selectArgsList.add(values.getAsString(item));
                    } 
                    final String[] selectArgs = selectArgsList.toArray(new String[0]);
                    final SQLiteDatabase db = this.openHelper.getWritableDatabase();
                    final long rowID;
                    final int count = db.update(Tables.QUICK_TBL, values, 
                            AmmoMockProviderBase.QUICK_KEY_CLAUSE, selectArgs);
                    if ( count < 1 ) {
                        rowID = db.insert(Tables.QUICK_TBL, QuickTableSchemaBase.A_SHORT_INTEGER, values);
                        if (rowID < 1) {
                            throw new SQLiteException("Failed to insert row into " + uri);
                        }
                    }
                    else {
                        final Cursor cursor = db.query(Tables.QUICK_TBL, null, 
                                AmmoMockProviderBase.QUICK_KEY_CLAUSE, selectArgs, 
                                null, null, null);
                        cursor.moveToFirst();
                        rowID = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
                        cursor.close();
                    }
                    final Uri playerURI = ContentUris.withAppendedId(QuickTableSchemaBase.CONTENT_URI, rowID);
                    getContext().getContentResolver().notifyChange(uri, null);
                    return playerURI;
                } catch (JSONException ex) {
                    logger.error("could not parse json {} {}", 
                            ex.getLocalizedMessage(), ex.getStackTrace());
                }
                return null;

            case START_SET:
		logger.trace(" start_set");
                try {
                    final ContentValues values = this.initializeStartWithDefaults(assignedValues);
                    if ( AmmoMockSchemaBase.START_KEY_COLUMNS.length < 1 ) {
                        final SQLiteDatabase db = openHelper.getWritableDatabase();
                        final long rowID = db.insert(Tables.START_TBL, StartTableSchemaBase.A_REAL, values);
                        if (rowID < 1) {
                            throw new SQLiteException("Failed to insert row into " + uri);
                        }
                        final Uri playerURI = ContentUris.withAppendedId(StartTableSchemaBase.CONTENT_URI, rowID);
                        getContext().getContentResolver().notifyChange(uri, null);
                        return playerURI;
                    }

                    final List<String> selectArgsList = new ArrayList<String>();
                    for (String item : AmmoMockSchemaBase.START_KEY_COLUMNS) {
                        selectArgsList.add(values.getAsString(item));
                    } 
                    final String[] selectArgs = selectArgsList.toArray(new String[0]);
                    final SQLiteDatabase db = openHelper.getWritableDatabase();

                    final long rowID;
                    final int count = db.update(Tables.START_TBL, values, 
                            AmmoMockProviderBase.START_KEY_CLAUSE, selectArgs);
                    if ( count < 1 ) {
                        rowID = db.insert(Tables.START_TBL, StartTableSchemaBase.A_REAL, values);
			logger.trace(" start_set (count < 1):  rowId = {}", rowID);
                        if (rowID < 1) {
                            throw new SQLiteException("Failed to insert row into " + uri);
                        }
                    }
                    else {
                        final Cursor cursor = db.query(Tables.START_TBL, null, 
                                AmmoMockProviderBase.START_KEY_CLAUSE, selectArgs, 
                                null, null, null);
                        cursor.moveToFirst();
                        rowID = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
                        cursor.close();
			logger.trace(" start_set (else):  rowId = {}", rowID);
                    }
                    final Uri playerURI = ContentUris.withAppendedId(StartTableSchemaBase.CONTENT_URI, rowID);
		    logger.trace(" start_set : playerURI = [{}]", playerURI);
                    getContext().getContentResolver().notifyChange(uri, null);
                    return playerURI;
                } catch (SQLiteException ex) {
                    logger.warn("bad column set {}", ex.getLocalizedMessage());
                }
		logger.trace(" start_set end");
                return null;

                /**
     Receive messages from the distributor and deserialize into the content provider.
                 */
            case START_DESERIAL:
                try {
                    final ContentValues values = this.initializeStartWithDefaults(assignedValues);
                    final String json = assignedValues.getAsString("_serial");
                    final JSONObject input = (JSONObject) new JSONTokener(json).nextValue();

                    final ContentValues cv = new ContentValues();
                    for (int ix=0; ix <  AmmoMockSchemaBase.START_CURSOR_COLUMNS.length; ix++) {
                        switch (AmmoMockSchemaBase.START_CURSOR_COLUMNS[ix].type) {
                            case AmmoMockSchemaBase.FIELD_TYPE_BLOB:
                                continue;
                        }
                        final String key = AmmoMockSchemaBase.START_CURSOR_COLUMNS[ix].name;
                        try {
                            final String value = input.getString(key);
                            cv.put(key, value);
                        } catch (JSONException ex) {
                            logger.error("could not extract from json {} {}", 
                                    ex.getLocalizedMessage(), ex.getStackTrace());
                        }
                    }

                    final List<String> selectArgsList = new ArrayList<String>();
                    for (String item : AmmoMockSchemaBase.START_KEY_COLUMNS) {
                        selectArgsList.add(values.getAsString(item));
                    } 
                    final String[] selectArgs = selectArgsList.toArray(new String[0]);
                    final SQLiteDatabase db = this.openHelper.getWritableDatabase();
                    final long rowID;
                    final int count = db.update(Tables.START_TBL, values, 
                            AmmoMockProviderBase.START_KEY_CLAUSE, selectArgs);
                    if ( count < 1 ) {
                        rowID = db.insert(Tables.START_TBL, StartTableSchemaBase.A_REAL, values);
                        if (rowID < 1) {
                            throw new SQLiteException("Failed to insert row into " + uri);
                        }
                    }
                    else {
                        final Cursor cursor = db.query(Tables.START_TBL, null, 
                                AmmoMockProviderBase.START_KEY_CLAUSE, selectArgs, 
                                null, null, null);
                        cursor.moveToFirst();
                        rowID = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
                        cursor.close();
                    }
                    final Uri playerURI = ContentUris.withAppendedId(StartTableSchemaBase.CONTENT_URI, rowID);
                    getContext().getContentResolver().notifyChange(uri, null);
                    return playerURI;
                } catch (JSONException ex) {
                    logger.error("could not parse json {} {}", 
                            ex.getLocalizedMessage(), ex.getStackTrace());
                }
                return null;


            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    /** Insert method helper */
    protected ContentValues initializeAmmoWithDefaults(ContentValues assignedValues) {
        final Long now = Long.valueOf(System.currentTimeMillis());
        final ContentValues values = (assignedValues == null) 
                ? new ContentValues() : assignedValues;

                if (!values.containsKey(AmmoTableSchemaBase.A_FOREIGN_KEY_REF)) {
                    values.put(AmmoTableSchemaBase.A_FOREIGN_KEY_REF, -1);
                } 
                if (!values.containsKey(AmmoTableSchemaBase.AN_EXCLUSIVE_ENUMERATION)) {
                    values.put(AmmoTableSchemaBase.AN_EXCLUSIVE_ENUMERATION, AmmoTableSchemaBase.AN_EXCLUSIVE_ENUMERATION_MEDIUM);
                } 
                if (!values.containsKey(AmmoTableSchemaBase.AN_INCLUSIVE_ENUMERATION)) {
                    values.put(AmmoTableSchemaBase.AN_INCLUSIVE_ENUMERATION, AmmoTableSchemaBase.AN_INCLUSIVE_ENUMERATION_APPLE);
                } 
                if (!values.containsKey(AmmoTableSchemaBase._RECEIVED_DATE)) {
                    values.put(AmmoTableSchemaBase._RECEIVED_DATE, now);
                }
                if (!values.containsKey(AmmoTableSchemaBase._DISPOSITION)) {
                    values.put(AmmoTableSchemaBase._DISPOSITION, AmmoMockSchemaBase.Disposition.LOCAL.name());
                }
                return values;
    }

    /** Insert method helper */
    protected ContentValues initializeQuickWithDefaults(ContentValues assignedValues) {
        final Long now = Long.valueOf(System.currentTimeMillis());
        final ContentValues values = (assignedValues == null) 
                ? new ContentValues() : assignedValues;

                if (!values.containsKey(QuickTableSchemaBase.A_SHORT_INTEGER)) {
                    values.put(QuickTableSchemaBase.A_SHORT_INTEGER, 0);
                } 
                if (!values.containsKey(QuickTableSchemaBase.AN_INTEGER)) {
                    values.put(QuickTableSchemaBase.AN_INTEGER, 0);
                } 
                if (!values.containsKey(QuickTableSchemaBase.A_BOOLEAN)) {
                    values.put(QuickTableSchemaBase.A_BOOLEAN, false);
                } 
                if (!values.containsKey(QuickTableSchemaBase.A_LONG_INTEGER)) {
                    values.put(QuickTableSchemaBase.A_LONG_INTEGER, 0.0);
                } 
                if (!values.containsKey(QuickTableSchemaBase.A_ABSOLUTE_TIME)) {
                    values.put(QuickTableSchemaBase.A_ABSOLUTE_TIME, now);
                } 
                if (!values.containsKey(QuickTableSchemaBase._RECEIVED_DATE)) {
                    values.put(QuickTableSchemaBase._RECEIVED_DATE, now);
                }
                if (!values.containsKey(QuickTableSchemaBase._DISPOSITION)) {
                    values.put(QuickTableSchemaBase._DISPOSITION, AmmoMockSchemaBase.Disposition.LOCAL.name());
                }
                return values;
    }

    /** Insert method helper */
    protected ContentValues initializeStartWithDefaults(ContentValues assignedValues) {
        final Long now = Long.valueOf(System.currentTimeMillis());
        final ContentValues values = (assignedValues == null) 
                ? new ContentValues() : assignedValues;

                if (!values.containsKey(StartTableSchemaBase.A_REAL)) {
                    values.put(StartTableSchemaBase.A_REAL, 0.0);
                } 
                if (!values.containsKey(StartTableSchemaBase.A_GLOBALLY_UNIQUE_IDENTIFIER)) {
                    values.put(StartTableSchemaBase.A_GLOBALLY_UNIQUE_IDENTIFIER, "");
                } 
                if (!values.containsKey(StartTableSchemaBase.SOME_ARBITRARY_TEXT)) {
                    values.put(StartTableSchemaBase.SOME_ARBITRARY_TEXT, "");
                } 
                if (!values.containsKey(StartTableSchemaBase.A_BLOB)) {
                    values.put(StartTableSchemaBase.A_BLOB, "");
                } 
                if (!values.containsKey(StartTableSchemaBase.A_FILE_TYPE)) {
                    values.put(StartTableSchemaBase.A_FILE_TYPE, "text/plain");
                }
                if (!values.containsKey(StartTableSchemaBase.A_FILE)) {
                    values.put(StartTableSchemaBase.A_FILE, "");
                } 
                if (!values.containsKey(StartTableSchemaBase._RECEIVED_DATE)) {
                    values.put(StartTableSchemaBase._RECEIVED_DATE, now);
                }
                if (!values.containsKey(StartTableSchemaBase._DISPOSITION)) {
                    values.put(StartTableSchemaBase._DISPOSITION, AmmoMockSchemaBase.Disposition.LOCAL.name());
                }
                return values;
    }



    @Override
    public synchronized int delete(Uri uri, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = this.openHelper.getWritableDatabase();
        final int count;
        Cursor cursor;
        final int match = uriMatcher.match(uri);

        logger.info("running delete with uri({}) selection({}) match({})",
                new Object[]{uri, selection, match});

        switch (match) {
            case AMMO_SET:
                cursor = db.query(Tables.AMMO_TBL, new String[] {AmmoTableSchemaBase._ID}, selection, selectionArgs, null, null, null);
                logger.info("cursor rows: {}", cursor.getCount());
                if (cursor.moveToFirst()) {
                    do {
                        long rowid = cursor.getLong(cursor.getColumnIndex(AmmoTableSchemaBase._ID));
                        String tuple = Long.toString(rowid);
                        logger.info("found rowid ({}) and tuple ({}) for deletion", rowid, tuple);
                        final File file = blobDir(Tables.AMMO_TBL, tuple);
                        logger.info("deleting directory: {}", file.getAbsolutePath());
                        recursiveDelete(file);
                    }
                    while (cursor.moveToNext());
                }
                cursor.close();
                count = db.delete(Tables.AMMO_TBL, selection, selectionArgs);
                break;

            case AMMO_ID:
                final String ammoID = uri.getPathSegments().get(1);
                cursor = db.query(Tables.AMMO_TBL, new String[] {AmmoTableSchemaBase._ID}, selection, selectionArgs, null, null, null);
                logger.info("cursor rows: {}", cursor.getCount());
                if (cursor.moveToFirst()) {
                    do {
                        final long rowid = cursor.getLong(cursor.getColumnIndex(AmmoTableSchemaBase._ID));
                        final String tuple = Long.toString(rowid);
                        logger.info("found rowid ({}) and tuple ({}) for deletion", rowid, tuple);
                        final File file = blobDir(Tables.AMMO_TBL, tuple);
                        logger.info("deleting directory: {}", file.getAbsolutePath());
                        recursiveDelete(file);
                    }
                    while (cursor.moveToNext());
                }
                cursor.close();
                count = db.delete(Tables.AMMO_TBL,
                        AmmoTableSchemaBase._ID
                        + "="
                        + ammoID
                        + (TextUtils.isEmpty(selection) ? "" 
                                : (" AND (" + selection + ')')),
                                selectionArgs);
                break;

            case QUICK_SET:
                cursor = db.query(Tables.QUICK_TBL, new String[] {QuickTableSchemaBase._ID}, selection, selectionArgs, null, null, null);
                logger.info("cursor rows: {}", cursor.getCount());
                if (cursor.moveToFirst()) {
                    do {
                        long rowid = cursor.getLong(cursor.getColumnIndex(QuickTableSchemaBase._ID));
                        String tuple = Long.toString(rowid);
                        logger.info("found rowid ({}) and tuple ({}) for deletion", rowid, tuple);
                        final File file = blobDir(Tables.QUICK_TBL, tuple);
                        logger.info("deleting directory: {}", file.getAbsolutePath());
                        recursiveDelete(file);
                    }
                    while (cursor.moveToNext());
                }
                cursor.close();
                count = db.delete(Tables.QUICK_TBL, selection, selectionArgs);
                break;

            case QUICK_ID:
                final String quickID = uri.getPathSegments().get(1);
                cursor = db.query(Tables.QUICK_TBL, new String[] {QuickTableSchemaBase._ID}, selection, selectionArgs, null, null, null);
                logger.info("cursor rows: {}", cursor.getCount());
                if (cursor.moveToFirst()) {
                    do {
                        final long rowid = cursor.getLong(cursor.getColumnIndex(QuickTableSchemaBase._ID));
                        final String tuple = Long.toString(rowid);
                        logger.info("found rowid ({}) and tuple ({}) for deletion", rowid, tuple);
                        final File file = blobDir(Tables.QUICK_TBL, tuple);
                        logger.info("deleting directory: {}", file.getAbsolutePath());
                        recursiveDelete(file);
                    }
                    while (cursor.moveToNext());
                }
                cursor.close();
                count = db.delete(Tables.QUICK_TBL,
                        QuickTableSchemaBase._ID
                        + "="
                        + quickID
                        + (TextUtils.isEmpty(selection) ? "" 
                                : (" AND (" + selection + ')')),
                                selectionArgs);
                break;

            case START_SET:
                cursor = db.query(Tables.START_TBL, new String[] {StartTableSchemaBase._ID}, selection, selectionArgs, null, null, null);
                logger.info("cursor rows: {}", cursor.getCount());
                if (cursor.moveToFirst()) {
                    do {
                        long rowid = cursor.getLong(cursor.getColumnIndex(StartTableSchemaBase._ID));
                        String tuple = Long.toString(rowid);
                        logger.info("found rowid ({}) and tuple ({}) for deletion", rowid, tuple);
                        final File file = blobDir(Tables.START_TBL, tuple);
                        logger.info("deleting directory: {}", file.getAbsolutePath());
                        recursiveDelete(file);
                    }
                    while (cursor.moveToNext());
                }
                cursor.close();
                count = db.delete(Tables.START_TBL, selection, selectionArgs);
                break;

            case START_ID:
                final String startID = uri.getPathSegments().get(1);
                cursor = db.query(Tables.START_TBL, new String[] {StartTableSchemaBase._ID}, selection, selectionArgs, null, null, null);
                logger.info("cursor rows: {}", cursor.getCount());
                if (cursor.moveToFirst()) {
                    do {
                        final long rowid = cursor.getLong(cursor.getColumnIndex(StartTableSchemaBase._ID));
                        final String tuple = Long.toString(rowid);
                        logger.info("found rowid ({}) and tuple ({}) for deletion", rowid, tuple);
                        final File file = blobDir(Tables.START_TBL, tuple);
                        logger.info("deleting directory: {}", file.getAbsolutePath());
                        recursiveDelete(file);
                    }
                    while (cursor.moveToNext());
                }
                cursor.close();
                count = db.delete(Tables.START_TBL,
                        StartTableSchemaBase._ID
                        + "="
                        + startID
                        + (TextUtils.isEmpty(selection) ? "" 
                                : (" AND (" + selection + ')')),
                                selectionArgs);
                break;


            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (count > 0) getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public synchronized int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = this.openHelper.getWritableDatabase();
        final Uri notifyUri;
        final int count;
        switch (uriMatcher.match(uri)) {
            case AMMO_SET:
                logger.debug("AMMO_SET");
                notifyUri = uri;
                count = db.update(Tables.AMMO_TBL, values, selection,
                        selectionArgs);
                break;

            case AMMO_ID:
                logger.debug("AMMO_ID");
                //  notify on the base URI - without the ID ?
                notifyUri = AmmoTableSchemaBase.CONTENT_URI; 
                String ammoID = uri.getPathSegments().get(1);
                count = db.update(Tables.AMMO_TBL, values, AmmoTableSchemaBase._ID
                        + "="
                        + ammoID
                        + (TextUtils.isEmpty(selection) ? "" 
                                : (" AND (" + selection + ')')),
                                selectionArgs);
                break;

            case QUICK_SET:
                logger.debug("QUICK_SET");
                notifyUri = uri;
                count = db.update(Tables.QUICK_TBL, values, selection,
                        selectionArgs);
                break;

            case QUICK_ID:
                logger.debug("QUICK_ID");
                //  notify on the base URI - without the ID ?
                notifyUri = QuickTableSchemaBase.CONTENT_URI; 
                String quickID = uri.getPathSegments().get(1);
                count = db.update(Tables.QUICK_TBL, values, QuickTableSchemaBase._ID
                        + "="
                        + quickID
                        + (TextUtils.isEmpty(selection) ? "" 
                                : (" AND (" + selection + ')')),
                                selectionArgs);
                break;

            case START_SET:
                logger.debug("START_SET");
                notifyUri = uri;
                count = db.update(Tables.START_TBL, values, selection,
                        selectionArgs);
                break;

            case START_ID:
                logger.debug("START_ID");
                //  notify on the base URI - without the ID ?
                notifyUri = StartTableSchemaBase.CONTENT_URI; 
                String startID = uri.getPathSegments().get(1);
                count = db.update(Tables.START_TBL, values, StartTableSchemaBase._ID
                        + "="
                        + startID
                        + (TextUtils.isEmpty(selection) ? "" 
                                : (" AND (" + selection + ')')),
                                selectionArgs);
                break;


            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (count > 0) 
            getContext().getContentResolver().notifyChange(notifyUri, null);
        return count;   
    }

    @Override
    public synchronized String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case AMMO_SET:
            case AMMO_ID:
                return AmmoTableSchemaBase.CONTENT_TOPIC;

            case QUICK_SET:
            case QUICK_ID:
                return QuickTableSchemaBase.CONTENT_TOPIC;

            case START_SET:
            case START_ID:
                return StartTableSchemaBase.CONTENT_TOPIC;


            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }   
    }

    // ===========================================================
    // Static declarations
    // ===========================================================

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AmmoMockSchemaBase.AUTHORITY, Tables.AMMO_TBL, AMMO_SET);
        uriMatcher.addURI(AmmoMockSchemaBase.AUTHORITY, Tables.AMMO_TBL + "/#", AMMO_ID);
        uriMatcher.addURI(AmmoMockSchemaBase.AUTHORITY, Tables.AMMO_TBL + "/#/_serial/*", AMMO_SERIAL);
        uriMatcher.addURI(AmmoMockSchemaBase.AUTHORITY, Tables.AMMO_TBL + "/_deserial/*", AMMO_DESERIAL);
        uriMatcher.addURI(AmmoMockSchemaBase.AUTHORITY, Tables.AMMO_TBL + "/#/_serial", AMMO_SERIAL);
        uriMatcher.addURI(AmmoMockSchemaBase.AUTHORITY, Tables.AMMO_TBL + "/_deserial", AMMO_DESERIAL);
        uriMatcher.addURI(AmmoMockSchemaBase.AUTHORITY, Tables.AMMO_TBL + "/#/_blob", AMMO_BLOB);
        uriMatcher.addURI(AmmoMockSchemaBase.AUTHORITY, Tables.AMMO_TBL + "/#/_data_type", AMMO_META);
        uriMatcher.addURI(AmmoMockSchemaBase.AUTHORITY, Tables.AMMO_TBL + "/_data_type", AMMO_META);

        {
            ammoFieldTypeCursor = new MatrixCursor(new String[] {
                    AmmoTableSchemaBase.A_FOREIGN_KEY_REF, 
                    AmmoTableSchemaBase.AN_EXCLUSIVE_ENUMERATION, 
                    AmmoTableSchemaBase.AN_INCLUSIVE_ENUMERATION, 
            }, 1);

            final MatrixCursor.RowBuilder row = ammoFieldTypeCursor.newRow();
            row.add(AmmoMockSchemaBase.FIELD_TYPE_FK); // A_FOREIGN_KEY_REF 
            row.add(AmmoMockSchemaBase.FIELD_TYPE_EXCLUSIVE); // AN_EXCLUSIVE_ENUMERATION 
            row.add(AmmoMockSchemaBase.FIELD_TYPE_INCLUSIVE); // AN_INCLUSIVE_ENUMERATION 
        }

        uriMatcher.addURI(AmmoMockSchemaBase.AUTHORITY, Tables.QUICK_TBL, QUICK_SET);
        uriMatcher.addURI(AmmoMockSchemaBase.AUTHORITY, Tables.QUICK_TBL + "/#", QUICK_ID);
        uriMatcher.addURI(AmmoMockSchemaBase.AUTHORITY, Tables.QUICK_TBL + "/#/_serial/*", QUICK_SERIAL);
        uriMatcher.addURI(AmmoMockSchemaBase.AUTHORITY, Tables.QUICK_TBL + "/_deserial/*", QUICK_DESERIAL);
        uriMatcher.addURI(AmmoMockSchemaBase.AUTHORITY, Tables.QUICK_TBL + "/#/_serial", QUICK_SERIAL);
        uriMatcher.addURI(AmmoMockSchemaBase.AUTHORITY, Tables.QUICK_TBL + "/_deserial", QUICK_DESERIAL);
        uriMatcher.addURI(AmmoMockSchemaBase.AUTHORITY, Tables.QUICK_TBL + "/#/_blob", QUICK_BLOB);
        uriMatcher.addURI(AmmoMockSchemaBase.AUTHORITY, Tables.QUICK_TBL + "/#/_data_type", QUICK_META);
        uriMatcher.addURI(AmmoMockSchemaBase.AUTHORITY, Tables.QUICK_TBL + "/_data_type", QUICK_META);

        {
            quickFieldTypeCursor = new MatrixCursor(new String[] {
                    QuickTableSchemaBase.A_SHORT_INTEGER, 
                    QuickTableSchemaBase.AN_INTEGER, 
                    QuickTableSchemaBase.A_BOOLEAN, 
                    QuickTableSchemaBase.A_LONG_INTEGER, 
                    QuickTableSchemaBase.A_ABSOLUTE_TIME, 
            }, 1);

            final MatrixCursor.RowBuilder row = quickFieldTypeCursor.newRow();
            row.add(AmmoMockSchemaBase.FIELD_TYPE_SHORT); // A_SHORT_INTEGER 
            row.add(AmmoMockSchemaBase.FIELD_TYPE_INTEGER); // AN_INTEGER 
            row.add(AmmoMockSchemaBase.FIELD_TYPE_BOOL); // A_BOOLEAN 
            row.add(AmmoMockSchemaBase.FIELD_TYPE_LONG); // A_LONG_INTEGER 
            row.add(AmmoMockSchemaBase.FIELD_TYPE_TIMESTAMP); // A_ABSOLUTE_TIME 
        }

        uriMatcher.addURI(AmmoMockSchemaBase.AUTHORITY, Tables.START_TBL, START_SET);
        uriMatcher.addURI(AmmoMockSchemaBase.AUTHORITY, Tables.START_TBL + "/#", START_ID);
        uriMatcher.addURI(AmmoMockSchemaBase.AUTHORITY, Tables.START_TBL + "/#/_serial/*", START_SERIAL);
        uriMatcher.addURI(AmmoMockSchemaBase.AUTHORITY, Tables.START_TBL + "/_deserial/*", START_DESERIAL);
        uriMatcher.addURI(AmmoMockSchemaBase.AUTHORITY, Tables.START_TBL + "/#/_serial", START_SERIAL);
        uriMatcher.addURI(AmmoMockSchemaBase.AUTHORITY, Tables.START_TBL + "/_deserial", START_DESERIAL);
        uriMatcher.addURI(AmmoMockSchemaBase.AUTHORITY, Tables.START_TBL + "/#/_blob", START_BLOB);
        uriMatcher.addURI(AmmoMockSchemaBase.AUTHORITY, Tables.START_TBL + "/#/_data_type", START_META);
        uriMatcher.addURI(AmmoMockSchemaBase.AUTHORITY, Tables.START_TBL + "/_data_type", START_META);

        {
            startFieldTypeCursor = new MatrixCursor(new String[] {
                    StartTableSchemaBase.A_REAL, 
                    StartTableSchemaBase.A_GLOBALLY_UNIQUE_IDENTIFIER, 
                    StartTableSchemaBase.SOME_ARBITRARY_TEXT, 
                    StartTableSchemaBase.A_BLOB, 
                    StartTableSchemaBase.A_FILE, 
            }, 1);

            final MatrixCursor.RowBuilder row = startFieldTypeCursor.newRow();
            row.add(AmmoMockSchemaBase.FIELD_TYPE_REAL); // A_REAL 
            row.add(AmmoMockSchemaBase.FIELD_TYPE_GUID); // A_GLOBALLY_UNIQUE_IDENTIFIER 
            row.add(AmmoMockSchemaBase.FIELD_TYPE_TEXT); // SOME_ARBITRARY_TEXT 
            row.add(AmmoMockSchemaBase.FIELD_TYPE_BLOB); // A_BLOB 
            row.add(AmmoMockSchemaBase.FIELD_TYPE_TEXT); // A_FILE 
        }


        ammoProjectionKey = new String[1];
        ammoProjectionKey[0] = AmmoTableSchemaBase._ID;

        {
            final HashMap<String, String> columns = new HashMap<String, String>();
            columns.put(AmmoTableSchemaBase._ID, AmmoTableSchemaBase._ID);
            columns.put(AmmoTableSchemaBase.A_FOREIGN_KEY_REF, "\""+AmmoTableSchemaBase.A_FOREIGN_KEY_REF+"\""); 
            columns.put(AmmoTableSchemaBase.AN_EXCLUSIVE_ENUMERATION, "\""+AmmoTableSchemaBase.AN_EXCLUSIVE_ENUMERATION+"\""); 
            columns.put(AmmoTableSchemaBase.AN_INCLUSIVE_ENUMERATION, "\""+AmmoTableSchemaBase.AN_INCLUSIVE_ENUMERATION+"\""); 
            columns.put(AmmoTableSchemaBase._RECEIVED_DATE, "\""+AmmoTableSchemaBase._RECEIVED_DATE+"\"");
            columns.put(AmmoTableSchemaBase._DISPOSITION, "\""+AmmoTableSchemaBase._DISPOSITION+"\"");

            ammoProjectionMap = columns;
        }


        quickProjectionKey = new String[1];
        quickProjectionKey[0] = QuickTableSchemaBase._ID;

        {
            final HashMap<String, String> columns = new HashMap<String, String>();
            columns.put(QuickTableSchemaBase._ID, QuickTableSchemaBase._ID);
            columns.put(QuickTableSchemaBase.A_SHORT_INTEGER, "\""+QuickTableSchemaBase.A_SHORT_INTEGER+"\""); 
            columns.put(QuickTableSchemaBase.AN_INTEGER, "\""+QuickTableSchemaBase.AN_INTEGER+"\""); 
            columns.put(QuickTableSchemaBase.A_BOOLEAN, "\""+QuickTableSchemaBase.A_BOOLEAN+"\""); 
            columns.put(QuickTableSchemaBase.A_LONG_INTEGER, "\""+QuickTableSchemaBase.A_LONG_INTEGER+"\""); 
            columns.put(QuickTableSchemaBase.A_ABSOLUTE_TIME, "\""+QuickTableSchemaBase.A_ABSOLUTE_TIME+"\""); 
            columns.put(QuickTableSchemaBase._RECEIVED_DATE, "\""+QuickTableSchemaBase._RECEIVED_DATE+"\"");
            columns.put(QuickTableSchemaBase._DISPOSITION, "\""+QuickTableSchemaBase._DISPOSITION+"\"");

            quickProjectionMap = columns;
        }


        startProjectionKey = new String[1];
        startProjectionKey[0] = StartTableSchemaBase._ID;

        {
            final HashMap<String, String> columns = new HashMap<String, String>();
            columns.put(StartTableSchemaBase._ID, StartTableSchemaBase._ID);
            columns.put(StartTableSchemaBase.A_REAL, "\""+StartTableSchemaBase.A_REAL+"\""); 
            columns.put(StartTableSchemaBase.A_GLOBALLY_UNIQUE_IDENTIFIER, "\""+StartTableSchemaBase.A_GLOBALLY_UNIQUE_IDENTIFIER+"\""); 
            columns.put(StartTableSchemaBase.SOME_ARBITRARY_TEXT, "\""+StartTableSchemaBase.SOME_ARBITRARY_TEXT+"\""); 
            columns.put(StartTableSchemaBase.A_BLOB, "\""+StartTableSchemaBase.A_BLOB+"\""); 
            columns.put(StartTableSchemaBase.A_FILE_TYPE, "\""+StartTableSchemaBase.A_FILE_TYPE+"\"");
            columns.put(StartTableSchemaBase.A_FILE, "\""+StartTableSchemaBase.A_FILE+"\""); 
            columns.put(StartTableSchemaBase._RECEIVED_DATE, "\""+StartTableSchemaBase._RECEIVED_DATE+"\"");
            columns.put(StartTableSchemaBase._DISPOSITION, "\""+StartTableSchemaBase._DISPOSITION+"\"");

            startProjectionMap = columns;
        }


    }


    static public final File applDir;
    static public final File applCacheDir;

    static public final File applCacheAmmoDir;
    static public final File applCacheQuickDir;
    static public final File applCacheStartDir;


    static public final File applTempDir;
    static {
        applDir = new File(Environment.getExternalStorageDirectory(), "support/edu.vu.isis"); 
        applDir.mkdirs();
        if (! applDir.exists()) {
            logger.error("cannot create support files check permissions : {}", 
                    applDir.toString());
        } else if (! applDir.isDirectory()) {
            logger.error("support directory is not a directory : {}", 
                    applDir.toString());
        }

        applCacheDir = new File(applDir, "cache/AmmoMock"); 
        applCacheDir.mkdirs();

        applCacheAmmoDir = new File(applCacheDir, "ammo"); 
        applCacheDir.mkdirs();

        applCacheQuickDir = new File(applCacheDir, "quick"); 
        applCacheDir.mkdirs();

        applCacheStartDir = new File(applCacheDir, "start"); 
        applCacheDir.mkdirs();


        applTempDir = new File(applDir, "tmp/AmmoMock"); 
        applTempDir.mkdirs();
    }

    protected static File blobFile(String table, String tuple, String field) throws IOException {
        final File tupleCacheDir = blobDir(table, tuple);
        final File cacheFile = new File(tupleCacheDir, field+".blob");
        if (cacheFile.exists()) return cacheFile;    

        cacheFile.createNewFile();
        return cacheFile;
    }

    protected static File blobDir(String table, String tuple) {
        final File tableCacheDir = new File(applCacheDir, table);
        final File tupleCacheDir = new File(tableCacheDir, tuple);
        if (!tupleCacheDir.exists()) tupleCacheDir.mkdirs();
        return tupleCacheDir;
    }

    protected static File receiveFile(String table, String tuple, File filePath) {
        final String baseName = new StringBuilder()
        .append("recv_")
        .append(tuple).append("_")
        .append(filePath.getName())
        .toString();

        final File dirPath = filePath.getParentFile();
        final File wipPath;
        if (dirPath == null) {
            wipPath = blobDir(table, tuple);
        } else if (! dirPath.exists()) {
            if (! dirPath.mkdirs()) {
                wipPath = blobDir(table, tuple);
            } else {
                wipPath = dirPath;
            }
        } else { 
            wipPath = dirPath;
        }
        return new File(wipPath, baseName);
    }

    protected static File tempFilePath(String table) throws IOException {
        return File.createTempFile(table, ".tmp", applTempDir);
    }


    protected static void clearBlobCache(String table, String tuple) {
        if (table == null) {
            if (applCacheDir.isDirectory()) {
                for (File child : applCacheDir.listFiles()) {
                    recursiveDelete(child);
                }
                return;
            }
        }
        final File tableCacheDir = new File(applCacheDir, table);
        if (tuple == null) {
            if (tableCacheDir.isDirectory()) {
                for (File child : tableCacheDir.listFiles()) {
                    recursiveDelete(child);
                }
                return;
            }
        }
        final File tupleCacheDir = new File(tableCacheDir, tuple);
        if (tupleCacheDir.isDirectory()) {
            for (File child : tupleCacheDir.listFiles()) {
                recursiveDelete(child);
            }
        }
    }

    /** 
     * Recursively delete all children of this directory and the directory itself.
     * 
     * @param dir
     */
    protected static void recursiveDelete(File dir) {
        if (!dir.exists()) return;

        if (dir.isFile()) {
            dir.delete();
            return;
        }
        if (dir.isDirectory()) {
            for (File child : dir.listFiles()) {
                recursiveDelete(child);
            }
            dir.delete();
            return;
        }
    } 
}
