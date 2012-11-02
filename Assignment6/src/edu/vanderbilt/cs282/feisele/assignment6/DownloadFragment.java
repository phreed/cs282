package edu.vanderbilt.cs282.feisele.assignment6;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
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
 * There is a some concern of the bitmap being updated concurrently so there is
 * protection around the bitmap and its image view.
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
	static private final Logger logger = LoggerFactory
			.getLogger("class.fragment.download");

	/** my oldest daughter */
	static private final String DEFAULT_PORT_IMAGE = "raquel_eisele_port_2012.jpg";
	static private final String DEFAULT_LAND_IMAGE = "raquel_eisele_land_2012.jpg";

	private Bitmap bitmap = null;
	private ImageView bitmapImage = null;

	public static Handler msgHandler = null;
	public AtomicBoolean downloadPending = new AtomicBoolean(false);

	private Context context = null;

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
	 * An extension to the basic connection which holds information about the
	 * state of the connection and the service binding.
	 * <p>
	 * This cannot be implemented as a generic as there is no interface defining
	 * the asInterface() method on the stub. (or at least I don't know how)
	 */
	static abstract public class DownloadServiceConnection<T> implements
			ServiceConnection {
		protected T service;
		protected boolean isBound;

		public void onServiceConnected(ComponentName className, IBinder iservice) {
			logger.debug("call service connected");
			this.isBound = true;
		}

		public void onServiceDisconnected(ComponentName name) {
			this.isBound = false;
		}
	};

	/**
	 * provide implementation for the DownloadCall class.
	 */
	static private DownloadServiceConnection<DownloadRequest> asyncConnection = new DownloadServiceConnection<DownloadRequest>() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder iservice) {
			super.onServiceConnected(className, iservice);
			this.service = DownloadRequest.Stub.asInterface(iservice);
		}
	};

	/**
	 * This ensures that the controlling activity implements the callback
	 * interface.
	 * <p>
	 * The intents could be implicit... <code>
	  final Intent syncIntent = new Intent(DownloadCall.class.getName()); 
	  final Intent asyncIntent = new Intent(DownloadRequest.class.getName()); 
	  </code> This would also require changes
	 * to the AndroidManifest.xml
	 * <p>
	 * <code>
	   <intent-filter>
                <action android:name="edu.vanderbilt.cs282.feisele.DownloadCall" />
       </intent-filter>
            </code> ...and... <code>
       <intent-filter>
                <action android:name="edu.vanderbilt.cs282.feisele.DownloadRequest" />
       </intent-filter>
            </code> ... but in this case we will be explicit.
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.context = activity.getApplicationContext();

		/** for reporting back to the controlling activity */
		try {
			this.eventHandler = (OnDownloadHandler) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement " + OnDownloadHandler.class.getName());
		}

		this.explicitlyBindService(DownloadFragment.asyncConnection,
				DownloadService.class);
	}

	/**
	 * Generic helper method for binding to a service.
	 * 
	 * @param <T>
	 */
	private void explicitlyBindService(ServiceConnection conn,
			Class<? extends DownloadService> clazz) {
		logger.debug("bining to service explicitly {} {}", conn, clazz);
		final Intent intent = new Intent(this.context, clazz);
		this.context.bindService(intent, conn, Context.BIND_AUTO_CREATE);
	}

	/**
	 * Disable the ability to receiver download completion messages.
	 */
	@Override
	public void onDetach() {
		super.onDetach();
		this.eventHandler = null;

		if (DownloadFragment.asyncConnection.isBound)
			this.context.unbindService(DownloadFragment.asyncConnection);
	}

	/**
	 * The bitmap field serves double duty. It serves to hold the downloaded
	 * bitmap image and, when null, it acts as a flag to indicate that the
	 * default image should be used.
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState, true);
		this.setRetainInstance(true);

		final View result = inflater.inflate(R.layout.downloaded_image,
				container, false);
		this.bitmapImage = (ImageView) result.findViewById(R.id.current_image);
		synchronized (this.downloadPending) {
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
			synchronized (this.downloadPending) {
				this.bitmap = null;
				final Bitmap bitmap = BitmapFactory.decodeStream(is);
				this.bitmapImage.setImageBitmap(bitmap);
			}
		} finally {
			try {
				is.close();
			} catch (IOException ex) {
				logger.error("cannot load a bitmap asset");
			}
		}
	}

	/**
	 * A new bitmap image has been generated. Update the bitmap and the
	 * ImageView.
	 * 
	 * @param result
	 */
	private void setBitmap(Bitmap result) {
		try {
			synchronized (this.downloadPending) {
				this.downloadPending.set(false);
				this.bitmap = result;
				if (this.bitmap != null) {
					this.getActivity().runOnUiThread(new Runnable() {
						final DownloadFragment master = DownloadFragment.this;

						public void run() {
							master.bitmapImage.setImageBitmap(master.bitmap);
						}
					});
				}
			}
		} catch (IllegalArgumentException ex) {
			logger.error("can not set bitmap image");
		}
	}

	/**
	 * Load the appropriate bitmap into the image view. Notice that the file is
	 * deleted after it is loaded. This is not an optimal solution but I could
	 * not get the ParcelFileDescriptor to operate as I wanted.
	 * 
	 * @param bitmapFilePath
	 */
	public void loadBitmap(File bitmapFile) {
		if (bitmapFile == null) {
			logger.error("null file");
			return;
		}
		InputStream fileStream = null;
		try {
			fileStream = new FileInputStream(bitmapFile);
			final Bitmap bitmap = BitmapFactory.decodeStream(fileStream);
			this.setBitmap(bitmap);
		} catch (FileNotFoundException ex) {
			logger.error("could not load file " + bitmapFile, ex);
		} finally {
			if (fileStream != null)
				try {
					fileStream.close();
				} catch (IOException e) {
					logger.error("could not close file " + bitmapFile);
				}
		}
		bitmapFile.delete();
	}

	/**
	 * The file path as a string.
	 * 
	 * @param imageFilePath
	 */
	public void loadBitmap(String imageFilePath) {
		if (imageFilePath == null) {
			logger.error("null file path");
			return;
		}
		final File bitmapFile = new File(imageFilePath);
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
	 * Asynchronous AIDL model ("Run Async AIDL").
	 * <p>
	 * In this model the DownloadActivity binds to a DownloadService
	 * process and uses an asynchronous one-way AIDL method invocation to
	 * request that this service:
	 * <ol>
	 * <li>download a designated bitmap file,</li>
	 * <li>store it in the Android file system, and</li>
	 * <li>use another one-way AIDL interface passed as a parameter to the
	 * original one-way AIDL method invocation to return the filename back to
	 * DownloadActivity as a callback, which opens the file and causes the
	 * bitmap to be displayed on the screen.
	 * </ol>
	 * 
	 * @param view
	 */
	public void downloadAsyncAidl(Uri uri) {
		if (!DownloadFragment.asyncConnection.isBound) {
			logger.warn("async service not bound");
			return;
		}
		logger.debug("download async aidl");
		try {
			DownloadFragment.asyncConnection.service.downloadImage(uri,
					callback);
		} catch (RemoteException ex) {
			logger.error("download async aidl", ex);
		}

	}

	private final DownloadCallback.Stub callback = new DownloadCallback.Stub() {
		private DownloadFragment master = DownloadFragment.this;

		public void sendPath(String imageFilePath) throws RemoteException {
			master.loadBitmap(imageFilePath);
			master.reportDownloadComplete();
		}

		public void sendFault(String msg) throws RemoteException {
			master.reportDownloadFault(msg);
		}

	};

}
