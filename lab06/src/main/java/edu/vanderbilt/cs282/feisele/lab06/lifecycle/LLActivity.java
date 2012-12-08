package edu.vanderbilt.cs282.feisele.lab06.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

/**
 * An abstract activity which logs the life-cycle call backs. The GOF decorator
 * pattern implemented via inheritance.
 * 
 * @author "Fred Eisele" <phreed@gmail.com>
 */
public abstract class LLActivity extends FragmentActivity {
	static private final Logger logger = LoggerFactory.getLogger("class.activity.lifecycle");

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		logger.debug("onCreate: activity rebuilt");
		if (savedInstanceState == null) {
			logger.debug("onCreate: activity created fresh");
		} else {
			logger.debug("onCreate: activity restarted");
		}
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState == null) {
			logger.debug("onRestoreInstanceState: activity created fresh");
		} else {
			logger.debug("onRestoreInstanceState: activity restarted");
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		logger.debug("onStart");
	}

	@Override
	public void onResume() {
		super.onResume();
		logger.debug("onResume");
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		logger.debug("onSaveInstanceState");
	}

	@Override
	public void onPause() {
		super.onPause();
		logger.debug("onPause");
	}

	@Override
	public void onStop() {
		super.onStop();
		logger.debug("onStop");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		logger.debug("onDestroy");
	}

	@Override
	public void onRestart() {
		super.onRestart();
		logger.debug("onRestart");
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		logger.debug("onActivityResult request={} result={} intent=[{}]",
				requestCode, resultCode, data);
	}

	@Override
	public void onAttachFragment(Fragment fragment) {
		super.onAttachFragment(fragment);
		logger.debug("onAttachFragment");
	}

}
