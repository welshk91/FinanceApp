package com.databases.example.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.databases.example.data.DatabaseHelper;

import java.util.ArrayList;

//An Object Class used to hold the data of each category record
public class Category implements Parcelable {
    public final int id;
    public final String name;
    public final String note;

    public Category(int id, String name, String note) {
        this.id = id;
        this.name = name;
        this.note = note;
    }

    /**
     * Method to get categories out of a cursor object
     *
     * @param cursor the cursor object containing categories
     * @return an array list of all the categories in the cursor object
     */
    public static ArrayList<Category> getCategories(Cursor cursor) {
        ArrayList<Category> categories = new ArrayList<>();
        Category category;

        while (cursor.moveToNext()) {
            category = new Category(
                    cursor.getInt(cursor.getColumnIndex(DatabaseHelper.CATEGORY_ID)),
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.CATEGORY_NAME)),
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.CATEGORY_NOTE))
            );
            categories.add(category);
        }

        return categories;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeString(this.note);
    }

    protected Category(Parcel in) {
        this.id = in.readInt();
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
