package edu.vanderbilt.cs282.feisele.api.type;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;
import android.test.AndroidTestCase;

import edu.vu.isis.ammo.testutils.TestUtils;

/**
 * Unit test for Payload API class
 * <p>
 * Use this class as a template to create new Ammo unit tests for classes which
 * use Android-specific components.
 * <p>
 * To run this test, you can type: <code>
 * adb shell am instrument -w \
 * -e class edu.vu.isis.ammo.core.TopicTest \
 * edu.vu.isis.ammo.core.tests/android.test.InstrumentationTestRunner
 * </code>
 */

public class PayloadTest extends AndroidTestCase
{
    final static private Logger logger = LoggerFactory.getLogger("trial.api.type.payload");

    public PayloadTest()
    {
    }

    public PayloadTest(String testName)
    {
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite(PayloadTest.class);
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
     * All the tests expect equivalence to work correctly. Verify that
     * equivalence works.
     */
    public void testEquivalence() {
        // Construct with a known string
        final String in = "foo";
        Payload p1 = new Payload(in);
        Payload p2 = new Payload(in);
        assertEquals(p1, p2);

        // Construct with a random string
        final int strSize = 20;
        final String in2 = TestUtils.randomText(strSize);
        Payload p3 = new Payload(in2);
        Payload p4 = new Payload(in2);
        assertEquals(p3, p4);

        // Construct with an empty string
        final String empty = "";
        Payload p3a = new Payload(empty);
        Payload p4a = new Payload(empty);
        assertEquals(p3a, p4a);

        // Construct with known byte array
        final byte[] ba = new byte[] {
                0, 1, 2, 3, 4, 5, 7, 10, 20, 50, 100
        };
        Payload p5 = new Payload(ba);
        Payload p6 = new Payload(ba);
        assertEquals(p5, p6);

        // Construct with random byte array
        final int bufSize = 80;
        byte[] ba2 = TestUtils.randomBytes(bufSize);
        Payload p7 = new Payload(ba2);
        Payload p8 = new Payload(ba2);
        assertEquals(p7, p8);

        // Construct with CV (known)
        ContentValues cv = TestUtils.createContentValues();
        Payload p09 = new Payload(cv);
        Payload p10 = new Payload(cv);
        assertEquals(p09, p10);

        // Construct with CV (random)
        final int cvSize = 20;
        ContentValues cv2 = TestUtils.randomContentValues(cvSize);
        Payload p11 = new Payload(cv2);
        Payload p12 = new Payload(cv2);
        assertEquals(p11, p12);

        // Construct with parcel
        final Parcel pp = Parcel.obtain();
        Payload p13 = new Payload(pp);
        Payload p14 = new Payload(pp);
        assertEquals(p13, p14);
        pp.recycle();

        // A payload should be equal to itself
        Payload p15 = new Payload(pp);
        assertEquals("A payload should be equal to itself", p15, p15);

        // A payload should not be equal to an object of a different type
        Payload p16 = new Payload(pp);
        String p16str = "I am not a Payload object";
        assertFalse(p16.equals(p16str));

        // A payload should not be equal to a payload of different type
        Payload p17 = new Payload(pp);
        String p18str = "I am a Payload object of type string";
        Payload p18 = new Payload(p18str);
        assertFalse(p17.equals(p18));

        // A "none" payload should be equal to itself
        assertEquals("a none is equal to itself", Payload.NONE, Payload.NONE);
    }

    public void testConstructorWithString()
    {
        // Construct with a known string
        final String in = "foo";
        Payload p = new Payload(in);
        assertNotNull(p);

        // Construct with a random string
        final int size = 20;
        final String in2 = TestUtils.randomText(size);
        Payload p2 = new Payload(in2);
        assertNotNull(p2);

        // Construct with an empty string
        final String in3 = "";
        Payload p3 = new Payload(in3);
        assertNotNull(p3);

        // Construct with a null string
        final String in4 = null;
        Payload p4 = new Payload(in4);
        assertNotNull(p4);

        // Need some Payload public accessors to examine content
        // e.g.
        // assertTrue(p.getString() == in);
    }

    public void testConstructorWithByteArray()
    {
        // Constructor with known byte array
        byte[] ba = new byte[] {
                0, 1, 2, 3, 4, 5, 7, 10, 20, 50, 100
        };
        Payload p = new Payload(ba);
        assertNotNull(p);

        // Constructor with random byte array
        final int size = 80;
        byte[] ba2 = TestUtils.randomBytes(size);
        Payload p2 = new Payload(ba2);
        assertNotNull(p2);

        // Constructor with empty byte array
        byte[] ba3 = new byte[10];
        Payload p3 = new Payload(ba3);
        assertNotNull(p3);

        // Constructor with null-pointer byte array
        byte[] ba4 = new byte[10];
        ba4 = null;
        Payload p4 = new Payload(ba4);
        assertNotNull(p4);
    }

    public void testConstructorWithContentValues()
    {
        // Construct with small, simple CV
        ContentValues cv = new ContentValues();
        cv.put("ammo", "great");
        Payload p = new Payload(cv);
        assertNotNull(p);

        // Construct with random CV
        final int cvSize = 20;
        ContentValues cv2 = TestUtils.randomContentValues(cvSize);
        Payload p2 = new Payload(cv2);
        assertNotNull(p2);
    }

    public void testConstructorWithParcel()
    {
        // Initialize payload with empty parcel
        Parcel in = null;
        try {
            in = Parcel.obtain();
            Payload p = new Payload(in);
            assertNotNull(p);
        } catch (Exception e) {
            fail("Unexpected exception");
        } finally {
            if (in != null)
                in.recycle();
        }

        // Initialize payload with null parcel
        try {
            in = null;
            Payload p = new Payload(in);
            assertNotNull(p);
        } catch (NullPointerException e) {
            // Expected behavior
            assertTrue(true);
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    /**
     * Test case of passing in a null Parcel - should throw a null pointer
     * exception
     */
    public void testNullParcel() {
        /**
         * Test case of passing in a null Parcel - should throw a null pointer
         * exception
         */
        boolean success = false;
        try {
            final Parcel p1 = null;
            Payload.readFromParcel(p1);

        } catch (NullPointerException ex) {
            success = true;
        }
        Assert.assertTrue("passing a null reference should fail", success);
    }

    /**
     * Generate a non-null Parcel containing a null payload When unmarshalled
     * this produces a NONE payload. - should return non-null
     */
    public void testNullContentParcel() {
        {
            final Payload expected = null;
            final Parcel parcel = Parcel.obtain();
            Payload.writeToParcel(expected, parcel, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
            parcel.setDataPosition(0);
            final Payload actual = Payload.CREATOR.createFromParcel(parcel);
            Assert.assertEquals("wrote a null but got something else", actual, Payload.NONE);
            parcel.recycle();
        }
    }

    /**
     * Generate a non-null Parcel containing a simple string payload - should
     * return non-null
     */
    public void testParcel() {
        ((ch.qos.logback.classic.Logger) logger).setLevel(Level.TRACE);

        final Parcel parcel1 = Parcel.obtain();
        final Parcel parcel2 = Parcel.obtain();
        try {
            final Payload expected = new Payload("an arbitrary Payload");
            Payload.writeToParcel(expected, parcel1, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
            final byte[] expectedBytes = parcel1.marshall();
            // Assert.assertEquals(4, bytes[0]);
            parcel2.unmarshall(expectedBytes, 0, expectedBytes.length);
            parcel2.setDataPosition(0);
            final Payload actual = Payload.readFromParcel(parcel2);
            Assert.assertNotNull("wrote something but got a null back", actual);
            Assert.assertEquals("did not get back an equivalent Payload", expected, actual);
        } finally {
            parcel1.recycle();
            parcel2.recycle();
        }
    }

    public void testWhatContent()
    {
        // Type STR
        Payload p1 = new Payload("foo");
        assertNotNull(p1);
        assertTrue(p1.whatContent() == Payload.Type.STR);

        // Type BYTE
        byte[] ba = new byte[10];
        Payload p2 = new Payload(ba);
        assertNotNull(p2);
        assertTrue(p2.whatContent() == Payload.Type.BYTE);

        // Type CV
        ContentValues cv = new ContentValues();
        cv.put("ammo", "great");
        Payload p3 = new Payload(cv);
        assertNotNull(p3);
        assertTrue(p3.whatContent() == Payload.Type.CV);
    }

    public void testAsBytes()
    {
        // Construct a payload from byte array
        final byte[] ba = new byte[] {
                0, 1, 2, 3, 4, 5, 7, 10, 20, 50, 100
        };
        Payload p = new Payload(ba);
        assertNotNull(p);
        assertTrue(p.whatContent() == Payload.Type.BYTE);

        // Make sure the returned byte array is same as original
        assertTrue(Arrays.equals(ba, p.asBytes()));

        // Construct with random byte array
        final int bufSize = 80;
        byte[] ba2 = TestUtils.randomBytes(bufSize);
        Payload p2 = new Payload(ba2);
        assertTrue(p2.whatContent() == Payload.Type.BYTE);

        // Make sure the returned byte array is same as original
        assertTrue(Arrays.equals(ba2, p2.asBytes()));
    }

    public void testGetCV()
    {
        // cv to initialize with
        ContentValues cv = new ContentValues();
        cv.put("foo", "bar");

        // Construct a payload with the cv
        Payload p = new Payload(cv);
        assertNotNull(p);
        assertTrue(p.whatContent() == Payload.Type.CV);

        // Check that retrieved cv is same as original
        assertTrue(p.getCV() == cv);
    }
}
