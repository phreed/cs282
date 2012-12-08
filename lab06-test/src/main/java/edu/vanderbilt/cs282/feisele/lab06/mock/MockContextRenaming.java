
package edu.vanderbilt.cs282.feisele.lab06.mock;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.vanderbilt.cs282.feisele.lab06.provider.DownloadContentProviderSchema;
import edu.vanderbilt.cs282.feisele.lab06.provider.MockDownloadContentProvider;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.test.mock.MockContentResolver;
import android.test.mock.MockContext;

public class MockContextRenaming extends MockContext {
    static final public Logger logger = LoggerFactory.getLogger("test.context,mock");

    static final private String PREFIX = "test.";
    final private Context targetContext;
    final private Context testContext;

    public MockContextRenaming(Context context) throws NameNotFoundException {
        logger.info("mock context constructor");
        this.targetContext = context;

        // (Context)getClass().getMethod("getTestContext").invoke(this);
        this.testContext = this.targetContext
                .createPackageContext("edu.vu.isis.ammo.core.tests",
                        Context.CONTEXT_IGNORE_SECURITY);
    }

    public String getDatabasePrefix() {
        return PREFIX;
    }

    /**
     * This could use some isolation.
     */

    @Override
    public AssetManager getAssets() {
        try {
            final String[] targetAssets = this.targetContext.getAssets().list("");
            logger.info("getting target assets {} {}", targetAssets.length, targetAssets);
            final String[] testAssets = this.testContext.getAssets().list("");
            logger.info("getting test assets {} {}", testAssets.length, testAssets);
        } catch (IOException ex) {
            logger.error("could not get test assets", ex);
        }
        return this.testContext.getAssets();
    }

    @Override
    public Resources getResources() {
        return this.targetContext.getResources();
    }

    @Override
    public PackageManager getPackageManager() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ContentResolver getContentResolver() {
        final ContentProvider provider = MockDownloadContentProvider.getInstance(null);

        // Content resolver
        final MockContentResolver resolver = new MockContentResolver();
        resolver.addProvider(DownloadContentProviderSchema.AUTHORITY, provider);
        return resolver;
    }

//    @Override
//    public Looper getMainLooper() {
//        throw new UnsupportedOperationException();
//    }

    @Override
    public Context getApplicationContext() {
        return this.targetContext;
    }

//    @Override
//    public void setTheme(int resid) {
//        throw new UnsupportedOperationException();
//    }

//    @Override
//    public Resources.Theme getTheme() {
//        throw new UnsupportedOperationException();
//    }

//    @Override
//    public ClassLoader getClassLoader() {
//        throw new UnsupportedOperationException();
//    }

    @Override
    public String getPackageName() {
        return this.targetContext.getPackageName();
    }

    @Override
    public ApplicationInfo getApplicationInfo() {
        return this.targetContext.getApplicationInfo();
    }

//    @Override
//    public String getPackageResourcePath() {
//        throw new UnsupportedOperationException();
//    }

//    @Override
//    public String getPackageCodePath() {
//        throw new UnsupportedOperationException();
//    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        return this.targetContext.getSharedPreferences(name, mode);
    }

//    @Override
//    public FileInputStream openFileInput(String name) throws FileNotFoundException {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public boolean deleteFile(String name) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public File getFileStreamPath(String name) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public String[] fileList() {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public File getFilesDir() {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public File getExternalFilesDir(String type) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public File getCacheDir() {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public File getExternalCacheDir() {
//        throw new UnsupportedOperationException();
//    }

    @Override
    public File getDir(String name, int mode) {
        return this.targetContext.getDir(name, mode);
    }

//    @Override
//    public SQLiteDatabase openOrCreateDatabase(String file, int mode,
//            SQLiteDatabase.CursorFactory factory) {
//        throw new UnsupportedOperationException();
//    }

    @Override
    public File getDatabasePath(String name) {
        return this.targetContext.getDatabasePath(name);
    }

//    @Override
//    public String[] databaseList() {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public boolean deleteDatabase(String name) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public Drawable getWallpaper() {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public Drawable peekWallpaper() {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public int getWallpaperDesiredMinimumWidth() {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public int getWallpaperDesiredMinimumHeight() {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public void setWallpaper(Bitmap bitmap) throws IOException {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public void setWallpaper(InputStream data) throws IOException {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public void clearWallpaper() {
//        throw new UnsupportedOperationException();
//    }

    @Override
    public void startActivity(Intent intent) {
        this.testContext.startActivity(intent);
    }

//    @Override
//    public void startIntentSender(IntentSender intent,
//            Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags)
//            throws IntentSender.SendIntentException {
//        throw new UnsupportedOperationException();
//    }

    @Override
    public void sendBroadcast(Intent intent) {
        this.testContext.sendBroadcast(intent);
    }

//    @Override
//    public void sendBroadcast(Intent intent, String receiverPermission) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public void sendOrderedBroadcast(Intent intent,
//            String receiverPermission) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public void sendOrderedBroadcast(Intent intent, String receiverPermission,
//            BroadcastReceiver resultReceiver, Handler scheduler, int initialCode,
//            String initialData,
//            Bundle initialExtras) {
//        throw new UnsupportedOperationException();
//    }

    @Override
    public void sendStickyBroadcast(Intent intent) {
        this.testContext.sendStickyBroadcast(intent);
    }

    @Override
    public void sendStickyOrderedBroadcast(Intent intent,
            BroadcastReceiver resultReceiver, Handler scheduler, int initialCode,
            String initialData,
            Bundle initialExtras) {
        this.testContext.sendStickyOrderedBroadcast(intent, resultReceiver, scheduler,
                initialCode, initialData, initialExtras);
    }

//    @Override
//    public void removeStickyBroadcast(Intent intent) {
//        throw new UnsupportedOperationException();
//    }

    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        return this.targetContext.registerReceiver(receiver, filter);
    }

    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter,
            String broadcastPermission, Handler scheduler) {
        return this.targetContext.registerReceiver(receiver, filter, broadcastPermission,
                scheduler);
    }

    @Override
    public void unregisterReceiver(BroadcastReceiver receiver) {
        logger.info("unregister receiver", receiver);
    }

    @Override
    public ComponentName startService(Intent service) {
        logger.info("starting external service {}", service);
        return this.targetContext.startService(service);
    }

//    @Override
//    public boolean stopService(Intent service) {
//        throw new UnsupportedOperationException();
//    }

    @Override
    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        logger.info("binding external service {}", service);
        return this.targetContext.bindService(service, conn, flags);
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        //this.targetContext.unbindService(conn);
    }

//    @Override
//    public boolean startInstrumentation(ComponentName className,
//            String profileFile, Bundle arguments) {
//        throw new UnsupportedOperationException();
//    }

    @Override
    public Object getSystemService(String name) {
        logger.info("get system service {}", name);
        return this.targetContext.getSystemService(name);
    }

//    @Override
//    public int checkPermission(String permission, int pid, int uid) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public int checkCallingPermission(String permission) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public int checkCallingOrSelfPermission(String permission) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public void enforcePermission(
//            String permission, int pid, int uid, String message) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public void enforceCallingPermission(String permission, String message) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public void enforceCallingOrSelfPermission(String permission, String message) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public void grantUriPermission(String toPackage, Uri uri, int modeFlags) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public void revokeUriPermission(Uri uri, int modeFlags) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public int checkUriPermission(Uri uri, int pid, int uid, int modeFlags) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public int checkCallingUriPermission(Uri uri, int modeFlags) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public int checkCallingOrSelfUriPermission(Uri uri, int modeFlags) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public int checkUriPermission(Uri uri, String readPermission,
//            String writePermission, int pid, int uid, int modeFlags) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public void enforceUriPermission(
//            Uri uri, int pid, int uid, int modeFlags, String message) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public void enforceCallingUriPermission(
//            Uri uri, int modeFlags, String message) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public void enforceCallingOrSelfUriPermission(
//            Uri uri, int modeFlags, String message) {
//        throw new UnsupportedOperationException();
//    }
//
//    public void enforceUriPermission(
//            Uri uri, String readPermission, String writePermission,
//            int pid, int uid, int modeFlags, String message) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public Context createPackageContext(String packageName, int flags)
//            throws PackageManager.NameNotFoundException {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public boolean isRestricted() {
//        throw new UnsupportedOperationException();
//    }

}
