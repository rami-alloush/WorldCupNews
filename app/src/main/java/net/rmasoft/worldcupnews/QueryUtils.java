package net.rmasoft.worldcupnews;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods related to requesting and receiving data from API
 */
final class QueryUtils {

    private static final String LOG_TAG = QueryUtils.class.getName();
    private static final int HTTP_READ_TIMEOUT = 10000; /* milliseconds */
    private static final int HTTP_CONNECT_TIMEOUT = 15000; /* milliseconds */

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    /**
     * Query the API and return a list of objects.
     */
    public static List<Article> fetchData(String requestUrl) {
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Extract and return relevant fields from the JSON response and create a list of Objects
        return extractArticlesFromJson(jsonResponse);
    }

    /**
     * Return a list of {@link Article} objects that has been built up from
     * parsing a JSON response.
     */
    private static ArrayList<Article> extractArticlesFromJson(String ArticleJSON) {

        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(ArticleJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding Articles to
        ArrayList<Article> Articles = new ArrayList<>();

        // Try to parse the SAMPLE_JSON_RESPONSE. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // Create a JSONObject from the provided URL string
            JSONObject baseJsonResponse = new JSONObject(ArticleJSON);
            JSONObject resultsJsonObject = baseJsonResponse.getJSONObject("response");

            // Extract the JSONArray associated with the key called "results",
            // which represents a list of Articles.
            JSONArray ArticleArray = resultsJsonObject.getJSONArray("results");

            // For each Article in the ArticleArray, create an {@link Article} object
            for (int i = 0; i < ArticleArray.length(); i++) {

                // Get a single Article at position i within the list of Articles
                JSONObject currentArticle = ArticleArray.getJSONObject(i);

                // Extract the value for the key called "webTitle"
                String webTitle = currentArticle.getString("webTitle");

                // Extract the value for the key called "sectionName"
                String sectionName = currentArticle.getString("sectionName");

                // Extract the value for the key called "webPublicationDate"
                String webPublicationDate = currentArticle.getString("webPublicationDate");

                // Extract the value for the key called "url"
                String url = currentArticle.getString("webUrl");

                // For a given Article, extract the JSONArray associated with the
                // key called "tags", which represents a list of all tags
                // for that Article.
                JSONArray tagsArray = currentArticle.getJSONArray("tags");

                String contributor = "Not Available";

                if (tagsArray.length() > 0) {
                    // Get first tags group at position 0 corresponding to Contributor
                    JSONObject contributorObject = tagsArray.getJSONObject(0);

                    // Extract the value for the key called "webTitle" for Contributor
                    contributor = contributorObject.getString("webTitle");
                }

                // Create a new {@link Article} object with the magnitude, location, time,
                // and url from the JSON response.
                Article Article = new Article(webTitle, sectionName, webPublicationDate, contributor, url);

                // Add the new {@link Article} to the list of Articles.
                Articles.add(Article);
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the Article JSON results", e);
        }

        // Return the list of Articles
        return Articles;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(HTTP_READ_TIMEOUT);
            urlConnection.setConnectTimeout(HTTP_CONNECT_TIMEOUT);
            urlConnection.setRequestMethod("GET");
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
            Log.e(LOG_TAG, "Problem retrieving the Article JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
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
}
