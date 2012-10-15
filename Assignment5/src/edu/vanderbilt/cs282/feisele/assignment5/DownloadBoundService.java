package edu.vanderbilt.cs282.feisele.assignment5;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.UnknownHostException;

import edu.vanderbilt.cs282.feisele.R;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

public abstract class DownloadBoundService extends LifecycleLoggingService {
	static private final String TAG = "Threaded Download Service";

	static protected final int MAXIMUM_SIZE = 100;

	/**
	 * The workhorse for the class. Download the provided image url. If there is
	 * a problem an exception is raised and the calling method is expected to
	 * handle it in an appropriate manner.
	 * 
	 * @param uri
	 *            the thing to download
	 */
	protected Bitmap downloadBitmap(Uri uri) throws FailedDownload,
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
		} catch (UnknownHostException ex) {
			Log.w(TAG, "download failed bad host", ex);
			throw new FailedDownload(this.getResources().getText(
					R.string.error_downloading_url));
		} catch (IOException ex) {
			Log.w(TAG, "download failed ?", ex);
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
	 * In order to preserve security for this object only a file descriptor is
	 * provided. The file is immediately deleted.
	 * 
	 * @param bitmap
	 * @return
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
			Log.e(TAG, "could not write bitmap file " + tempFile);
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

		final Uri uri = intent.getData();
		if (uri == null) {
			Log.e(TAG, "null uri provided");
			return Service.START_NOT_STICKY;
		}
		Log.d(TAG, "process " + uri.toString());

		return Service.START_NOT_STICKY;
	}

}
