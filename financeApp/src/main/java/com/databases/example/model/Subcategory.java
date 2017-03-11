package com.databases.example.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.databases.example.data.DatabaseHelper;

import java.util.ArrayList;

//An Object Class used to hold the data of each sub-category record
public class Subcategory implements Parcelable {
    public final int id;
    public final int catId;
    public final boolean isDefault;
    public final String name;
    public final String note;

    public Subcategory(int id, int catId, boolean isDefault, String name, String note) {
        this.id = id;
        this.catId = catId;
        this.isDefault = isDefault;
        this.name = name;
        this.note = note;
    }

    /**
     * Method to get subcategories out of a cursor object
     *
     * @param cursor the cursor object containing subcategory
     * @return an array list of all the subcategories in the cursor object
     */
    public static ArrayList<Subcategory> getSubcategories(Cursor cursor) {
        ArrayList<Subcategory> subcategories = new ArrayList<>();
        Subcategory subcategory;

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            subcategory = new Subcategory(
                    cursor.getInt(cursor.getColumnIndex(DatabaseHelper.SUBCATEGORY_ID)),
                    cursor.getInt(cursor.getColumnIndex(DatabaseHelper.SUBCATEGORY_CAT_ID)),
                    Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(DatabaseHelper.SUBCATEGORY_IS_DEFAULT))),
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.SUBCATEGORY_NAME)),
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.SUBCATEGORY_NOTE))
            );
            subcategories.add(subcategory);
        }

        return subcategories;
    }

    @Override
    public String toString() {
        return "Subcategory{" +
                "id=" + id +
                ", catId=" + catId +
                ", isDefault=" + isDefault +
                ", name='" + name + '\'' +
                ", note='" + note + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeInt(this.catId);
        dest.writeByte(this.isDefault ? (byte) 1 : (byte) 0);
        dest.writeString(this.name);
        dest.writeString(this.note);
    }

    protected Subcategory(Parcel in) {
        this.id = in.readInt();
        this.catId = in.readInt();
        this.isDefault = in.readByte() != 0;
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
