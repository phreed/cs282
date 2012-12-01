package edu.vanderbilt.cs282.feisele.assignment6;

/**
 * Request that a URI be downloaded.
 *
 * @author "Fred Eisele" <phreed@gmail.com>
 */
import edu.vanderbilt.cs282.feisele.assignment6.DownloadCallback;
 
interface DownloadRequest {
    oneway void downloadImage (in Uri uri, in DownloadCallback callback); 
}
