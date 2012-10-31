package edu.vanderbilt.cs282.feisele.assignment6;

/**
 * Request that a URI be downloaded.
 * Returns the image file path.
 *
 * @author "Fred Eisele" <phreed@gmail.com>
 */
import edu.vanderbilt.cs282.feisele.assignment6.DownloadCallback;
 
interface DownloadCall {
    String downloadImage ( in Uri uri );
}
