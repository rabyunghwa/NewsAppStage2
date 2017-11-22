package com.example.byunghwa.newsapp.data;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.example.byunghwa.newsapp.BuildConfig;
import com.example.byunghwa.newsapp.interfaces.OnRefreshStarted;
import com.example.byunghwa.newsapp.model.News;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class NewsListLoader extends AsyncTaskLoader<List<News>> {
    private static final String TAG = "NewsListLoader";

    private static final String KEY_ROOT = "response";
    private static final String KEY_JSON_ARRAY = "results";

    private static final String KEY_ID = "id";
    private static final String KEY_TYPE = "type";
    private static final String KEY_SECTION_ID = "sectionId";
    private static final String KEY_SECTION_NAME = "sectionName";
    private static final String KEY_PUBLICATION_DATE = "webPublicationDate";
    private static final String KEY_WEB_TITLE = "webTitle";
    private static final String KEY_WEB_URL = "webUrl";
    private static final String KEY_API_URL = "apiUrl";
    private static final String KEY_IS_HOSTED = "isHosted";
    private static final String KEY_ID_PILLAR = "pillarId";
    private static final String KEY_NAME_PILLAR = "pillarName";

    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 10000;

    private static final String URI_SCEHME = "http";
    private static final String URI_AUTHORITY = "content.guardianapis.com";
    private static final String URI_PATH = "search";
    private static final String URI_QUERY_PARAM = "q";
    private static final String URI_QUERY_PARAM_API_KEY = "api-key";

    private OnRefreshStarted listener;

    private String topicNews;


    public NewsListLoader(Context context, String topicNews, OnRefreshStarted listener) {
        super(context);
        this.listener = listener;
        this.topicNews = topicNews;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        Log.i(TAG, "onStartLoading getting called...");
        // show refresh indicator
        this.listener.onTaskStarted();
    }

    @Override
    public List<News> loadInBackground() {
        JSONArray jsonArray;
        ArrayList<News> newsArrayList = null;
        HttpURLConnection connection = null;
        try {
            // convert to the Uri.Builder pattern
            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.scheme(URI_SCEHME)
                    .authority(URI_AUTHORITY)
                    .appendPath(URI_PATH)
                    .appendQueryParameter(URI_QUERY_PARAM, topicNews)
                    .appendQueryParameter(URI_QUERY_PARAM_API_KEY, BuildConfig.API_KEY);
            String queryUrl = uriBuilder.build().toString();
            URL url = new URL(queryUrl);

            connection = (HttpURLConnection) url.openConnection();

            String line;
            StringBuilder builder = new StringBuilder();

            // set the connection timeout to 5 seconds and the read timeout to 10 seconds
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            JSONObject json = new JSONObject(builder.toString());

            json = json.getJSONObject(KEY_ROOT);
            jsonArray = json.getJSONArray(KEY_JSON_ARRAY);

            Log.i(TAG, "response: " + jsonArray);

            newsArrayList = responseJSONToNewsArrayList(jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return newsArrayList;
    }

    private ArrayList<News> responseJSONToNewsArrayList(JSONArray jsonArray) {
        ArrayList<News> newsArrayList = new ArrayList<>();
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    JSONObject jsonObject = new JSONObject(String.valueOf(jsonArray.getJSONObject(i)));

                    // parse each json object and store the info into News object
                    newsArrayList.add(jsonObjectToNewsObject(jsonObject));
                    Log.i(TAG, "news array list size: " + newsArrayList.size());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        return newsArrayList;
    }

    private News jsonObjectToNewsObject(JSONObject jsonObject) throws JSONException {
        String id = null;
        if (jsonObject.has(KEY_ID)) {
            id = jsonObject.getString(KEY_ID);
        }
        String type = null;
        if (jsonObject.has(KEY_TYPE)) {
            type = jsonObject.getString(KEY_TYPE);
        }
        String idSection = null;
        if (jsonObject.has(KEY_SECTION_ID)) {
            idSection = jsonObject.getString(KEY_SECTION_ID);
        }
        String nameSection = null;
        if (jsonObject.has(KEY_SECTION_NAME)) {
            nameSection = jsonObject.getString(KEY_SECTION_NAME);
        }
        String datePublication = null;
        if (jsonObject.has(KEY_PUBLICATION_DATE)) {
            datePublication = jsonObject.getString(KEY_PUBLICATION_DATE);
        }
        String titleWeb = null;
        if (jsonObject.has(KEY_WEB_TITLE)) {
            titleWeb = jsonObject.getString(KEY_WEB_TITLE);
        }
        String urlWeb = null;
        if (jsonObject.has(KEY_WEB_URL)) {
            urlWeb = jsonObject.getString(KEY_WEB_URL);
        }
        String urlApi = null;
        if (jsonObject.has(KEY_API_URL)) {
            urlApi = jsonObject.getString(KEY_API_URL);
        }
        boolean isHosted = false;
        if (jsonObject.has(KEY_IS_HOSTED)) {
            isHosted = jsonObject.getBoolean(KEY_IS_HOSTED);
        }
        String idPillar = null;
        if (jsonObject.has(KEY_ID_PILLAR)) {
            idPillar = jsonObject.getString(KEY_ID_PILLAR);
        }
        String namePillar = null;
        if (jsonObject.has(KEY_NAME_PILLAR)) {
            namePillar = jsonObject.getString(KEY_NAME_PILLAR);
        }

        News news = new News();
        news.setId(id);
        news.setType(type);
        news.setSectionID(idSection);
        news.setSectionName(nameSection);
        news.setWebPublicationDate(truncatePublicationData(datePublication));
        news.setWebTitle(titleWeb);
        news.setWebURL(urlWeb);
        news.setApiURL(urlApi);
        news.setHosted(isHosted);
        news.setPillarID(idPillar);
        news.setPillarName(namePillar);

        return news;
    }

    // only get the publication date data
    private String truncatePublicationData(String originalData) {
        return originalData.substring(0, originalData.indexOf("T"));
    }
}
