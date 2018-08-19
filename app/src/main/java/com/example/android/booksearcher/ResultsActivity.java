package com.example.android.booksearcher;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ResultsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Book>> {

    private ListView mListView;
    private TextView mEmptyView;
    private View mProgressBar;
    private String mQueryURLString;
    private String mKeyphrase;
    private BookAdapter mAdapter;
    public static final String LOG_TAG = QueryUtils.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_result_list);

        mEmptyView = findViewById(R.id.empty_list_state);
        mProgressBar = findViewById(R.id.progress_bar);
        TextView resultsFor = findViewById(R.id.results_for);

        mKeyphrase = getIntent().getStringExtra("KEYPHRASE");
        resultsFor.setText(getString(R.string.search_results_for) + mKeyphrase + "\":");

        //Create a query and perform http request in background
        mQueryURLString = QueryUtils.formulateQueryURL(mKeyphrase, 30);
        getLoaderManager().initLoader(1, null, ResultsActivity.this);

        //Set adapter on ListView
        mListView = (ListView) findViewById(R.id.list);
        mAdapter = new BookAdapter(ResultsActivity.this, new ArrayList<Book>());
        mListView.setAdapter(mAdapter);

        //Check if there is internet connection, set adequate String to be displayed if not
        ConnectivityManager connectivityManager = (ConnectivityManager) ResultsActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork == null || !activeNetwork.isConnectedOrConnecting()) {
            mEmptyView.setText(R.string.no_internet_connection);
        }

        //Try to open applicable url in google chrome on list item click, open in any capable app if google chrome package is not found
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String url = mAdapter.getItem(position).getInfoLink();
                Intent openUrl = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                openUrl.setPackage("com.android.chrome");
                if (openUrl.resolveActivity(getPackageManager()) != null) {
                    startActivity(openUrl);
                    return;
                }
                openUrl.setPackage(null);
                if (openUrl.resolveActivity(getPackageManager()) != null) {
                    startActivity(openUrl);
                } else {
                    Log.e(LOG_TAG, "No app to handle the intent found");
                }
            }
        });

    }

    @Override
    public Loader<List<Book>> onCreateLoader(int id, Bundle args) {
        return new BookListLoader(ResultsActivity.this, mQueryURLString);
    }

    @Override
    //Hide progress indicator when loader finishes and update adapter with data obtained from performed request or display empty state if there is none
    public void onLoadFinished(Loader<List<Book>> loader, List<Book> data) {
        mProgressBar.setVisibility(View.GONE);
        mAdapter.clear();
        ArrayList<Book> foundBooks = (ArrayList<Book>) data;
        if (data != null && !data.isEmpty()) {
            mAdapter.addAll(data);
        }
        mListView.setEmptyView(mEmptyView);
    }

    @Override
    public void onLoaderReset(Loader<List<Book>> loader) {
        mAdapter.clear();
    }
}
