package edu.vanderbilt.cs282.feisele.assignment5;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;

/**
 * Asynchronous AIDL model ("Run Async AIDL").
 * <p>
 * This service supports the DownloadRequest interface for inbound calls.
 * 
 *  @author "Fred Eisele" <phreed@gmail.com>
 */
public class DownloadBoundServiceAsync extends DownloadBoundService {
	static private final Logger logger = LoggerFactory.getLogger("class.service.download.bound.async");

	// ===========================================================
	// AIDL Implementation
	// ===========================================================

	final private DownloadRequest.Stub stub = new DownloadRequest.Stub() {
		final DownloadBoundServiceAsync master = DownloadBoundServiceAsync.this;

		public void downloadImage(Uri uri, DownloadCallback callback)
				throws RemoteException {
			try {
				final Bitmap bitmap = master.downloadBitmap(uri);
				final File bitmapFile = master.storeBitmap(bitmap);
				callback.sendPath(bitmapFile.toString());

			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
				callback.sendFault("file not found :");

			} catch (FailedDownload ex) {
				ex.printStackTrace();
				callback.sendFault("download failed");

			} catch (IOException ex) {
				ex.printStackTrace();
				callback.sendFault("not implemented by this service");
			}
		}
	};

	/**
	 * The onBind() is called after construction and onCreate().
	 */
	@Override
	public IBinder onBind(Intent intent) {
		logger.debug("sync service on bind");
		return this.stub;
	}

}
