package edu.vanderbilt.cs282.feisele.assignment5;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class DownloadBoundServiceAsync extends DownloadBoundService {
	static private final String TAG = "Threaded Download Service";

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
		Log.d(TAG, "sync service on bind");
		return this.stub;
	}

}
