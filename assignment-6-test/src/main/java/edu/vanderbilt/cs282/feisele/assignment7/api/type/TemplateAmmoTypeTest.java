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

/**
 * This is the cannonical example of a test for Ammo Types. Unit test for
 * Template API class
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

public class TemplateAmmoTypeTest extends AndroidTestCase
{
    final static private Logger logger = LoggerFactory.getLogger("trial.api.type.template");

    public TemplateAmmoTypeTest()
    {
    }

    public TemplateAmmoTypeTest(String testName)
    {
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite(TemplateAmmoTypeTest.class);
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
     * All the tests expect equivalence to work correctly. So we best verify
     * that equivalence works.
     */
    public void testEquivalence() {
        Assert.assertEquals("a none is equal to itself", Template.NONE, Template.NONE);
    }

    public void testConstructorWithString()
    {
        final String in = "foo";
        Template p = new Template(in);
        assertNotNull(p);

        // Need some Template public accessors to examine content
        // e.g.
        // assertTrue(p.getString() == in);
    }

    public void testConstructorWithByteArray()
    {
        byte[] ba = new byte[10];
        Template p = new Template(ba);
        assertNotNull(p);
    }

    public void testConstructorWithContentValues()
    {
        ContentValues cv = new ContentValues();
        cv.put("ammo", "great");
        Template p = new Template(cv);
        assertNotNull(p);
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
            Template.readFromParcel(p1);

        } catch (NullPointerException ex) {
            success = true;
        }
        Assert.assertTrue("passing a null reference should fail", success);
    }

    /**
     * Generate a non-null Parcel containing a null template When unmarshalled
     * this produces a NONE template. - should return non-null
     */
    public void testNullContentParcel() {
        {
            final Template expected = null;
            final Parcel parcel = Parcel.obtain();
            Template.writeToParcel(expected, parcel, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
            parcel.setDataPosition(0);
            final Template actual = Template.CREATOR.createFromParcel(parcel);
            Assert.assertEquals("wrote a null but got something else", actual, Template.NONE);
            parcel.recycle();
        }
    }

    /**
     * Generate a non-null Parcel containing a simple string template - should
     * return non-null
     */
    public void testParcel() {
        ((ch.qos.logback.classic.Logger) logger).setLevel(Level.TRACE);

        final Parcel parcel1 = Parcel.obtain();
        final Parcel parcel2 = Parcel.obtain();
        try {
            final Template expected = new Template("an arbitrary Template");
            Template.writeToParcel(expected, parcel1, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
            final byte[] expectedBytes = parcel1.marshall();
            // Assert.assertEquals(4, bytes[0]);
            parcel2.unmarshall(expectedBytes, 0, expectedBytes.length);
            parcel2.setDataPosition(0);
            final Template actual = Template.readFromParcel(parcel2);
            Assert.assertNotNull("wrote something but got a null back", actual);
            Assert.assertEquals("did not get back an equivalent Template", expected, actual);
        } finally {
            parcel1.recycle();
            parcel2.recycle();
        }
    }

    public void testWhatContent()
    {
        // Type STR
        Template p1 = new Template("foo");
        assertNotNull(p1);
        assertTrue(p1.whatContent() == Template.Type.STR);

        // Type BYTE
        byte[] ba = new byte[10];
        Template p2 = new Template(ba);
        assertNotNull(p2);
        assertTrue(p2.whatContent() == Template.Type.BYTE);

        // Type CV
        ContentValues cv = new ContentValues();
        cv.put("ammo", "great");
        Template p3 = new Template(cv);
        assertNotNull(p3);
        assertTrue(p3.whatContent() == Template.Type.CV);
    }

    public void testAsBytes()
    {
        // Construct a template from byte array
        final byte[] ba = new byte[] {
                0, 1, 2, 3, 4, 5, 7, 10, 20, 50, 100
        };
        Template p = new Template(ba);
        assertNotNull(p);
        assertTrue(p.whatContent() == Template.Type.BYTE);

        // Make sure the returned byte array is same as original
        assertTrue(Arrays.equals(ba, p.asBytes()));
    }

    public void testGetCV()
    {
        // cv to initialize with
        ContentValues cv = new ContentValues();
        cv.put("foo", "bar");

        // Construct a template with the cv
        Template p = new Template(cv);
        assertNotNull(p);
        assertTrue(p.whatContent() == Template.Type.CV);

        // Check that retrieved cv is same as original
        assertTrue(p.getCV() == cv);
    }
}
