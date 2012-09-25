package edu.vanderbilt.cs282.feisele;

import java.io.IOException;
import java.io.InputStream;
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
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

public class ThreadedDownloadFragment extends LifecycleLoggingFragment {
	static private final String TAG = "Threaded Download Fragment";

	/** my oldest daughter */
	static private final String DEFAULT_PORT_IMAGE = "raquel_eisele_port_2012.jpg";
	static private final String DEFAULT_LAND_IMAGE = "raquel_eisele_land_2012.jpg";

	private Bitmap bitmap = null;
	private ImageView bitmapImage = null;

	private static Handler msgHandler = null;
	private ProgressDialog progress;

	public interface OnDownloadFault {
		public void onFault(CharSequence msg);
	}

	/**
	 * 
	 * @param savedInstanceState
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ThreadedDownloadFragment.msgHandler = initMsgHandler(this);
	}

	/**
	 * The bitmap field serves double duty. It serves to hold the downloaded
	 * bitmap image and, when null, it acts as a flag to indicate that the
	 * default image should be used.
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		this.setRetainInstance(true);

		final View result = inflater.inflate(R.layout.downloaded_image,
				container, false);
		this.bitmapImage = (ImageView) result.findViewById(R.id.current_image);
		if (this.bitmap == null) {
			this.resetImage(null);
		} else {
			this.bitmapImage.setImageBitmap(this.bitmap);
		}
		return result;
	}

	/**
	 * Load the default image from the assets. Just for fun a different asset is
	 * loaded depending on the screen orientation.
	 * 
	 * @param view
	 */
	public void resetImage(View view) {
		this.bitmap = null;

		final AssetManager am = this.getActivity().getAssets();
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
			Toast.makeText(this.getActivity(),
					R.string.error_opening_default_image, Toast.LENGTH_LONG)
					.show();
			return;
		}
		try {
			final Bitmap bitmap = BitmapFactory.decodeStream(is);
			this.bitmapImage.setImageBitmap(bitmap);
		} finally {
			try {
				is.close();
			} catch (IOException ex) {
				Log.e(TAG, "cannot load a bitmap asset");
			}
		}
	}

	/**
	 * The workhorse for the class.
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
	 * Initialize and configure the progress dialog.
	 * 
	 */
	private void startProgress(CharSequence msg) {
		Log.d(TAG, "startProgress");

		this.progress = new ProgressDialog(this.getActivity());
		this.progress.setTitle(R.string.dialog_progress_title);
		this.progress.setMessage(msg);
		this.progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		this.progress.setProgress(0);
		this.progress.show();
	}

	/**
	 * A new bitmap image has been generated. Update the
	 * 
	 * @param result
	 */
	private void setBitmap(Bitmap result) {
		try {
			this.bitmap = result;
			if (this.bitmap != null) {
				this.bitmapImage.setImageBitmap(this.bitmap);
			}
			if (this.progress.isShowing()) {
				this.progress.dismiss();
			}
		} catch (IllegalArgumentException ex) {
			Log.e(TAG, "can not set bitmap image");
		}
	}

	/**
	 * Report problems with downloading the image back to the parent activity.
	 * 
	 * @param errorMsg
	 */
	private void reportDownloadFault(CharSequence errorMsg) {
		final FragmentActivity parent = this.getActivity();

		Toast.makeText(parent, errorMsg, Toast.LENGTH_LONG).show();

		if (parent instanceof OnDownloadFault) {
			((OnDownloadFault) parent).onFault(errorMsg);
		}
	}

	/**
	 * The async task will be stopped if the enclosing activity is stopped. In
	 * general the intent service should be used for downloading from the
	 * internet.
	 * 
	 * @param view
	 */
	public void runAsyncTask(View view, URL url) {
		Log.d(TAG, "runAsyncTask");
		if (url == null)
			return;
		(new AsyncTask<URL, Void, Bitmap>() {
			private final ThreadedDownloadFragment master = ThreadedDownloadFragment.this;

			@Override
			protected Bitmap doInBackground(URL... params) {
				if (params.length < 1)
					return null;
				final URL url = params[0];
				try {
					return master.downloadBitmap(url);
				} catch (FailedDownload ex) {
					master.reportDownloadFault(ex.msg);
				}
				return null;
			}

			/**
			 * Set the downloaded image.
			 */
			@Override
			protected void onPostExecute(Bitmap result) {
				master.setBitmap(result);
			}

			@Override
			protected void onPreExecute() {
				master.startProgress(master.getResources().getText(
						R.string.message_progress_async_task));
			}

		}).execute(url);
	}

	/**
	 * Start a new thread to perform the download. Once the download is
	 * completed the UI thread is notified via a post to on the handler.
	 * 
	 * @param view
	 */
	public void runRunnable(View view, final URL url) {
		Log.d(TAG, "runRunnable");
		if (url == null)
			return;
		this.startProgress(this.getResources().getText(
				R.string.message_progress_run_runnable));

		(new Thread(null, new Runnable() {
			private final ThreadedDownloadFragment master = ThreadedDownloadFragment.this;
			final URL url_ = url;

			public void run() {
				final Bitmap bitmap;
				try {
					bitmap = master.downloadBitmap(url_);
				} catch (FailedDownload ex) {
					master.reportDownloadFault(ex.msg);
					return;
				}
				master.bitmapImage.post(new Runnable() {
					public void run() {
						master.setBitmap(bitmap);
					}
				});
			}
		})).start();
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
	 * Initialized the handler used by the run message method.
	 * 
	 * @return
	 */
	private static Handler initMsgHandler(final ThreadedDownloadFragment master) {

		return new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (DownloadState.lookup[msg.what]) {
				case SET_PROGRESS_VISIBILITY: {
					master.startProgress(master.getResources().getText(
							R.string.message_progress_run_messages));
				}
					break;
				case SET_BITMAP: {
					final Bitmap bitmap = (Bitmap) msg.obj;
					master.setBitmap(bitmap);
				}
					break;
				case SET_ERROR: {
					master.reportDownloadFault((CharSequence) msg.obj);
					break;
				}
				}
			}
		};
	}

	/**
	 * Make use of the message handler to keep the UI updated.
	 * 
	 */
	public void runMessages(View view, final URL url) {
		Log.d(TAG, "runMessages");

		if (url == null)
			return;

		(new Thread(null, new Runnable() {
			private final ThreadedDownloadFragment master = ThreadedDownloadFragment.this;
			private final Handler handler = ThreadedDownloadFragment.msgHandler;
			private final URL url_ = url;

			public void run() {
				final Message startMsg = handler.obtainMessage(
						DownloadState.SET_PROGRESS_VISIBILITY.ordinal(),
						ProgressDialog.STYLE_SPINNER);
				handler.sendMessage(startMsg);

				final Bitmap bitmap;
				try {
					bitmap = master.downloadBitmap(url_);
				} catch (FailedDownload ex) {
					final Message errorMsg = handler.obtainMessage(
							DownloadState.SET_ERROR.ordinal(), ex.msg);
					handler.sendMessage(errorMsg);
					return;
				}
				final Message bitmapMsg = handler.obtainMessage(
						DownloadState.SET_BITMAP.ordinal(), bitmap);
				handler.sendMessage(bitmapMsg);
			}
		})).start();
	}

}
