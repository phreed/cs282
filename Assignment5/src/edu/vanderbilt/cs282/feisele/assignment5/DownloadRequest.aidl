package edu.vanderbilt.cs282.feisele.assignment5;

/**
 * Request that a URI be downloaded.
 */
import edu.vanderbilt.cs282.feisele.assignment5.DownloadCallback;
 
interface DownloadRequest {
    oneway void downloadImage (in Uri uri, in DownloadCallback callback); 
}
