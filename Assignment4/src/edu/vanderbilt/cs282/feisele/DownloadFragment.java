package edu.vanderbilt.cs282.feisele;

import java.io.FileInputStream;
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
import android.os.ParcelFileDescriptor;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

public class DownloadFragment extends LifecycleLoggingFragment {
	static private final String TAG = "Threaded Download Fragment";

	/** my oldest daughter */
	static private final String DEFAULT_PORT_IMAGE = "raquel_eisele_port_2012.jpg";
	static private final String DEFAULT_LAND_IMAGE = "raquel_eisele_land_2012.jpg";

	private Bitmap bitmap = null;
	private ImageView bitmapImage = null;

	public static Handler msgHandler = null;
	private ProgressDialog progress;

	public interface OnDownloadFaultHandler {
		public void onFault(CharSequence msg);
	}

	/**
	 * 
	 * @param savedInstanceState
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DownloadFragment.msgHandler = initMsgHandler(this);
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
		synchronized (this) {
			if (this.bitmap == null) {
				this.resetImage(null);
			} else {
				this.bitmapImage.setImageBitmap(this.bitmap);
			}
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
			synchronized (this) {
				this.bitmap = null;
				final Bitmap bitmap = BitmapFactory.decodeStream(is);
				this.bitmapImage.setImageBitmap(bitmap);
			}
		} finally {
			try {
				is.close();
			} catch (IOException ex) {
				Log.e(TAG, "cannot load a bitmap asset");
			}
		}
	}

	/**
	 * A new bitmap image has been generated. Update the
	 * 
	 * @param result
	 */
	private void setBitmap(Bitmap result) {
		try {
			synchronized (this) {
				this.bitmap = result;
				if (this.bitmap != null) {
					this.bitmapImage.setImageBitmap(this.bitmap);
				}
			}
			if (this.progress.isShowing()) {
				this.progress.dismiss();
			}
		} catch (IllegalArgumentException ex) {
			Log.e(TAG, "can not set bitmap image");
		}
	}

	public void loadBitmap(ParcelFileDescriptor pfd) {
		final InputStream fileStream = new FileInputStream(pfd.getFileDescriptor());
		final Bitmap bitmap = BitmapFactory.decodeStream(fileStream);
		this.setBitmap(bitmap);
	}


	/**
	 * Report problems with downloading the image back to the parent activity.
	 * 
	 * @param errorMsg
	 */
	private void reportDownloadFault(CharSequence errorMsg) {	
		final FragmentActivity parent = this.getActivity();

		if (parent instanceof OnDownloadFaultHandler) {
			((OnDownloadFaultHandler) parent).onFault(errorMsg);
		}
		this.progress.cancel();
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
	private static Handler initMsgHandler(final DownloadFragment master) {

		return new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (DownloadState.lookup[msg.what]) {
				case SET_PROGRESS_VISIBILITY: {
					//master.startProgress(master.getResources().getText(
					//		R.string.message_progress_run_messages));
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


}
