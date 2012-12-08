package edu.vanderbilt.cs282.feisele.lab06.service;

import static org.hamcrest.CoreMatchers.equalTo;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.vanderbilt.cs282.feisele.lab06.service.DownloadService.FailedDownload;

/**
 * A simple unit test set.
 * <p>
 * Most android tests must be run as part of an android package specifically
 * created to run on a device. The android.jar which is available for running on
 * the desktop will throw RuntimeExceptions as it only provides stubs.
 * For this reason no object may be constructed which instantiates any 
 * android objects. This includes: MatrixCursor, Context, MockContext, etc.
 */
@RunWith(JUnit4.class)
public class ProviderSchemaTest {
	private static final Logger logger = LoggerFactory
			.getLogger("test.FailedDownload");

	/**
	 * A test to make sure the behavior is nominally correct. A tree is built
	 * for an object which belongs to the java language.
	 */
	@Test
	public void checkFailedDownload() {
		logger.info("checkFailedDownload");

		final FailedDownload actual = new FailedDownload(null, "foo");

		Assert.assertThat("check create table statement", actual.msg.toString(),
				equalTo("foo"));
	}

}
