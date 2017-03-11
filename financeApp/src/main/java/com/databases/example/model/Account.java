package com.databases.example.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.databases.example.data.DatabaseHelper;

import java.util.ArrayList;

//An Object Class used to hold the data of each account record
public class Account implements Parcelable {

    public final int id;
    public final String name;
    public final String balance;
    public final String date;
    public final String time;

    public Account(int id, String name, String balance, String date, String time) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.date = date;
        this.time = time;
    }

    /**
     * Method to get accounts out of a cursor object
     *
     * @param cursor the cursor object containing accounts
     * @return an array list of all the accounts in the cursor object
     */
    public static ArrayList<Account> getAccounts(Cursor cursor) {
        ArrayList<Account> accounts = new ArrayList<>();
        Account account;

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            account = new Account(
                    cursor.getInt(cursor.getColumnIndex(DatabaseHelper.ACCOUNT_ID)),
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.ACCOUNT_NAME)),
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.ACCOUNT_BALANCE)),
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.ACCOUNT_DATE)),
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.ACCOUNT_TIME))
            );
            accounts.add(account);
        }

        return accounts;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", balance='" + balance + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeString(this.balance);
        dest.writeString(this.date);
        dest.writeString(this.time);
    }

    protected Account(Parcel in) {
        this.id = in.readInt();
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
