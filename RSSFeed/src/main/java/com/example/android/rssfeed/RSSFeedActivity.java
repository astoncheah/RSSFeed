/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.rssfeed;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class RSSFeedActivity extends AppCompatActivity {
    private static final String MAIN_URL = "http://content.guardianapis.com/search?";
    private static final int REQUEST = 1;
    public static final String LOG_TAG = RSSFeedActivity.class.getName();
    private ListView listView;
    private MyArrayAdapter adapter;
    private TextView textInfo;
    private ProgressBar loading_indicator;
    private LoaderManager loaderManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_activity);

        listView = (ListView) findViewById(R.id.list);
        textInfo = (TextView)findViewById(R.id.empty_view);
        loading_indicator = (ProgressBar)findViewById(R.id.loading_indicator);
        loaderManager = getSupportLoaderManager();

        loadNews(MAIN_URL,null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("onActivityResult",requestCode+"/"+resultCode);
        if(requestCode==REQUEST){
            loadNews(MAIN_URL,null);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_item, menu);

        final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                showProgress();

                try {
                    query = URLEncoder.encode(query,"utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                loadNews(MAIN_URL,query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivityForResult(settingsIntent,REQUEST);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void loadNews(final String url,final String query){
        if(!isNetworkConnected()){
            if(adapter!=null){
                adapter.clear();
            }
            showTextInfo(R.string.no_connection);
            return;
        }
        showProgress();
        loaderManager.restartLoader(1, null, new LoaderManager.LoaderCallbacks<ArrayList<RSSFeed>>(){
            @Override
            public Loader<ArrayList<RSSFeed>> onCreateLoader(int id, Bundle args) {
                if(adapter!=null){
                    adapter.clear();
                }

                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(RSSFeedActivity.this);
                String orderBy = sharedPrefs.getString(
                        getString(R.string.settings_order_by_key),
                        getString(R.string.settings_order_by_default)
                );

                Uri baseUri = Uri.parse(url);
                Uri.Builder uriBuilder = baseUri.buildUpon();

                if(query!=null && !query.isEmpty()){
                    String encodedQuery = "";
                    try {
                        encodedQuery = URLEncoder.encode(query,"utf-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    uriBuilder.appendQueryParameter("q",encodedQuery);
                }
                uriBuilder.appendQueryParameter("api-key", "test");
                uriBuilder.appendQueryParameter("order-by", orderBy);

                return new LoadRSS(RSSFeedActivity.this, uriBuilder.toString());
            }

            @Override
            public void onLoadFinished(Loader<ArrayList<RSSFeed>> loader, ArrayList<RSSFeed> data) {
                if(adapter!=null){
                    adapter.clear();
                }
                updateViews(data);
            }

            @Override
            public void onLoaderReset(Loader<ArrayList<RSSFeed>> loader) {
                if(adapter!=null){
                    adapter.clear();
                }
            }
        }).forceLoad();
    }
    private void showProgress(){
        textInfo.setVisibility(View.GONE);
        loading_indicator.setVisibility(View.VISIBLE);
    }
    private void showTextInfo(int infoId){
        textInfo.setText(infoId);
        textInfo.setVisibility(View.VISIBLE);
        loading_indicator.setVisibility(View.GONE);
    }
    private void updateViews(ArrayList<RSSFeed> info){
        loading_indicator.setVisibility(View.GONE);
        if(info==null || info.isEmpty()){
            showTextInfo(R.string.no_book_found);
            return;
        }
        adapter = new MyArrayAdapter(RSSFeedActivity.this, R.layout.list_layout, info);
        listView.setAdapter(adapter);
    }
    private boolean isNetworkConnected() {
        // BEGIN_INCLUDE(connect)
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            boolean wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            boolean mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
            if(wifiConnected) {
                return true;
            } else if (mobileConnected){
                return true;
            }
        }
        return false;
    }
    private static class LoadRSS extends AsyncTaskLoader<ArrayList<RSSFeed>> {
        String url;
        public LoadRSS(Context context, String url) {
            super(context);
            this.url = url;
            Log.e("LoadRSS",url);
        }
        @Override
        public ArrayList<RSSFeed> loadInBackground() {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return extractNews(url);
        }
        public ArrayList<RSSFeed> extractNews(String url) {
            ArrayList<RSSFeed> books = new ArrayList<>();
            try {
                //JSONObject json = new JSONObject(SAMPLE_JSON_RESPONSE);
                JSONObject json = readJsonFromUrl(url);
                JSONObject response = json.optJSONObject("response");
                JSONArray jsonArray = response.optJSONArray("results");

                if(jsonArray!=null){
                    for(int i=0; i < jsonArray.length(); i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        String title = jsonObject.optString("webTitle");
                        String sectionName = jsonObject.optString("sectionName");
                        String infoLink = jsonObject.optString("webUrl");
                        String publishedDate = jsonObject.optString("webPublicationDate");

                        books.add(new RSSFeed(title,sectionName,infoLink,publishedDate));
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            // Return the list of earthquakes
            return books;
        }
        private JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;

            try {
                urlConnection = (HttpURLConnection) new URL(url).openConnection();
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                if (urlConnection.getResponseCode() == 200) {
                    inputStream = urlConnection.getInputStream();
                    BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
                    String jsonText = readAll(rd);
                    JSONObject json = new JSONObject(jsonText);
                    return json;
                } else {
                    Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
                }
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            }
            return null;
        }
        private String readAll(Reader rd) throws IOException {
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            return sb.toString();
        }
    }
}
