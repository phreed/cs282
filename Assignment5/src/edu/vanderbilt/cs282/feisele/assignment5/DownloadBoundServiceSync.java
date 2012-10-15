package edu.vanderbilt.cs282.feisele.assignment5;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class DownloadBoundServiceSync extends DownloadBoundService {
	static private final String TAG = "Threaded Download Service";

	
    final private DownloadCall.Stub stub = new DownloadCall.Stub() {
    	final DownloadBoundServiceSync master = DownloadBoundServiceSync.this;
    	
		public String downloadImage(Uri uri) throws RemoteException {
			try {
				final Bitmap bitmap = master.downloadBitmap(uri);
				final File bitmapFile = master.storeBitmap(bitmap);
				return bitmapFile.toString();
				
			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
				throw new RemoteException("file not found :");
				
			} catch (FailedDownload ex) {
				ex.printStackTrace();
				throw new RemoteException("download failed");
				
			} catch (IOException ex) {
				ex.printStackTrace();
				throw new RemoteException("not implemented by this service");
			}
		}
    };
    
    /**
     * The onBind() is called after construction and onCreate().
     */
    @Override
    public IBinder onBind(Intent intent) {
    	Log.d(TAG, "sync service on bind");
        return this.stub;
    }

}
