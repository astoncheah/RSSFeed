package com.example.android.rssfeed;

/**
 * Created by cheah on 19/10/16.
 */

public class RSSFeed {
    String title;
    String sectionName;
    String infoLink;
    String publishedDate;

    public RSSFeed(String title, String sectionName, String infoLink, String publishedDate) {
        this.title = title;
        this.sectionName = sectionName;
        this.infoLink = infoLink;
        this.publishedDate = publishedDate;
    }

    public String getTitle() {
        return title;
    }

    public String getSectionName() {
        return sectionName;
    }

    public String getInfoLink() {
        return infoLink;
    }

    public String getPublishedDate() {
        return publishedDate;
    }
}
