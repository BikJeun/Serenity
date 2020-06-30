package com.example.serenity.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.ServerValue;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

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

    public void setmID(String mID) {
        this.mID = mID;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public void setmDate(Calendar mDate) {
        this.mDate = mDate;
    }

    public void setmColor(int mColor) {
        this.mColor = mColor;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
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

    public Map<String, Object> toFirebaseObject() {
        HashMap<String, Object> event = new HashMap<>();
        event.put("mid", mID);
        event.put("title", mTitle);
        event.put("date", mDate.getTimeInMillis());
        event.put("color", mColor);
        event.put("completed", isCompleted);

        return event;
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
