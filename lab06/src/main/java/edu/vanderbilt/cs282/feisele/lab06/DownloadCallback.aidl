package edu.vanderbilt.cs282.feisele.lab06;

/**
 * Reply to the downloadUri() request.
 *
 * @author "Fred Eisele" <phreed@gmail.com>
 */
interface DownloadCallback {
    oneway void sendPath (in String imageFilePath); 
    oneway void sendFault (in String msg); 
}
