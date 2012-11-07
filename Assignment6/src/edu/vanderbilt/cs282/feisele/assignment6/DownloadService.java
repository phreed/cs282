package edu.vanderbilt.cs282.feisele.assignment6;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.UnknownHostException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
import edu.vanderbilt.cs282.feisele.assignment6.DownloadContentProviderSchema.ContentAction;
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
public class DownloadService extends LLService {
	static private final Logger logger = LoggerFactory
			.getLogger("class.service.download");

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
			logger.info("download request received {}", uri);

			try {
				final String title = master.downloadImages(master, uri);
				callback.sendPath(title);

			} catch (FileNotFoundException ex) {
				logger.info("file not found ", ex);
				callback.sendFault("file not found :");

			} catch (FailedDownload ex) {
				logger.info("download failed ", ex);
				callback.sendFault("download failed");

			} catch (IOException ex) {
				logger.info("io failed ", ex);
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
	 * The uri is presumed to be an http page containing elements like the
	 * following: <code>
	 * <img 
	 *   src="http://somethingscrawlinginmyhair.com/wp-content/uploads/2012/10/Wasp.spider.and_.nearby.male_.jpg" 
	 *   alt="" title="Wasp.spider.and.nearby.male" 
	 *   width="600" 
	 *   height="334" 
	 *   class="alignnone size-full wp-image-7431" />
	 *   </code>
	 * 
	 * <p>
	 * The url is indicated by the
	 * <p>
	 * An allowance is made for a uri of a content provider, which have a shema
	 * of "content".
	 * 
	 * @param master
	 * 
	 * @param uri
	 *            the thing to download
	 */
	protected String downloadImages(DownloadService master, Uri uri)
			throws FailedDownload, FileNotFoundException, IOException {
		logger.debug("download images:");

		try {
			final String scheme = uri.getScheme();
			if ("http".equals(scheme)) {
				master.clearImages(uri);

				final Document doc = Jsoup.connect(uri.toString()).get();
				final String title = doc.title();
				final Elements imageUrlSet = doc.select("img[src]");
				for (Element imageUrl : imageUrlSet) {
					final InputStream is = new URL(imageUrl.text())
							.openStream();
					final Bitmap bitmap = BitmapFactory.decodeStream(is);
					logger.debug("bitmap size [{}:{}]", bitmap.getWidth(),
							bitmap.getHeight());
					master.storeBitmap(uri, bitmap);
				}
				return title;
			} else {

			}
			return uri.toString();
		} catch (UnknownHostException ex) {
			logger.warn("download failed bad host", ex);
			throw new FailedDownload(uri, this.getResources().getText(
					R.string.error_downloading_url));
		} catch (IOException ex) {
			logger.warn("download failed ?", ex);
			throw new FailedDownload(uri, this.getResources().getText(
					R.string.error_downloading_url));
		}
	}

	/**
	 * clear out any prior images having the same uri.
	 * 
	 * @param uri
	 */
	private void clearImages(Uri uri) {
		try {
			this.cpc.delete(ImageTable.CONTENT_URI,
					ContentAction.DELETE_BY_URI.code,
					new String[] { uri.toString() });
		} catch (RemoteException ex) {
			logger.error("could not expunge the old values {}", ex);
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

		final public Uri uri;
		final public CharSequence msg;

		@Override
		public String getMessage() {
			return new StringBuilder().append("uri=<").append(this.uri)
					.append(">").append(" msg=<").append(this.msg).append(">")
					.toString();
		}

		public FailedDownload(final Uri uri, final CharSequence msg) {
			super();
			this.uri = uri;
			this.msg = msg;
		}
	}

}
