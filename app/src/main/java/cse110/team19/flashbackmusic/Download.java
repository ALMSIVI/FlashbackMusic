package cse110.team19.flashbackmusic;

import android.app.DownloadManager;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import java.io.File;

/**
 * Created by sarahji on 3/8/18.
 *
 * Link: https://www.androidtutorialpoint.com/networking/android-download-manager-tutorial-download-file-using-download-manager-internet/
 * Link title: Android Download Manager Tutorial
 * Date captured: 3/8/18
 * Used to: learn how to use Android DownloadManager for downloading songs
 *
 */

public class Download {
    private DownloadManager dm;
    private String downloadFolder;

    public Download(DownloadManager d, String folder) {
        dm = d;
        downloadFolder = folder;
    }

    public long downloadData (Uri uri) {
        long downloadReference;

        // Create request for android download manager
        //dm = (DownloadManager)context.getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        //Setting title of request
        request.setTitle("Vibe Music");

        //Setting description of request
        request.setDescription("Downloading music from server...");

        //Set the local destination for the downloaded file to a path within the application's external files directory
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()
                + downloadFolder;
        Log.d("path name", path);
        File directory = new File(path);
        if (!directory.isDirectory()) {
            directory.mkdirs();
            Log.d("hi", "woo");
        }
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, downloadFolder + "/hi.mp3");
        //Enqueue download and save into referenceId
        downloadReference = dm.enqueue(request);
        return downloadReference;
    }
}
