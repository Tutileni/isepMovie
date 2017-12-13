package com.simao.isepmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

public class SimilarModel implements Parcelable {
    private int id;
    private String posterPath, title, releaseDate, mediaType;

    public SimilarModel() {

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

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    protected SimilarModel(Parcel in) {
        id = in.readInt();
        posterPath = in.readString();
        title = in.readString();
        releaseDate = in.readString();
        mediaType = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(posterPath);
        dest.writeString(title);
        dest.writeString(releaseDate);
        dest.writeString(mediaType);
    }

    public static final Parcelable.Creator<SimilarModel> CREATOR = new Parcelable.Creator<SimilarModel>() {
        @Override
        public SimilarModel createFromParcel(Parcel in) {
            return new SimilarModel(in);
        }

        @Override
        public SimilarModel[] newArray(int size) {
            return new SimilarModel[size];
        }
    };
}
