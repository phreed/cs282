package edu.vanderbilt.cs282.feisele;

import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import edu.vanderbilt.cs282.feisele.ThreadedDownloadFragment.OnDownloadFault;

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
public class ThreadedDownloadActivity extends LifecycleLoggingActivity
		implements OnDownloadFault {
	static private final String TAG = "Threaded Download Activity";

	private EditText urlEditText = null;

	private ThreadedDownloadFragment imageFragment = null;

	/**
	 * The fragment is used to preserve state across various changes.
	 * <dl>
	 * <dt>orientation</dt>
	 * <dt>startActivity</dt>
	 * <dd>configuration change doesn't handle properly</dt>
	 * <dt>keyboard</dt>
	 * </dl>
	 * <p>
	 * 
	 * @param savedInstanceState
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.threaded_download);

		this.urlEditText = (EditText) findViewById(R.id.edit_image_url);

		final FragmentManager fm = this.getSupportFragmentManager();
		final Fragment fobj = fm.findFragmentById(R.id.fragment_container);
		if (fobj == null) {
			final FragmentTransaction txn = fm.beginTransaction();
			this.imageFragment = new ThreadedDownloadFragment();
			txn.add(R.id.fragment_container, this.imageFragment);
			txn.commit();
		} else {
			this.imageFragment = (ThreadedDownloadFragment) fobj;
		}
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
	 * Load the default image from the assets.
	 * 
	 * @param view
	 */
	public void resetImage(View view) {
		if (this.imageFragment == null)
			return;
		this.imageFragment.resetImage(view);
	}

	/**
	 * Each of the following actions will make use of a background thread.
	 * 
	 * @param view
	 */
	public void runAsyncTask(View view) {
		if (this.imageFragment == null)
			return;
		final URL url = getValidUrlFromWidget();
		this.imageFragment.runAsyncTask(view, url);
	}

	public void runMessages(View view) {
		if (this.imageFragment == null)
			return;
		final URL url = getValidUrlFromWidget();
		this.imageFragment.runMessages(view, url);
	}

	public void runRunnable(View view) {
		if (this.imageFragment == null)
			return;
		final URL url = getValidUrlFromWidget();
		this.imageFragment.runRunnable(view, url);
	}

	/**
	 * Should any of the downloads fail produce a warning indicator on the url
	 * field. As this is most likely called from the background thread
	 * performing the download the field update is forced to the ui thread.
	 */
	public void onFault(final CharSequence msg) {
		this.runOnUiThread(new Runnable() {
			final CharSequence msg_ = msg;
			final ThreadedDownloadActivity master = ThreadedDownloadActivity.this;

			public void run() {
				final Drawable dr = master.getResources().getDrawable(
						R.drawable.indicator_input_warn);
				dr.setBounds(0, 0, dr.getIntrinsicWidth(),
						dr.getIntrinsicHeight());
				master.urlEditText.setError(msg_, dr);
			}
		});
	}

}
