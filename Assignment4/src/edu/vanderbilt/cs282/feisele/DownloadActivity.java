package edu.vanderbilt.cs282.feisele;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Messenger;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import edu.vanderbilt.cs282.feisele.DownloadFragment.OnDownloadHandler;
import edu.vanderbilt.cs282.feisele.ThreadedDownloadService.DownloadMethod;

/**
 * 
 * An activity which prompts the user for an image to download. <h2>Program
 * Description</h2>
 * <p>
 * This assignment builds upon the various Android concurrency models from third
 * assignment to give you experience using an Android Service to download bitmap
 * images from a web server and display them via an Activity. The application
 * has the same interface as in assignment 3 and works as follows:
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
 * It uses four Button objects with the labels "Run Thread Messenger",
 * "Run Thread PendingIntent", "Run Async Receiver", and "Reset Image" to run
 * the corresponding hook methods that use the URL provided by the user to start
 * a ThreadedDownloadService that downloads the designated bitmap file via the
 * following two Android concurrency and response models
 * <li>
 * The DownloadService component must run in a separate process than the
 * DownloadActivity component.
 * <li>
 * The Button objects that initiate the downloading of the bitmap file must be
 * connected to the corresponding DownloadActivity.run*() methods via the
 * appropriate android:onClick="..." attributes.
 * </ul>
 * 
 * @author "Fred Eisele" <phreed@gmail.com>
 * 
 */
public class DownloadActivity extends LifecycleLoggingActivity implements
		OnDownloadHandler {
	static private final String TAG = "Threaded Download Activity";

	private EditText urlEditText = null;

	private DownloadFragment imageFragment = null;
	private BroadcastReceiver onEvent = null;
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
		setContentView(R.layout.threaded_download);

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

	@Override
	public void onAttachFragment(Fragment fragment) {
		super.onAttachFragment(fragment);

	}

	@Override
	public void onResume() {
		super.onResume();

		this.onEvent = new BroadcastReceiver() {
			final private DownloadActivity master = DownloadActivity.this;

			public void onReceive(Context ctxt, Intent intent) {
				Log.d(TAG, "received broadcast bitmap");
				final String faultMsg = intent
						.getStringExtra(ThreadedDownloadService.RESULT_FAULT);
				if (faultMsg != null) {
					Toast.makeText(master, faultMsg, Toast.LENGTH_LONG).show();
				}
				final String bitmapFileString = intent
						.getStringExtra(ThreadedDownloadService.RESULT_BITMAP_FILE);
				master.imageFragment.loadBitmap(bitmapFileString);

				master.stopProgress();
			}
		};
		final IntentFilter filter = new IntentFilter(
				ThreadedDownloadService.BROADCAST_INTENT_ACTION);
		this.registerReceiver(this.onEvent, filter);
	}

	@Override
	public void onPause() {
		super.onPause();
		this.unregisterReceiver(this.onEvent);
	}

	/**
	 * 
	 */
	@Override
	public void onStop() {
		super.onStop();
		this.stopService(this.newServiceIntent());
	}

	/**
	 * Handle the result from the PendingIntent generated by the download
	 * service. Note that this will cause the activity to restart (I think) so
	 * that the fragment is not really needed to persist the state.
	 * 
	 * @see(runThreadWithPendingIntent) method.
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		case ThreadedDownloadService.RESULT_BITMAP_ID:
			final String faultMsg = data
					.getStringExtra(ThreadedDownloadService.RESULT_FAULT);
			if (faultMsg != null) {
				// TODO
			}
			final String bitmapFilePath = data
					.getStringExtra(ThreadedDownloadService.RESULT_BITMAP_FILE);
			this.imageFragment.loadBitmap(bitmapFilePath);
			this.stopProgress();
		}
	}

	/**
	 * Initialize and configure the progress dialog.
	 * 
	 */
	private void startProgress(CharSequence msg) {
		Log.d(TAG, "startProgress");

		this.progress = new ProgressDialog(this);
		this.progress.setTitle(R.string.dialog_progress_title);
		this.progress.setMessage(msg);
		this.progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		this.progress.setProgress(0);
		this.progress.show();
	}

	/**
	 * The progress can be null when the activity is stopped.
	 * The progress is not dismissed unless it is showing.
	 */
	private void stopProgress() {
		Log.d(TAG, "stopProgress");
		if (this.progress == null)
			return;
		if (!this.progress.isShowing())
			return;
		this.progress.dismiss();
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
	 * The service is started with an intent indicating the URI of the file to
	 * download for the bitmap.
	 * <p>
	 * The action of the returning intent is also provided as an extra.
	 * 
	 * @param view
	 */
	public void runAsyncTaskWithReceiver(View view) {
		Log.d(TAG, "runAsyncTaskWithReceiver");
		final Intent request = newRequestIntent();

		request.putExtra(ThreadedDownloadService.DOWNLOAD_METHOD,
				DownloadMethod.ASYNC_TASK_BROADCAST.asParcelable());

		runDownload(request, R.string.message_progress_async_task_w_receiver);
	}

	/**
	 * The service is started with an intent indicating the URI of the file to
	 * download for the bitmap.
	 * <p>
	 * The action of the returning intent is also provided as an extra.
	 * 
	 * @param view
	 */
	public void runThreadWithMessenger(View view) {
		Log.d(TAG, "runThreadWithMessenger");

		final Messenger messenger = new Messenger(DownloadFragment.msgHandler);
		final Intent request = newRequestIntent();
		request.putExtra(ThreadedDownloadService.MESSENGER_KEY, messenger);
		request.putExtra(ThreadedDownloadService.DOWNLOAD_METHOD,
				DownloadMethod.THREAD_MESSENGER.asParcelable());

		runDownload(request, R.string.message_progress_thread_w_messenger);
	}

	/**
	 * The service is started with an intent indicating the URI of the file to
	 * download for the bitmap. The action of the returning intent is also
	 * provided as an extra.
	 * 
	 * @param view
	 */
	public void runThreadWithPendingIntent(View view) {
		Log.d(TAG, "runThreadWithPendingIntent");
		final PendingIntent pi = this.createPendingResult(
				ThreadedDownloadService.RESULT_BITMAP_ID, new Intent(),
				PendingIntent.FLAG_UPDATE_CURRENT);
		final Intent request = newRequestIntent();

		request.putExtra(ThreadedDownloadService.PENDING_INTENT_KEY, pi);
		request.putExtra(ThreadedDownloadService.DOWNLOAD_METHOD,
				DownloadMethod.THREAD_PENDING_INTENT.asParcelable());

		runDownload(request, R.string.message_progress_thread_w_pending_intent);
	}

	/**
	 * A factory method for producing intents which request a download.
	 * 
	 * @return the
	 */
	private Intent newRequestIntent() {
		final Intent request = newServiceIntent();
		final Uri uri = getValidUrlFromWidget();
		Log.v(TAG, "downloading " + uri);
		request.setData(uri);
		return request;
	}

	/**
	 * A factory method for producing intents suitable for starting the target
	 * service.
	 * 
	 * @return
	 */
	private Intent newServiceIntent() {
		return new Intent(this, ThreadedDownloadService.class);
	}

	/**
	 * A method for starting the service and the progress dialog.
	 * 
	 * @param request
	 * @param msg
	 */
	private void runDownload(Intent request, int msg) {
		this.startService(request);
		this.startProgress(this.getResources().getText(msg));
	}

}
