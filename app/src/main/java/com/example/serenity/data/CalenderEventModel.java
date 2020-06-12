package com.example.serenity.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

public class CalenderEventModel implements Parcelable {

    private String mID;
    private String mTitle;
    private Calendar mDate;
    private int mColor;
    private boolean isCompleted;

    public CalenderEventModel(String id, String title, Calendar date, int color, boolean isCompleted) {
        mID = id;
        mTitle = title;
        mDate = date;
        mColor = color;
        this.isCompleted = isCompleted;
    }

    public String getID() {
        return mID;
    }

    public String getTitle() {
        return mTitle;
    }

    public Calendar getDate() {
        return mDate;
    }

    public int getColor() {
        return mColor;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    protected CalenderEventModel(Parcel in) {
        mID = in.readString();
        mTitle = in.readString();
        mColor = in.readInt();
        mDate = (Calendar) in.readSerializable();
        isCompleted = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mID);
        dest.writeString(mTitle);
        dest.writeInt(mColor);
        dest.writeSerializable(mDate);
        dest.writeByte((byte) (isCompleted ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CalenderEventModel> CREATOR = new Creator<CalenderEventModel>() {
        @Override
        public CalenderEventModel createFromParcel(Parcel in) {
            return new CalenderEventModel(in);
        }

        @Override
        public CalenderEventModel[] newArray(int size) {
            return new CalenderEventModel[size];
        }
    };
}
