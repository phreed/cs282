package edu.vanderbilt.cs282.feisele.lab05;

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
 * Sync AIDL model ("Run Sync AIDL").
 * <p>
 * This service supports the DownloadCall interface.
 * 
 *  @author "Fred Eisele" <phreed@gmail.com>
 */
public class DownloadBoundServiceSync extends DownloadBoundService {
	static private final Logger logger = LoggerFactory.getLogger("class.service.download.bound.sync");

	final private DownloadCall.Stub stub = new DownloadCall.Stub() {
		final DownloadBoundServiceSync master = DownloadBoundServiceSync.this;

		public String downloadImage(Uri uri) throws RemoteException {
			try {
				final Bitmap bitmap = master.downloadBitmap(uri);
				final File bitmapFile = master.storeBitmap(bitmap);
				return bitmapFile.toString();

			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
				throw new RemoteException("file not found :");

			} catch (FailedDownload ex) {
				ex.printStackTrace();
				throw new RemoteException("download failed");

			} catch (IOException ex) {
				ex.printStackTrace();
				throw new RemoteException("not implemented by this service");
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
