package com.example.moleigh.newsapp;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {

    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String GUARDIAN_REQUEST_URL =
        "https://content.guardianapis.com/search?q=agriculture&from-date=2018-09-01&to-date=2018-09-06&api-key=727920b2-be9a-4727-841d-133401cf04ad";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArticleAsyncTask task = new ArticleAsyncTask();
        task.execute();
    }

    private void updateUi(Story article) {
        TextView titleTextView = (TextView) findViewById(R.id.title);
        titleTextView.setText(article.title);

        TextView sectionTextView = (TextView) findViewById(R.id.section);
        sectionTextView.setText(article.section);
    }

    private class ArticleAsyncTask extends AsyncTask<URL, Void, Story> {

        @Override
        protected Story doInBackground(URL... urls) {

        URL url = createUrl(GUARDIAN_REQUEST_URL);

        String jsonResponse = "";
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        Story article = extractResponseFromJson(jsonResponse);

        return article;
    }

    /**
     * Update the screen with the given earthquake (which was the result of the
     * {@link ArticleAsyncTask}).
     */
    @Override
    protected void onPostExecute(Story article) {
        if (article == null) {
            return;
        }

        updateUi(article);
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException exception) {
            Log.e(LOG_TAG, "Error with creating URL", exception);
            return null;
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // function must handle java.io.IOException here
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return an {@link Story} object by parsing out information
     * about the first earthquake from the input earthquakeJSON string.
     */
    private Story extractResponseFromJson(String articleJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(articleJSON)) {
            return null;
        }

        try {
            JSONObject baseJsonResponse = new JSONObject(articleJSON);
            JSONArray responseArray = baseJsonResponse.getJSONObject("response").getJSONArray("results");

            // If there are results in the results array
            if (responseArray.length() > 0) {

                JSONObject firstResponse = responseArray.getJSONObject(0);
                String title = firstResponse.getString("webTitle");
                String section = firstResponse.getString("sectionName");

                // Create a new {@link Event} object
                return new Story(title, section);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the JSON results", e);
        }
        return null;
        }
    }
}
