package edu.vanderbilt.cs282.feisele.assignment6;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import edu.vanderbilt.cs282.feisele.assignment6.DownloadContentProviderSchema.ImageTable;

/**
 * The parent class for performing the work. The child classes implement the
 * specific communication mechanism.
 * <p>
 * 
 * Given that, only one content provider is accessed and in the same process,
 * two fast mechanisms are available for communicating with the content
 * provider. The local binder and the content provider client.
 * 
 * 
 * @author "Fred Eisele" <phreed@gmail.com>
 */
public abstract class DownloadService extends LifecycleLoggingService {
	static private final Logger logger = LoggerFactory
			.getLogger("class.service.download.bound");

	static protected final int MAXIMUM_SIZE = 100;

	/** this intent action indicates that the bind request is for a local binder */
	public static final boolean ACTION_LOCAL = false;

	/**
	 * The content provider client object is obtained.
	 */
	private ContentProviderClient cpc = null;

	@Override
	public void onCreate() {
		super.onCreate();
		this.cpc = this.getContentResolver().acquireContentProviderClient(
				DownloadContentProviderSchema.AUTHORITY);
	}

	/**
	 * AIDL Stub
	 */
	final private DownloadRequest.Stub stub = new DownloadRequest.Stub() {
		final DownloadService master = DownloadService.this;

		/**
		 * The download image method acquires the images and loads them into the
		 * content provider.
		 */
		public void downloadImage(Uri uri, DownloadCallback callback)
				throws RemoteException {
			try {
				final Bitmap bitmap = master.downloadBitmap(uri);

				master.storeBitmap(uri, bitmap);

				callback.sendPath(uri.toString());

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
		if (DownloadService.ACTION_LOCAL) {
			return this.localBinder;
		}
		return this.stub;
	}

	/**
	 * Used when making local connections to the service.
	 */
	private final IBinder localBinder = new LocalBinder();

	public class LocalBinder extends Binder {
		DownloadService getService() {
			return DownloadService.this;
		}
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

	protected void storeBitmap(final Uri uri, final Bitmap bitmap) {
		try {
			final ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 40, outBytes);

			final ContentValues cv = new ContentValues();
			cv.put(ImageTable.URI.title, uri.toString());
			final Uri tupleUri = this.cpc.insert(ImageTable.CONTENT_URI, cv);
			final ParcelFileDescriptor pfd = this.cpc.openFile(tupleUri, "w");

			final FileOutputStream fileOutputStream = new FileOutputStream(
					pfd.getFileDescriptor());
			fileOutputStream.write(outBytes.toByteArray());
			fileOutputStream.close();
			outBytes.close();

		} catch (IOException ex) {
			logger.error("could not write bitmap file {}", uri, ex);
		} catch (RemoteException ex) {
			logger.error("remote exception write bitmap file {}", uri, ex);
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

}
