package com.example.sv.pictureloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static LinearLayout leftPictureLayout;
    private static LinearLayout rightPictureLayout;
    private EditText edtRequest;
    private static final int MIN_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 5;
    private static final long KEEP_ALIVE_TIME = 60L;
    private static final int IMAGE_VIEW_PADDING = 16;
    private static final int NUMBER_OF_PICTURES = 100;
    private ThreadPoolExecutor threadPool;
    private Button btnSearch;
    private Snackbar snackbar;
    private volatile int downloadedPictures = 0;
    private volatile int picturesInQueue = 0;
    private String progressText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edtRequest = (EditText)findViewById(R.id.edtRequest);
        leftPictureLayout = (LinearLayout) findViewById(R.id.leftImages);
        rightPictureLayout = (LinearLayout) findViewById(R.id.rightImages);
        btnSearch = (Button)findViewById(R.id.btnSearch);
        progressText = getResources().getString(R.string.downloading);
        threadPool = new ThreadPoolExecutor(MIN_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());
    }

    private void initSnackBar() {
        snackbar = Snackbar.make(btnSearch, progressText, Snackbar.LENGTH_INDEFINITE)
                .setAction("Action", null);
        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();
        snackbarLayout.addView(new ProgressBar(this));
        snackbar.show();
    }


    public void btnSearchOnClick(View view) {

        String requestStr = edtRequest.getText().toString();

        if (connectionIsAvailable()){
            if (!requestStr.isEmpty()) {
                leftPictureLayout.removeAllViews();
                rightPictureLayout.removeAllViews();
                new LoadUrlsTask().execute(requestStr, String.valueOf(NUMBER_OF_PICTURES));
            } else {
                Toast.makeText(this, getResources().getString(R.string.emptyRequest), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, getResources().getString(R.string.notConnected), Toast.LENGTH_SHORT).show();
        }

    }




    public void loadPictures(ArrayList<String> urls) {
        picturesInQueue = urls.size();
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

    private boolean connectionIsAvailable() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connManager.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }



    private class LoadUrlsTask extends AsyncTask<String, Void, ArrayList<String>>{

        @Override
        protected ArrayList<String> doInBackground(String... params) {
            Flickr flickr = new Flickr();
            return flickr.search(params[0], Integer.parseInt(params[1]));
        }

        @Override
        protected void onPostExecute(ArrayList<String> urls) {
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
                ++downloadedPictures;
                ImageView imageView = new ImageView(getApplicationContext());
                imageView.setImageBitmap(bitmap);
                imageView.setPadding(IMAGE_VIEW_PADDING, IMAGE_VIEW_PADDING,
                        IMAGE_VIEW_PADDING, IMAGE_VIEW_PADDING);
                if (downloadedPictures % 2 == 0) {
                    leftPictureLayout.addView(imageView);
                } else {
                    rightPictureLayout.addView(imageView);
                }
                updateProgress();
            }
        }

        private void updateProgress(){
            if (snackbar == null || !snackbar.isShown()){
                initSnackBar();
            }

            snackbar.setText(progressText + String.valueOf(downloadedPictures)
                    + "/" + String.valueOf(picturesInQueue) );
            if (downloadedPictures == picturesInQueue){
                snackbar.dismiss();
                picturesInQueue = downloadedPictures = 0;
            }
        }
    }


}
