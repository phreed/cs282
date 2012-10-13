package edu.vanderbilt.cs282.feisele;

/**
 * Reply to the downloadUri() request.
 */
interface DownloadCallback {
    oneway void sendPath (in String imageFilePath); 
    oneway void sendFault (in String msg); 
}
