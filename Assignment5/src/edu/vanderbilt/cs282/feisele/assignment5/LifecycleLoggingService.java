package edu.vanderbilt.cs282.feisele.assignment5;

import edu.vanderbilt.cs282.feisele.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * An abstract activity which logs the life-cycle call backs. A decorator
 * pattern implemented via inheritance.
 */
public abstract class LifecycleLoggingService extends Service {
	static private final String TAG = "Lifecycle Logging Service";

	private NotificationManager mNM;

	// Unique Identification Number for the Notification.
	// We use it on Notification start, and to cancel it.
	private int NOTIFICATION = R.string.lifecycle_logging_service;

	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		LifecycleLoggingService getService() {
			return LifecycleLoggingService.this;
		}
	}

	/**
	 * Display a notification about us starting. We put an icon in the status
	 * bar.
	 */
	@Override
	public void onCreate() {
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Log.d(TAG, "onCreate: service created");

		showNotification();
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
		Log.d(TAG, "onStartCommand: Received start id " + startId + ": "
				+ intent);
		return START_STICKY;
	}

	/**
	 * Cancel the persistent notification. Tell the user we stopped.
	 */
	@Override
	public void onDestroy() {
		mNM.cancel(NOTIFICATION);
		Toast.makeText(this, R.string.lifecycle_logging_service_started,
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	/**
	 * This is the object that would receive interactions from clients.
	 */
	private final IBinder mBinder = new LocalBinder();

	/**
	 * Show a notification while this service is running.
	 * 
	 * In this sample, we'll use the same text for the ticker and the expanded
	 * notification. Set the icon, scrolling text and timestamp.
	 */
	@SuppressWarnings("deprecation")
	private void showNotification() {
		CharSequence text = getText(R.string.lifecycle_logging_service_started);

		
		final Notification notification = new Notification(
				R.drawable.stat_sample, text, System.currentTimeMillis());

		// The PendingIntent to launch our activity if the user selects this
		// notification
		final PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this,
						LifecycleLoggingServiceActivities.Controller.class), 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this,
				getText(R.string.lifecycle_logging_service_label), text,
				contentIntent);

		// Send the notification.
		mNM.notify(NOTIFICATION, notification);
	}
}
