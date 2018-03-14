package cse110.team19.flashbackmusic;

import android.app.DownloadManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.URLUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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

    public long downloadData (String url) {
        String filename = URLUtil.guessFileName(url, null, null);
        long downloadReference;

        Uri uri = Uri.parse(url);
        // Create request for android download manager
        //dm = (DownloadManager)context.getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        //Setting title of request
        //request.setTitle(filename);

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
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, downloadFolder + filename);
        //Enqueue download and save into referenceId
        downloadReference = dm.enqueue(request);
        return downloadReference;
    }

    public String getLatestFileName(long id) {

        DownloadManager.Query q = new DownloadManager.Query();
        q.setFilterById(id);
        Cursor c = dm.query(q);

        if (c.moveToFirst()) {
            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                // process download
                return c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE));
                // get other required data by changing the constant passed to getColumnIndex
            }
        }

        return null;
    }

    /**
     * Link: https://stackoverflow.com/questions/3382996/how-to-unzip-files-programmatically-in-android
     * Link Title: How to unzip files programmatically in Android?
     * Date captured: 3/13/18
     * Used to: Learn how to download zipped files.
     * @param zipFile
     * @param targetDirectory
     * @throws IOException
     */
    public void unzipFile(File zipFile, File targetDirectory) throws IOException {
        ZipInputStream zis = new ZipInputStream(
                new BufferedInputStream(new FileInputStream(zipFile)));
        try {
            ZipEntry ze;
            int count;
            byte[] buffer = new byte[8192];
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, ze.getName());
                File dir = ze.isDirectory() ? file : file.getParentFile();
                if (!dir.isDirectory() && !dir.mkdirs())
                    throw new FileNotFoundException("Failed to ensure directory: " +
                            dir.getAbsolutePath());
                if (ze.isDirectory())
                    continue;
                FileOutputStream fout = new FileOutputStream(file);
                try {
                    while ((count = zis.read(buffer)) != -1)
                        fout.write(buffer, 0, count);
                } finally {
                    fout.close();
                }
            }
        } finally {
            zis.close();
        }

    }
}
