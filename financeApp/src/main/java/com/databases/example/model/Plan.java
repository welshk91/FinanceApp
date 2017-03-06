package com.databases.example.model;

import android.os.Parcel;
import android.os.Parcelable;

//An Object Class used to hold the data of each transaction record
public class Plan implements Parcelable {
    public final String id;
    public final String acctId;
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

    public Plan(String id, String acctId, String name, String value, String type, String category, String memo, String offset, String rate, String next, String scheduled, String cleared) {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.acctId);
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
        this.id = in.readString();
        this.acctId = in.readString();
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
