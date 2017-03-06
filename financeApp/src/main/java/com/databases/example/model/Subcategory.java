package com.databases.example.model;

import android.os.Parcel;
import android.os.Parcelable;

//An Object Class used to hold the data of each sub-category record
public class Subcategory implements Parcelable {
    public final String id;
    public final String catId;
    public final String name;
    public final String note;

    public Subcategory(String id, String catId, String name, String note) {
        this.id = id;
        this.catId = catId;
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
        dest.writeString(this.catId);
        dest.writeString(this.name);
        dest.writeString(this.note);
    }

    protected Subcategory(Parcel in) {
        this.id = in.readString();
        this.catId = in.readString();
        this.name = in.readString();
        this.note = in.readString();
    }

    public static final Parcelable.Creator<Subcategory> CREATOR = new Parcelable.Creator<Subcategory>() {
        @Override
        public Subcategory createFromParcel(Parcel source) {
            return new Subcategory(source);
        }

        @Override
        public Subcategory[] newArray(int size) {
            return new Subcategory[size];
        }
    };
}
