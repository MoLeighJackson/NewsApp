package com.example.moleigh.newsapp;

public class Story {

    /** web title of news article **/
    public final String title;

    /** section name of news article **/
    public final String section;

    public Story(String articleTitle, String articleSectionName) {

        title = articleTitle;
        section = articleSectionName;
    }
}
