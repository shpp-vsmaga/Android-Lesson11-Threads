package com.example.sv.pictureloader;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by SV on 08.05.2016.
 */
public class Flickr {
    private static final String API_KEY = "&api_key=79266464605ba4b02faaed95863ad306";
    private static final String FLICKR_BASE_URL = "https://api.flickr.com/services/rest/?method=";
    private static final String FLICKR_SEARCH_METHOD = "flickr.photos.search";
    private static final String TAGS_STRING = "&tags=";
    private static final String PER_PAGE_STRING = "&per_page=";
    private static final String FORMAT_STRING = "&format=json";
    private static final String MEDIA_TYPE_STRING = "&media=photos";

    public ArrayList<String> search(String tag, int maxResults) {
        String formatedTag = tag.replace(' ', '+');
        String url = FLICKR_BASE_URL + FLICKR_SEARCH_METHOD + API_KEY + TAGS_STRING + formatedTag
                + FORMAT_STRING + PER_PAGE_STRING + maxResults + MEDIA_TYPE_STRING;

        String result = flickrRequest(url);
        String jsonStr = filterResponseStr(result);
        return convertJSONtoURLsList(jsonStr);
    }


    private String flickrRequest(String requestUrl){
        String result = "";
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
            jsonStr = responseStr.substring(responseStr.indexOf("{"), responseStr.lastIndexOf("}") + 1);
        }
        return jsonStr;
    }

    private ArrayList<String> convertJSONtoURLsList(String jsonStr){
        ArrayList<String> list = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            JSONObject jsonPhotos = jsonObject.getJSONObject("photos");
            JSONArray photosArray = jsonPhotos.getJSONArray("photo");
            for (int i = 0; i < photosArray.length(); i++){
                JSONObject picture = photosArray.getJSONObject(i);
                String farm = picture.getString("farm");
                String server = picture.getString("server");
                String id = picture.getString("id");
                String secret = picture.getString("secret");
                String pictureUrl = String.format("https://farm%s.staticflickr.com/%s/%s_%s_n.jpg",
                        farm, server, id, secret);
                list.add(pictureUrl);
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
        return list;
    }
}
