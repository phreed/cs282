package edu.vanderbilt.cs282.feisele;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * An activity which prompts the user for latitude and longitude. These values
 * are used as part of a location intent. The resulting intent is used to start
 * an activity.
 * 
 * <p>
 * This behavior can be investigated with the activity manager "am".
 * 
 * @author phreed
 * 
 */
public class MapDemoActivity extends LifecycleLoggingActivity {
	static private final String TAG = "MapDemo";

	/**
	 * Determine if the latitude is valid. Recall that the Mercator projection
	 * does not extend to the poles.
	 */
	private double LATITUDE_MIN = -90.0;
	private double LATITUDE_MERC_MIN = -85.05;
	private double LATITUDE_MERC_MAX = 85.05;
	private double LATITUDE_MAX = 90.0;

	private Double validateLatitude(String latitudeString) {
		final double latitude;
		try {
			latitude = Double.parseDouble(latitudeString);
		} catch (NumberFormatException ex) {
			Toast.makeText(MapDemoActivity.this,
					R.string.latitude_format_error, Toast.LENGTH_LONG).show();
			return Double.NaN;
		}
		if (latitude < LATITUDE_MIN) {
			Toast.makeText(MapDemoActivity.this,
					R.string.latitude_lower_bound_error, Toast.LENGTH_LONG)
					.show();
			return Double.NaN;
		}
		if (LATITUDE_MAX < latitude) {
			Toast.makeText(MapDemoActivity.this,
					R.string.latitude_upper_bound_error, Toast.LENGTH_LONG)
					.show();
			return Double.NaN;
		}
		if (latitude < LATITUDE_MERC_MIN) {
			Toast.makeText(MapDemoActivity.this,
					R.string.latitude_mercator_lower_bound_warn,
					Toast.LENGTH_LONG).show();
		}
		if (LATITUDE_MERC_MAX < latitude) {
			Toast.makeText(MapDemoActivity.this,
					R.string.latitude_mercator_upper_bound_warn,
					Toast.LENGTH_LONG).show();
		}
		return latitude;
	}

	/**
	 * Determine if the latitude is valid. Recall that the Mercator projection
	 * does not extend to the poles.
	 */
	private double LONGITUDE_MIN = -180.0;
	private double LONGITUDE_MAX = 180.0;

	private Double validateLongitude(String longitudeString) {
		final double longitude;
		try {
			longitude = Double.parseDouble(longitudeString);
		} catch (NumberFormatException ex) {
			Toast.makeText(MapDemoActivity.this,
					R.string.longitude_format_error, Toast.LENGTH_LONG).show();
			return Double.NaN;
		}
		if (longitude < LONGITUDE_MIN) {
			Toast.makeText(MapDemoActivity.this,
					R.string.longitude_lower_bound_error, Toast.LENGTH_LONG)
					.show();
			return Double.NaN;
		}
		if (LONGITUDE_MAX < longitude) {
			Toast.makeText(MapDemoActivity.this,
					R.string.longitude_upper_bound_error, Toast.LENGTH_LONG)
					.show();
			return Double.NaN;
		}
		return longitude;
	}

	/**
	 * There are some choices for the location schema.
	 * <p>
	 * <h2>Geo URI</h2>
	 * <p>
	 * A Geo URI is "a Uniform Resource Identifier (URI) for geographic
	 * locations using the 'geo' scheme name. A 'geo' URI identifies a physical
	 * location in a two- or three-dimensional coordinate reference system in a
	 * compact, simple, human-readable, and protocol-independent way." -- RFC
	 * 5870 e.g. geo:36.16,-86.16
	 * <h2>Google Maps</h2>
	 * <p>
	 * This is not a standard but it is in common use. e.g.
	 * http://maps.google.com/?q=36.16,-86.16
	 * 
	 */

	static final private String geoUriPrefix = "geo:";
	static final private String httpUriPrefix = "http://maps.google.com/?q=";

	static private Uri getLocationUri(String prefix, double latitude,
			double longitude) {
		final StringBuilder sb = new StringBuilder().append(prefix)
				.append(latitude).append(",").append(longitude);
		return Uri.parse(sb.toString());
	}

	private EditText latitudeView;
	private EditText longitudeView;

	/**
	 * The fields are checked that they are numerical and are within the normal
	 * bounds for latitude and longitude.
	 * 
	 * @param savedInstanceState
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_demo);

		this.latitudeView = (EditText) findViewById(R.id.edit_latitude);
		this.longitudeView = (EditText) findViewById(R.id.edit_longitude);

	}

	/**
	 * Performed when the show location button is clicked.
	 * 
	 * @param view
	 *            the button view object (unused)
	 */
	public void showLocation(View view) {

		final Editable latitudeEditable = this.latitudeView.getText();
		final Double latitude = validateLatitude(latitudeEditable.toString());
		if (latitude == Double.NaN)
			return;

		final Editable longitudeEditable = this.longitudeView.getText();
		final Double longitude = validateLongitude(longitudeEditable.toString());
		if (longitude == Double.NaN)
			return;

		final Uri geoUri = getLocationUri(geoUriPrefix, latitude, longitude);
		Log.d(TAG, "starting activity with " + geoUri.toString());

		try {
			final Intent locateIntent = new Intent(
					android.content.Intent.ACTION_VIEW, geoUri);
			startActivity(locateIntent);
			// Activity started successfully
			return;
		} catch (ActivityNotFoundException ex) {
			Log.w(TAG,
					"no application to handle the intent " + geoUri.toString());
		}

		final Uri httpUri = getLocationUri(httpUriPrefix, latitude, longitude);
		Log.d(TAG, "starting activity with " + httpUri.toString());

		try {
			final Intent locateIntent = new Intent(
					android.content.Intent.ACTION_VIEW, geoUri);
			startActivity(locateIntent);
			// Activity started successfully
			return;
		} catch (ActivityNotFoundException ex) {
			Log.w(TAG,
					"no application to handle the intent " + httpUri.toString());
			Toast.makeText(MapDemoActivity.this, R.string.no_app_capable,
					Toast.LENGTH_LONG).show();
		}
	}

}
