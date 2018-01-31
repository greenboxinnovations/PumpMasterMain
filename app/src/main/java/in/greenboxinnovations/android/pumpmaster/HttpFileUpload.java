package in.greenboxinnovations.android.pumpmaster;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpFileUpload extends AsyncTask<String, Integer, String> {

    private URL connectURL;
    private File file;
    private boolean fileSuccess = false;
    private AysncInterface listener;


    HttpFileUpload(URL connectURL, File file, AysncInterface listener) {
        this.connectURL = connectURL;
        this.file = file;
        this.listener = listener;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.e("onPreExecute", "onPreExecute");

    }

    private void Sending() {

        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int fileSize = (int) file.length();

        String iFileName = file.toString().substring(file.toString().lastIndexOf("/") + 1);
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        String Tag = "fSnd";
        try {
            Log.e(Tag, "Starting Http File Sending to URL");

            // Open a HTTP connection to the URL
            HttpURLConnection conn = (HttpURLConnection) connectURL.openConnection();
            Log.e("url", "" + connectURL);
            // Allow Inputs
            conn.setDoInput(true);

            // Allow Outputs
            conn.setDoOutput(true);

            // Don't use a cached copy.
            conn.setUseCaches(false);

            // Use a post method.
            conn.setRequestMethod("POST");

            conn.setConnectTimeout(10 * 1000);

            conn.setRequestProperty("Connection", "Keep-Alive");

            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(twoHyphens + boundary + lineEnd);

            dos.writeBytes("Content-Disposition: form-data; name=\"title\"" + lineEnd);
            dos.writeBytes(lineEnd);

            dos.writeBytes("akshay");
            dos.writeBytes(lineEnd);

            dos.writeBytes(twoHyphens + boundary + lineEnd);

            dos.writeBytes("Content-Disposition: form-data; name=\"myFile\";filename=\"" + iFileName + "\"" + lineEnd);
            dos.writeBytes(lineEnd);

            Log.e(Tag, "Headers are written");

            // create a buffer of maximum size

            int bytesAvailable;
            if (fileInputStream != null) {
                bytesAvailable = fileInputStream.available();
                Log.e("bytesAvailable", "" + bytesAvailable);
                int maxBufferSize = 10240;
                int bufferSize = Math.min(bytesAvailable, maxBufferSize);
                Log.e("bufferSize", "" + bufferSize);
                byte[] buffer = new byte[bufferSize];

                // read file and write it into form...
                int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                Log.e("filesize", "" + fileSize);

                int counter = 0;

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    counter += bytesRead;
                    int progress = ((counter * 100) / (fileSize));
                    publishProgress(progress);

                }
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // close streams
                fileInputStream.close();
            }


            dos.flush();

            Log.e(Tag, "File Sent, Response: " + String.valueOf(conn.getResponseCode()));

            InputStream is = conn.getInputStream();

            // retrieve the response from server
            int ch;

            StringBuilder b = new StringBuilder();
            while ((ch = is.read()) != -1) {
                b.append((char) ch);
            }
            String s = b.toString();
            Log.e("Response", s);
            dos.close();
            if (s.equals("true")) {
                fileSuccess = true;
            }else{
                Log.e("Response", "error");
                fileSuccess = false;
                listener.asyncError();
            }


        } catch (MalformedURLException ex) {
            Log.e(Tag, "URL error: " + ex.getMessage(), ex);
            fileSuccess = false;
            listener.asyncError();

        } catch (IOException ioe) {
            Log.e(Tag, "IO error: " + ioe.getMessage(), ioe);
            fileSuccess = false;
            listener.asyncError();
        }
    }


    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Log.e("onPostExecute", "onPostExecute");

        String file_name = file.getName();
        Log.e("photo filename", file_name);

        if (fileSuccess) {
            listener.uploadSuccess();
            if (file.delete()){
                Log.e("file","deleted");
            }else{
                Log.e("file","not deleted");
            }

        }
    }

    @Override
    protected String doInBackground(String... params) {
        Sending();
        Log.e("doInBackground", "doInBackground");
        return null;
    }


}