package edu.vanderbilt.cs282.feisele.assignment6.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;


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
import edu.vu.isis.ammo.core.distributor.RequestSerializerHelper.SchemaTable;
import edu.vu.isis.ammo.core.distributor.RequestSerializerHelper.SchemaTable1Data;
import edu.vu.isis.ammo.core.distributor.RequestSerializerHelper.SchemaTable2Data;
import edu.vu.isis.ammo.core.distributor.RequestSerializerHelper.SchemaTable3Data;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more
 * information on how to write and extend Application tests.
 * <p/>
 * To run this test, you can type: <code>
  adb shell am instrument -w \
  -e class edu.vu.isis.ammo.core.ui.RequestSerializerTest \
  edu.vu.isis.ammo.core.tests/pl.polidea.instrumentation.PolideaInstrumentationTestRunner
 * </code
 */


public class RequestSerializerComponentTest extends AndroidTestCase {

    private static final String TAG = "RequestSerializerComponentTest";

    private static final Logger logger = LoggerFactory.getLogger("test.request.serial");

    private Context mContext;

    public RequestSerializerComponentTest() {
        //super("edu.vu.isis.ammo.core.distributor", RequestSerializer.class);
    }

    public RequestSerializerComponentTest( String testName )
    {
        //super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( RequestSerializerComponentTest.class );
    }

    protected void setUp() throws Exception
    {
        mContext = getContext();
    }

    protected void tearDown() throws Exception
    {
        mContext = null;
    }

    
    private MockContentResolver utilGetContentResolver()
    {
        final MockContentResolver mcr = new MockContentResolver();
        mcr.addProvider(MockSchema01.AUTHORITY,
                        MockProvider01.getInstance(getContext()));

        return mcr;
    }

    private MockProvider01 utilMakeTestProvider01(Context context)
    {
        return MockProvider01.getInstance(context);
    }


    /**
     *====================================================================
     *  These tests focus on the Request Serializer objects as components.
     *  Namely how the parts interact with the differing content.
     *=====================================================================
     */

    public void testRoundTripJson_table1_basic()
    {
	SchemaTable1Data d = new SchemaTable1Data();
	ContentValues cv = d.createContentValues();
        this.roundTripTrial(Encoding.newInstance(Encoding.Type.JSON), cv, d.getTable(), d );
    }

    public void testRoundTripJson_table1_random()
    {
	SchemaTable1Data d = new SchemaTable1Data();
	ContentValues cv = d.createContentValuesRandom();
        this.roundTripTrial(Encoding.newInstance(Encoding.Type.JSON), cv, d.getTable(), d );
    }

    public void testRoundTripJson_table2_basic()
    {
	SchemaTable2Data d = new SchemaTable2Data();
	ContentValues cv = d.createContentValues();
        this.roundTripTrial(Encoding.newInstance(Encoding.Type.JSON), cv, d.getTable(), d );
    }

    public void testRoundTripJson_table2_random()
    {
	SchemaTable2Data d = new SchemaTable2Data();
	ContentValues cv = d.createContentValuesRandom();
        this.roundTripTrial(Encoding.newInstance(Encoding.Type.JSON), cv, d.getTable(), d );
    }
    
    // TODO: more tests
    /*    public void testRoundTripJson_table3_basic()
    {
	SchemaTable3Data d = new SchemaTable3Data();
	ContentValues cv = d.createContentValues();
        this.roundTripTrial(Encoding.newInstance(Encoding.Type.JSON), cv, d.getTable(), d );
    }

    public void testRoundTripJson_table3_random()
    {
	// ...
    }

    // JSON
    public void testRoundTripJson_table3_basic_withBlob() {}
    public void testRoundTripJson_table3_basic_withFile() {}
    public void testRoundTripJson_table3_random_withBlob() {}    
    public void testRoundTripJson_table3_random_withFile() {}

    // Terse
    public void testRoundTripTerse_table2_basic() {}
    public void testRoundTripTerse_table3_basic() {}
    public void testRoundTripTerse_table1_random() {}
    public void testRoundTripTerse_table2_random() {}
    public void testRoundTripTerse_table3_random() {}
    */

    /**
     * This is round trip test what is taken from the database
     * is identical to what the database ends up with.
     *
     * <ol>
     * <li>constructs a mock content provider,
     * <li>loads some data into the content provider,(imitating the application)
     * <li>serializes that data into a json string
     * <li>clear the content provider (imitating the network)
     * <li>deserialize into the content provider
     * <li>check the content of the content provider,(imitating the application)
     * </ol>
     */
    private void roundTripTrial(Encoding encoding, ContentValues cv, String table, SchemaTable d) 
    {
        ((ch.qos.logback.classic.Logger) RequestSerializerComponentTest.logger).setLevel(Level.TRACE);
        ((ch.qos.logback.classic.Logger) MockDownloadContentProvider.clogger).setLevel(Level.TRACE);
        ((ch.qos.logback.classic.Logger) MockDownloadContentProvider.hlogger).setLevel(Level.TRACE);
        ((ch.qos.logback.classic.Logger) MockDownloadContentProvider.logger).setLevel(Level.TRACE);
        ((ch.qos.logback.classic.Logger) RequestSerializer.logger).setLevel(Level.TRACE);

        MockProvider01 provider = null;
        try {
            provider = MockProvider01.getInstance(mContext);
            Assert.assertNotNull(provider);
            final MockContentResolver resolver = new MockContentResolver();
            resolver.addProvider(MockSchema01.AUTHORITY, provider);

	    // Stage 1: serialize from provider
            final byte[] encodedBytes = encodeTripTrial(provider, resolver, encoding, cv, d);

	    Assert.assertNotNull(encodedBytes);

	    // Stage 2: deserialize to provider
            decodeTripTrial(provider, resolver, encoding, cv, d, encodedBytes);

        } finally {
            if (provider != null) provider.release();

            ((ch.qos.logback.classic.Logger) RequestSerializerComponentTest.logger).setLevel(Level.OFF);
            ((ch.qos.logback.classic.Logger) MockDownloadContentProvider.clogger).setLevel(Level.OFF);
            ((ch.qos.logback.classic.Logger) MockDownloadContentProvider.hlogger).setLevel(Level.OFF);
            ((ch.qos.logback.classic.Logger) RequestSerializer.logger).setLevel(Level.WARN);
        }


    }

    private byte[] encodeTripTrial(final MockProvider01 provider,
                                   final ContentResolver resolver,
                                   final Encoding enc, final ContentValues cv, final SchemaTable d) 
    {

	Uri tupleUri = d.populateProviderWithData(provider, cv);
	Log.d(TAG, "  populated uri = " + tupleUri.toString());

        // Serialize the provider content into JSON bytes
        final byte[] encodedBytes;
        try
            {
                encodedBytes = RequestSerializer.serializeFromProvider(resolver, tupleUri, enc);
            }
        catch (NonConformingAmmoContentProvider ex)
            {
                Assert.fail("Should not have thrown NonConformingAmmoContentProvider in this case");
                return null;
            }
        catch (TupleNotFoundException ex)
            {
                Assert.fail("Should not have thrown TupleNotFoundException in this case");
                return null;
            }
        catch (IOException ex)
            {
                Assert.fail("failure of the test itself");
                return null;
            }
	Log.d(TAG, "  serializedFromProvider bytes = [" + (new String(encodedBytes)) + "]");
        return encodedBytes;
    }


    private void decodeTripTrial(final MockProvider01 provider,
                                 final ContentResolver resolver,
                                 final Encoding enc, final ContentValues cv, final SchemaTable d,
                                 final byte[] encodedBytes) 
    {

        final SQLiteDatabase db = provider.getDatabase();
        final int deletedCount = db.delete(d.getTable(), "1", null);
        Assert.assertEquals("check deleted tuple count", 1, deletedCount);

	Log.d(TAG, "  encodedBytes bytes = [" + (new String(encodedBytes)) + "]");

	// Deserialize "received" bytes to provider
        final Uri tupleIn = RequestSerializer.deserializeToProvider(mContext, resolver,
                                                                    "dummy", 
								    d.getBaseUri(), 
								    enc, 
								    encodedBytes);
	
	Assert.assertNotNull(tupleIn);
	Log.d(TAG, "deserialized uri = " + tupleIn.toString());

        // Now query the provider and examine its contents,
        // checking that they're the same as the original.
	d.compareJsonToUri(encodedBytes, provider, tupleIn);
    }


}
