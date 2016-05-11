package com.example.sv.pictureloader;

//import android.content.Context;
//import android.graphics.BitmapFactory;
//import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
//import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
//import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
//import com.example.sv.pictureloader.MainActivity;

/**
 * Created by SV on 08.05.2016.
 */
public class Flickr {
    private static final String API_KEY = "&api_key=79266464605ba4b02faaed95863ad306";
    //private static final String SECRET = "021ce2cf0ee62071";
    private static final String FLICKR_BASE_URL = "https://api.flickr.com/services/rest/?method=";
    private static final String FLICKR_SEARCH_METHOD = "flickr.photos.search";
    private static final String TAGS_STRING = "&tags=";
    private static final String FORMAT_STRING = "&format=json";

    private static final String LOG_TAG = "svcom";

    //private static final int NUMBER_OF_PHOTOS = 20;

//    private Context context;
//
//    public Flickr(Context context) {
//        this.context = context;
//    }

    public ArrayList<String> search(String tag, int maxResults) {
        String formatedTag = tag.replace(' ', '+');
        String url = FLICKR_BASE_URL + FLICKR_SEARCH_METHOD + API_KEY + TAGS_STRING + formatedTag
                + FORMAT_STRING + "&per_page=" + maxResults + "&media=photos";

        String result = flickrRequest(url);
        String jsonStr = filterResponseStr(result);

        //if (connectionIsAvailable()) {
            //result = flickrRequest(url);
             //new MyTask().execute(url);
        //}

        Log.d("svcom", "result: " + result);
        return parseJSON(jsonStr);
    }


    private String flickrRequest(String requestUrl){
        String result = "";
        Log.d(LOG_TAG, requestUrl);
        InputStream is;
        StringBuffer sb = new StringBuffer();
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            is = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String inputLine = "";
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
            }
            result = sb.toString();
            urlConnection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private String filterResponseStr(String responseStr){
        String jsonStr = "";
        if (!responseStr.isEmpty()){
            jsonStr = responseStr.substring(responseStr.indexOf("{"), responseStr.length() - 1);
        }
        return jsonStr;
    }

//    private class MyTask extends AsyncTask<String, Void, String>{
//
//
//        @Override
//        protected String doInBackground(String... request) {
//            String result = "";
//            Log.d(LOG_TAG, request[0]);
//            InputStream is;
//            StringBuffer sb = new StringBuffer();
//            try {
//                URL url = new URL(request[0]);
//                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//                urlConnection.setRequestMethod("GET");
//                urlConnection.connect();
//                is = new BufferedInputStream(urlConnection.getInputStream());
//                BufferedReader br = new BufferedReader(new InputStreamReader(is));
//
//                String inputLine = "";
//                while ((inputLine = br.readLine()) != null) {
//                    sb.append(inputLine);
//                }
//                result = sb.toString();
//                urlConnection.disconnect();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return result;
//        }
//
//
//        @Override
//        protected void onPostExecute(String response) {
//
//            Log.d(LOG_TAG, response);
//            String jsonStr = response.substring(response.indexOf("{"), response.length() - 1);
//            Log.d(LOG_TAG, jsonStr);
//            ArrayList<String> urlsList = parseJSON(jsonStr);
//
//
//        }
//    }

    private ArrayList<String> parseJSON(String jsonStr){
        ArrayList<String> list = new ArrayList<>();
        Log.d(LOG_TAG, "parseJson");
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            Log.d(LOG_TAG, "jsonObject");
            JSONObject jsonPhotos = jsonObject.getJSONObject("photos");
            Log.d(LOG_TAG, "photos");
            JSONArray photosArray = jsonPhotos.getJSONArray("photo");
            Log.d(LOG_TAG, "photo");
            for (int i = 0; i < photosArray.length(); i++){
                JSONObject picture = photosArray.getJSONObject(i);
                String farm = picture.getString("farm");
                String server = picture.getString("server");
                String id = picture.getString("id");
                String secret = picture.getString("secret");
                String pictureUrl = String.format("https://farm%s.staticflickr.com/%s/%s_%s.jpg",
                        farm, server, id, secret);
                Log.d(LOG_TAG, pictureUrl);
                list.add(pictureUrl);
            }
        } catch (JSONException e){
            Log.d(LOG_TAG, "json error");
            e.printStackTrace();
        }

        return list;
    }

//    private boolean connectionIsAvailable() {
//        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo netInfo = connManager.getActiveNetworkInfo();
//
//        if (netInfo != null && netInfo.isConnected()) {
//            return true;
//        } else {
//            return false;
//        }
//    }
}
