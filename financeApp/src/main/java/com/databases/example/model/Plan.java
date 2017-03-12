package com.databases.example.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.databases.example.data.DatabaseHelper;

import java.util.ArrayList;

//An Object Class used to hold the data of each transaction record
public class Plan implements Parcelable {
    public final int id;
    public final int acctId;
    public final String name;
    public final String value;
    public final String type;
    public final String category;
    public final String memo;
    public final String offset;
    public final String rate;
    public final String next;
    public final String scheduled;
    public final String cleared;

    public Plan(int id, int acctId, String name, String value, String type, String category, String memo, String offset, String rate, String next, String scheduled, String cleared) {
        this.id = id;
        this.acctId = acctId;
        this.name = name;
        this.value = value;
        this.type = type;
        this.category = category;
        this.memo = memo;
        this.offset = offset;
        this.rate = rate;
        this.next = next;
        this.scheduled = scheduled;
        this.cleared = cleared;
    }

    /**
     * Method to get plans out of a cursor object
     *
     * @param cursor the cursor object containing plans
     * @return an array list of all the plans in the cursor object
     */
    public static ArrayList<Plan> getPlans(Cursor cursor) {
        ArrayList<Plan> plans = new ArrayList<>();
        Plan plan;

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            plan = new Plan(
                    cursor.getInt(cursor.getColumnIndex(DatabaseHelper.PLAN_ID)),
                    cursor.getInt(cursor.getColumnIndex(DatabaseHelper.PLAN_ACCT_ID)),
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAN_NAME)),
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAN_VALUE)),
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAN_TYPE)),
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAN_CATEGORY)),
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAN_MEMO)),
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAN_OFFSET)),
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAN_RATE)),
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAN_NEXT)),
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAN_SCHEDULED)),
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAN_CLEARED))
            );
            plans.add(plan);
        }

        return plans;
    }

    @Override
    public String toString() {
        return "Plan{" +
                "id=" + id +
                ", acctId=" + acctId +
                ", name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", type='" + type + '\'' +
                ", category='" + category + '\'' +
                ", memo='" + memo + '\'' +
                ", offset='" + offset + '\'' +
                ", rate='" + rate + '\'' +
                ", next='" + next + '\'' +
                ", scheduled='" + scheduled + '\'' +
                ", cleared='" + cleared + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeInt(this.acctId);
        dest.writeString(this.name);
        dest.writeString(this.value);
        dest.writeString(this.type);
        dest.writeString(this.category);
        dest.writeString(this.memo);
        dest.writeString(this.offset);
        dest.writeString(this.rate);
        dest.writeString(this.next);
        dest.writeString(this.scheduled);
        dest.writeString(this.cleared);
    }

    protected Plan(Parcel in) {
        this.id = in.readInt();
        this.acctId = in.readInt();
        this.name = in.readString();
        this.value = in.readString();
        this.type = in.readString();
        this.category = in.readString();
        this.memo = in.readString();
        this.offset = in.readString();
        this.rate = in.readString();
        this.next = in.readString();
        this.scheduled = in.readString();
        this.cleared = in.readString();
    }

    public static final Parcelable.Creator<Plan> CREATOR = new Parcelable.Creator<Plan>() {
        @Override
        public Plan createFromParcel(Parcel source) {
            return new Plan(source);
        }

        @Override
        public Plan[] newArray(int size) {
            return new Plan[size];
        }
    };
}
