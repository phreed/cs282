package edu.vanderbilt.cs282.feisele;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * The Fragment is the android user interface component. Fragments can have a
 * lifetime which spans the demise of its parent activity. In this particular
 * case the fragment is attached to an effective clone of its original activity.
 * <p>
 * A fragment does not need to persist its view elements but in this
 * implementation it does.
 * <p>
 * There is a some concern of the bitmap being updated concurrently some there
 * is protection around the bitmap and its image view.
 * <p>
 * The following indicate the tolerated changes.
 * <dl>
 * <dt>orientation</dt>
 * <dt>startActivity</dt>
 * <dd>configuration change doesn't handle properly</dt>
 * <dt>keyboard</dt>
 * </dl>
 * <p>
 * 
 * @author "Fred Eisele" <phreed@gmail.com>
 * 
 */
public class DownloadFragment extends LifecycleLoggingFragment {
	static private final String TAG = "Threaded Download Fragment";

	/** my oldest daughter */
	static private final String DEFAULT_PORT_IMAGE = "raquel_eisele_port_2012.jpg";
	static private final String DEFAULT_LAND_IMAGE = "raquel_eisele_land_2012.jpg";

	private Bitmap bitmap = null;
	private ImageView bitmapImage = null;

	public static Handler msgHandler = null;

	/**
	 * In order for a fragment to be useful it must have a containing activity.
	 * In many cases it is important for the fragment to communicate with that
	 * activity. A common case is when the fragment generates an event in which
	 * the other components of the UI may be interested. That is the case here,
	 * when the fragment detects a failure related to the uri it received the
	 * uri edit field should be marked in such a way that the operator is
	 * notified. It would be presumptuous for the fragment to post the error
	 * itself so it calls a method implemented by the controlling activity. In
	 * keeping with the fragment being a UI component it relies on the
	 * <p>
	 * 
	 * @see http://developer.android.com/guide/components/fragments.html#
	 *      CommunicatingWithActivity
	 */
	public interface OnDownloadHandler {
		public void onFault(CharSequence msg);

		public void onComplete();
	}

	private OnDownloadHandler eventHandler = null;

	/**
	 * This ensures that the controlling activity implements the callback
	 * interface.
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			this.eventHandler = (OnDownloadHandler) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement " + OnDownloadHandler.class.getName());
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		this.eventHandler = null;
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
		} catch (IllegalArgumentException ex) {
			Log.e(TAG, "can not set bitmap image");
		}
	}

	public void loadBitmap(File bitmapFile) {
		if (bitmapFile == null) {
			Log.e(TAG, "null file");
			return;
		}
		InputStream fileStream = null;
		try {
			fileStream = new FileInputStream(bitmapFile);
			final Bitmap bitmap = BitmapFactory.decodeStream(fileStream);
			this.setBitmap(bitmap);
		} catch (FileNotFoundException ex) {
			Log.e(TAG, "could not load file " + bitmapFile, ex);
		} finally {
			if (fileStream != null)
				try {
					fileStream.close();
				} catch (IOException e) {
					Log.e(TAG, "could not close file " + bitmapFile);
				}
		}
		bitmapFile.delete();
	}

	/**
	 * The file path as a string.
	 * 
	 * @param bitmapFilePath
	 */
	public void loadBitmap(String bitmapFilePath) {
		if (bitmapFilePath == null) {
			Log.e(TAG, "null file path");
			return;
		}
		final File bitmapFile = new File(bitmapFilePath);
		this.loadBitmap(bitmapFile);
	}

	/**
	 * Report problems with downloading the image back to the parent activity.
	 * 
	 * @param errorMsg
	 */
	private void reportDownloadFault(CharSequence errorMsg) {
		if (this.eventHandler == null)
			return;
		this.eventHandler.onFault(errorMsg);
	}

	private void reportDownloadComplete() {
		if (this.eventHandler == null)
			return;
		this.eventHandler.onComplete();
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
					// master.startProgress(master.getResources().getText(
					// R.string.message_progress_run_messages));
				}
					break;
				case SET_BITMAP: {
					final Bundle bundle = msg.getData();
					final String bitmapFileString = bundle
							.getString(ThreadedDownloadService.RESULT_BITMAP_FILE);
					master.loadBitmap(bitmapFileString);
					master.reportDownloadComplete();
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
