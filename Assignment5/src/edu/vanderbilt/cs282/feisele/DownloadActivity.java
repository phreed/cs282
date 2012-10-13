package edu.vanderbilt.cs282.feisele;

import java.net.MalformedURLException;
import java.net.URL;

import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import edu.vanderbilt.cs282.feisele.DownloadFragment.OnDownloadHandler;

/**
 * 
 * An activity which prompts the user for an image to download. <h2>Program
 * Description</h2>
 * <p>
 * This assignment builds upon the various Android concurrency models from the
 * third and fourth assignments to give you experience using an Android Service
 * to download bitmap images from a web server and display them via an Activity.
 * The application has the same interface as in assignments 3 and 4 and works as
 * follows:
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
 * <ul>
 * <li>
 * It contains a DownloadActivity class that inherits from Activity and uses the
 * XML layout containing a TextView object that prompts for the URL of the
 * bitmap file and stores the entered URL in an EditText object.
 * <li>
 * It uses three Button objects with the labels "Run Sync AIDL",
 * "Run Async AIDL", and "Reset Image" to run the corresponding hook methods
 * that use the URL provided by the user to start a service that downloads the
 * designated bitmap file via the following two Android concurrency and response
 * models
 * 
 * <li>
 * The service components must run in a separate process than the
 * DownloadActivity component.
 * <li>
 * The Button objects that initiate the downloading of the bitmap file must be
 * connected to the corresponding DownloadActivity.run*() methods via the
 * appropriate android:onClick="..." attributes.
 * </ul>
 * 
 * 
 * @author "Fred Eisele" <phreed@gmail.com>
 * 
 */
public class DownloadActivity extends LifecycleLoggingActivity implements
		OnDownloadHandler 
{
	static private final String TAG = "Threaded Download Activity";

	private EditText urlEditText = null;

	private DownloadFragment imageFragment = null;
	private ProgressDialog progress;

	/**
	 * The fragment is used to preserve state across various changes.
	 * <p>
	 * In this implementation fragment is not intimately tied to a single
	 * activity instance. When the activity is restarted a new activity instance
	 * is (may be) created. The fragment persists across this restart and it
	 * must be attached to the new activity instance. For this reason the
	 * convenient "<fragment>" xml element cannot be used. A ViewGroup (in this
	 * case a FrameLayout is used.
	 * <p>
	 * 
	 * @see http 
	 *      ://developer.android.com/training/basics/fragments/fragment-ui.html
	 *      #AddAtRuntime
	 * 
	 * @param savedInstanceState
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aidl_service_download);

		this.urlEditText = (EditText) findViewById(R.id.edit_image_url);

		final FragmentManager fm = this.getSupportFragmentManager();
		final Fragment fobj = fm.findFragmentById(R.id.fragment_container);
		if (fobj == null) {
			final FragmentTransaction txn = fm.beginTransaction();
			this.imageFragment = new DownloadFragment();
			txn.add(R.id.fragment_container, this.imageFragment);
			txn.commit();
		} else {
			this.imageFragment = (DownloadFragment) fobj;
		}

	}

	final static String PROGRESS_RUNNING_STATE_KEY = "progress_running_state_key";

	/**
	 * If the download is ongoing then the progress indicator will need to be
	 * started. Set a flag in the fragment indicating that there is a pending
	 * download as well.
	 */
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		final boolean wasProgressRunning = savedInstanceState
				.getBoolean(PROGRESS_RUNNING_STATE_KEY);
		if (wasProgressRunning)
			Log.v(TAG, "progress was running ");
		final boolean isDownloadStillPending = this.imageFragment.downloadPending
				.get();
		if (wasProgressRunning & isDownloadStillPending)
			this.startProgress("progress still pending");
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		final boolean progressIsRunning = this.isProgressRunning();
		this.imageFragment.downloadPending.set(progressIsRunning);

		savedInstanceState.putBoolean(PROGRESS_RUNNING_STATE_KEY,
				this.isProgressRunning());
		super.onSaveInstanceState(savedInstanceState);
		Log.d(TAG, "onSaveInstanceState");
	}

	/**
	 * Shut things down that aren't needed.
	 */
	@Override
	public void onStop() {
		super.onStop();
		this.stopProgress();
	}

	/**
	 * Initialize and configure the progress dialog. Record the fact that a
	 * download is expected in the fragment.
	 */
	private void startProgress(CharSequence msg) {
		Log.d(TAG, "startProgress");
		this.imageFragment.downloadPending.set(true);

		this.progress = new ProgressDialog(this);
		this.progress.setTitle(R.string.dialog_progress_title);
		this.progress.setMessage(msg);
		this.progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		this.progress.setProgress(0);
		this.progress.show();
	}

	/**
	 * The progress can be null when the activity is stopped. The progress is
	 * not dismissed unless it is showing. Record the fact that progress in
	 * complete in the fragment.
	 */
	private void stopProgress() {
		Log.d(TAG, "stopProgress");
		this.imageFragment.downloadPending.set(false);
		if (this.isProgressRunning())
			this.progress.dismiss();
	}

	/**
	 * Check to see if the progress indicator is active.
	 * 
	 * @return
	 */
	private boolean isProgressRunning() {
		if (this.progress == null)
			return false;
		if (!this.progress.isShowing())
			return false;
		return true;
	}

	/**
	 * Progress dialog can be shut down the
	 */
	public void onComplete() {
		Log.d(TAG, "onComplete");
		this.runOnUiThread(new Runnable() {
			final DownloadActivity master = DownloadActivity.this;

			public void run() {
				master.stopProgress();
			}
		});
	}

	/**
	 * Should any of the downloads fail produce a warning indicator on the url
	 * field. As this is most likely called from the background thread
	 * performing the download the field update is forced to the ui thread.
	 */
	public void onFault(final CharSequence msg) {
		this.runOnUiThread(new Runnable() {
			final CharSequence msg_ = msg;
			final DownloadActivity master = DownloadActivity.this;

			public void run() {
				Log.d(TAG, "onFault");
				Toast.makeText(master, msg, Toast.LENGTH_LONG).show();

				final Drawable dr = master.getResources().getDrawable(
						R.drawable.indicator_input_warn);
				dr.setBounds(0, 0, dr.getIntrinsicWidth(),
						dr.getIntrinsicHeight());
				master.urlEditText.setError(msg_, dr);

				master.stopProgress();
			}
		});
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
	private Uri getValidUrlFromWidget() {
		final Editable urlEditable = this.urlEditText.getText();
		if (urlEditable.length() < 1) {
			return getUrlFromHint(this.urlEditText.getHint());
		}
		final String uriStr = urlEditable.toString();
		if (uriStr == null) {
			return getUrlFromHint(this.urlEditText.getHint());
		}
		try {
			/** a cheap parse, not exactly correct, I'll get this next time */
			new URL(uriStr);
			return Uri.parse(uriStr);
		} catch (MalformedURLException e) {
			Log.w(TAG, "bad uri string");
		}
		final CharSequence errorMsg = this.getResources().getText(
				R.string.error_malformed_url);
		this.urlEditText.setError(errorMsg);
		Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
		return null;
	}

	/**
	 * If the operator has not actually entered a uri then get the one provided
	 * as a hint.
	 * 
	 * @param seq
	 * @return
	 */
	private Uri getUrlFromHint(final CharSequence seq) {
		final String uriStr = seq.toString();
		if (uriStr == null) {
			return Uri.parse(this.getResources()
					.getText(R.string.prompt_image_url).toString());
		}
		return Uri.parse(uriStr);
	}

	/**
	 * Load the default image from the assets.
	 * 
	 * @param view
	 */
	public void resetImage(View view) {
		Log.d(TAG, "resetImage");
		this.imageFragment.resetImage(view);
	}

	/**
	 * Sync AIDL model ("Run Sync AIDL").
	 * 
	 * @param view
	 */
	public void runDownloadSyncAidl(View view) {
		Log.d(TAG, "runDownloadSyncAidl");
		final Uri uri = this.getValidUrlFromWidget();
		if (uri == null) return;
		
		this.imageFragment.downloadSyncAidl(uri);
		
		this.startProgress(this.getResources().getText(
				R.string.message_progress_sync));
	}

	/**
	 * Asynchronous AIDL model ("Run Async AIDL").
	 * 
	 * @param view
	 */
	public void runDownloadAsyncAidl(View view) {
		Log.d(TAG, "runDownloadAsyncAidl");
		final Uri uri = this.getValidUrlFromWidget();
		if (uri == null) return;
		this.imageFragment.downloadAsyncAidl(uri);
		
		this.startProgress(this.getResources().getText(
				R.string.message_progress_async));
	}

}
