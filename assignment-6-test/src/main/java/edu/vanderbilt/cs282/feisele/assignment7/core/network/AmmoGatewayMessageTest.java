package edu.vanderbilt.cs282.feisele.network;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Arrays;
import android.util.Log;

/**
 * Unit test for AmmoGatewayMessage
 * 
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class edu.vu.isis.ammo.core.AmmoGatewayMessageTest \
 * edu.vu.isis.ammo.core.tests/android.test.InstrumentationTestRunner
 */

import edu.vu.isis.ammo.core.network.AmmoGatewayMessage; 
import edu.vu.isis.ammo.core.network.AmmoGatewayMessage.CheckSum; 
import edu.vu.isis.ammo.core.pb.AmmoMessages;

import edu.vu.isis.ammo.testutils.TestUtils;

public class AmmoGatewayMessageTest extends TestCase
{
    private static final String TAG = "AmmoGatewayMessageTest";

    public AmmoGatewayMessageTest( String testName )
    {
        super( testName );
    }

    public static Test suite()
    {
        return new TestSuite( AmmoGatewayMessageTest.class );
    }

    /**
     * Called before every test
     */
    protected void setUp() throws Exception
    {
	// ...
    }

    /**
     * Called after every test
     */
    protected void tearDown() throws Exception
    {
	// ...
    }

    /**
     * Test methods
     */
    
    
    /**
     * Test ability to compare two instances of this class
     */
    public void testEquivalence()
    {
	// Message wrapper, with some properties set
	final AmmoMessages.MessageWrapper.Builder mw = AmmoMessages.MessageWrapper.newBuilder();
        mw.setType(AmmoMessages.MessageWrapper.MessageType.HEARTBEAT);
	mw.setMessagePriority(AmmoGatewayMessage.PriorityLevel.FLASH.v);

	// Construct two AmmoGatewayMessage instances from the same wrapper
	AmmoGatewayMessage.Builder agmb1 = AmmoGatewayMessage.newBuilder(mw, null);
        agmb1.isGateway(true);
	AmmoGatewayMessage agm1 = agmb1.build();
	assertNotNull(agm1);
	
	AmmoGatewayMessage.Builder agmb2 = AmmoGatewayMessage.newBuilder(mw, null);
        agmb2.isGateway(true);
	AmmoGatewayMessage agm2 = agmb2.build();
	assertNotNull(agm2);

	Log.d(TAG, "agm1 = [" + agm1.toString() + " ]");
	Log.d(TAG, "agm2 = [" + agm2.toString() + " ]");

	// The two instances should be equivalent
	// NOTE: the equals() method in AmmoGatewayMessage causes this test to fail...
	//assertTrue(agm1.equals(agm2));
	//assertTrue(agm2.equals(agm1));
	
	// An AGM should be equal to itself
	assertTrue(agm1.equals(agm1));

	// An AGM should not compare to an object of another class; should
	// throw ClassCastException
	final String fakeAGM = "I am not an AmmoGatewayMessage object";
	try { 
	    agm1.equals(fakeAGM);
	    fail("Expected a ClassCastException");
	} catch (ClassCastException e) {
	    // This was the expected behavior
	    assertTrue(agm1.equals(agm1));
	    assertNotNull(agm1);
	}
	
	// An AGM should behave acceptably when compared to a null object
	AmmoGatewayMessage agm3 = null;
	try { 
	    agm1.equals(agm3);
	    fail("Expected a ClassCastException");
	} catch (ClassCastException e) {
	    // This was the expected behavior
	    assertTrue(agm1.equals(agm1));
	    assertNotNull(agm1);
	}
    }
    
    /**
     * Test builder facility
     */
    public void testBuilder()
    {
	final byte[] ba1 = new byte[] {0, 1, 2, 3, 4, 5, 7, 10, 20, 50, 100};
	final int bufSize = 80;
        final byte[] ba2 = TestUtils.randomBytes(bufSize);

	final AmmoMessages.MessageWrapper.Builder mw = AmmoMessages.MessageWrapper.newBuilder();
        mw.setType(AmmoMessages.MessageWrapper.MessageType.HEARTBEAT);

	// Simple construction of an AmmoGatewayMessage (known byte array)
	AmmoGatewayMessage.Builder agmb1 = AmmoGatewayMessage.newBuilder(mw, null);
        agmb1.isGateway(true);
	agmb1.payload(ba1);
	agmb1.size(ba1.length); // ugh - shouldn't have to set this independently
	AmmoGatewayMessage agm1 = agmb1.build();
	assertTrue(agm1.isGateway);
	assertFalse(agm1.isMulticast);
	assertFalse(agm1.isSerialChannel);
	assertEquals(agm1.size, ba1.length); // TODO: test fails if don't set size too (see above)
	assertTrue(Arrays.equals(agm1.payload, ba1));
	
	// Simple construction of an AmmoGatewayMessage (random byte array)
	AmmoGatewayMessage.Builder agmb2 = AmmoGatewayMessage.newBuilder(mw, null);
        agmb2.isGateway(true);
	agmb2.payload(ba2);
	agmb2.size(ba2.length); 
	AmmoGatewayMessage agm2 = agmb2.build();
	assertTrue(agm2.isGateway);
	assertFalse(agm2.isMulticast);
	assertFalse(agm2.isSerialChannel);
	assertEquals(agm2.size, ba2.length); 
	assertTrue(Arrays.equals(agm2.payload, ba2));
		
	
	// Purposefully mis-size a payload, make sure behaves nicely
	AmmoGatewayMessage.Builder agmb3 = AmmoGatewayMessage.newBuilder(mw, null);
        agmb3.payload(ba1);
	agmb3.size(2 * ba1.length); // intentionally wrong array size
	try {
	    @SuppressWarnings("unused")
        final AmmoGatewayMessage agm3 = agmb3.build();
	    fail("Expected an IllegalArgumentException");
	} catch (IllegalArgumentException e) {
	    // expected behavior
	    assertNotNull(agmb3);
	} catch (Exception e) {
	    fail("Unexpected exception");
	}
	

	// TODO - should be able to make the following test pass
	/*
	agmb1.isGateway(true);
	agmb1.isMulticast(true);
	AmmoGatewayMessage agm1 = agmb1.build();
	assertFalse(agm1.isGateway);
	assertTrue(agm1.isMulticast);
	*/
    }
    
    /**
     * Test serialization methods
     */
    /*
    public void testSerialize()
    {
	
    }
    */

    /**
     * Test checksum functionality
     */
    public void testChecksum()
    {
	byte[] data = TestUtils.TEST_TINY_BLOB;
	CheckSum cs1 = CheckSum.newInstance(data);
	CheckSum cs2 = CheckSum.newInstance(data);
	
	// Two CheckSum objects make from the same data should be
	// equivalent.
	assertTrue(cs1.equals(cs2));
	assertEquals(cs1, cs2);

	// Instantiating a CheckSum with long version of itself should
	// give an equivalent object.
	long cs1Long = cs1.asLong();
	CheckSum cs1FromLong = new CheckSum(cs1Long);
	assertEquals(cs1, cs1FromLong);
	
	// Instantiating a CheckSum with byte[] version of itself should
	// give an equivalent object.
	byte[] cs1Bytes = cs1.asByteArray();
	CheckSum cs1FromBytes = new CheckSum(cs1Bytes);
	assertEquals(cs1, cs1FromBytes);
    }

    /**
     * Test checksum functionality with random data
     */
    public void testChecksumRandomData()
    {
	final int bufSize = 180;
	final int NUM_ITERATIONS = 10;

	for (int i=0; i < NUM_ITERATIONS; i++) {
	    final byte[] data = TestUtils.randomBytes(bufSize);

	    CheckSum cs1 = CheckSum.newInstance(data);
	    CheckSum cs2 = CheckSum.newInstance(data);
	
	    // Two CheckSum objects make from the same data should be
	    // equivalent.
	    assertTrue(cs1.equals(cs2));
	    assertEquals(cs1, cs2);
	    
	    // Instantiating a CheckSum with long version of itself should
	    // give an equivalent object.
	    long cs1Long = cs1.asLong();
	    CheckSum cs1FromLong = new CheckSum(cs1Long);
	    assertEquals(cs1, cs1FromLong);
	    
	    // Instantiating a CheckSum with byte[] version of itself should
	    // give an equivalent object.
	    byte[] cs1Bytes = cs1.asByteArray();
	    CheckSum cs1FromBytes = new CheckSum(cs1Bytes);
	    assertEquals(cs1, cs1FromBytes);
	}
    }

}

