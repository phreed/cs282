package edu.vanderbilt.cs282.feisele;

/**
 * Request that a URI be downloaded.
 * Returns the image file path.
 */
import edu.vanderbilt.cs282.feisele.DownloadCallback;
 
interface DownloadCall {
    String downloadImage ( in Uri uri );
}
