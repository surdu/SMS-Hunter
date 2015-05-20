package com.example.surdu.smshunter;

import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxServerException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;


public class UpdateDropboxTask extends AsyncTask<Object, Void, Void> {

    @Override
    protected Void doInBackground(Object... params) {
        DropboxAPI mDBApi = (DropboxAPI)params[0];
        String message = (String)params[1];

        String fileContent = "";

        // try to read the existing Dropbox file
        try {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            DropboxAPI.DropboxFileInfo info = mDBApi.getFile("/trans.txt", null, outStream, null);

            fileContent = outStream.toString() + "\n";
        } catch (DropboxException e) {
            e.printStackTrace();
        }

        // try to update the Dropbox file
        try {
            fileContent += message;

            InputStream inStream = new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));
            mDBApi.putFileOverwrite("/trans.txt", inStream, inStream.available(), null);
        } catch (DropboxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
