package com.example.android.booksearcher;

import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText searchWindow = (EditText) findViewById(R.id.search_window);
        final TextInputLayout searchWindowLayout = (TextInputLayout) findViewById(R.id.search_window_input_layout);
        final Button searchButton = (Button) findViewById(R.id.search_button);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            //Check if user entered the phrase, open new activity to perform search
            public void onClick(View v) {
                if (searchWindow.getText().toString().trim().isEmpty()) {
                    searchWindowLayout.setError(getString(R.string.no_phrase_error));
                } else {
                    Intent openResultsActivity = new Intent(MainActivity.this, ResultsActivity.class);
                    openResultsActivity.putExtra("KEYPHRASE", searchWindow.getText().toString().trim());
                    startActivity(openResultsActivity);
                }
            }
        });
    }
}
