package com.simao.isepmovies.model;

import android.os.Parcel;
import android.os.Parcelable;


public class GenresModel implements Parcelable {

    private int id;
    private String name;

    public GenresModel() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    protected GenresModel(Parcel in) {
        id = in.readInt();
        name = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
    }

    public static final Parcelable.Creator<GenresModel> CREATOR = new Parcelable.Creator<GenresModel>() {
        @Override
        public GenresModel createFromParcel(Parcel in) {
            return new GenresModel(in);
        }

        @Override
        public GenresModel[] newArray(int size) {
            return new GenresModel[size];
        }
    };
}
