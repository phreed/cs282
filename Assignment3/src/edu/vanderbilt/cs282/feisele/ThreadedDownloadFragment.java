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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import edu.vanderbilt.cs282.feisele.ThreadedDownloadActivity.InvalidUriRunnable;

public class ThreadedDownloadFragment extends LifecycleLoggingFragment {
	static private final String TAG = "Threaded Download Fragment";

	/** my oldest daughter */
	static private final String DEFAULT_PORT_IMAGE = "raquel_eisele_port_2012.jpg";
	static private final String DEFAULT_LAND_IMAGE = "raquel_eisele_land_2012.jpg";

	private boolean defaultImage = true;
	private Bitmap bitmap = null;
	private ImageView bitmapImage = null;
	private static Handler msgHandler = null;

	private ProgressDialog progress;

	/**
	 * 
	 * @param savedInstanceState
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ThreadedDownloadFragment.msgHandler = initMsgHandler(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		this.setRetainInstance(true);

		final View result = inflater.inflate(R.layout.downloaded_image,
				container, false);
		this.bitmapImage = (ImageView) result.findViewById(R.id.current_image);
		if (this.defaultImage) {
			this.resetImage(null);
		} else {
			this.bitmapImage.setImageBitmap(this.bitmap);
		}

		return result;
	}

	/* package */static class FailedDownload extends Exception {
		private static final long serialVersionUID = 6673968049922918951L;

		final public CharSequence msg;

		public FailedDownload(CharSequence msg) {
			super();
			this.msg = msg;
		}
	}

	/**
	 * Load the default image from the assets.
	 * 
	 * @param view
	 */
	public void resetImage(View view) {
		this.defaultImage = true;

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
			this.bitmap = BitmapFactory.decodeStream(is);
			this.bitmapImage.setImageBitmap(this.bitmap);
		} finally {
			try {
				is.close();
			} catch (IOException ex) {

			}
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
	 * The async task will be stopped if the enclosing activity is stopped. In
	 * general the intent service should be used for downloading from the
	 * internet.
	 * 
	 * @param view
	 */
	public void runAsyncTask(View view, URL url) {
		Log.d(TAG, "runAsyncTask");
		final AsyncTask<URL, Void, Bitmap> task = new AsyncTask<URL, Void, Bitmap>() {
			private final ThreadedDownloadFragment master = ThreadedDownloadFragment.this;

			@Override
			protected Bitmap doInBackground(URL... params) {
				for (final URL url : params) {
					try {
						return master.downloadBitmap(url);
					} catch (FailedDownload ex) {
						final InvalidUriRunnable error = new InvalidUriRunnable(
								master.getActivity(), ex.msg);
						master.getActivity().runOnUiThread(error);
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
				master.bitmap = result;
				if (result != null) {
					master.defaultImage = false;
					master.bitmap = result;
					master.bitmapImage.setImageBitmap(master.bitmap);
				}
				if (master.progress.isShowing()) {
					master.progress.dismiss();
				}
			}

			@Override
			protected void onPreExecute() {
				master.startProgress(master.getActivity().getResources()
						.getText(R.string.message_progress_async_task));
			}

		};

		if (url != null)
			task.execute(url);
	}

	/**
	 * Start a new thread to perform the download. Once the download is
	 * completed the UI thread is notified via a post to on the handler.
	 * 
	 * @param view
	 */
	public void runRunnable(View view, final URL url) {
		Log.d(TAG, "runRunnable");
		this.startProgress(this.getActivity().getResources()
				.getText(R.string.message_progress_run_runnable));

		final Thread thread = new Thread(null, new Runnable() {
			private final ThreadedDownloadFragment master = ThreadedDownloadFragment.this;
			final URL url_ = url;

			public void run() {
				try {
					master.bitmap = master.downloadBitmap(url_);
				} catch (FailedDownload ex) {
					// master.bitmapImage.post(new InvalidUriRunnable(master,
					// ex.msg));
					return;
				}
				master.bitmapImage.post(new Runnable() {
					public void run() {
						if (master.bitmap != null) {
							master.defaultImage = false;
							master.bitmapImage.setImageBitmap(master.bitmap);
						}
						if (master.progress.isShowing()) {
							master.progress.dismiss();
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
	public void runMessages(View view, final URL url) {
		Log.d(TAG, "runMessages");
		final Thread thread = new Thread(null, new Runnable() {
			private final ThreadedDownloadFragment master = ThreadedDownloadFragment.this;
			private final Handler handler = ThreadedDownloadFragment.msgHandler;
			private final URL url_ = url;

			public void run() {
				final Message startMsg = handler.obtainMessage(
						DownloadState.SET_PROGRESS_VISIBILITY.ordinal(),
						ProgressDialog.STYLE_SPINNER);
				handler.sendMessage(startMsg);

				try {
					master.bitmap = master.downloadBitmap(url_);
				} catch (FailedDownload ex) {
					final Message errorMsg = handler.obtainMessage(
							DownloadState.SET_ERROR.ordinal(), ex.msg);
					handler.sendMessage(errorMsg);
					return;
				}
				final Message bitmapMsg = handler.obtainMessage(
						DownloadState.SET_BITMAP.ordinal(), master.bitmap);
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
	private static Handler initMsgHandler(final ThreadedDownloadFragment master) {
		return new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (DownloadState.lookup[msg.what]) {
				case SET_PROGRESS_VISIBILITY: {
					master.startProgress(master.getActivity().getResources()
							.getText(R.string.message_progress_run_messages));
				}
					break;
				case SET_BITMAP: {
					master.bitmap = (Bitmap) msg.obj;
					if (master.bitmap != null) {
						master.defaultImage = false;
						master.bitmapImage.setImageBitmap(master.bitmap);
					}
					if (master.progress.isShowing()) {
						master.progress.dismiss();
					}
				}
					break;
				case SET_ERROR: {
					final CharSequence errorMsg = (CharSequence) msg.obj;
					// master.urlEditText.setError(errorMsg);
					Toast.makeText(master.getActivity(), errorMsg,
							Toast.LENGTH_LONG).show();
				}
					break;
				}
			}
		};
	}

}
