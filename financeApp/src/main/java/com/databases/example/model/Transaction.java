package com.databases.example.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.databases.example.data.DatabaseHelper;

import java.util.ArrayList;

//An Object Class used to hold the data of each transaction record
public class Transaction implements Parcelable {

    public final int id;
    public final int acctId;
    public final int planId;
    public final String name;
    public final String value;
    public final String type;
    public final String category;
    public final String checknum;
    public final String memo;
    public final String date;
    public final String time;
    public final String cleared;

    public Transaction(int id, int acctId, int planId, String name, String value, String type, String category, String checknum, String memo, String date, String time, String cleared) {
        this.id = id;
        this.acctId = acctId;
        this.planId = planId;
        this.name = name;
        this.value = value;
        this.type = type;
        this.category = category;
        this.checknum = checknum;
        this.memo = memo;
        this.date = date;
        this.time = time;
        this.cleared = cleared;
    }

    /**
     * Method to get transactions out of a cursor object
     *
     * @param cursor the cursor object containing transactions
     * @return an array list of all the transactions in the cursor object
     */
    public static ArrayList<Transaction> getTransactions(Cursor cursor) {
        ArrayList<Transaction> transactions = new ArrayList<>();
        Transaction transaction;

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            transaction = new Transaction(
                    cursor.getInt(cursor.getColumnIndex(DatabaseHelper.TRANS_ID)),
                    cursor.getInt(cursor.getColumnIndex(DatabaseHelper.TRANS_ACCT_ID)),
                    cursor.getInt(cursor.getColumnIndex(DatabaseHelper.TRANS_PLAN_ID)),
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.TRANS_NAME)),
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.TRANS_VALUE)),
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.TRANS_TYPE)),
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.TRANS_CATEGORY)),
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.TRANS_CHECKNUM)),
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.TRANS_MEMO)),
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.TRANS_TIME)),
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.TRANS_DATE)),
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.TRANS_CLEARED))
            );
            transactions.add(transaction);
        }

        return transactions;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeInt(this.acctId);
        dest.writeInt(this.planId);
        dest.writeString(this.name);
        dest.writeString(this.value);
        dest.writeString(this.type);
        dest.writeString(this.category);
        dest.writeString(this.checknum);
        dest.writeString(this.memo);
        dest.writeString(this.time);
        dest.writeString(this.date);
        dest.writeString(this.cleared);
    }

    protected Transaction(Parcel in) {
        this.id = in.readInt();
        this.acctId = in.readInt();
        this.planId = in.readInt();
        this.name = in.readString();
        this.value = in.readString();
        this.type = in.readString();
        this.category = in.readString();
        this.checknum = in.readString();
        this.memo = in.readString();
        this.time = in.readString();
        this.date = in.readString();
        this.cleared = in.readString();
    }

    public static final Parcelable.Creator<Transaction> CREATOR = new Parcelable.Creator<Transaction>() {
        @Override
        public Transaction createFromParcel(Parcel source) {
            return new Transaction(source);
        }

        @Override
        public Transaction[] newArray(int size) {
            return new Transaction[size];
        }
    };
}
