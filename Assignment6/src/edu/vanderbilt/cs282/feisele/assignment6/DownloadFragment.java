package edu.vanderbilt.cs282.feisele.assignment6;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import edu.vanderbilt.cs282.feisele.assignment6.DownloadContentProviderSchema.ImageTable;

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
public class DownloadFragment extends LLFragment {
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
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	/**
	 * Disable the ability to receiver download completion messages.
	 */
	@Override
	public void onDetach() {
		super.onDetach();
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
			}
		} catch (IllegalArgumentException ex) {
			logger.error("can not set bitmap image");
		}
	}

	@Override
	public void setArguments(Bundle bundle) {
		try {
			this.loadBitmap(bundle.getInt(ImageTable.ID.title));
		} catch (FileNotFoundException ex) {
			logger.error("could not load file {}", bundle, ex);
		} catch (IOException ex) {
			logger.error("could not close file {}", bundle, ex);
		}
	}

	public void loadBitmap(int tupleId) throws IOException {
		logger.debug("tuple id=<{}>", tupleId);
		InputStream fileStream = null;
		try {
			final Uri tupleUri = ContentUris.withAppendedId(
					ImageTable.CONTENT_URI, tupleId);
			fileStream = this.context.getContentResolver().openInputStream(
					tupleUri);
			final Bitmap bitmap = BitmapFactory.decodeStream(fileStream);
			if (bitmap == null) {
				logger.error("null bitmap returned {}", tupleUri);
				return;
			}
			logger.trace("bitmap meta-data {}x{}", bitmap.getHeight(),
					bitmap.getWidth());
			this.setBitmap(bitmap);
		} finally {
			if (fileStream != null)
				fileStream.close();
		}
	}

}
