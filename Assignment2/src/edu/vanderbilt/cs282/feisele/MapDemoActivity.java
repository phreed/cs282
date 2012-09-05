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
	 * There are some choices for the location schema.
	 * <p>
	 * <h2>Geo URI</h2>
	 * <p>
	 * A Geo URI is "a Uniform Resource Identifier (URI) for geographic
	 * locations using the 'geo' scheme name. A 'geo' URI identifies a physical
	 * location in a two- or three-dimensional coordinate reference system in a
	 * compact, simple, human-readable, and protocol-independent way." -- RFC
	 * 5870
	 * e.g.
	 * geo:36.16,-86.16
	 * <h2>Google Maps</h2>
	 * <p>
	 * This is not a standard but it is in common use.
	 * e.g.
	 * http://maps.google.com/?q=36.16,-86.16
	 * 
	 */
	
	static private Uri getLocationUri(double latitude, double longitude) {
		final StringBuilder sb = new StringBuilder();
		// sb.append("geo:");
		sb.append("http://maps.google.com/?q=");
		sb.append(latitude).append(",").append(longitude);
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
		final Double latitude;
		try {
			latitude = Double.parseDouble(latitudeEditable.toString());
		} catch (NumberFormatException ex) {
			Toast.makeText(MapDemoActivity.this,
					R.string.latitude_format_error, Toast.LENGTH_LONG).show();
			return;
		}
		if (latitude < -90.0 || 90.0 < latitude) {
			Toast.makeText(MapDemoActivity.this, R.string.latitude_bound_error,
					Toast.LENGTH_LONG).show();
			return;
		}

		final Editable longitudeEditable = this.longitudeView.getText();
		final Double longitude;
		try {
			longitude = Double.parseDouble(longitudeEditable.toString());
		} catch (NumberFormatException ex) {
			Toast.makeText(MapDemoActivity.this,
					R.string.longitude_format_error, Toast.LENGTH_LONG).show();
			return;
		}
		if (longitude < -180.0 || 180.0 < longitude) {
			Toast.makeText(MapDemoActivity.this,
					R.string.longitude_bound_error, Toast.LENGTH_LONG).show();
			return;
		}

		final Uri geoUri = getLocationUri(latitude, longitude);
		Log.d(TAG, "starting activity with " + geoUri.toString());

		final Intent locateIntent = new Intent(
				android.content.Intent.ACTION_VIEW, geoUri);
		try {
			startActivity(locateIntent);
		} catch (ActivityNotFoundException ex) {
			Log.w(TAG, "no application to handle the intent");
			Toast.makeText(MapDemoActivity.this, R.string.no_app_capable,
					Toast.LENGTH_LONG).show();
		}
	}

}
