package com.databases.example.model;

import android.os.Parcel;
import android.os.Parcelable;

//An Object Class used to hold the data of each category record
public class Category implements Parcelable {
    public final String id;
    public final String name;
    public final String note;

    public Category(String id, String name, String note) {
        this.id = id;
        this.name = name;
        this.note = note;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.note);
    }

    protected Category(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.note = in.readString();
    }

    public static final Parcelable.Creator<Category> CREATOR = new Parcelable.Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel source) {
            return new Category(source);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };
}
