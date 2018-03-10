package cse110.team19.flashbackmusic;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import java.io.File;

import static android.content.Context.DOWNLOAD_SERVICE;

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
    private Context context;

    public Download(DownloadManager d, Context c) {
        dm = d;
        context = c;
    }

    public long downloadData (Uri uri) {
        long downloadReference;

        // Create request for android download manager
        //dm = (DownloadManager)context.getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        //Setting title of request
        //request.setTitle("Data Download");

        //Setting description of request
        request.setDescription("Android Data download using DownloadManager.");

        //Set the local destination for the downloaded file to a path within the application's external files directory
        String path = Environment.DIRECTORY_DOWNLOADS;
        request.setDestinationInExternalFilesDir(context, path, "hi.mp3");

        //Enqueue download and save into referenceId
        downloadReference = dm.enqueue(request);

        return downloadReference;
    }
}
