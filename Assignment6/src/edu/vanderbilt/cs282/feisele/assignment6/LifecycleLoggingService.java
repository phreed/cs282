package edu.vanderbilt.cs282.feisele.assignment6;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Service;
import android.content.Intent;

/**
 * An abstract activity which logs the life-cycle call backs. A decorator
 * pattern implemented via inheritance.
 * 
 * @author "Fred Eisele" <phreed@gmail.com>
 */
public abstract class LifecycleLoggingService extends Service {
	static private final Logger logger = LoggerFactory.getLogger("class.logging.service");

	/**
	 * Display a notification about us starting. We put an icon in the status
	 * bar.
	 */
	@Override
	public void onCreate() {
		logger.debug("onCreate: service created");
	}

	/**
	 * We want this service to continue running until it is explicitly stopped,
	 * so return sticky.
	 * 
	 * @param intent
	 * @param flags
	 * @param startId
	 * @return
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		logger.debug("onStartCommand: Received start id {}:{}", startId, intent);
		return START_STICKY;
	}

	/**
	 * Cancel the persistent notification. Tell the user we stopped.
	 */
	@Override
	public void onDestroy() {
		logger.debug("onDestroy");
	}

}
