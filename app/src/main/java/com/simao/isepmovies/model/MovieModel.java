package com.simao.isepmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;


public class MovieModel implements Comparator<MovieModel>, Parcelable {


    private int id;
    private String title, releaseDate, posterPath, character, departmentAndJob, mediaType;

    public MovieModel() {
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        try {
            this.releaseDate = releaseDate.substring(0, 4);
        } catch (java.lang.StringIndexOutOfBoundsException e) {
            this.releaseDate = null;
        }
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCharacter() {
        return this.character;
    }

    public void setCharacter(String character) {
        this.character = character;
    }

    public String getDepartmentAndJob() {
        return this.departmentAndJob;
    }

    public void setDepartmentAndJob(String departmentAndJob) {
        this.departmentAndJob = departmentAndJob;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public int compare(MovieModel movie1, MovieModel movie2) {
        String year1, year2;
        int compareYear1, compareYear2;

        try {
            year1 = movie1.getReleaseDate();
        } catch (java.lang.NullPointerException e) {
            year1 = "0";
        }

        try {
            year2 = movie2.getReleaseDate();
        } catch (java.lang.NullPointerException e) {
            year2 = "0";
        }


        try {
            compareYear1 = Integer.parseInt(year1);
        } catch (java.lang.NumberFormatException e) {
            compareYear1 = 0;
        }


        try {
            compareYear2 = Integer.parseInt(year2);
        } catch (java.lang.NumberFormatException e) {
            compareYear2 = 0;
        }

        if (compareYear1 == compareYear2)
            return 0;

        if (compareYear1 < compareYear2)
            return 1;
        else return -1;
    }


    protected MovieModel(Parcel in) {
        id = in.readInt();
        title = in.readString();
        releaseDate = in.readString();
        posterPath = in.readString();
        character = in.readString();
        departmentAndJob = in.readString();
        mediaType = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(releaseDate);
        dest.writeString(posterPath);
        dest.writeString(character);
        dest.writeString(departmentAndJob);
        dest.writeString(mediaType);
    }

    public static final Parcelable.Creator<MovieModel> CREATOR = new Parcelable.Creator<MovieModel>() {
        @Override
        public MovieModel createFromParcel(Parcel in) {
            return new MovieModel(in);
        }

        @Override
        public MovieModel[] newArray(int size) {
            return new MovieModel[size];
        }
    };
}