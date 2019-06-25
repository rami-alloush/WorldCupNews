package net.rmasoft.worldcupnews;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

class Article {

    private final String mTitle;
    private final String mSection;
    private final String mContributor;
    private final String mPublishDate;
    private final String mURL;

    public Article(String Title, String Section, String PublishDate, String Contributor, String URL) {
        this.mTitle = Title;
        this.mSection = Section;
        this.mPublishDate = PublishDate;
        this.mContributor = Contributor;
        this.mURL = URL;
    }

    public String getTitle() {
        return mTitle;
    }

    /**
     * Helper method to convert the server PublishDate into Date Object
     * @return dateObject
     */
    private Date getPublishDate() {
        // Create a new Date object from the time String of the Article
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        // Tell the formatter about the original timezone of thr Article
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        Date dateObject = null;
        try {
            dateObject = sdf.parse(mPublishDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateObject;
    }

    /**
     * Return the formatted date string (i.e. "Mar 3, 1984") from a Date object.
     */
    public String getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("LLL dd, yyyy", Locale.ENGLISH);
        return dateFormat.format(getPublishDate());
    }

    /**
     * Return the formatted date string (i.e. "4:30 PM") from a Date object.
     */
    public String getTime() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.ENGLISH);
        return timeFormat.format(getPublishDate());
    }

    public String getSectionAndContributor() {
        if (mContributor.equals("Not Available")) {
            return "In " + mSection;
        } else{
            return "By " + mContributor + " in " + mSection;
        }
    }

    public String getURL() {
        return mURL;
    }


}