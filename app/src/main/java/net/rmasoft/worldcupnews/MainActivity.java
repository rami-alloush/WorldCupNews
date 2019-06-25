package net.rmasoft.worldcupnews;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.LoaderManager;
import android.content.Loader;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Article>> {

    /** URL for Article data from the guardian API */
    public final String apiKey = BuildConfig.guardianAPIs_ApiKey;
    public final String BASE_REQUEST_URL = "https://content.guardianapis.com/search";

    /** Adapter for the list of Articles - to be globally accessible for updates*/
    private ArticleAdapter mAdapter;

    /** TextView that is displayed when the list is empty */
    private TextView mEmptyStateTextView;

    /** swipeContainer for pull to refresh */
    private SwipeRefreshLayout swipeContainer;

    /**
     * Constant value for the Article loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders. */
    private static final int Article_LOADER_ID = 1;

    // Monitor if SharedPreferences changes
    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //listener on change any preference
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                restartTheLoader();
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(prefListener);

        // Define the mEmptyStateTextView
        mEmptyStateTextView = findViewById(R.id.empty_view);

        // Create a new adapter that takes an empty list of Articles as input
        // it will be dynamically updated from the ArticleAsyncTask > OnFinishLoad
        mAdapter = new ArticleAdapter(this, new ArrayList<Article>());

        /* Find a reference to the {@link RecyclerView} in the layout */
        RecyclerView ArticleRecyclerView = findViewById(R.id.list);
        ArticleRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ArticleRecyclerView.setAdapter(mAdapter);

        // Check internet connectivity and start Loader if there's connection
        if (isConnected()) {
            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            getLoaderManager().initLoader(Article_LOADER_ID, null, this);
        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            View loadingIndicator = findViewById(R.id.progressBar);
            loadingIndicator.setVisibility(View.GONE);

            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }

        // Getting SwipeContainerLayout
        swipeContainer = findViewById(R.id.swipe_container);
        // Adding Listener
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Restart loader to get fresh data
                restartTheLoader();
            }
        });

        // Scheme colors for animation
        swipeContainer.setColorSchemeColors(
                getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_red_light)
        );
    }

    //*** Loader Functions ***//
    @NonNull
    @Override
    public Loader<List<Article>> onCreateLoader(int i, @Nullable Bundle bundle) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        // getString retrieves a String value from the preferences. The second parameter is the default value for this preference.
        String articles_count = sharedPrefs.getString(getString(R.string.settings_articles_count_key),getString(R.string.settings_articles_count_default));

        String orderBy  = sharedPrefs.getString(getString(R.string.settings_order_by_key),getString(R.string.settings_order_by_default));

        // parse breaks apart the URI string that's passed into its parameter
        Uri baseUri = Uri.parse(BASE_REQUEST_URL);

        // buildUpon prepares the baseUri that we just parsed so we can add query parameters to it
        Uri.Builder uriBuilder = baseUri.buildUpon();

        // Append query parameter and its value.
        uriBuilder.appendQueryParameter("page-size", articles_count);
        uriBuilder.appendQueryParameter("order-by", orderBy);
        uriBuilder.appendQueryParameter("show-tags", "contributor");
        uriBuilder.appendQueryParameter("q", "football worldcup");
        uriBuilder.appendQueryParameter("api-key", apiKey);

        // Feed the completed uri and create a new loader for the given URL
        // That returns list of Objects
        return new ArticleLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Article>> loader, List<Article> Articles) {
        // Clear any previous objects
        // Clear/Hide loading indicator because the data has been loaded
        View loadingIndicator = findViewById(R.id.progressBar);
        loadingIndicator.setVisibility(View.GONE);

        // Clear/Stop swipeContainer refresh
        swipeContainer.setRefreshing(false);

        // Clear the adapter of previous Article data
        mAdapter.clear();

        // Clear empty state text
        mEmptyStateTextView.setText("");

        // If there is a valid list of {@link Article}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (Articles != null && !Articles.isEmpty()) {
            mAdapter.updateContent(Articles);
            mAdapter.notifyDataSetChanged();
        } else {
            // Set empty state text to display "No Articles found."
            mEmptyStateTextView.setText(R.string.no_articles);
        }

        // Check internet last step to override no_articles
        if (isConnected()) {
            // Signal getting new data done
            Toast.makeText(getApplicationContext(), "Fetching new data completed.", Toast.LENGTH_SHORT).show();
        } else {
            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Article>> loader) {
        // Loader reset, so we can clear out our existing data.
        mAdapter.clear();
    }

    //*** Menu Functions ***//
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        } else if (id == R.id.refresh) {
            restartTheLoader();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Helper function to start the Article Loader
     */
    public void restartTheLoader() {
        if (isConnected()) {
            // Clear empty state
            mEmptyStateTextView.setText(null);

            getLoaderManager().restartLoader(Article_LOADER_ID, null, this);
        } else {
            // Clear the adapter of previous Article data
            mAdapter.clear();

            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet_connection);

            // Stop swipeContainer refresh
            swipeContainer.setRefreshing(false);
        }
    }

    /**
     * Helper function to check internet connectivity
     */
    public boolean isConnected() {
        // Check internet connectivity
        // Get a reference to the ConnectivityManager to check state of network connectivity
        final ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo activeNetwork = null;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}