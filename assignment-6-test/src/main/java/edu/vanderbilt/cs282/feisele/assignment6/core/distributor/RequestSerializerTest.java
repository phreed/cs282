package edu.vanderbilt.cs282.feisele;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
//import java.io.ByteArrayInputStream;
//import java.io.ObjectInputStream;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.util.Log;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Parcel;
import android.test.AndroidTestCase;
import android.test.mock.MockContentResolver;
import ch.qos.logback.classic.Level;
import edu.vu.isis.ammo.api.type.Payload;
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

import edu.vu.isis.ammo.core.distributor.RequestSerializerHelper;
import edu.vu.isis.ammo.core.distributor.RequestSerializerHelper.SchemaTable1Data;
import edu.vu.isis.ammo.core.distributor.RequestSerializerHelper.SchemaTable2Data;
import edu.vu.isis.ammo.core.distributor.RequestSerializerHelper.SchemaTable3Data;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class edu.vu.isis.ammo.core.ui.RequestSerializerTest \
 * edu.vu.isis.ammo.core.tests/android.test.InstrumentationTestRunner
 */


public class RequestSerializerTest extends AndroidTestCase {
    private static final String TAG = "RequestSerializerTest";

    private static final Logger logger = LoggerFactory.getLogger("test.request.serial");

    private Context mContext;

    public RequestSerializerTest() {
        //super("edu.vu.isis.ammo.core.distributor", RequestSerializer.class);
    }

    public RequestSerializerTest( String testName )
    {
        //super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( RequestSerializerTest.class );
    }

    protected void setUp() throws Exception
    {
        mContext = getContext();
    }

    protected void tearDown() throws Exception
    {
        mContext = null;
    }

    // =========================================================
    // utility: given a URI, serialize what it contains into terse
    // encoding and return
    // =========================================================
    private byte[] utilSerializeTerseFromProvider(MockContentResolver cr, Uri uri)
    {
        final Encoding enc = Encoding.newInstance(Encoding.Type.TERSE);

        // Serialize the provider content into JSON bytes
        final byte[] terseEncoded;
        try {
            terseEncoded = RequestSerializer.serializeFromProvider(cr, uri, enc);
        } catch (NonConformingAmmoContentProvider ex) {
            fail("Should not have thrown NonConformingAmmoContentProvider in this case");
            return null;
        } catch (TupleNotFoundException ex) {
            fail("Should not have thrown TupleNotFoundException in this case");
            return null;
        } catch (IOException ex) {
            fail("failure of the test itself");
            return null;
        }
	return terseEncoded;
    }


    // =========================================================
    // utility: given a URI, serialize what it contains into JSON
    // (plus attached binary data, if any) and return a byte array
    // =========================================================
    private byte[] utilSerializeJsonFromProvider_withBlob(MockContentResolver cr, Uri uri)
    {
        final Encoding enc = Encoding.newInstance(Encoding.Type.JSON);

        // Serialize the provider content into bytes (JSON + blob data)
        final byte[] serialized;
        try {
            serialized = RequestSerializer.serializeFromProvider(cr, uri, enc);
        } catch (NonConformingAmmoContentProvider ex) {
            fail("Should not have thrown NonConformingAmmoContentProvider in this case");
            return null;
        } catch (TupleNotFoundException ex) {
            fail("Should not have thrown TupleNotFoundException in this case");
            return null;
        } catch (IOException ex) {
            fail("failure of the test itself");
            return null;
        }

	if (serialized != null) {
	    Log.d(TAG, "serialized data size = [" + serialized.length + "]");
	} else {
	    Log.d(TAG, "serialized data is NULL");
	}
        return serialized;
    }

    // =========================================================
    // utility: given a URI, serialize what it contains into JSON
    // and return a JSONObject
    // =========================================================
    private JSONObject utilSerializeJsonFromProvider(MockContentResolver cr, Uri uri)
    {
        final Encoding enc = Encoding.newInstance(Encoding.Type.JSON);

        // Serialize the provider content into JSON bytes
        final byte[] jsonBytes = utilSerializeJsonFromProvider_withBlob(cr, uri);
	return RequestSerializerHelper.jsonObjectFromBytes(jsonBytes);	
    }

    private MockContentResolver utilGetContentResolver()
    {
        final MockContentResolver mcr = new MockContentResolver();
        mcr.addProvider(AmmoMockSchema01.AUTHORITY,
                        AmmoMockProvider01.getInstance(getContext()));

        return mcr;
    }

    private AmmoMockProvider01 utilMakeTestProvider01(Context context)
    {
        return AmmoMockProvider01.getInstance(context);
    }

    // =========================================================
    // newInstance() with no parameters
    // =========================================================
    public void testNewInstanceNoArgs()
    {
        RequestSerializer rs = RequestSerializer.newInstance();
        assertNotNull(rs);
    }

    // =========================================================
    // newInstance() with parameters
    // =========================================================
    /*
      public void testNewInstanceArgs()
      {
      Uri uri = null;
      Provider p1 = new Provider(uri);

      Parcel par = utilCreatePayloadParcel();
      Payload  p2 = new Payload(par);

      // Provider.Type.URI, Payload.Type.CV
      RequestSerializer rs = RequestSerializer.newInstance(p1,p2);
      assertNotNull(rs);
      }
    */


    /**
     * Serialize from ContentProvider (JSON encoding) :
     * Simple case of known constant values on Table 1 ("Ammo") in schema.
     *
     * This test
     * <ol>
     * <li>constructs a mock content provider,
     * <li>loads some data into the content provider,(imitating the application)
     * <li>serializes that data into a json string
     * <li>checks the json string to verify it's correct
     */
    public void testSerializeFromProviderJson_table1_basic() {
        AmmoMockProvider01 provider = null;
        try {
            provider = utilMakeTestProvider01(mContext);
            assertNotNull(provider);
            final MockContentResolver cr = new MockContentResolver();
            cr.addProvider(AmmoMockSchema01.AUTHORITY, provider);

            SchemaTable1Data d = new SchemaTable1Data();

            // Serialize values from the db
            ContentValues cv = d.createContentValues();
            Uri uri = d.populateProviderWithData(provider, cv);
            JSONObject json = utilSerializeJsonFromProvider(cr, uri);
            if (json == null) {
                fail("unexpected JSON error");
            }
            d.compareJsonToCv(json, cv);
        } finally {
            if (provider != null) provider.release();
        }
    }

    /**
     * Serialize from ContentProvider (JSON encoding) :
     * iterated random trials on Table 1 ("Ammo") in schema.
     *
     */
    public void testSerializeFromProviderJson_table1_random() {
        final int NUM_ITERATIONS = 10;
        AmmoMockProvider01 provider = null;
        try {
            provider = utilMakeTestProvider01(mContext);
            assertNotNull(provider);
            final MockContentResolver cr = new MockContentResolver();
            cr.addProvider(AmmoMockSchema01.AUTHORITY, provider);

            SchemaTable1Data d = new SchemaTable1Data();

            // Repeatedly serialize random values from the db
            for (int i=0; i < NUM_ITERATIONS; i++) {
                ContentValues cv = d.createContentValuesRandom();
                Uri uri = d.populateProviderWithData(provider, cv);
                JSONObject json = utilSerializeJsonFromProvider(cr, uri);
                if (json == null) {
                    fail("unexpected JSON error");
                }

                d.compareJsonToCv(json, cv);
            }
        } finally {
            if (provider != null) provider.release();
        }
    }

    /**
     * Serialize from ContentProvider (JSON encoding) :
     * Simple case of known constant values on Table 2 ("Quick") in schema.
     *
     * This test
     * <ol>
     * <li>constructs a mock content provider,
     * <li>loads some data into the content provider,(imitating the application)
     * <li>serializes that data into a json string
     * <li>checks the json string to verify it's correct
     */
    public void testSerializeFromProviderJson_table2_basic() {
        AmmoMockProvider01 provider = null;
        try {
            provider = utilMakeTestProvider01(mContext);
            assertNotNull(provider);
            final MockContentResolver cr = new MockContentResolver();
            cr.addProvider(AmmoMockSchema01.AUTHORITY, provider);

            SchemaTable2Data d = new SchemaTable2Data();

            // Repeatedly serialize random values from the db
            ContentValues cv = d.createContentValues();
            Uri uri = d.populateProviderWithData(provider, cv);
            JSONObject json = utilSerializeJsonFromProvider(cr, uri);
            if (json == null) {
                fail("unexpected JSON error");
            }

            d.compareJsonToCv(json, cv);
        } finally {
            if (provider != null) provider.release();
        }
    }


    /**
     * Serialize from ContentProvider (JSON encoding) :
     * iterated random trials on Table 2 ("Quick") in schema.
     *
     */
    public void testSerializeFromProviderJson_table2_random() {
        final int NUM_ITERATIONS = 10;
        AmmoMockProvider01 provider = null;
        try {
            provider = utilMakeTestProvider01(mContext);
            assertNotNull(provider);
            final MockContentResolver cr = new MockContentResolver();
            cr.addProvider(AmmoMockSchema01.AUTHORITY, provider);

            SchemaTable2Data d = new SchemaTable2Data();

            // Repeatedly serialize random values from the db
            for (int i=0; i < NUM_ITERATIONS; i++) {
                ContentValues cv = d.createContentValuesRandom();
                Uri uri = d.populateProviderWithData(provider, cv);
                JSONObject json = utilSerializeJsonFromProvider(cr, uri);
                if (json == null) {
                    fail("unexpected JSON error");
                }

                d.compareJsonToCv(json, cv);
            }
        } finally {
            if (provider != null) provider.release();
        }
    }

    /**
     * Serialize from ContentProvider (JSON encoding)
     * Simple case of known constant values on Table 3 ("Start") in schema.
     *
     * This test
     * <ol>
     * <li>constructs a mock content provider,
     * <li>loads some data into the content provider,(imitating the application)
     * <li>serializes that data into a json string
     * <li>checks the json string to verify it's correct
     */
    public void testSerializeFromProviderJson_table3_basic() {
        AmmoMockProvider01 provider = null;
        try {
            provider = utilMakeTestProvider01(mContext);
            assertNotNull(provider);
            final MockContentResolver cr = new MockContentResolver();
            cr.addProvider(AmmoMockSchema01.AUTHORITY, provider);

            SchemaTable3Data d = new SchemaTable3Data();

            // Repeatedly serialize random values from the db
            ContentValues cv = d.createContentValues();
            Uri uri = d.populateProviderWithData(provider, cv);
            JSONObject json = utilSerializeJsonFromProvider(cr, uri);
            if (json == null) {
                fail("unexpected JSON error");
            }

            d.compareJsonToCv(json, cv);
        } finally {
            if (provider != null) provider.release();
        }
    }

    /**
     * Serialize from ContentProvider (JSON encoding)
     * iterated random trials on Table 3 ("Start") in schema.
     *
     */
    public void testSerializeFromProviderJson_table3_random() {
        final int NUM_ITERATIONS = 10;
        AmmoMockProvider01 provider = null;
        try {
            provider = utilMakeTestProvider01(mContext);
            assertNotNull(provider);
            final MockContentResolver cr = new MockContentResolver();
            cr.addProvider(AmmoMockSchema01.AUTHORITY, provider);

            SchemaTable3Data d = new SchemaTable3Data();

            // Repeatedly serialize random values from the db
            for (int i=0; i < NUM_ITERATIONS; i++) {
                ContentValues cv = d.createContentValuesRandom();
                Uri uri = d.populateProviderWithData(provider, cv);
                JSONObject json = utilSerializeJsonFromProvider(cr, uri);
                if (json == null) {
                    fail("unexpected JSON error");
                }

                d.compareJsonToCv(json, cv);
            }
        } finally {
            if (provider != null) provider.release();
        }
    }


    /**
     * Serialize to ContentProvider (JSON encoding) for Table 1 ("Ammo").
     * Basic use case of serializing a JSON-encoded message into a provider table.
     */
    public void testDeserializeToProviderJson_table1_basic()
    {
        // Mock provider and resolver
        AmmoMockProvider01 provider = null;
        final MockContentResolver cr = new MockContentResolver();

        try {
            provider = utilMakeTestProvider01(mContext);
            assertNotNull(provider);
            cr.addProvider(AmmoMockSchema01.AUTHORITY, provider);

            // Choose JSON encoding for this test
            final Encoding enc = Encoding.newInstance(Encoding.Type.JSON);

            // Object with "Table 1" knowledge
            SchemaTable1Data d = new SchemaTable1Data();

            ContentValues cv = d.createContentValues();
	    byte[] jsonBytes = TestUtils.createJsonAsBytes(cv);
            Uri uriIn = RequestSerializer.deserializeToProvider(mContext,
                                                                cr,
                                                                "dummy",
                                                                d.getBaseUri(),
                                                                enc,
                                                                jsonBytes);
            d.compareJsonToUri(jsonBytes, provider, uriIn);
        } finally {
            if (provider != null) provider.release();
        }
    }

    /**
     * Serialize to ContentProvider (JSON encoding) for Table 2 ("Quick").
     * Basic use case of serializing a JSON-encoded message into a provider table.
     */
    public void testDeserializeToProviderJson_table2_basic()
    {
        // Mock provider and resolver
        AmmoMockProvider01 provider = null;
        final MockContentResolver cr = new MockContentResolver();

        try {
            provider = utilMakeTestProvider01(mContext);
            assertNotNull(provider);
            cr.addProvider(AmmoMockSchema01.AUTHORITY, provider);

            // Choose JSON encoding for this test
            final Encoding enc = Encoding.newInstance(Encoding.Type.JSON);

            // Object with "Table 2" knowledge
            SchemaTable2Data d = new SchemaTable2Data();

            ContentValues cv = d.createContentValues();
	    byte[] jsonBytes = TestUtils.createJsonAsBytes(cv);
            Uri uriIn = RequestSerializer.deserializeToProvider(mContext,
                                                                cr,
                                                                "dummy",
                                                                d.getBaseUri(),
                                                                enc,
                                                                jsonBytes);
            d.compareJsonToUri(jsonBytes, provider, uriIn);
        } finally {
            if (provider != null) provider.release();
        }
    }

    /**
     * Serialize to ContentProvider (JSON encoding) for Table 3 ("Start").
     * Basic use case of serializing a JSON-encoded message into a provider table.
     */
    public void testDeserializeToProviderJson_table3_basic()
    {
        // Mock provider and resolver
        AmmoMockProvider01 provider = null;
        final MockContentResolver cr = new MockContentResolver();

        try {
            provider = utilMakeTestProvider01(mContext);
            assertNotNull(provider);
            cr.addProvider(AmmoMockSchema01.AUTHORITY, provider);

            // Choose JSON encoding for this test
            final Encoding enc = Encoding.newInstance(Encoding.Type.JSON);

            // Object with "Table 2" knowledge
            SchemaTable3Data d = new SchemaTable3Data();

            ContentValues cv = d.createContentValues();
	    byte[] jsonBytes = TestUtils.createJsonAsBytes(cv);
            Uri uriIn = RequestSerializer.deserializeToProvider(mContext,
                                                                cr,
                                                                "dummy",
                                                                d.getBaseUri(),
                                                                enc,
                                                                jsonBytes);
	    assertNotNull(uriIn);
            d.compareJsonToUri(jsonBytes, provider, uriIn);
        } finally {
            if (provider != null) provider.release();
        }
    }


    /**
     * Serialize to ContentProvider (JSON encoding) for Table 1 ("Ammo").
     * Iterated with random data.
     */
    public void testDeserializeToProviderJson_table1_random()
    {
        final int NUM_ITERATIONS = 10;

        // Mock provider and resolver
        AmmoMockProvider01 provider = null;
        final MockContentResolver cr = new MockContentResolver();

        try {
            provider = utilMakeTestProvider01(mContext);
            assertNotNull(provider);
            cr.addProvider(AmmoMockSchema01.AUTHORITY, provider);

            // Choose JSON encoding for this test
            final Encoding enc = Encoding.newInstance(Encoding.Type.JSON);

            // Object with "Table 1" knowledge
            SchemaTable1Data d = new SchemaTable1Data();

            // Repeatedly deserialize random values to the db
            for (int i=0; i < NUM_ITERATIONS; i++) {
                ContentValues cv = d.createContentValuesRandom();
		byte[] jsonBytes = TestUtils.createJsonAsBytes(cv);
                Uri uriIn = RequestSerializer.deserializeToProvider(mContext,
                                                                    cr,
                                                                    "dummy",
                                                                    d.getBaseUri(),
                                                                    enc,
                                                                    jsonBytes);
                d.compareJsonToUri(jsonBytes, provider, uriIn);
            }
        } finally {
            if (provider != null) provider.release();
        }
    }


    /**
     * Serialize to ContentProvider (JSON encoding) for Table 2 ("Quick").
     * Iterated with random data.
     */
    public void testDeserializeToProviderJson_table2_random()
    {
        final int NUM_ITERATIONS = 10;

        // Mock provider and resolver
        AmmoMockProvider01 provider = null;
        final MockContentResolver cr = new MockContentResolver();

        try {
            provider = utilMakeTestProvider01(mContext);
            assertNotNull(provider);
            cr.addProvider(AmmoMockSchema01.AUTHORITY, provider);

            // Choose JSON encoding for this test
            final Encoding enc = Encoding.newInstance(Encoding.Type.JSON);

            // Object with "Table 1" knowledge
            SchemaTable2Data d = new SchemaTable2Data();

            // Repeatedly deserialize random values to the db
            for (int i=0; i < NUM_ITERATIONS; i++) {
                ContentValues cv = d.createContentValuesRandom();
		byte[] jsonBytes = TestUtils.createJsonAsBytes(cv);
                Uri uriIn = RequestSerializer.deserializeToProvider(mContext,
                                                                    cr,
                                                                    "dummy",
                                                                    d.getBaseUri(),
                                                                    enc,
                                                                    jsonBytes);
                d.compareJsonToUri(jsonBytes, provider, uriIn);
            }
        } finally {
            if (provider != null) provider.release();
        }
    }

    /**
     * Serialize to ContentProvider (JSON encoding) for Table 3 ("Start").
     * Iterated with random data.
     */
    public void testDeserializeToProviderJson_table3_random()
    {
        final int NUM_ITERATIONS = 10;

        // Mock provider and resolver
        AmmoMockProvider01 provider = null;
        final MockContentResolver cr = new MockContentResolver();

        try {
            provider = utilMakeTestProvider01(mContext);
            assertNotNull(provider);
            cr.addProvider(AmmoMockSchema01.AUTHORITY, provider);

            // Choose JSON encoding for this test
            final Encoding enc = Encoding.newInstance(Encoding.Type.JSON);

            // Object with "Table 1" knowledge
            SchemaTable3Data d = new SchemaTable3Data();

            // Repeatedly deserialize random values to the db
            for (int i=0; i < NUM_ITERATIONS; i++) {
                ContentValues cv = d.createContentValuesRandom();
		byte[] jsonBytes = TestUtils.createJsonAsBytes(cv);
                Uri uriIn = RequestSerializer.deserializeToProvider(mContext,
                                                                    cr,
                                                                    "dummy",
                                                                    d.getBaseUri(),
                                                                    enc,
                                                                    jsonBytes);
                d.compareJsonToUri(jsonBytes, provider, uriIn);
            }
        } finally {
            if (provider != null) provider.release();
        }
    }


    /**
     * Serialize from ContentProvider (JSON encoding) :
     * Simple case of known constant values on Table 3 ("Start") in schema.
     * WITH BLOB DATA
     *
     */
    public void testSerializeFromProviderJson_withBlob_basic_smallBlob() {
        AmmoMockProvider01 provider = null;
        try {
            provider = utilMakeTestProvider01(mContext);
            assertNotNull(provider);
            final MockContentResolver cr = new MockContentResolver();
            cr.addProvider(AmmoMockSchema01.AUTHORITY, provider);

            SchemaTable3Data d = new SchemaTable3Data();

            // Serialize values from the db
            ContentValues cv = d.createContentValuesWithBlobSmall();
            Uri uri = d.populateProviderWithData(provider, cv);
            byte[] serialized = utilSerializeJsonFromProvider_withBlob(cr, uri);
            if (serialized == null) {
		fail("unexpected serialization error");
            }
            d.compareBytesToCv(serialized, cv);
        } finally {
            if (provider != null) provider.release();
        }
    }
    
    /**
     * Serialize from ContentProvider (JSON encoding) :
     * Repeatedly serialize random values on Table 3 ("Start") in schema.
     * WITH BLOB DATA
     *
     */
    public void testSerializeFromProviderJson_withBlob_random_smallBlob() {
	final int NUM_ITERATIONS = 100;

	AmmoMockProvider01 provider = null;
        try {
            provider = utilMakeTestProvider01(mContext);
            assertNotNull(provider);
            final MockContentResolver cr = new MockContentResolver();
            cr.addProvider(AmmoMockSchema01.AUTHORITY, provider);

            SchemaTable3Data d = new SchemaTable3Data();

	    for (int i=0; i < NUM_ITERATIONS; i++) {
		ContentValues cv = d.createContentValuesWithBlobRandomSmall();
		Uri uri = d.populateProviderWithData(provider, cv);
		byte[] serialized = utilSerializeJsonFromProvider_withBlob(cr, uri);
		if (serialized == null) {
		    fail("unexpected serialization error");
		}
		d.compareBytesToCv(serialized, cv);
	    }

	    /*
	      catch (OutOfMemoryError e) {
	      // This won't happen most of the time, but catch if it does.
	      // Don't call it a test failure, just log and return.
	      Log.e(TAG, "*** out of memory error ***");
	      e.printStackTrace();
	      return;
	      }
	    */

        } finally {
            if (provider != null) provider.release();
        }
    }

    // TODO: more test cases
    /*
    public void testSerializeFromProviderJson_withBlob_basic_largeBlob() {
	// TODO
    }

    public void testSerializeFromProviderJson_withBlob_random_largeBlob() {
	// TODO
    }

    public void testSerializeFromProviderJson_withBlob_errors_smallBlob() {
	// TODO
    }

    public void testSerializeFromProviderJson_withBlob_errors_largeBlob() {
	// TODO
    }

    public void testDeserializeToProviderJson_withBlob_basic_smallBlob() {
	// TODO
    }
    
    public void testDeserializeToProviderJson_withBlob_basic_largeBlob() {
	// TODO
    }

    public void testDeserializeToProviderJson_withBlob_random_smallBlob() {
	// TODO
    }

    public void testDeserializeToProviderJson_withBlob_random_largeBlob() {
	// TODO
    }
    
    public void testSerializeFromProviderJson_basic_withFile() {
	// TODO
    }
    */



    /**
     * Serialize from ContentProvider (Terse encoding) :
     * Simple case of known constant values on Table 1 ("Ammo") in schema.
     *
     * This test
     * <ol>
     * <li>constructs a mock content provider,
     * <li>loads some data into the content provider,(imitating the application)
     * <li>serializes that data into a terse-encoding object
     * <li>checks the serialization to verify it's correct
     */
    public void testSerializeFromProviderTerse_table1_basic() {
        AmmoMockProvider01 provider = null;
        try {
            provider = utilMakeTestProvider01(mContext);
            assertNotNull(provider);
            final MockContentResolver cr = new MockContentResolver();
            cr.addProvider(AmmoMockSchema01.AUTHORITY, provider);

            SchemaTable1Data d = new SchemaTable1Data();

            // Serialize values from the db
            ContentValues cv = d.createContentValues();
            Uri uri = d.populateProviderWithData(provider, cv);
            byte[] terse = utilSerializeTerseFromProvider(cr, uri);
            
	    assertNotNull(terse);
	    
	    // TODO: compare terse message to original content
            //d.compareTerseToCv(terse, cv);
        } finally {
            if (provider != null) provider.release();
        }
    }

}
