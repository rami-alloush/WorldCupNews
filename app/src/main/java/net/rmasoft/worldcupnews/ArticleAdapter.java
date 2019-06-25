package net.rmasoft.worldcupnews;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;

/**
 * An {@link ArticleAdapter} knows how to create a list item layout for each Article
 * using the provided Holder
 */
public class ArticleAdapter extends RecyclerView.Adapter<ArticleHolder> {

    private List<Article> Articles;
    private final Context context;

    /**
     * Constructs a new {@link ArticleAdapter}.
     *
     * @param context  of the app
     * @param Articles is the list of Articles, which is the data source of the adapter
     */
    public ArticleAdapter(Context context, List<Article> Articles) {

        // 1. Initialize our adapter
        this.Articles = Articles;
        this.context = context;
    }

    // 2. Override the onCreateViewHolder method
    // no need to override getView as the holder handles this
    @NonNull
    @Override
    public ArticleHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // 3. Inflate the view and return the new ViewHolder
        //return new ArticleHolder(this.context, view);

        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.article_list_item, parent, false);

        return new ArticleHolder(this.context, v);
    }

    // 4. Override the onBindViewHolder method
    @Override
    public void onBindViewHolder(@NonNull ArticleHolder articleHolder, int i) {

        // 5. Use position to access the correct Article object
        Article currentArticle = this.Articles.get(i);

        // 6. Bind the Article object to the holder
        articleHolder.bindArticle(currentArticle);
    }

    @Override
    public int getItemCount() {
        if (this.Articles != null) {
            return this.Articles.size();
        } else {
            return 0;
        }
    }

    /**
     * Helper method for update
     */
    public void updateContent(List<Article> newArticles) {
        this.Articles = newArticles;
        this.notifyDataSetChanged();
    }

    /**
     * Helper method for clear
     */
    public void clear() {
        updateContent(null);
        this.notifyDataSetChanged();
    }
}