package edu.vanderbilt.cs282.feisele;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.ProgressDialog;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * 
 * An activity which prompts the user for an image to download. <h2>Program
 * Description</h2>
 * <p>
 * The purpose of this example is to demonstrate different ways of performing
 * background tasks. The purpose of this assignment is to give you experience
 * using various Android concurrency models to download bitmap images from a web
 * server. The application works as follows:
 * <ol>
 * <li>The Activity provides a menu of buttons and displays a default image</li>
 * <li>The user is prompted to enter the URL for a new bitmap image.</li>
 * <li>After entering the desired URL, the user can select one of several
 * buttons that provide different ways to download the image concurrently.</li>
 * <li>Upon making the selection, a progress dialog is displayed when
 * downloading the designated image.</li>
 * <li>After the URL download has completed it will be displayed in an
 * ImageView.</li>
 * <li>The user can reset the image to its default contents by clicking the
 * "Reset Image" button.</li>
 * <p>
 * note: the default image is configured via an XML resource file and the
 * default image itself is part of the project's assets.
 * 
 * <h2>Fault Handling</h2>
 * If there is a problem in the entered URL a toast is displayed indicating the
 * problem.
 * 
 * <h2>Details</h2>
 * It uses four Button objects with the label "Run Runnable", "Run Messages",
 * "Run Async", and "Reset Image" to run the corresponding hook methods that use
 * the URL provided by the user to download the designated bitmap file via one
 * of the following three Android concurrency models:
 * <dl>
 * <dt>Run Runnables</dt>
 * <dd><b>Runnables and Handlers</b> model: faults are handled by posting a
 * runnable to the handler</dd>
 * <dt>Run Messages</dt>
 * <dd><b>Messages and Handlers</b> model: faults are handled by sending a
 * message to the message handler</dd>
 * <dt>Run Async</dt>
 * <dd><b>AsyncTask</b> model: faults are handled by running them on the UI
 * thread</dd>
 * </dl>
 * The Button objects that download the bitmap file are connected to the
 * corresponding <code>ThreadedDownloadActivity.run*()</code> methods via the
 * appropriate <code>android:onClick="..."</code> attributes.
 * <p>
 * 
 * @author Fred Eisele <phreed@gmail.com>
 * 
 */
public class ThreadedDownloadActivity extends LifecycleLoggingActivity {
	static private final String TAG = "Threaded Download Activity";

	/** my oldest daughter */
	static private final String DEFAULT_PORT_IMAGE = "raquel_eisele_port_2012.jpg";
	static private final String DEFAULT_LAND_IMAGE = "raquel_eisele_land_2012.jpg";

	private EditText urlEditText = null;
	private ImageView image = null;
	private Bitmap bitmapImage = null;
	private ProgressDialog progress;

	private static Handler msgHandler = null;

	/**
	 * 
	 * @param savedInstanceState
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_threaded_download);

		this.urlEditText = (EditText) findViewById(R.id.edit_image_url);
		this.image = (ImageView) findViewById(R.id.current_image);

		final Object obj = getLastNonConfigurationInstance();
		if (obj == null) {
			this.resetImage(null);
		} else {
			this.bitmapImage = (Bitmap) obj;
			this.image.setImageBitmap(this.bitmapImage);
		}
		ThreadedDownloadActivity.msgHandler = initMsgHandler(this);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return this.bitmapImage;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	/**
	 * Load the default image from the assets.
	 * 
	 * @param view
	 */
	public void resetImage(View view) {
		final AssetManager am = this.getAssets();
		final InputStream is;
		try {
			switch (this.getResources().getConfiguration().orientation) {
			case Configuration.ORIENTATION_LANDSCAPE:
				is = am.open(DEFAULT_LAND_IMAGE);
				break;
			default:
				is = am.open(DEFAULT_PORT_IMAGE);
			}
		} catch (IOException ex) {
			Toast.makeText(this, R.string.error_opening_default_image,
					Toast.LENGTH_LONG).show();
			return;
		}
		try {
			this.bitmapImage = null;
			final Bitmap bitmap = BitmapFactory.decodeStream(is);
			this.image.setImageBitmap(bitmap);
		} finally {
			try {
				is.close();
			} catch (IOException ex) {

			}
		}
	}

	/**
	 * Extract the url from the edit text widget. Check that the string in the
	 * widget is a proper url. If the field is empty then use the value provided
	 * as the hint. If the field is invalid
	 * <ul>
	 * <li>return a null indicating that the action should not be performed.</li>
	 * <li>generate a toast informing the operator of his error</li>
	 * <li>mark the field as having an error</li>
	 * </ul>
	 * 
	 * @return
	 */
	private URL getValidUrlFromWidget() {
		final Editable urlEditable = this.urlEditText.getText();
		if (urlEditable.length() < 1) {
			final String urlStr = this.urlEditText.getHint().toString();
			try {
				return new URL(urlStr);
			} catch (MalformedURLException e) {
				Log.e(TAG, "hard coded, should never happen");
				return null;
			}
		}
		final String urlStr = urlEditable.toString();
		try {
			return new URL(urlStr);
		} catch (MalformedURLException ex) {
			Log.i(TAG, "malformed url " + urlStr);
		}
		final CharSequence errorMsg = this.getResources().getText(
				R.string.error_malformed_url);
		this.urlEditText.setError(errorMsg);
		Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
		return null;
	}

	/**
	 * Indicate that the specified URL cannot be down loaded.
	 */
	private static class InvalidUriRunnable implements Runnable {
		final private ThreadedDownloadActivity parent;
		final private CharSequence msg;

		public InvalidUriRunnable(ThreadedDownloadActivity parent,
				CharSequence msg) {
			this.parent = parent;
			this.msg = msg;
		}

		public void run() {
			parent.urlEditText.setError(this.msg);
			Toast.makeText(this.parent, this.msg, Toast.LENGTH_LONG).show();
		}
	}

	private static class FailedDownload extends Exception {
		private static final long serialVersionUID = 6673968049922918951L;

		final public CharSequence msg;

		public FailedDownload(CharSequence msg) {
			super();
			this.msg = msg;
		}
	}

	/**
	 * Download the provided image url. If there is a problem an exception is
	 * raised and the calling method is expected to handle it in an appropriate
	 * manner.
	 * 
	 * @param view
	 *            the button view object (unused)
	 */
	private Bitmap downloadBitmap(URL url) throws FailedDownload {
		try {
			final InputStream is = url.openConnection().getInputStream();
			return BitmapFactory.decodeStream(is);
		} catch (IOException e) {
			throw new FailedDownload(this.getResources().getText(
					R.string.error_downloading_url));
		}
	}

	/**
	 * Initialize and configure the progress dialog.
	 * 
	 */
	private void startProgress() {
		this.progress = new ProgressDialog(this);
		this.progress.setTitle(R.string.dialog_progress_title);
		this.progress.setMessage(this.getResources().getText(
				R.string.message_progress_start));
		this.progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		this.progress.setProgress(0);
		this.progress.show();
	}

	/**
	 * The async task will be stopped if the enclosing activity is stopped. In
	 * general the intent service should be used for downloading from the
	 * internet.
	 * 
	 * @param view
	 */
	public void runAsyncTask(View view) {

		final AsyncTask<URL, Void, Bitmap> task = new AsyncTask<URL, Void, Bitmap>() {
			private final ThreadedDownloadActivity parent = ThreadedDownloadActivity.this;

			@Override
			protected Bitmap doInBackground(URL... params) {
				for (final URL url : params) {
					try {
						return parent.downloadBitmap(url);
					} catch (FailedDownload ex) {
						parent.runOnUiThread(new InvalidUriRunnable(parent,
								ex.msg));
						return null;
					}
				}
				return null;
			}

			/**
			 * Set the downloaded image.
			 */
			@Override
			protected void onPostExecute(Bitmap result) {
				parent.bitmapImage = result;
				if (result != null) {
					parent.image.setImageBitmap(result);
				}
				if (parent.progress.isShowing()) {
					parent.progress.dismiss();
				}
			}

			@Override
			protected void onPreExecute() {
				parent.startProgress();
			}

			@Override
			protected void onProgressUpdate(Void... values) {
			}
		};
		final URL url = getValidUrlFromWidget();
		if (url != null)
			task.execute(url);
	}

	/**
	 * Start a new thread to perform the download. Once the download is
	 * completed the UI thread is notified via a post to on the handler.
	 * 
	 * @param view
	 */
	public void runRunnable(View view) {
		this.startProgress();
		final URL url = getValidUrlFromWidget();

		final Thread thread = new Thread(null, new Runnable() {
			private final ThreadedDownloadActivity parent = ThreadedDownloadActivity.this;

			public void run() {
				try {
					parent.bitmapImage = parent.downloadBitmap(url);
				} catch (FailedDownload ex) {
					parent.image.post(new InvalidUriRunnable(parent, ex.msg));
					return;
				}
				parent.image.post(new Runnable() {
					public void run() {
						if (parent.bitmapImage != null) {
							parent.image.setImageBitmap(parent.bitmapImage);
						}
						if (parent.progress.isShowing()) {
							parent.progress.dismiss();
						}
					}
				});
			}
		});
		thread.start();
	}

	/**
	 * The valid message types for the handler.
	 */
	protected static enum DownloadState {
		/**
		 * indicate that the download is in progress and the progress spinner
		 * should be displayed
		 */
		SET_PROGRESS_VISIBILITY,
		/**
		 * indicate that the download is complete and so the bitmap should be
		 * displayed
		 */
		SET_BITMAP,
		/**
		 * indicate that the download has failed and so the default image should
		 * be shown
		 */
		SET_ERROR;

		static DownloadState[] lookup = DownloadState.values();
	}

	/**
	 * Make use of the message handler to keep the UI updated.
	 * 
	 */
	public void runMessages(View view) {
		final URL url = getValidUrlFromWidget();

		final Thread thread = new Thread(null, new Runnable() {
			private final ThreadedDownloadActivity parent = ThreadedDownloadActivity.this;
			private final Handler handler = ThreadedDownloadActivity.msgHandler;

			public void run() {
				final Message startMsg = handler.obtainMessage(
						DownloadState.SET_PROGRESS_VISIBILITY.ordinal(),
						ProgressDialog.STYLE_SPINNER);
				handler.sendMessage(startMsg);

				try {
					parent.bitmapImage = parent.downloadBitmap(url);
				} catch (FailedDownload ex) {
					final Message errorMsg = handler.obtainMessage(
							DownloadState.SET_ERROR.ordinal(), ex.msg);
					handler.sendMessage(errorMsg);
					return;
				}
				final Message bitmapMsg = handler.obtainMessage(
						DownloadState.SET_BITMAP.ordinal(), parent.bitmapImage);
				handler.sendMessage(bitmapMsg);
			}
		});
		thread.start();
	}

	/**
	 * Initialized the handler used by the run message method.
	 * 
	 * @return
	 */
	private static Handler initMsgHandler(final ThreadedDownloadActivity parent) {
		return new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (DownloadState.lookup[msg.what]) {
				case SET_PROGRESS_VISIBILITY: {
					parent.startProgress();
				}
					break;
				case SET_BITMAP: {
					parent.bitmapImage = (Bitmap) msg.obj;
					if (parent.bitmapImage != null) {
						parent.image.setImageBitmap(parent.bitmapImage);
					}
					if (parent.progress.isShowing()) {
						parent.progress.dismiss();
					}
				}
					break;
				case SET_ERROR: {
					final CharSequence errorMsg = (CharSequence) msg.obj;
					parent.urlEditText.setError(errorMsg);
					Toast.makeText(parent, errorMsg, Toast.LENGTH_LONG).show();
				}
					break;
				}
			}
		};
	}
}
