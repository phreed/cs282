package edu.vanderbilt.cs282.feisele;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.ProgressDialog;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
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
 * <dd><b>Runnables and Handlers</b> model</dd>
 * <dt>Run Messages</dt>
 * <dd><b>Messages and Handlers</b> model</dd>
 * <dt>Run Async</dt>
 * <dd><b>AsyncTask</b> model</dd>
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
	static private final String DEFAULT_IMAGE = "raquel_eisele_2012.jpg";

	private EditText urlEditText = null;
	private ImageView image = null;
	private ProgressDialog progress;

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

		this.resetImage(null);
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
			is = am.open(DEFAULT_IMAGE);
		} catch (IOException ex) {
			Toast.makeText(this, R.string.error_opening_default_image,
					Toast.LENGTH_LONG).show();
			return;
		}
		final Bitmap bm;
		try {
			bm = BitmapFactory.decodeStream(is);
			this.image.setImageBitmap(bm);
		} finally {
			try {
				is.close();
			} catch (IOException ex) {

			}
		}

	}

	/**
	 * Extract the url from the edit text widget.
	 * Check that the string in the widget is a proper url.
	 * If the field is empty then use the value provided as the hint.
	 * If the field is invalid
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
			Log.i(TAG, "malformed url "+urlStr);
		}
		final CharSequence errorMsg = this.getResources().getText(R.string.error_malformed_url);
		this.urlEditText.setError(errorMsg);
		Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
		return null;
	}
	
	/** 
	 * Indicate that the specified URL cannot be downloaded.
	 */
	private static class InvalidUriRunnable implements Runnable {
		final private ThreadedDownloadActivity parent;
		final private CharSequence msg;
		public InvalidUriRunnable(ThreadedDownloadActivity parent, CharSequence msg) {
			this.parent = parent;
			this.msg = msg;
		}
		public void run() {
			parent.urlEditText.setError(this.msg);
			Toast.makeText(this.parent, this.msg, Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Performed when the show location button is clicked. - extract and
	 * validate the latitude and longitude - try starting activities with
	 * various intents
	 * 
	 * @param view
	 *            the button view object (unused)
	 */
	private Bitmap downloadBitmap(URL url) throws InterruptedException {
		try {
			final InputStream is = url.openConnection().getInputStream();
			return BitmapFactory.decodeStream(is);
		} catch (IOException e) {
			final CharSequence errorMsg = this.getResources().getText(R.string.error_downloading_url);
			this.runOnUiThread(new InvalidUriRunnable(this, errorMsg)); 
			return null;
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
					} catch (InterruptedException ex) {
						Toast.makeText(parent,
								R.string.error_loading_via_async_task,
								Toast.LENGTH_LONG).show();
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
				if (parent.progress.isShowing()) {
					parent.progress.dismiss();
				}
				if (result == null) return;
				parent.image.setImageBitmap(result);
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
		if (url != null) task.execute(url);
	}

	/**
	 * Add runnable to the message queue
	 * 
	 * @param view
	 */
	public void runRunnable(View view) {

		Toast.makeText(this, R.string.error_loading_via_runnable,
				Toast.LENGTH_LONG).show();
	}

	public void runMessages(View view) {
		Toast.makeText(this, R.string.error_loading_via_messages,
				Toast.LENGTH_LONG).show();
	}

}
