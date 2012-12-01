package edu.vanderbilt.cs282.feisele;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.util.Map;
import java.util.Arrays;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.BufferOverflowException;

import java.lang.Double;
import java.lang.Float;
import java.lang.Boolean;
import java.lang.Integer;
import java.lang.Short;
import java.lang.Long;


import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import android.util.Log;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import android.test.mock.MockContentResolver;

import edu.vu.isis.ammo.core.distributor.DistributorPolicy.Encoding;
import edu.vu.isis.ammo.provider.AmmoMockProvider01;
import edu.vu.isis.ammo.provider.AmmoMockProviderBase;
import edu.vu.isis.ammo.provider.AmmoMockProviderBase.Tables;
import edu.vu.isis.ammo.provider.AmmoMockSchema01;
import edu.vu.isis.ammo.provider.AmmoMockSchema01.AmmoTableSchema;
import edu.vu.isis.ammo.provider.AmmoMockSchema01.QuickTableSchema;
import edu.vu.isis.ammo.provider.AmmoMockSchema01.StartTableSchema;
import edu.vu.isis.ammo.provider.AmmoMockSchemaBase.AmmoTableSchemaBase;
import edu.vu.isis.ammo.testutils.TestUtils;



public class RequestSerializerHelper {
    private static final String TAG = "RequestSerializerHelper";

    private Context mContext;

    public RequestSerializerHelper() {
    }


    public static JSONObject jsonObjectFromBytes(byte[] jsonBytes)
    {
	// Create a string from the JSON bytes
        final String jsonString;
        try {
            jsonString = new String(jsonBytes, "US-ASCII");
        } catch (UnsupportedEncodingException ex) {
            Assert.fail("Unexpected error -- could not convert json blob to string");
            return null;
        }

        // Create a JSONObject to return
        Log.d(TAG, "encoded json=[ " + jsonString + " ]");
        JSONObject json = null;
        try {
            json = new JSONObject(jsonString);
        } catch (JSONException ex) {
            Assert.fail("Unexpected JSONException -- JSON string =   " + jsonString);
        }
        //Log.d(TAG, "jsonobject as string = [" + json.toString() + "]");

        return json;
    }


    // Private classes for containing knowledge about schema. These are intended
    // to keep the schema-specific knowledge localized so that if the schema
    // changes, we can change only these classes and shouldn't need to re-write
    // the tests themselves (or only minimally).

    public interface SchemaTable {
	public Uri getBaseUri();
	public String getTable();
        public ContentValues createContentValues();
        public ContentValues createContentValuesRandom();
        public Uri populateProviderWithData(AmmoMockProvider01 provider, ContentValues cv);
        public void compareJsonToCv(JSONObject json, ContentValues cv);
        public void compareJsonToUri(byte[] jsonBytes, AmmoMockProvider01 provider, Uri uri);
    }

    // =========================================================
    // Encapsulate knowledge of Table 1 ("Ammo") in the schema
    // =========================================================
    public static class SchemaTable1Data implements SchemaTable {
        public SchemaTable1Data() {}

        private final Uri mBaseUri = AmmoTableSchema.CONTENT_URI;
        private final String mTable =  Tables.AMMO_TBL;

        private final String schemaForeignKey = AmmoTableSchema.A_FOREIGN_KEY_REF;
        private final String schemaExEnum = AmmoTableSchema.AN_EXCLUSIVE_ENUMERATION;
        private final String schemaInEnum = AmmoTableSchema.AN_INCLUSIVE_ENUMERATION;

	public Uri getBaseUri() { return mBaseUri; }
	public String getTable() { return mTable; }

        public ContentValues createContentValues()
        {
            final ContentValues cv = new ContentValues();
            final int sampleForeignKey = 1;
            cv.put(schemaForeignKey, sampleForeignKey);
            cv.put(schemaExEnum, AmmoTableSchema.AN_EXCLUSIVE_ENUMERATION_HIGH);
            cv.put(schemaInEnum, AmmoTableSchema.AN_INCLUSIVE_ENUMERATION_APPLE);
            Log.d(TAG, "generated ContentValues: cv=[" + cv.toString() + "]");
            return cv;
        }

        public ContentValues createContentValuesRandom()
        {
            final ContentValues cv = new ContentValues();
            final int keyUpperBound = 100;
            int[] ExEnum = new int[] {AmmoTableSchema.AN_EXCLUSIVE_ENUMERATION_HIGH,
                                      AmmoTableSchema.AN_EXCLUSIVE_ENUMERATION_LOW,
                                      AmmoTableSchema.AN_EXCLUSIVE_ENUMERATION_MEDIUM};
            int[] InEnum = new int[] {AmmoTableSchema.AN_INCLUSIVE_ENUMERATION_APPLE,
                                      AmmoTableSchema.AN_INCLUSIVE_ENUMERATION_ORANGE,
                                      AmmoTableSchema.AN_INCLUSIVE_ENUMERATION_PEAR};
            cv.put(schemaForeignKey, TestUtils.randomInt(keyUpperBound));
            cv.put(schemaExEnum, ExEnum[TestUtils.randomInt(ExEnum.length)]);
            cv.put(schemaInEnum, InEnum[TestUtils.randomInt(InEnum.length)]);
            Log.d(TAG, "generated ContentValues: cv=[" + cv.toString() + "]");
            return cv;
        }

        public Uri populateProviderWithData(AmmoMockProvider01 provider, ContentValues cv)
        {
            SQLiteDatabase db = provider.getDatabase();
            long rowid = -1;
            Uri tupleUri = null;

            rowid = db.insert(Tables.AMMO_TBL, AmmoTableSchemaBase.A_FOREIGN_KEY_REF, cv);
            tupleUri = ContentUris.withAppendedId(AmmoTableSchema.CONTENT_URI, rowid);

            //Log.d(TAG, "rowId = " + String.valueOf(rowid));
            Log.d(TAG, "inserted uri = " + tupleUri.toString());
            return tupleUri;
        }

        // Compare json serialization to the cv which was written to the db originally
        public void compareJsonToCv(JSONObject json, ContentValues cv)
        {
            try {

                Assert.assertTrue(json.has(schemaForeignKey));
                Assert.assertTrue(json.has(schemaExEnum));
                Assert.assertTrue(json.has(schemaInEnum));

                JSONArray names = json.names();
                JSONArray values = json.toJSONArray(names);
                for(int i = 0 ; i < values.length(); i++) {
                    if(names.getString(i).equals(schemaForeignKey)) {
                        int actual = Integer.decode(values.getString(i)).intValue(); //values.getInt(i)
                        int expected = cv.getAsInteger(schemaForeignKey).intValue();
                        Log.d(TAG, "   json value='" + String.valueOf(actual)
                              + "'     cv value='"+ String.valueOf(expected)  + "'");
                        Assert.assertEquals(actual, expected);
                    }
                    if(names.getString(i).equals(schemaExEnum)) {
                        long actual = Long.decode(values.getString(i)).longValue();
                        long expected = cv.getAsLong(schemaExEnum).longValue();
                        Log.d(TAG, "   json value='" + actual + "'     cv value='"+ expected  + "'");
                        Assert.assertEquals(actual, expected);
                    }
                    if(names.getString(i).equals(schemaInEnum)) {
                        long actual = Long.decode(values.getString(i)).longValue();
                        long expected = cv.getAsLong(schemaInEnum).longValue();
                        Log.d(TAG, "   json value='" + actual + "'     cv value='"+ expected  + "'");
                        Assert.assertEquals(actual, expected);
                    }
                }

            } catch (JSONException ex) {
                Assert.fail("unexpected JSONException");
                return;
            }

        }

        // Compare json serialization to the provider content written from it
        public void compareJsonToUri(byte[] jsonBytes, AmmoMockProvider01 provider, Uri uri)
        {
            try {
		JSONObject json = RequestSerializerHelper.jsonObjectFromBytes(jsonBytes);

                Assert.assertTrue(json.has(schemaForeignKey));
                Assert.assertTrue(json.has(schemaExEnum));
                Assert.assertTrue(json.has(schemaInEnum));

                // Now query the provider and examine its contents, checking that they're
                // the same as the original JSON.
                final String[] projection = null;
                final String selection = null;
                final String[] selectArgs = null;
                final String orderBy = null;
                final Cursor cursor = provider.query(uri, projection, selection, selectArgs, orderBy);

                // The query should have succeeded
                Assert.assertNotNull("Query into provider failed", cursor);

                // There should be only one entry
                Assert.assertEquals("Unexpected number of rows in cursor", 1, cursor.getCount());

                // Row should be accessible with a cursor
                Assert.assertTrue("Row not accessible with cursor", (cursor.moveToFirst()));

                // Examine the provider content in detail, making sure it contains what we expect
                // (i.e. the contents of the original JSON)
                JSONArray names = json.names();
                JSONArray values = json.toJSONArray(names);
                for(int i = 0 ; i < values.length(); i++) {
                    if(names.getString(i).equals(schemaForeignKey)) {
                        int expected = Integer.decode(values.getString(i)).intValue(); //values.getInt(i);
                        int actual = cursor.getInt(cursor.getColumnIndex(schemaForeignKey));
                        Log.d(TAG, "   json value='" + expected + "'     db value='" + actual + "'");
                        Assert.assertEquals(actual, expected);
                    }
                    if(names.getString(i).equals(schemaExEnum)) {
                        int expected = Integer.decode(values.getString(i)).intValue();  //values.getInt(i);
                        int actual = cursor.getInt(cursor.getColumnIndex(schemaExEnum));
                        Log.d(TAG, "   json value='" + expected + "'     db value='" + actual + "'");
                        Assert.assertEquals(actual, expected);
                    }
                    if(names.getString(i).equals(schemaInEnum)) {
                        int expected = Integer.decode(values.getString(i)).intValue();  //values.getInt(i);
                        int actual = cursor.getInt(cursor.getColumnIndex(schemaInEnum));
                        Log.d(TAG, "   json value='" + expected + "'     db value='" + actual + "'");
                        Assert.assertEquals(actual, expected);
                    }

                }

                // Close cursor when finished
                cursor.close();
            } catch (JSONException e) {
                e.printStackTrace();
                Assert.fail("unexpected JSONException");
                return;
            }
        }
    }

    // =========================================================
    // Encapsulate knowledge of Table 2 ("Quick") in the schema
    // =========================================================
    public static class SchemaTable2Data implements SchemaTable {
        public SchemaTable2Data() {}

        private final Uri mBaseUri = QuickTableSchema.CONTENT_URI;
        private final String mTable =  Tables.QUICK_TBL;

        private final String schemaShortInt = QuickTableSchema.A_SHORT_INTEGER;
        private final String schemaLongInt = QuickTableSchema.A_LONG_INTEGER;
        private final String schemaInt = QuickTableSchema.AN_INTEGER;
        private final String schemaBool = QuickTableSchema.A_BOOLEAN;
        private final String schemaTime = QuickTableSchema.A_ABSOLUTE_TIME;

	public Uri getBaseUri() { return mBaseUri; }
	public String getTable() { return mTable; }

        public ContentValues createContentValues()
        {
            final ContentValues cv = new ContentValues();
            cv.put(schemaShortInt, TestUtils.TEST_SHORT_INTEGER);
            cv.put(schemaInt, TestUtils.TEST_INTEGER);
            cv.put(schemaLongInt, TestUtils.TEST_LONG_INTEGER);
            cv.put(schemaBool, TestUtils.TEST_BOOLEAN);
            //cv.put(schemaTime, ???);
            Log.d(TAG, "generated ContentValues: cv=[" + cv.toString() + "]");
            return cv;
        }

        public ContentValues createContentValuesRandom()
        {
            final ContentValues cv = new ContentValues();
            cv.put(schemaShortInt, TestUtils.randomShort());
            cv.put(schemaInt, TestUtils.randomInt());
            cv.put(schemaLongInt, TestUtils.randomLong());
            cv.put(schemaBool, TestUtils.randomBoolean());
            //cv.put(schemaTime, ???);
            Log.d(TAG, "generated ContentValues: cv=[" + cv.toString() + "]");
            return cv;
        }

        public Uri populateProviderWithData(AmmoMockProvider01 provider, ContentValues cv)
        {
            SQLiteDatabase db = provider.getDatabase();
            long rowid = -1;
            Uri tupleUri = null;

            rowid = db.insert(Tables.QUICK_TBL, null, cv);
            tupleUri = ContentUris.withAppendedId(QuickTableSchema.CONTENT_URI, rowid);

            //Log.d(TAG, "rowId = " + String.valueOf(rowid));
            Log.d(TAG, "inserted uri = " + tupleUri.toString());
            return tupleUri;
        }



        // Compare json serialization to the cv which was written to the db originally
        public void compareJsonToCv(JSONObject json, ContentValues cv)
        {
            try {
                Assert.assertTrue(json.has(schemaShortInt));
                Assert.assertTrue(json.has(schemaInt));
                Assert.assertTrue(json.has(schemaLongInt));
                // Assert.assertTrue(json.has(schemaBool));
                // Assert.assertTrue(json.has(schemaTime));

                JSONArray names = json.names();
                JSONArray values = json.toJSONArray(names);
                for(int i = 0 ; i < values.length(); i++) {
                    if(names.getString(i).equals(schemaShortInt)) {
                        int actual =  Short.decode(values.getString(i)).shortValue();
                        int expected = cv.getAsInteger(schemaShortInt).intValue();
                        Log.d(TAG, "   json value='" + String.valueOf(actual)
                              + "'     cv value='"+ String.valueOf(expected)  + "'");
                        Assert.assertEquals(actual, expected);
                    }
                    if(names.getString(i).equals(schemaLongInt)) {
                        long actual = Long.decode(values.getString(i)).longValue();
                        long expected = cv.getAsLong(schemaLongInt).longValue();
                        Log.d(TAG, "   json value='" + actual + "'     cv value='"+ expected  + "'");
                        Assert.assertEquals(actual, expected);
                    }
                    if(names.getString(i).equals(schemaInt)) {
                        int actual = Integer.decode(values.getString(i)).intValue();
                        int expected = cv.getAsInteger(schemaInt).intValue();
                        Log.d(TAG, "   json value='" + actual + "'     cv value='"+ expected  + "'");
                        Assert.assertEquals(actual, expected);
                    }
                    if(names.getString(i).equals(schemaBool)) {
                        boolean actual = (values.getInt(i) == 1); //Boolean.parseBoolean(values.getString(i))
                        boolean expected = cv.getAsBoolean(schemaBool).booleanValue();
                        Log.d(TAG, "   json value='" + actual + "'     cv value='"+ expected  + "'");
                        Assert.assertEquals(actual, expected);
                    }
                    if(names.getString(i).equals(schemaTime)) {
                        // TODO
                    }
                }
            } catch (JSONException ex) {
                Assert.fail("unexpected JSONException");
                return;
            }
        }

        // Compare json serialization to the provider content written from it
        public void compareJsonToUri(byte[] jsonBytes, AmmoMockProvider01 provider, Uri uri)
        {
            try {
		JSONObject json = RequestSerializerHelper.jsonObjectFromBytes(jsonBytes);

                Assert.assertTrue(json.has(schemaShortInt));
                Assert.assertTrue(json.has(schemaInt));
                Assert.assertTrue(json.has(schemaLongInt));
                Assert.assertTrue(json.has(schemaBool));

                // Now query the provider and examine its contents, checking that they're
                // the same as the original JSON.
                final String[] projection = null;
                final String selection = null;
                final String[] selectArgs = null;
                final String orderBy = null;
                final Cursor cursor = provider.query(uri, projection, selection, selectArgs, orderBy);

                // The query should have succeeded
                Assert.assertNotNull("Query into provider failed", cursor);

                // There should be only one entry
                Assert.assertEquals("Unexpected number of rows in cursor", 1, cursor.getCount());

                // Row should be accessible with a cursor
                Assert.assertTrue("Row not accessible with cursor", (cursor.moveToFirst()));

                // Examine the provider content in detail, making sure it contains what we expect
                // (i.e. the contents of the original JSON)
                JSONArray names = json.names();
                JSONArray values = json.toJSONArray(names);
                for(int i = 0 ; i < values.length(); i++) {
                    if(names.getString(i).equals(schemaShortInt)) {
                        int actual = Short.decode(values.getString(i)).shortValue(); //values.getInt(i);
                        int expected = cursor.getInt(cursor.getColumnIndex(schemaShortInt));
                        Log.d(TAG, "   json value='" + actual + "'     db value='"+ expected + "'");
                        Assert.assertEquals(actual, expected);
                    }
                    if(names.getString(i).equals(schemaLongInt)) {
                        long actual = Long.decode(values.getString(i)).longValue(); //values.getLong(i);
                        long expected = cursor.getLong(cursor.getColumnIndex(schemaLongInt));
                        Log.d(TAG, "   json value='" + actual + "'     db value='"+ expected  + "'");
                        Assert.assertEquals(actual, expected);
                    }
                    if(names.getString(i).equals(schemaInt)) {
                        int actual = Integer.decode(values.getString(i)).intValue(); //values.getInt(i);
                        int expected = cursor.getInt(cursor.getColumnIndex(schemaInt));
                        Log.d(TAG, "   json value='" + actual + "'     db value='"+ expected  + "'");
                        Assert.assertEquals(actual, expected);
                    }
                    if(names.getString(i).equals(schemaBool)) {
			// Handle confusion of boolean value being "1"/"true, "0"/"false"
			boolean actual;
			try {
			    actual = (values.getInt(i) == 1);
			} catch (JSONException e) {
			    actual = Boolean.parseBoolean(values.getString(i));
			}
                        boolean expected = (cursor.getInt(cursor.getColumnIndex(schemaBool)) == 1);
                        Log.d(TAG, "   json value='" + actual + "'     db value='"+ expected  + "'");
                        Assert.assertEquals(actual, expected);
                    }
                    if(names.getString(i).equals(schemaTime)) {
                        // TODO
                    }
                }

                // Close cursor when finished
                cursor.close();
            } catch (JSONException e) {
                e.printStackTrace();
                Assert.fail("unexpected JSONException");
                return;
            }
        }

    }

    // =========================================================
    // Encapsulate knowledge of Table 3 ("Start") in the schema
    // =========================================================
    public static class SchemaTable3Data implements SchemaTable {
        public SchemaTable3Data() {}

        private final Uri mBaseUri = StartTableSchema.CONTENT_URI;
        private final String mTable =  Tables.START_TBL;

        private final String schemaReal = StartTableSchema.A_REAL;
        private final String schemaGuid = StartTableSchema.A_GLOBALLY_UNIQUE_IDENTIFIER;
        private final String schemaText = StartTableSchema.SOME_ARBITRARY_TEXT;
        private final String schemaFile = StartTableSchema.A_FILE;
        private final String schemaBlob = StartTableSchema.A_BLOB;

	public Uri getBaseUri() { return mBaseUri; }
	public String getTable() { return mTable; }

        public ContentValues createContentValues()
        {
            final ContentValues cv = new ContentValues();
            cv.put(schemaReal, TestUtils.TEST_DOUBLE);
            cv.put(schemaGuid, TestUtils.TEST_GUID_STR);
            cv.put(schemaText, TestUtils.TEST_FIXED_STRING);
            //cv.put(StartTableSchema.A_BLOB, ???);
            //cv.put(StartTableSchema.A_FILE, ???);

            Log.d(TAG, "generated ContentValues: cv=[" + cv.toString() + "]");
            return cv;
        }

	public ContentValues createContentValuesWithFile()
        {
            ContentValues cv = createContentValues();
            cv.put(StartTableSchema.A_FILE, "/tmp/foo.jpg");

            Log.d(TAG, "generated ContentValues: cv=[" + cv.toString() + "]");
            return cv;
        }
	
	public ContentValues createContentValuesWithBlobSmall()
        {
            ContentValues cv = createContentValues();
            cv.put(StartTableSchema.A_BLOB, TestUtils.TEST_SMALL_BLOB);
            //cv.put(StartTableSchema.A_BLOB, TestUtils.TEST_TINY_BLOB);

            Log.d(TAG, "generated ContentValues: cv=[" + cv.toString() + "]");
            return cv;
        }
	
	public ContentValues createContentValuesWithBlobLarge()
        {
            ContentValues cv = createContentValues();
	    cv.put(StartTableSchema.A_BLOB, TestUtils.TEST_LARGE_BLOB);

            Log.d(TAG, "generated ContentValues with large blob (do not print)");
            return cv;
        }

	public ContentValues createContentValuesWithBlobRandomSmall()
        {
            ContentValues cv = createContentValuesRandom();
	    
	    // Attach blob of 'small' size
	    cv.put(StartTableSchema.A_BLOB, 
		   TestUtils.randomBytes(TestUtils.randomInt(TestUtils.SMALL_BLOB_SIZE)));

            Log.d(TAG, "generated ContentValues with blob (small)");

            return cv;
        }

	public ContentValues createContentValuesWithBlobRandomLarge()
        {
            ContentValues cv = createContentValues();
	    
	    // Attach blob of 'large' size
	    cv.put(StartTableSchema.A_BLOB, 
		   TestUtils.randomBytes(TestUtils.randomInt(TestUtils.LARGE_BLOB_SIZE)));

            Log.d(TAG, "generated ContentValues with blob (large)");

            return cv;
        }

        public ContentValues createContentValuesRandom()
        {
            final ContentValues cv = new ContentValues();
            cv.put(schemaReal, TestUtils.randomDouble());
            cv.put(schemaGuid, TestUtils.randomGuidAsString());
            final int max_text_size = 50;
            int text_size = TestUtils.randomInt(max_text_size);
            if (text_size == 0) { text_size = 1; }
            cv.put(schemaText, TestUtils.randomText(text_size));
            //cv.put(StartTableSchema.A_BLOB, ???);
            //cv.put(StartTableSchema.A_FILE, ???);

            Log.d(TAG, "generated ContentValues: cv=[" + cv.toString() + "]");
            return cv;
        }

        public Uri populateProviderWithData(AmmoMockProvider01 provider, ContentValues cv)
        {
            SQLiteDatabase db = provider.getDatabase();
            long rowid = -1;
            Uri tupleUri = null;

            rowid = db.insert(Tables.START_TBL, null, cv);
            tupleUri = ContentUris.withAppendedId(StartTableSchema.CONTENT_URI, rowid);

            //Log.d(TAG, "rowId = " + String.valueOf(rowid));
            Log.d(TAG, "inserted uri = " + tupleUri.toString());
            return tupleUri;
        }

	// Convert 4-byte bytearray to integer
	private int bytesToInt( byte[] b)
	{
	    ByteBuffer bb = ByteBuffer.wrap(b);
	    IntBuffer ib = bb.asIntBuffer();
	    int i0 = ib.get(0);
	    return i0;
	}

	// Compare serialized bytes to the cv which was written to the db originally
	/*
	  Notes on order of bytes:
	  - json
	  - 0x0
	  - field name (e.g.  "a_blob", "a_file")
	  - 0x0
	  - 4-byte size (of blob/file)
	  - blob/file data
	  - 4-byte size (again)
	*/
        public void compareBytesToCv(byte[] serialized, ContentValues cv)
        {
	    //Log.d(TAG, "  compareBytesToCv: serialized=[" + new String(serialized) + "]");
	    //Log.d(TAG, "  compareBytesToCv: cv=[" + cv.toString() + "]");

	    if (serialized == null) {
		Assert.fail("unexpected null serialization");
	    }

	    // Step 1 - strip off the JSON header, make JSONObject json
	    // 
	    // find first occurrence of '}', which ends JSON header.
	    // When we find it, break, and keep the array index.
	    int i=0;
	    ByteBuffer jsonBuf = null;
	    for ( i=0; i < serialized.length; i++) {
		if (serialized[i] == '}') {
		    break;
		}
	    }
	    int endOfJsonIndex = i;
	    if (endOfJsonIndex > 0) {
		jsonBuf = ByteBuffer.allocate(endOfJsonIndex+1);
		for (int j=0; j < endOfJsonIndex + 1; j++) {
		    jsonBuf.put(serialized[j]);
		}
	    } else {
		// ???
	    }
	    JSONObject json = jsonObjectFromBytes(jsonBuf.array());
	    Log.d(TAG, "  compareBytesToCv: json string=[" + json.toString() + "]");

	    // Compare the JSON header to corresponding values in the CV
	    ContentValues cvNoBlob = new ContentValues(cv);
	    cvNoBlob.remove(schemaBlob);
	    compareJsonToCv(json, cvNoBlob);


	    // Step 2 - get the data blob following the json header
	    // 
	    // after the JSON header, next byte should be a null
	    int pos = endOfJsonIndex;
	    //Log.d(TAG, "  pos=" + pos);
	    pos++;
	    //Log.d(TAG, "  pos=" + pos);
	    Assert.assertEquals(0x0, serialized[pos]);

	    // Next is the field name (e.g. schemaBlob)
	    pos++;
	    //Log.d(TAG, "  pos=" + pos );
	    String fieldName = new String(serialized, pos, schemaBlob.length() );
	    Log.d(TAG, "  compareBytesToCv: fieldname=[" + fieldName + "]");
	    Assert.assertEquals(fieldName, schemaBlob);

	    // Next is another null character
	    pos = pos + schemaBlob.length() ;
	    Assert.assertEquals(0x0, serialized[pos]);
	    //Log.d(TAG, "  pos=" + pos);
	    
	    // Next is a 4-byte size (i.e. size of the blob data)
	    pos++;
	    //Log.d(TAG, "  pos=" + pos);
	    ByteBuffer sizeBuf1 = ByteBuffer.allocate(4);
	    for (int j=0; j < 4; j++) {
		sizeBuf1.put(serialized[pos]);
		pos++;
	    }
	    int blobSize1 = bytesToInt(sizeBuf1.array());
	    Log.d(TAG, "  size1=[" + blobSize1 + "]");
	    

	    // Next is the blob data itself
	    /*
	    ByteBuffer bw = ByteBuffer.wrap(serialized, pos, serialized.length-pos);
	    byte[] blobArray = new byte[blobSize1];
	    Log.d(TAG, " blob = [" + Arrays.toString(bw.get(blobArray, 0, blobSize1).array())  + "]");
	    */
	    Log.d(TAG, "  pos=" + pos);
	    ByteBuffer blobBuf = ByteBuffer.allocate(blobSize1);
	    try {
		int count=0;
		for (int j=0; j < blobSize1; j++) {
		    //blobBuf.put(serialized[pos + j]);
		    blobBuf.put(serialized[pos]);
		    pos++;
		    count++;
		}
		Log.d(TAG, "  read count = " + count);
		//Log.d(TAG, "  pos=" + pos);
	    } catch (BufferOverflowException e) {
		e.printStackTrace();
		Assert.fail("unexpected buffer overflow (blob data)");
	    }

	    byte[] cvBytes = cv.getAsByteArray(schemaBlob);
	    Assert.assertTrue(Arrays.equals(cvBytes, blobBuf.array()));


	    // Finally the 4-byte size is repeated, WITH a possible
	    // flag in the first byte, for which we must check.
	    ByteBuffer sizeBuf2 = ByteBuffer.allocate(4);

	    int howMany = 4;
	    if (serialized[pos] == RequestSerializer.BLOB_MARKER_FIELD) {
		Log.d(TAG, "  found blob marker field (" + RequestSerializer.BLOB_MARKER_FIELD + ")");
		sizeBuf2.put((byte)0x0);
		pos++;
		howMany = 3;
	    } 
	    for (int j=0; j < howMany; j++) {
		sizeBuf2.put(serialized[pos]);
		pos++;
	    }
	    //Log.d(TAG, "  pos=" + pos);
	    int blobSize2 = bytesToInt(sizeBuf2.array());
	    Log.d(TAG, "  size2=[" + blobSize2 + "]");
	    
	    Assert.assertEquals(blobSize1, blobSize2);
	}

        // Compare json serialization to the cv which was written to the db originally
        public void compareJsonToCv(JSONObject json, ContentValues cv)
        {
            final double error_bar = 0.00001;
            try {

                Assert.assertTrue(json.has(schemaReal));
                Assert.assertTrue(json.has(schemaGuid));
                Assert.assertTrue(json.has(schemaText));
                //Assert.assertTrue(json.has(schemaBlob));
                //Assert.assertTrue(json.has(schemaFile));

                JSONArray names = json.names();
                JSONArray values = json.toJSONArray(names);
                for(int i = 0 ; i < values.length(); i++) {
                    if(names.getString(i).equals(schemaReal)) {
                        double actual = Double.parseDouble(values.getString(i)); //values.getDouble(i);
                        double expected = cv.getAsDouble(schemaReal).doubleValue();
                        Log.d(TAG, "   json value='" + String.valueOf(actual)
                              + "'     cv value='"+ String.valueOf(expected)  + "'");
                        Assert.assertEquals(actual, expected, error_bar);
                    }
                    if(names.getString(i).equals(schemaText)) {
                        String actual = values.getString(i);
                        String expected = cv.getAsString(schemaText);
                        Log.d(TAG, "   json value='" + actual + "'     cv value='"+ expected  + "'");
                        Assert.assertEquals(actual, expected);
                    }
                    if(names.getString(i).equals(schemaGuid)) {
                        String actual = values.getString(i);
                        String expected = cv.getAsString(schemaGuid);
                        Log.d(TAG, "   json value='" + actual + "'     cv value='"+ expected  + "'");
                        Assert.assertEquals(actual, expected);
                    }
                    if(names.getString(i).equals(schemaFile)) {
                        // TODO
                    }
                    if(names.getString(i).equals(schemaBlob)) {
                        // TODO
                    }
                }
            } catch (JSONException ex) {
                Assert.fail("unexpected JSONException");
                return;
            }
        }

        // Compare json serialization to the provider content written from it
        public void compareJsonToUri(byte[] jsonBytes, AmmoMockProvider01 provider, Uri uri)
        {
            final double error_bar = 0.00001;
            try {
		JSONObject json = RequestSerializerHelper.jsonObjectFromBytes(jsonBytes);

                Assert.assertTrue(json.has(schemaReal));
                Assert.assertTrue(json.has(schemaGuid));
                Assert.assertTrue(json.has(schemaText));
                //Assert.assertTrue(json.has(schemaBlob));
                //Assert.assertTrue(json.has(schemaFile));

                // Now query the provider and examine its contents, checking that they're
                // the same as the original JSON.
                final String[] projection = null;
                final String selection = null;
                final String[] selectArgs = null;
                final String orderBy = null;
                final Cursor cursor = provider.query(uri, projection, selection, selectArgs, orderBy);


                // The query should have succeeded
                Assert.assertNotNull("Query into provider failed", cursor);

                // There should be only one entry
                Assert.assertEquals("Unexpected number of rows in cursor", 1, cursor.getCount());

                // Row should be accessible with a cursor
                Assert.assertTrue("Row not accessible with cursor", (cursor.moveToFirst()));

                // Examine the provider content in detail, making sure it contains what we expect
                // (i.e. the contents of the original JSON)
                JSONArray names = json.names();
                JSONArray values = json.toJSONArray(names);
                for(int i = 0 ; i < values.length(); i++) {
                    if(names.getString(i).equals(schemaReal)) {
                        double actual = Double.parseDouble(values.getString(i)); //values.getDouble(i);
                        double expected = cursor.getDouble(cursor.getColumnIndex(schemaReal));
                        Log.d(TAG, "   json value='" + actual + "'     db value='"+ expected + "'");
                        Assert.assertEquals(actual, expected, error_bar);
                    }
                    if(names.getString(i).equals(schemaText)) {
                        String actual = values.getString(i);
                        String expected = cursor.getString(cursor.getColumnIndex(schemaText));
                        Log.d(TAG, "   json value='" + actual + "'     db value='"+ expected  + "'");
                        Assert.assertEquals(actual, expected);
                    }
                    if(names.getString(i).equals(schemaGuid)) {
                        String actual = values.getString(i);
                        String expected = cursor.getString(cursor.getColumnIndex(schemaGuid));
                        Log.d(TAG, "   json value='" + actual + "'     db value='"+ expected  + "'");
                        Assert.assertEquals(actual, expected);
                    }
                    if(names.getString(i).equals(schemaFile)) {
                        // TODO
                    }
                    if(names.getString(i).equals(schemaBlob)) {
                        // TODO
                    }
                }

                // Close cursor when finished
                cursor.close();
            } catch (JSONException e) {
                e.printStackTrace();
                Assert.fail("unexpected JSONException");
                return;
            }
        }
    }

}
