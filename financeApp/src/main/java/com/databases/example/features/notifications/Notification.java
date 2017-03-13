package com.databases.example.features.notifications;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.databases.example.database.DatabaseHelper;

import java.util.ArrayList;

/**
 * Created by kwelsh on 3/9/17.
 */

public class Notification implements Parcelable {
    public final int id;
    public final String name;
    public final String value;
    public final String date;

    public Notification(int id, String name, String value, String date) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.date = date;
    }

    /**
     * Method to get subcategories out of a cursor object
     *
     * @param cursor the cursor object containing subcategory
     * @return an array list of all the subcategories in the cursor object
     */
    public static ArrayList<Notification> getNotifications(Cursor cursor) {
        ArrayList<Notification> notifications = new ArrayList<>();
        Notification notification;

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            notification = new Notification(
                    cursor.getInt(cursor.getColumnIndex(DatabaseHelper.NOT_ID)),
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.NOT_NAME)),
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.NOT_VALUE)),
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.NOT_DATE))
            );
            notifications.add(notification);
        }

        return notifications;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", date='" + date + '\'' +
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
        dest.writeString(this.value);
        dest.writeString(this.date);
    }

    protected Notification(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.value = in.readString();
        this.date = in.readString();
    }

    public static final Parcelable.Creator<Notification> CREATOR = new Parcelable.Creator<Notification>() {
        @Override
        public Notification createFromParcel(Parcel source) {
            return new Notification(source);
        }

        @Override
        public Notification[] newArray(int size) {
            return new Notification[size];
        }
    };
}
