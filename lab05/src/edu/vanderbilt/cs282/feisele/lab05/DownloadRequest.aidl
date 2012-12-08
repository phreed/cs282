package edu.vanderbilt.cs282.feisele.lab05;

/**
 * Request that a URI be downloaded.
 *
 * @author "Fred Eisele" <phreed@gmail.com>
 */
import edu.vanderbilt.cs282.feisele.lab05.DownloadCallback;
 
interface DownloadRequest {
    oneway void downloadImage (in Uri uri, in DownloadCallback callback); 
}
