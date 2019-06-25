package net.rmasoft.worldcupnews;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

/**
 * Loads a list of Article by using an AsyncTask to perform the
 * network request to the given URL.
 */
class ArticleLoader extends AsyncTaskLoader<List<Article>> {

    /** Query URL */
    private final String mUrl;

    /**
     * Constructs a new {@link ArticleLoader}.
     *
     * @param context of the activity
     * @param url to load data from
     */
    public ArticleLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    /**
     * This is on a background thread.
     */
    @Override
    public List<Article> loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        // Perform the network request, parse the response, and extract a list of Article.
        return QueryUtils.fetchData(mUrl);
    }
}