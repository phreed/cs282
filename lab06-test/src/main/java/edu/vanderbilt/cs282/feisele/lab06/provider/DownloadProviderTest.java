package edu.vanderbilt.cs282.feisele.lab06.provider;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.test.AndroidTestCase;

/**
 * This is a simple framework for a test of an Application. See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more
 * information on how to write and extend Application tests.
 * <p/>
 * To run this test, you can type: adb shell am instrument -w \ -e class
 * edu.vanderbilt.isis.ammo.core.ui.RequestSerializerTest \
 * edu.vanderbilt.isis.ammo.core.tests/android.test.InstrumentationTestRunner
 */

public class DownloadProviderTest extends AndroidTestCase {
	
	private static final Logger logger = LoggerFactory
			.getLogger("test.request.serial");

	private Context context;

	public DownloadProviderTest() {
		// super("edu.vanderbilt.isis.ammo.core.distributor", RequestSerializer.class);
	}

	public DownloadProviderTest(String testName) {
		// super( testName );
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(DownloadProviderTest.class);
	}

	protected void setUp() throws Exception {
		mContext = getContext();
	}

	protected void tearDown() throws Exception {
		mContext = null;
	}

	/**
	 * @param cr
	 * @param uri
	 * @return
	 */
	private void testContentProvider() {
	}

}
