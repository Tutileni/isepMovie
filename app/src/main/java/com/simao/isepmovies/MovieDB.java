package com.simao.isepmovies;

import android.app.Application;


public class MovieDB extends Application {
    public static final String url = "https://api.themoviedb.org/3/";
    public static final String key = "d0f837b0ebf2eef499229f1cc3038b23";
    public static final String imageUrl = "https://image.tmdb.org/t/p/";

    public static final String trailerImageUrl = "http://i1.ytimg.com/vi/";
    public static final String youtube = "https://www.youtube.com/watch?v=";
    @Override
    public void onCreate() {
        super.onCreate();
    }
}