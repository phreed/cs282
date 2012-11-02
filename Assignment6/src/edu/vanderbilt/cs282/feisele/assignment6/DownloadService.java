package edu.vanderbilt.cs282.feisele.assignment6;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;

/**
 * The parent class for performing the work. The child classes implement the
 * specific communication mechanism.
 * 
 * @author "Fred Eisele" <phreed@gmail.com>
 */
public abstract class DownloadService extends LifecycleLoggingService {
	static private final Logger logger = LoggerFactory
			.getLogger("class.service.download.bound");

	static protected final int MAXIMUM_SIZE = 100;

	// ===========================================================
	// AIDL Implementation
	// ===========================================================

	final private DownloadRequest.Stub stub = new DownloadRequest.Stub() {
		final DownloadService master = DownloadService.this;

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
	/**
	 * The workhorse for the class. Download the provided image uri. If there is
	 * a problem an exception is raised and the calling method is expected to
	 * handle it in an appropriate manner.
	 * <p>
	 * Two types of uri are handled, url and content provider uri.
	 * <p>
	 * The url is indicated by the
	 * <p>
	 * An allowance is made for a uri of a content provider, which have a shema
	 * of "content".
	 * 
	 * @param uri
	 *            the thing to download
	 */
	protected Bitmap downloadBitmap(Uri uri) throws FailedDownload,
			FileNotFoundException, IOException {
		logger.debug("downloadBitmap:");
		final Bitmap bitmap;
		try {
			final String scheme = uri.getScheme();
			if ("http".equals(scheme)) {
				final InputStream is = new URL(uri.toString()).openStream();
				bitmap = BitmapFactory.decodeStream(is);
			} else {
				return null;
			}
			logger.debug("bitmap size [{}:{}]", bitmap.getWidth(),
					bitmap.getHeight());
			return bitmap;
		} catch (UnknownHostException ex) {
			logger.warn("download failed bad host", ex);
			throw new FailedDownload(this.getResources().getText(
					R.string.error_downloading_url));
		} catch (IOException ex) {
			logger.warn("download failed ?", ex);
			throw new FailedDownload(this.getResources().getText(
					R.string.error_downloading_url));
		}
	}

	/**
	 * An exception class used when there is a problem with the download.
	 */
	public static class FailedDownload extends Exception {
		private static final long serialVersionUID = 6673968049922918951L;

		final public CharSequence msg;

		@Override
		public String getMessage() {
			return new StringBuilder().append(this.msg).toString();
		}

		public FailedDownload(CharSequence msg) {
			super();
			this.msg = msg;
		}
	}

	/**
	 * In order to preserve security for this object it would be good if only a
	 * file descriptor were provided (the file being immediately deleted).
	 * However, I have been unable to properly return a ParcelFileDescriptor,
	 * therefore this method (and its calling routines) work with the file by
	 * name. The requesting application is expected to delete the file as
	 * needed.
	 * 
	 * @param bitmap
	 * @return the temporary file by name.
	 */
	@TargetApi(9)
	protected File storeBitmap(Bitmap bitmap) {
		final File cacheDir = this.getCacheDir();
		File tempFile = null;
		try {
			tempFile = File.createTempFile("download", "tmp", cacheDir);
			final ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 40, outBytes);

			final FileOutputStream fo = new FileOutputStream(tempFile);
			fo.write(outBytes.toByteArray());
			fo.close();
			outBytes.close();
			if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
				tempFile.setReadable(true, false);
				tempFile.setWritable(true, false);
			}
			return tempFile;
		} catch (IOException ex) {
			logger.error("could not write bitmap file {}", tempFile);
		}
		return null;
	}

}
