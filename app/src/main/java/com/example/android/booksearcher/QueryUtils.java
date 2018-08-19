package com.example.android.booksearcher;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import java.util.ArrayList;
import java.util.List;

public class QueryUtils {

    private static final String mBaseRequestURL = "https://www.googleapis.com/books/v1/volumes?q=";
    public static final String LOG_TAG = QueryUtils.class.getName();

    /**
     * Create query String for http request
     *
     * @param keyphrase - phrase for search
     * @return - full query String for http request
     */
    public static String formulateQueryURL(String keyphrase) {
        StringBuilder query = new StringBuilder();
        query.append(mBaseRequestURL);
        query.append("\"" + keyphrase + "\"");

        return query.toString();
    }

    /**
     * Create query String for http request
     *
     * @param keyphrase  - phrase for search
     * @param maxResults - maximum number of results you want to get
     * @return - full query String for http request with a result number limitation
     */
    public static String formulateQueryURL(String keyphrase, int maxResults) {
        StringBuilder query = new StringBuilder();
        query.append(formulateQueryURL(keyphrase));
        query.append("&maxResults=");
        query.append(maxResults);

        return query.toString();
    }

    /**
     * Get data in form of List of Book objects for provided URL
     *
     * @param requestURL - url in String format
     * @return - server response converted into List of Book objects
     */
    public static List<Book> getDataFromServer(String requestURL) {
        return parseServerResponse(readInputStream(performNetworkRequest(convertStringToURL(requestURL))));
    }

    /**
     * Convert provided String into URL object
     *
     * @param url - url in String format
     * @return - passed String converted into URL object
     */
    private static URL convertStringToURL(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "String could not be converted into URL object");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get input stream for http request
     *
     * @param url - URL object for http request
     * @return - InputStream response for passed URL
     */
    private static InputStream performNetworkRequest(URL url) {
        if (url == null) {
            Log.e(LOG_TAG, "Provided URL is null, exiting method early");
            return null;
        }

        HttpURLConnection connection = null;
        InputStream responseStream = null;

        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(90000);
            connection.setRequestMethod("GET");
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                Log.e(LOG_TAG, "Response code different from expected code 200. Received code: " + responseCode + ", exiting method early");
                return null;
            }
            responseStream = connection.getInputStream();

        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem occured while performing network request");
            e.printStackTrace();
        }
        return responseStream;
    }

    /**
     * Get server response String for provided InputStream
     *
     * @param responseStream - InputStream to be read
     * @return - server response String for passed InputStream
     */
    private static String readInputStream(InputStream responseStream) {
        if (responseStream == null) {
            Log.e(LOG_TAG, "Provided InputStream is null, exiting method early");
            return null;
        }

        StringBuilder response = new StringBuilder();
        String line;

        InputStreamReader inputStreamReader = new InputStreamReader(responseStream);
        BufferedReader reader = new BufferedReader(inputStreamReader);

        try {
            line = reader.readLine();
            while (line != null) {
                response.append(line);
                line = reader.readLine();
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem occured while reading from response stream");
            e.printStackTrace();
        }

        return response.toString();
    }

    /**
     * Parse provided server response String and get List of Book objects
     *
     * @param serverResponse - server response String
     * @return - List of Book objects created while parsing passed server response String
     */
    private static List<Book> parseServerResponse(String serverResponse) {
        if (TextUtils.isEmpty(serverResponse)) {
            Log.e(LOG_TAG, "Provided server response is empty, exiting method early");
            return null;
        }

        ArrayList<Book> foundBooks = new ArrayList<>();
        JSONObject response = null;
        try {
            response = new JSONObject(serverResponse);
            JSONArray items = response.getJSONArray("items");

            for (int i = 0; i < items.length();
                 i++) {

                JSONObject currentItem = items.getJSONObject(i);
                JSONObject volumeInfo = currentItem.getJSONObject("volumeInfo");

                String infoLink = getInfoLink(volumeInfo);
                Bitmap image = getImage(volumeInfo);
                String title = getFullTitle(volumeInfo);
                String authors = getAuthors(volumeInfo);
                String categories = getCategories(volumeInfo);

                foundBooks.add(new Book(infoLink, image, title, authors, categories));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem occured while parsing server response");
            e.printStackTrace();
        }
        return foundBooks;
    }

    /**
     * Get value of "infoLink" String for provided JSONObject
     *
     * @param volumeInfo - JSONObject to search inside
     * @return - String with value for "infoLink"
     * @throws JSONException - exception to be handled in calling method
     */
    private static String getInfoLink(JSONObject volumeInfo) throws JSONException {
        return volumeInfo.getString("infoLink");
    }

    /**
     * Get Bitmap image using provided JSONObject
     *
     * @param volumeInfo - JSONObject to search for image url inside
     * @return - Bitmap image linked inside passed JSONObject
     * @throws JSONException - exception to be handled in calling method
     */
    private static Bitmap getImage(JSONObject volumeInfo) throws JSONException {
        Bitmap image = null;
        JSONObject imageLinks = volumeInfo.optJSONObject("imageLinks");
        if (imageLinks == null) {
            Log.e(LOG_TAG, "No image link for this book");
            return null;
        }
        String imageLink = imageLinks.optString("smallThumbnail");
        if (imageLink.isEmpty()) {
            imageLink = imageLinks.optString("thumbnail");
        }

        URL imageURL = convertStringToURL(imageLink);
        if (imageURL != null) {
            try {
                image = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
            } catch (IOException e) {
                Log.e(LOG_TAG, "Could not get image from the server");
                e.printStackTrace();
            }
        }
        return image;
    }


    /**
     * Get value of "title" and "subtitle" String for provided JSONObject, put them into single String
     *
     * @param volumeInfo - JSONObject to search inside
     * @return - String containing both title and subtitle (if available)
     * @throws JSONException - exception to be handled in calling method
     */
    private static String getFullTitle(JSONObject volumeInfo) throws JSONException {
        String title = volumeInfo.getString("title");
        String subTitle = volumeInfo.optString("subtitle");
        if (!TextUtils.isEmpty(subTitle)) {
            title += "\n" + subTitle;
        }
        return title;
    }

    /**
     * Get values of all elements in "authors" JSONArray for provided JSONObject, put them into single String
     *
     * @param volumeInfo - JSONObject to search inside
     * @return - String containing all author values (if available)
     * @throws JSONException - exception to be handled in calling method
     */
    private static String getAuthors(JSONObject volumeInfo) throws JSONException {
        JSONArray authors = volumeInfo.optJSONArray("authors");
        if (authors == null) {
            Log.e(LOG_TAG, "No authors for this book");
            return null;
        }
        StringBuilder authorsString = new StringBuilder();
        for (int i = 0; i < authors.length(); i++) {
            authorsString.append(authors.getString(i));
            if (i + 1 < authors.length()) {
                authorsString.append("\n");
            }
        }
        return authorsString.toString();
    }

    /**
     * Get values of all elements in "categories" JSONArray for provided JSONObject, put them into single String
     *
     * @param volumeInfo - JSONObject to search inside
     * @return - String containing all categories values (if available)
     * @throws JSONException - exception to be handled in calling method
     */
    private static String getCategories(JSONObject volumeInfo) throws JSONException {
        JSONArray categories = volumeInfo.optJSONArray("categories");
        if (categories == null) {
            Log.e(LOG_TAG, "No categories for this book");
            return null;
        }
        StringBuilder categoriesString = new StringBuilder();
        for (int i = 0; i < categories.length(); i++) {
            categoriesString.append(categories.getString(i));
            if (i + 1 < categories.length()) {
                categoriesString.append(", ");
            }
        }
        return categoriesString.toString();
    }

}
