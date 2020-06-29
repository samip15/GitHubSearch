package com.example.githubsearch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {
    private static final String TAG = "MainActivity";
    private static final int GITHUB_SEARCH_LODER = 22;
    private static final String SEARCHQUERY_URL = "searchUrl";
    private static final String SEARCH_JSON_DATA = "searchJsonData";
    private EditText mSearchBoxEditText;
    private TextView mUrlDisplayTextView;
    private TextView mSearchResultsTextView;
    private TextView mErrorMessageDisplay;
    private ProgressBar mLoadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        String[] dummydata = {
//                "Today, May 17 - Clear - 17°C / 15°C",
//                "Tomorrow - Cloudy - 19°C / 15°C",
//                "Thursday - Rainy- 30°C / 11°C",
//                "Friday - Thunderstorms - 21°C / 9°C",
//                "Saturday - Thunderstorms - 16°C / 7°C",
//                "Sunday - Rainy - 16°C / 8°C",
//                "Monday - Partly Cloudy - 15°C / 10°C",
//                "Tue, May 24 - Meatballs - 16°C / 18°C",
//                "Wed, May 25 - Cloudy - 19°C / 15°C",
//                "Thu, May 26 - Sto",
//        "Fri, May 27 - Hurricane - 21°C / 9°C",
//                "Sat, May 28 - Meteors - 16°C / 7°C",
//                "Sun, May 29 - Apocalypse - 16°C / 8°C",
//                "Mon, May 30 - Post Apocalypse - 15°C / 10°C",
//        };
        mSearchBoxEditText = findViewById(R.id.et_search_box);

        mUrlDisplayTextView = findViewById(R.id.tv_url_display);
        mSearchResultsTextView = findViewById(R.id.tv_github_search_results_json);

        mErrorMessageDisplay = findViewById(R.id.tv_error_message_display);
        mLoadingIndicator = findViewById(R.id.pb_loading_indicator);

        if (savedInstanceState != null) {
            String queryUrl = savedInstanceState.getString(SEARCHQUERY_URL);
            String queryJsondata = savedInstanceState.getString(SEARCH_JSON_DATA);
            mUrlDisplayTextView.setText(queryUrl);
            mSearchResultsTextView.setText(queryJsondata);
        }
        //initilizing loader
        getSupportLoaderManager().initLoader(GITHUB_SEARCH_LODER, null, this);

    }

    //--------------------loading function-------------------

    /**
     * making url to fetch data from and giving to loader manager with bundle
     */

    private void makeGithubSearchQuery() {
        mSearchResultsTextView.setText("");
        String githubQuery = mSearchBoxEditText.getText().toString();
        URL githubSearchUrl = NetworkUtils.buildUrl(githubQuery);
        mUrlDisplayTextView.setText(githubSearchUrl.toString());
        //bundle  = url and sending bundle or url to init loader to reset loder
        Bundle queryBundle = new Bundle();
        queryBundle.putString(SEARCHQUERY_URL, githubSearchUrl.toString());
        //implement the loader manager
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> githubsearch_loader = loaderManager.getLoader(GITHUB_SEARCH_LODER);
        if (githubsearch_loader == null) {
            loaderManager.initLoader(GITHUB_SEARCH_LODER, queryBundle, this);
        } else {
            //new data need to be loaded
            loaderManager.restartLoader(GITHUB_SEARCH_LODER, queryBundle, this);
        }
    }

    private void showJsonDataView() {
        // First, make sure the error is invisible
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        // Then, make sure the JSON data is visible
        mSearchResultsTextView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        // First, hide the currently visible data
        mSearchResultsTextView.setVisibility(View.INVISIBLE);
        // Then, show the error
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    //-----------------Loader Manager Function------------------------

    /**
     * Creating async task loader to fetch data
     *
     * @param id
     * @param args
     * @return
     */
    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, @Nullable final Bundle args) {
        //create a async task laader
        return new AsyncTaskLoader<String>(this) {
            String mGithubJson;

            @Override
            protected void onStartLoading() {
                if (args == null) {
                    return;
                }
                //checking if we have cache data
                if (mGithubJson != null) {
                    deliverResult(mGithubJson);
                    Log.e(TAG,"caching is happning");
                } else {
                    mLoadingIndicator.setVisibility(View.VISIBLE);
                    //tiggers load in background function to load data
                    forceLoad();
                    Log.e(TAG,"starting new loading");
                }
            }

            @Nullable
            @Override
            public String loadInBackground() {
                String search_query_url = args.getString(SEARCHQUERY_URL);
                if (search_query_url == null || TextUtils.isEmpty(search_query_url)) {
                    return null;
                }
                try {
                    URL searchUrl = new URL(search_query_url);
                    return NetworkUtils.getResponseFromHttpUrl(searchUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void deliverResult(@Nullable String githubjson) {
                mGithubJson = githubjson;
                super.deliverResult(githubjson);
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String githubSearchResults) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        if (githubSearchResults != null && !githubSearchResults.equals("")) {
            showJsonDataView();
            mSearchResultsTextView.setText(githubSearchResults);
        } else {
            showErrorMessage();
        }
    }

    /**
     * This function is used to reset  the loader
     *
     * @param loader
     */
    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }
    // ------------- MENU ----------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemThatWasClicked = item.getItemId();
        if (itemThatWasClicked == R.id.action_search) {
            makeGithubSearchQuery();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
