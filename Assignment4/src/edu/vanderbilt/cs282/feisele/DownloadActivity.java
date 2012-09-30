package edu.vanderbilt.cs282.feisele;

import java.io.File;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Messenger;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import edu.vanderbilt.cs282.feisele.DownloadFragment.OnDownloadFaultHandler;
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
 * @author Fred Eisele <phreed@gmail.com>
 * 
 */
public class DownloadActivity extends LifecycleLoggingActivity implements
		OnDownloadFaultHandler {
	static private final String TAG = "Threaded Download Activity";

	private EditText urlEditText = null;

	private DownloadFragment imageFragment = null;
	private BroadcastReceiver onEvent = null;

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
			this.imageFragment = new DownloadFragment();
			txn.add(R.id.fragment_container, this.imageFragment);
			txn.commit();
		} else {
			this.imageFragment = (DownloadFragment) fobj;
		}

	}

	@Override
	public void onResume() {
		super.onResume();

		this.onEvent = new BroadcastReceiver() {
			final private DownloadActivity master = DownloadActivity.this;

			public void onReceive(Context ctxt, Intent intent) {
				Log.d(TAG, "received broadcast bitmap");
				final String bitmapFileString = intent
						.getStringExtra(ThreadedDownloadService.RESULT_BITMAP_FILE);
				master.imageFragment.loadBitmap(bitmapFileString);
			}
		};
		final IntentFilter filter =
				new IntentFilter(ThreadedDownloadService.BROADCAST_INTENT_ACTION);
		this.registerReceiver(this.onEvent, filter);
	}

	@Override
	public void onPause() {
		super.onPause();
		this.unregisterReceiver(this.onEvent);
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

	static private Uri getUrlFromHint(final CharSequence seq) {
		final String uriStr = seq.toString();
		if (uriStr == null) {
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
		if (this.imageFragment == null)
			return;
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
		if (this.imageFragment == null)
			return;
		final Uri uri = getValidUrlFromWidget();
		final Intent request = new Intent(this, ThreadedDownloadService.class);

		request.setData(uri);
		request.putExtra(ThreadedDownloadService.DOWNLOAD_METHOD,
				DownloadMethod.ASYNC_TASK_BROADCAST.asParcelable());
		this.startService(request);
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
		if (this.imageFragment == null)
			return;
		final Uri uri = getValidUrlFromWidget();
		final Messenger messenger = new Messenger(DownloadFragment.msgHandler);
		final Intent request = new Intent(this, ThreadedDownloadService.class);
		request.setData(uri);
		request.putExtra(ThreadedDownloadService.MESSENGER_KEY, messenger);
		request.putExtra(ThreadedDownloadService.DOWNLOAD_METHOD,
				DownloadMethod.THREAD_MESSENGER.asParcelable());
		this.startService(request);
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
		if (this.imageFragment == null)
			return;
		final Uri uri = getValidUrlFromWidget();
		final PendingIntent pi = this.createPendingResult(
				ThreadedDownloadService.RESULT_BITMAP_ID, new Intent(),
				PendingIntent.FLAG_UPDATE_CURRENT);
		final Intent request = new Intent(this, ThreadedDownloadService.class);
		request.setData(uri);
		request.putExtra(ThreadedDownloadService.PENDING_INTENT_KEY, pi);
		request.putExtra(ThreadedDownloadService.DOWNLOAD_METHOD,
				DownloadMethod.THREAD_PENDING_INTENT.asParcelable());
		this.startService(request);
	}

	/**
	 * Handle the result from the PendingIntent generated by the
	 * 
	 * @see(runThreadWithPendingIntent) method.
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, new StringBuilder("onActivityResult ").append(" request=")
				.append(requestCode).append(" result=").append(resultCode)
				.append(" intent=[").append(data).append("]").toString());
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
				Toast.makeText(master, msg, Toast.LENGTH_LONG).show();

				final Drawable dr = master.getResources().getDrawable(
						R.drawable.indicator_input_warn);
				dr.setBounds(0, 0, dr.getIntrinsicWidth(),
						dr.getIntrinsicHeight());
				master.urlEditText.setError(msg_, dr);
			}
		});
	}

}
