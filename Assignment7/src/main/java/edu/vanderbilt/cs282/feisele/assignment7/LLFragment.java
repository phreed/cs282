package edu.vanderbilt.cs282.feisele.assignment7;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * An abstract activity which logs the life-cycle call backs. A decorator
 * pattern implemented via inheritance.
 * 
 * @author "Fred Eisele" <phreed@gmail.com>
 */
public abstract class LLFragment extends Fragment {
	static private final Logger logger = LoggerFactory.getLogger("class.fragment.lifecycle");

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		logger.debug("onAttach: fragment attached " + activity.toString());
	}

	@Override
	public void onDetach() {
		super.onDetach();
		logger.debug("onDetach: fragment detach");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			logger.debug("onCreate: fragment created fresh");
		} else {
			logger.debug("onCreate: fragment restarted");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (savedInstanceState == null) {
			logger.debug("onCreateView: fragment created fresh");
		} else {
			logger.debug("onCreateView: fragment restarted");
		}
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	/**
	 * This helper method allows the caller to specify whether the calling chain
	 * should be continued. The android documentation suggests that in the case
	 * of programmatically attached fragment the super should not be called (or
	 * is unnecessary).
	 * 
	 * @param inflater
	 * @param container
	 * @param savedInstanceState
	 * @param isDynamic
	 * @return
	 */
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState, boolean isDynamic) {
		if (isDynamic) {
			if (savedInstanceState == null) {
				logger.debug("onCreateView: dynamic fragment created fresh");
			} else {
				logger.debug("onCreateView: dynamic fragment restarted");
			}
			return null;
		}
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		logger.debug("onDestroyView: fragment view destroyed");
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
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		logger.debug("onActivityCreated: fragment activity created ");
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		logger.debug("onActivityResult  request={}  result={} intent=[{}]",
				requestCode, resultCode, data);
	}

}
