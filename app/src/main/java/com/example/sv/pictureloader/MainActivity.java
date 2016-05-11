package com.example.sv.pictureloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    //Button btnDownload;
    private static final String LOG_TAG = "svcom";
    private static LinearLayout linearLayout;
    private EditText edtRequest;
    private static final int MIN_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 5;
    private static final long KEEP_ALIVE_TIME = 60L;
    private ThreadPoolExecutor threadPool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edtRequest = (EditText)findViewById(R.id.edtRequest);
        //btnDownload = (Button) findViewById(R.id.btnDownload);
        linearLayout = (LinearLayout) findViewById(R.id.linLayoutImages);
        //catsUrlList = loadCatsUrlList(catsFile);
        threadPool = new ThreadPoolExecutor(MIN_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());
    }

//    private ArrayList<String> loadCatsUrlList(String catsFile) {
//        ArrayList<String> catsUrls = new ArrayList<>();
//        String line;
//        try {
//            BufferedReader br = new BufferedReader(new InputStreamReader(getApplicationContext().getAssets().
//                    open(catsFile)));
//
//            line = br.readLine();
//            while (line != null) {
//                catsUrls.add(line);
//                line = br.readLine();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return catsUrls;
//    }

    public void btnSearchOnClick(View view) {

//        if (connectionIsAvailable()) {
//            for (final String url : catsUrlList) {
//                threadPool.execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        loadPictureFromUrl(url);
//                    }
//                });
//            }
//        }
        String requestStr = edtRequest.getText().toString();

        if (connectionIsAvailable() && !requestStr.isEmpty()){
            linearLayout.removeAllViews();
            new LoadUrlsTask().execute(requestStr, "20");
        }
    }

//    public void btnClearOnClick(View view) {
//        linearLayout.removeAllViews();
//    }

    public void loadPictures(ArrayList<String> urls) {
        if (connectionIsAvailable()) {
            for (final String url : urls) {
                threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        loadPictureFromUrl(url);
                    }
                });
            }
        }
    }

    private boolean connectionIsAvailable() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connManager.getActiveNetworkInfo();

//        if (netInfo != null && netInfo.isConnected()) {
//            return true;
//        } else {
//            return false;
//        }

        return netInfo != null && netInfo.isConnected();
    }

    private void loadPictureFromUrl(String picUrl) {
        Bitmap result = null;
        InputStream inputStream;

        try {
            URL url = new URL(picUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            inputStream = urlConnection.getInputStream();
            result = BitmapFactory.decodeStream(inputStream);
            urlConnection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        changeBitmapRunnable changeBitmapRunnable = new changeBitmapRunnable(result);
        runOnUiThread(changeBitmapRunnable);
    }

    private class LoadUrlsTask extends AsyncTask<String, Void, ArrayList<String>>{

        @Override
        protected ArrayList<String> doInBackground(String... params) {

            Flickr flickr = new Flickr();
            Log.d(LOG_TAG, "secondParam - " + Integer.parseInt(params[1]));
            ArrayList<String> urlsList = flickr.search(params[0], Integer.parseInt(params[1]));
            return urlsList;
        }

        @Override
        protected void onPostExecute(ArrayList<String> urls) {
            //super.onPostExecute(aVoid);
            loadPictures(urls);
        }
    }

    private class changeBitmapRunnable implements Runnable {
        private Bitmap bitmap;

        changeBitmapRunnable(final Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        @Override
        public void run() {
            if (bitmap != null) {
                ImageView imageView = new ImageView(getApplicationContext());
                imageView.setImageBitmap(bitmap);
                linearLayout.addView(imageView);
            }
        }
    }


}
