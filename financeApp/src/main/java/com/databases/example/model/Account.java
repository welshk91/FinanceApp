package com.databases.example.model;

import android.os.Parcel;
import android.os.Parcelable;

//An Object Class used to hold the data of each account record
public class Account implements Parcelable {

    public final String id;
    public final String name;
    public final String balance;
    public final String date;
    public final String time;

    public Account(String id, String name, String balance, String date, String time) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.date = date;
        this.time = time;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.balance);
        dest.writeString(this.date);
        dest.writeString(this.time);
    }

    protected Account(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.balance = in.readString();
        this.date = in.readString();
        this.time = in.readString();
    }

    public static final Parcelable.Creator<Account> CREATOR = new Parcelable.Creator<Account>() {
        @Override
        public Account createFromParcel(Parcel source) {
            return new Account(source);
        }

        @Override
        public Account[] newArray(int size) {
            return new Account[size];
        }
    };
}
