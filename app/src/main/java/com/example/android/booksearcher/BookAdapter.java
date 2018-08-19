package com.example.android.booksearcher;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class BookAdapter extends ArrayAdapter<Book> {

    public BookAdapter(Activity context, ArrayList<Book> foundBooks) {
        super(context, 0, foundBooks);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        //Check if there are available views, inflate new if not
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.result_list_item, parent, false);
        }

        Book currentItem = getItem(position);

        ImageView bookImage = (ImageView) convertView.findViewById(R.id.book_image);
        TextView bookTitle = (TextView) convertView.findViewById(R.id.book_title);
        TextView bookAuthor = (TextView) convertView.findViewById(R.id.book_author);
        TextView bookCategory = (TextView) convertView.findViewById(R.id.book_category);

        //Check if current item has picture, display adequate picture if not
        if (currentItem.getImage() != null) {
            bookImage.setImageBitmap(currentItem.getImage());
        } else {
            bookImage.setImageResource(R.mipmap.no_image);
        }

        bookTitle.setText(currentItem.getTitle());

        //Check if current item has information about authors, display adequate string if not
        if (currentItem.getAuthor() != null) {
            bookAuthor.setText(currentItem.getAuthor());
        } else {
            bookAuthor.setText(getContext().getString(R.string.no_authors));
        }

        //Check if there are any categories for the current items, don't display the view if not
        if (!TextUtils.isEmpty(currentItem.getCategories())) {
            bookCategory.setText(currentItem.getCategories());
        } else {
            convertView.findViewById(R.id.category_view_group).setVisibility(View.GONE);
        }

        return convertView;
    }
}
