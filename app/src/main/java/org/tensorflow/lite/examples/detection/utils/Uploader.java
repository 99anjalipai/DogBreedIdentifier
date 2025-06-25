package org.tensorflow.lite.examples.detection.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Pair;
import android.util.Log;


//import org.tensorflow.lite.examples.detection.BackgroundTask;
import org.tensorflow.lite.examples.detection.Configure;
import org.tensorflow.lite.examples.detection.OnDetectListener;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class Uploader {

    Context context;
    int percent = 0;
    OnDetectListener onDetectListener;

    private String LOCAL_SERVER;

    public Uploader(Context context,  OnDetectListener onDetectListener){

        this.context = context;
        this.onDetectListener = onDetectListener;

        LOCAL_SERVER = Configure.getServerPath(context) + "save_files.php";
    }

    public void uploadImage(Bitmap imageBitmap, String name){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] b = stream.toByteArray();
        String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        ArrayList<Pair<String, String>> params = new ArrayList<Pair<String, String>>();
        params.add(new Pair<>("image", encodedImage));

        try {
            new AsyncUploader().execute(LOCAL_SERVER, getQuery(params), name);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private String getQuery(List<Pair<String, String>> params) throws UnsupportedEncodingException{
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for(Pair<String, String> pair : params){
            if(first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.first, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.second, "UTF-8"));
        }
        return result.toString();
    }

    private class AsyncUploader extends AsyncTask<String, Integer, String>
    {
        String name;

        @Override
        protected String doInBackground(String... strings) {
            String urlString = strings[0];
            String params = strings[1];
            name = strings[2];

            URL url = null;
            InputStream stream = null;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);

                urlConnection.connect();

                OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
                wr.write(params);
                wr.flush();

                stream = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"), 8);
                String result = reader.readLine();
                return result;
            }catch (IOException ioe){
                ioe.printStackTrace();
            } finally {
                if (urlConnection != null)
                    urlConnection.disconnect();
            }
            return null;
        }

        @Override
        protected  void onPostExecute(String result)
        {
            if(result != null)
            {
                if(result.contains("upload_complete"))
                {
                    onDetectListener.onUploaded(true);
                    callPython();
                }
            }
            else
            {
                onDetectListener.onUploaded(false);
            }
        }
    }

    private void callPython()
    {
       // BackgroundTask backgroundTask = new BackgroundTask(context, percent, onDetectListener);
       // backgroundTask.execute("detect", "");
        Log.d("Uploader", "Upload skipped – BackgroundTask removed");

    }
}
