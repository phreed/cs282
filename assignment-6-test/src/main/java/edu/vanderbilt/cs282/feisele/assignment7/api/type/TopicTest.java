package edu.vanderbilt.cs282.feisele.api.type;

import java.io.UnsupportedEncodingException;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.os.Parcel;
import android.os.Parcelable;
import android.test.AndroidTestCase;
import ch.qos.logback.classic.Level;

import edu.vu.isis.ammo.api.IncompleteRequest;

/**
 * Unit test for Topic API class
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
// [IMPORT AMMO CLASS(ES) TO BE TESTED HERE]

public class TopicTest extends AndroidTestCase
{
    final static private Logger logger = LoggerFactory.getLogger("trial.api.type.topic");

    public TopicTest()
    {
    }

    public TopicTest(String testName)
    {
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite(TopicTest.class);
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
     * All the tests expect equivalence to work correctly. So we best verify
     * that equivalence works.
     */
    public void testEquivalence() {
        final Topic first = new Topic("this is a string");
        final Topic second = new Topic("this is a differenct string");
        Assert.assertEquals("an object should be equal to itself", first, first);
        Assert.assertFalse("an objects which are not equal", first.equals(second));
    }

    /**
     * Test case of passing in a null Parcel - should throw a null pointer
     * exception
     */
    public void testNullParcel() {
        boolean success = false;
        try {
            final Parcel p1 = null;
            Topic.readFromParcel(p1);

        } catch (NullPointerException ex) {
            success = true;
        }
        Assert.assertTrue("passing a null reference should fail", success);
    }

    /**
     * Generate a non-null Parcel containing a null Topic When unmarshalled this
     * produces a NONE Topic. - should return non-null
     */
    public void testNullContentParcel() {
        final Topic expected = null;
        final Parcel parcel = Parcel.obtain();
        Topic.writeToParcel(expected, parcel, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
        parcel.setDataPosition(0);
        final Topic actual = Topic.CREATOR.createFromParcel(parcel);
        Assert.assertEquals("wrote a null expecting a NONE but got something else back", actual,
                Topic.NONE);
        parcel.recycle();
    }

    /**
     * Generate a non-null Parcel containing a simple string Topic - should
     * return non-null
     */
    public void testParcel() {
        ((ch.qos.logback.classic.Logger) logger).setLevel(Level.TRACE);

        final Parcel parcel1 = Parcel.obtain();
        final Parcel parcel2 = Parcel.obtain();
        try {
            final Topic expected = new Topic("an arbitrary Topic");
            logger.info("parcel 1 position before {}", parcel1.dataPosition());
            Topic.writeToParcel(expected, parcel1, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
            logger.info("parcel 1 position after {}", parcel1.dataPosition());

            final byte[] expectedBytes = parcel1.marshall();
            logger.info("parcel 1 content [{}] [{}]", expectedBytes, new String(expectedBytes,
                    "UTF-8"));
            // Assert.assertEquals(4, bytes[0]);
            parcel2.unmarshall(expectedBytes, 0, expectedBytes.length);
            logger.info("parcel 2 position before {}", parcel2.dataPosition());

            parcel2.setDataPosition(0);
            logger.info("parcel 2 position after {}", parcel2.dataPosition());
            final Topic actual = Topic.readFromParcel(parcel2);
            logger.info("actual {}", actual);
            Assert.assertNotNull("wrote something but got a null back", actual);
            Assert.assertEquals("did not get back an equivalent Topic", expected, actual);

        } catch (UnsupportedEncodingException ex) {
            logger.error("bad byte array encoding");
        } finally {
            parcel1.recycle();
            parcel2.recycle();
        }
    }

    /**
     * Test Topic constructor with string
     */
    public void testConstructorWithString()
    {
        final String in = "foo";
        Topic t = new Topic(in);
        assertNotNull(t);

        // Need some Topic public accessors to examine content
        // e.g.
        // assertTrue(t.getString() == in);
    }

    /**
     * Test Topic constructor with parcel
     */
    public void testConstructorWithParcel()
    {

        // Initialize topic with empty parcel
        Parcel in = null;
        try {
            in = Parcel.obtain();
            Topic t = new Topic(in);
            assertNotNull(t);
        } catch (IncompleteRequest e) {
            fail("Unexpected IncompleteRequest exception");
        } catch (Exception e) {
            fail("Unexpected exception");
        } finally {
            if (in != null)
                in.recycle();
        }

        // Initialize topic with null parcel - should throw NullPointerException
        try {
            in = null;
            Topic t = new Topic(in);
            assertNotNull(t);
        } catch (NullPointerException e) {
            // Expected behavior
            assertTrue(true);
        } catch (IncompleteRequest e) {
            fail("Unexpected IncompleteRequest exception");
        } catch (Exception e) {
            fail("Unexpected exception");
        }

        // TODO: Try a malformed parcel -- should throw IncompleteRequest

    }
}
