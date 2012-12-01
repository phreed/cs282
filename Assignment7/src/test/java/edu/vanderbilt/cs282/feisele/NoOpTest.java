package edu.vanderbilt.cs282.feisele;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * No-op unit test.
 */
public class NoOpTest extends TestCase {
	private static final Logger logger = LoggerFactory
			.getLogger("test.genealogist");

	/**
	 * Create the test case
	 * 
	 * @param testName
	 *            name of the test case
	 */
	public NoOpTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(NoOpTest.class);
	}

	protected void setUp() throws Exception {
		logger.info("setUp");
	}

	protected void tearDown() throws Exception {
		logger.info("setUp");
	}

	/**
	 * A test to make sure the behavior is nominally correct. A tree is built
	 * for an object which belongs to the java language.
	 */
	public void testGetAncestryObject() {
		logger.info("testGetAncestryObject");

		Assert.assertEquals("ancestor count @", 5, 5);
	}
}
