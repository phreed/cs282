package edu.vanderbilt.cs282.feisele;

/**
 * Request that a URI be downloaded.
 */
import edu.vanderbilt.cs282.feisele.DownloadCallback;
 
interface DownloadRequest {
    oneway void downloadImage (in Uri uri, in DownloadCallback callback); 
}
