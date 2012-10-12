package edu.vanderbilt.cs282.feisele;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

/**
 * An abstract activity which logs the life-cycle call backs.
 * The GOF decorator pattern implemented via inheritance.
 */
public abstract class LifecycleLoggingActivity extends FragmentActivity {
	static private final String TAG = "Lifecycle Logging Activity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate: activity rebuilt");
		if (savedInstanceState == null) {
			Log.d(TAG, "onCreate: activity created fresh");
        } else {
        	Log.d(TAG, "onCreate: activity restarted");
        }
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState == null) {
			Log.d(TAG, "onRestoreInstanceState: activity created fresh");
        } else {
        	Log.d(TAG, "onRestoreInstanceState: activity restarted");
        }
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG, "onStart");
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		Log.d(TAG, "onSaveInstanceState");
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, "onPause");
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.d(TAG, "onStop");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
	}

	@Override
	public void onRestart() {
		super.onRestart();
		Log.d(TAG, "onRestart");
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, new StringBuilder("onActivityResult ").
				append(" request=").append(requestCode).
				append(" result=").append(resultCode).
				append(" intent=[").append(data).append("]").
				toString());
	}
	
	@Override
	public void onAttachFragment(Fragment fragment) {
		super.onAttachFragment(fragment);
		Log.d(TAG, "onAttachFragment");
	}

}
