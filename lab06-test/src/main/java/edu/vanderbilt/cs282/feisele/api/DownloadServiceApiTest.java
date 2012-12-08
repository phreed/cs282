package edu.vanderbilt.cs282.feisele.api;

import junit.framework.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.test.suitebuilder.annotation.MediumTest;
import edu.vanderbilt.cs282.feisele.lab06.DownloadCallback;
import edu.vanderbilt.cs282.feisele.lab06.DownloadRequest;
import edu.vanderbilt.cs282.feisele.lab06.annotation.DesignPattern;
import edu.vanderbilt.cs282.feisele.lab06.service.DownloadService;

/**
 * This is a simple framework for a test of a Service. See
 * {@link android.test.ServiceTestCase ServiceTestCase} for more information on
 * how to write and extend service tests.
 * 
 */
public class DownloadServiceApiTest extends
		android.test.ServiceTestCase<DownloadService> {
	private Logger logger;

	private DownloadRequest request = null;

	public DownloadServiceApiTest() {
		super(DownloadService.class);
		logger = LoggerFactory.getLogger("test.service.request");
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		final Intent startIntent = new Intent();
		startIntent.setClass(getContext(), DownloadService.class);
		final IBinder iservice = bindService(startIntent);
		this.request = DownloadRequest.Stub.asInterface(iservice);
	}

	/**
	 * Tear down is run once everything is complete.
	 */
	@Override
	protected void tearDown() throws Exception {
		logger.info("Tear Down");
	}

	/**
	 * This test mimics the sending of a request to the download service. Notice
	 * that it does not require that the service do anything useful.
	 */
	@MediumTest
	@DesignPattern(name = "downloader", namespace = "gof", pattern = "strategy", role = "context")
	public void testDownloadRequest() {
		logger.info("download request : ");

		logger.debug("runDownloadAsyncAidl");
		final Uri uri = Uri.parse("http://foo/bar.html");

		if (uri == null)
			return;

		logger.debug("download async aidl");
		try {
			final DownloadCallback.Stub callback = new DownloadCallback.Stub() {
				public void sendPath(String url) throws RemoteException {
					Assert.assertEquals(uri.toString(), url);
				}

				public void sendFault(String msg) throws RemoteException {
					Assert.assertEquals("download failed", msg);
				}

			};

			this.request.downloadImage(uri, callback);
		} catch (RemoteException ex) {
			logger.error("download async aidl", ex);
			Assert.fail("could not send request to service");
		}
	}

}
