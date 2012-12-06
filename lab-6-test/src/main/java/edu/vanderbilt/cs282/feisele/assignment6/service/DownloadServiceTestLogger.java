/**
 * 
 */

package edu.vanderbilt.cs282.feisele.assignment6.service;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Intent;
import android.os.IBinder;

/**
 */
abstract public class DownloadServiceTestLogger extends
		android.test.ServiceTestCase<DownloadService> {
	static final private Logger logger = LoggerFactory
			.getLogger("test.service.lifecycle");

	public DownloadServiceTestLogger(Class<DownloadService> serviceClass) {
		super(serviceClass);
	}

	@Override
	protected IBinder bindService(Intent intent) {
		logger.info("bind service {}", intent);
		return super.bindService(intent);
	}

	@Override
	protected void startService(Intent intent) {
		logger.info("start service {}", intent);
		super.startService(intent);
	}

	protected void setupService() {
		logger.info("setup service ");
		super.setupService();
	}

	/**
	 * Accomodating methods available in JUnit4
	 * <p>
	 * Asserts that two byte arrays are equal. If they are not, an
	 * java.lang.AssertionError is thrown.
	 * <p>
	 * 
	 * @param message
	 *            the identifying message for the java.lang.AssertionError
	 * @param expecteds
	 *            byte array with expected values.
	 * @param actuals
	 *            byte array with actual values
	 */
	public static void assertArrayEquals(String message, byte[] expected,
			byte[] actuals) throws AssertionError {
		if (expected == actuals)
			return;
		if (Arrays.equals(expected, actuals)) {
			return;
		}
		final StringBuilder sb = new StringBuilder(message);
		sb.append('\n').append(" expected={").append(Arrays.toString(expected))
				.append("}");
		sb.append('\n').append(" actuals={").append(Arrays.toString(actuals))
				.append("}");
		throw new AssertionError(sb.toString());
	}

}
