package com.example.android.booksearcher;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class BookListLoader extends AsyncTaskLoader<List<Book>> {

    private String[] mUrl;

    public BookListLoader(Context context, String... mUrl) {
        super(context);
        this.mUrl = mUrl;
    }

    @Override
    //Check if URL String was provided, perform http request, return List of Book objects
    public List<Book> loadInBackground() {
        if (mUrl.length < 1 || mUrl[0] == null) {
            return null;
        }
        ArrayList<Book> foundBooks = (ArrayList<Book>) QueryUtils.getDataFromServer(mUrl[0]);
        if (foundBooks == null || foundBooks.isEmpty()) {
            foundBooks = null;
        }
        return foundBooks;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }
}
