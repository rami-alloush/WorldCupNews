package net.rmasoft.worldcupnews;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class ArticleHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final TextView articleTitle;
    private final TextView articleSectionAndContributor;
    private final TextView articleDate;
    private final TextView articleTime;

    private Article mArticle;
    private final Context context;

    public ArticleHolder(Context context, @NonNull View itemView) {
        super(itemView);

        // 1. Set the context
        this.context = context;

        // 2. Set up the UI widgets of the holder
        this.articleTitle = itemView.findViewById(R.id.title);
        this.articleSectionAndContributor = itemView.findViewById(R.id.section_and_contributor);
        this.articleDate = itemView.findViewById(R.id.date);
        this.articleTime = itemView.findViewById(R.id.time);

        // 3. Set the "onClick" listener of the holder
        itemView.setOnClickListener(this);
    }

    public void bindArticle(Article article) {

        // 4. Bind the data to the ViewHolder
        this.mArticle = article;
        this.articleTitle.setText(article.getTitle());
        this.articleSectionAndContributor.setText(article.getSectionAndContributor());
        this.articleDate.setText(article.getDate());
        this.articleTime.setText(article.getTime());
        // URL ...
    }

    // Replaces the setOnItemClickListener as we implements View.OnClickListener
    @Override
    public void onClick(View view) {

        // 5. Handle the onClick event for the ViewHolder
        // Convert the String URL into a URI object (to pass into the Intent constructor)
        Uri ArticleUri = Uri.parse(mArticle.getURL());

        // Create a new intent to view the Article URI
        Intent websiteIntent = new Intent(Intent.ACTION_VIEW, ArticleUri);

        // Send the intent to launch a new activity
        context.startActivity(websiteIntent);
    }
}
