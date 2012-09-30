package edu.vanderbilt.cs282.feisele;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.util.Log;
import android.util.SparseArray;

public class ThreadedDownloadService extends LifecycleLoggingService {
	static private final String TAG = "Threaded Download Service";

	static public final String DOWNLOAD_METHOD = "edu.vanderbilt.cs282.feisele.DOWNLOAD_METHOD";

	static public final String PENDING_INTENT_KEY = "edu.vanderbilt.cs282.feisele.pending_intent";
	static public final String BROADCAST_INTENT_ACTION = "edu.vanderbilt.cs282.feisele.DOWNLOAD_COMPLETE_ACTION";
	static public final String MESSENGER_KEY = "edu.vanderbilt.cs282.feisele.broadcast_intent";
	static public final int RESULT_BITMAP_ID = 12;
	static public final String RESULT_BITMAP_FILE = "edu.vanderbilt.cs282.feisele.bitmap_file_descriptor";

	static public final int MAXIMUM_SIZE = 100;

	static public enum DownloadMethod implements Parcelable {
		/** Thread and Messenger model */
		THREAD_MESSENGER(0x01, "edu.vanderbilt.cs282.feisele.THREAD_MESSENGER"),
		/** Thread and PendingIntent model */
		THREAD_PENDING_INTENT(0x02,
				"edu.vanderbilt.cs282.feisele.THREAD_PENDING_INTENT"),
		/** AsyncTask with BroadcastReceiver */
		ASYNC_TASK_BROADCAST(0x03,
				"edu.vanderbilt.cs282.feisele.ASYNC_TASK_BROADCAST");

		final public int nominal;
		final public String key;

		private DownloadMethod(int nominal, String key) {
			this.nominal = nominal;
			this.key = key;
		}

		static final public Map<String, DownloadMethod> lookup = new HashMap<String, DownloadMethod>(
				3);
		static final public SparseArray<DownloadMethod> lookupByNominal = new SparseArray<DownloadMethod>(
				3);
		static {
			for (DownloadMethod action : DownloadMethod.values()) {
				DownloadMethod.lookupByNominal.put(action.nominal, action);
				DownloadMethod.lookup.put(action.key, action);
			}
		}

		public int describeContents() {
			return 0;
		}

		public void writeToParcel(Parcel dest, int flags) {
			dest.writeInt(this.nominal);
		}

		public static final Creator<DownloadMethod> CREATOR = new Creator<DownloadMethod>() {
			public DownloadMethod createFromParcel(Parcel source) {
				return DownloadMethod.lookupByNominal.get(source.readInt());
			}

			public DownloadMethod[] newArray(int size) {
				return new DownloadMethod[size];
			}
		};

		public Parcelable asParcelable() {
			return (Parcelable) this;
		}
	}

	/**
	 * The workhorse for the class. Download the provided image url. If there is
	 * a problem an exception is raised and the calling method is expected to
	 * handle it in an appropriate manner.
	 * 
	 * @param uri
	 *            the thing to download
	 */
	private Bitmap downloadBitmap(Uri uri) throws FailedDownload,
			FileNotFoundException, IOException {
		Log.d(TAG, "downloadBitmap:");
		final Bitmap bitmap;
		try {
			final String scheme = uri.getScheme();
			if ("http".equals(scheme)) {
				final URL url = new URL(uri.toString());
				final InputStream is = url.openConnection().getInputStream();
				bitmap = BitmapFactory.decodeStream(is);

			} else if ("content".equals(scheme)) {
				final InputStream detection = this.getContentResolver()
						.openInputStream(uri);
				final BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
				onlyBoundsOptions.inJustDecodeBounds = true;
				onlyBoundsOptions.inDither = true;
				onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
				BitmapFactory.decodeStream(detection, null, onlyBoundsOptions);
				detection.close();
				Log.d(TAG,
						"bitmap size:"
								+ Integer.toString(onlyBoundsOptions.outWidth)
								+ " : "
								+ Integer.toString(onlyBoundsOptions.outWidth));
				if (onlyBoundsOptions.outWidth == -1)
					return null;
				if (onlyBoundsOptions.outHeight == -1)
					return null;

				final int majorSize = Math.max(onlyBoundsOptions.outHeight,
						onlyBoundsOptions.outWidth);
				final int ratio = (majorSize < MAXIMUM_SIZE) ? 1 : (int) Math
						.floor(majorSize / MAXIMUM_SIZE);

				final BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
				final int base2Scale = Integer.highestOneBit(ratio);
				bitmapOptions.inSampleSize = (base2Scale < 1) ? 1 : base2Scale;
				bitmapOptions.inDither = true;// optional
				bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
				final InputStream input = this.getContentResolver()
						.openInputStream(uri);
				bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
				input.close();
			} else {
				return null;
			}
			Log.d(TAG, "bitmap size [" + Integer.toString(bitmap.getWidth())
					+ " : " + Integer.toString(bitmap.getHeight()) + "]");
			return bitmap;
		} catch (IOException ex) {
			Log.w(TAG, "download failed", ex);
			throw new FailedDownload(this.getResources().getText(
					R.string.error_downloading_url));
		}
	}

	/**
	 * An exception class used when there is a problem with the download.
	 */
	/* package */static class FailedDownload extends Exception {
		private static final long serialVersionUID = 6673968049922918951L;

		final public CharSequence msg;

		public FailedDownload(CharSequence msg) {
			super();
			this.msg = msg;
		}
	}


	/**
	 * Report problems with downloading the image back to the parent activity.
	 * 
	 * @param errorMsg
	 */
	private void reportDownloadFault(CharSequence errorMsg) {

	}

	/**
	 * <p>
	 * AsyncTask with BroadcastReceiver model ("Run Async Receiver"). In this
	 * model the ThreadedDownloadService process executes an AsyncTask that
	 * downloads the designated bitmap file, stores it in the Android file
	 * system, and use sendBroadcast() to send the filename back to a
	 * BroadcastReceiver in the DownloadActivity process, which opens the file
	 * and displays the bitmap on the screen.
	 * 
	 * @param intent
	 */
	public int downloadWithAsyncTaskViaBroadcastIntent(final Uri uri,
			final Bundle extras) {
		Log.d(TAG, "downloadWithAsyncTaskViaBroadcastIntent");
		(new AsyncTask<Uri, Void, Bitmap>() {
			private final ThreadedDownloadService master = ThreadedDownloadService.this;

			@Override
			protected Bitmap doInBackground(Uri... params) {
				if (params.length < 1)
					return null;
				final Uri uri = params[0];
				try {
					return master.downloadBitmap(uri);
				} catch (FailedDownload ex) {
					master.reportDownloadFault(ex.msg);
				} catch (FileNotFoundException ex) {
					master.reportDownloadFault(ex.getLocalizedMessage());
				} catch (IOException ex) {
					master.reportDownloadFault(ex.getLocalizedMessage());
				}
				return null;
			}

			/**
			 * Provide the down loaded image as a file descriptor.
			 */
			@Override
			protected void onPostExecute(Bitmap result) {
				final Intent send = new Intent(BROADCAST_INTENT_ACTION);
				final File bitmapFile = master.storeBitmap(result);
				try {
					final String bitmapFilePath = bitmapFile.getCanonicalPath();
					Log.d(TAG, "bitmap file name "+ bitmapFilePath);
					send.putExtra(RESULT_BITMAP_FILE, bitmapFilePath);
				} catch (IOException ex) {
					Log.w(TAG, "could not find file "+bitmapFile.toString(), ex);
				}
				master.sendBroadcast(send);
			}

		}).execute(uri);
		return Service.START_NOT_STICKY;
	}

	/**
	 * Start a new thread to perform the download. Once the download is
	 * completed the UI thread is notified via a post to on the handler.
	 * <p>
	 * Thread and Messenger model ("Run Thread Messenger"). In this model the
	 * ThreadedDownloadService process starts a thread that downloads the
	 * designated bitmap file, stores it in the Android file system, and use a
	 * Messenger to send the filename back to the DownloadActivity process,
	 * which opens the file and displays the bitmap on the screen.
	 * 
	 * @param uri
	 * @return
	 */
	public int downloadWithThreadViaMessage(final Uri uri, final Bundle extras) {
		Log.d(TAG, "downloadWithThreadViaMessage");
		if (extras == null)
			return Service.START_NOT_STICKY;

		(new Thread(null, new Runnable() {
			private final ThreadedDownloadService master = ThreadedDownloadService.this;
			final Uri uri_ = uri;

			public void run() {
				final Bitmap bitmap;
				try {
					bitmap = master.downloadBitmap(uri_);
				} catch (FailedDownload ex) {
					master.reportDownloadFault(ex.msg);
					return;
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		})).start();
		return Service.START_NOT_STICKY;
	}

	/**
	 * Make use of the message handler to keep the UI updated. Thread and
	 * PendingIntent model ("Run Thread Pending Intent"). In this model the
	 * ThreadedDownloadService process starts a thread that downloads the
	 * designated bitmap file, stores it in the Android file system, and use a
	 * PendingIntent to send the filename back to the DownloadActivity process
	 * via it's onActivityResult() method, which opens the file and displays the
	 * bitmap on the screen.
	 * 
	 * @return
	 * 
	 */
	public int downloadWithThreadViaPendingIntent(final Uri uri,
			final Bundle extras) {
		Log.d(TAG, "downloadWithThreadViaPendingIntent");

		if (extras == null)
			return Service.START_NOT_STICKY;
		final Object piobj = extras.get(PENDING_INTENT_KEY);
		if (!(piobj instanceof PendingIntent)) {
			return Service.START_NOT_STICKY;
		}
		final PendingIntent pendingIntent = (PendingIntent) piobj;

		(new Thread(null, new Runnable() {
			private final ThreadedDownloadService master = ThreadedDownloadService.this;
			private final Uri uri_ = uri;

			public void run() {
				try {
					final Bitmap bitmap = master.downloadBitmap(uri_);
					final File bitmapFile = master.storeBitmap(bitmap);
					pendingIntent.send(master, RESULT_BITMAP_ID,
							new Intent().putExtra(RESULT_BITMAP_FILE, bitmapFile));
				} catch (FileNotFoundException ex) {
					ex.printStackTrace();
				} catch (FailedDownload ex) {
					try {
						pendingIntent.send(master, RESULT_BITMAP_ID, null);
					} catch (CanceledException ex1) {
						Log.e(TAG, "download failed and report failed", ex1);
					}
				} catch (IOException ex) {
					Log.e(TAG, "download io exception", ex);
				} catch (CanceledException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
			}
		})).start();
		return Service.START_NOT_STICKY;
	}

	/**
	 * In order to preserve security for this object only a file descriptor is
	 * provided. The file is immediately deleted.
	 * 
	 * @param bitmap
	 * @return
	 */
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
			tempFile.setReadable(true, false);
			tempFile.setWritable(true, false);
			return tempFile;
		} catch (IOException ex) {
			Log.e(TAG, "could not write bitmap file "+tempFile);
		}
		return null;
	}
	/**
	 * In order to preserve security for this object only a file descriptor is
	 * provided. The file is immediately deleted.
	 * 
	 * @param bitmap
	 * @return
	 */
	protected ParcelFileDescriptor storeBitmapGetPFD(Bitmap bitmap) {
		final File tempFile = this.storeBitmap(bitmap);
		try {
			final ParcelFileDescriptor pdf = ParcelFileDescriptor.open(
					tempFile, ParcelFileDescriptor.MODE_READ_ONLY);
			//tempFile.delete();
			return pdf;
		} catch (IOException ex) {
		}
		return null;
	}

	/**
	 * This method
	 * <ol>
	 * <li>receives the request from the application/fragment,
	 * <li>determines which type of processing is desired
	 * <li>calls the appropriate method with the intent.
	 * </ol>
	 * 
	 * @param intent
	 * @param flags
	 * @param startId
	 * @return
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand: Received start id " + startId + ": "
				+ intent);
		final DownloadMethod method = intent
				.getParcelableExtra(DOWNLOAD_METHOD);
		if (method == null) {
			Log.e(TAG, "invalid action supplied");
			return Service.START_NOT_STICKY;
		}

		final Uri uri = intent.getData();
		Log.d(TAG, "process " + method + " : " + uri.toString());
		final Bundle extras = intent.getExtras();

		switch (method) {
		case THREAD_MESSENGER:
			return this.downloadWithThreadViaMessage(uri, extras);
		case THREAD_PENDING_INTENT:
			return this.downloadWithThreadViaPendingIntent(uri, extras);
		case ASYNC_TASK_BROADCAST:
			return this.downloadWithAsyncTaskViaBroadcastIntent(uri, extras);
		}
		return Service.START_NOT_STICKY;
	}

}
