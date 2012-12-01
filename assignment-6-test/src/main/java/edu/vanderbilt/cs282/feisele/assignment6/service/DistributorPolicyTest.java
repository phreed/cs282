package edu.vanderbilt.cs282.feisele.assignment6.service;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.test.AndroidTestCase;
import ch.qos.logback.classic.Level;
import edu.vu.isis.ammo.core.distributor.DistributorPolicy.Category;
import edu.vu.isis.ammo.core.distributor.DistributorPolicy.Encoding;
import edu.vu.isis.ammo.core.distributor.DistributorPolicy.Topic;

/**
 * Unit test for DistributorPolicy To run this test, you can type:
 * <p>
 * <code> 
 adb shell am instrument -w \
  -e class edu.vu.isis.ammo.core.DistributorPolicyTest#testDefaultMatchExactInterstitial \
  edu.vu.isis.ammo.core.tests/pl.polidea.instrumentation.PolideaInstrumentationTestRunner
 * </code>
 */

public class DistributorPolicyTest extends AndroidTestCase
{
    private static final Logger logger = LoggerFactory.getLogger("test.policy.routing");

    private DistributorPolicy policy;

    public DistributorPolicyTest()
    {
    }

    public DistributorPolicyTest(String testName)
    {
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite(DistributorPolicyTest.class);
    }

    /**
     * Called before every test
     */
    protected void setUp() throws Exception
    {
        this.policy = DistributorPolicy.newInstance(getContext(), null);
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
     * This test verifies that the default policy file works as intended.
     * <p>
     * The toString() and asString() methods (for Topic) do not return the same
     * string. The toString is for humans while the asString is for machines.
     * <p>
     */
    public void testDefaultMatchExactInterstitial()
    {
        ((ch.qos.logback.classic.Logger) logger).setLevel(Level.TRACE);

        final String topicName = "ammo/transapps.pli.locations";
        final edu.vu.isis.ammo.api.type.Topic topic = new edu.vu.isis.ammo.api.type.Topic(topicName);
        final Topic expected = this.policy
                .newBuilder()
                .newRouting(
                        Category.POSTAL,
                        0, DistributorDataStore.DEFAULT_POSTAL_LIFESPAN)
                .addClause()
                .addLiteral("serial", true, Encoding.TERSE)
                .addLiteral("multicast", true, Encoding.JSON)
                .addLiteral("gateway", true, Encoding.JSON)
                .build();

        expected.setType(topicName);

        final Topic fail_actual = this.policy.matchPostal(topic.toString());
        assertFalse("topic matches", expected.equals(fail_actual));

        final Topic actual = this.policy.matchPostal(topic.asString());
        assertEquals("topic does not match", expected, actual);

        ((ch.qos.logback.classic.Logger) logger).setLevel(Level.OFF);
    }

    public void testNumberTwo()
    {
        assertTrue(true);
    }
}
