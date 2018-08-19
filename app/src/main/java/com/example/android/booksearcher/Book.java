package com.example.android.booksearcher;

import android.graphics.Bitmap;

public class Book {

    private String mInfoLink;
    private Bitmap mImage;
    private String mTitle;
    private String mAuthor;
    private String mCategories;

    public Book(String infoLink, Bitmap image, String title, String authors, String categories) {
        this.mInfoLink = infoLink;
        this.mImage = image;
        this.mTitle = title;
        this.mAuthor = authors;
        this.mCategories = categories;
    }

    public String getInfoLink() {
        return mInfoLink;
    }

    public Bitmap getImage() {
        return mImage;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getCategories() {
        return mCategories;
    }
}
