package io.androidovshchik.weather24h.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.github.androidovshchik.models.Row;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;

@SuppressWarnings("WeakerAccess")
public class Data extends Row implements Cloneable {

    public static final String COLUMN_API_ID = "api_id";
    public static final String COLUMN_MEANING = "meaning";
    public static final String COLUMN_ICON_DAY = "icon_day";
    public static final String COLUMN_ICON_NIGHT = "icon_night";
    public static final String COLUMN_BACK_DAY = "back_day";
    public static final String COLUMN_BACK_NIGHT = "back_night";

    public int apiId;
    public String meaning;
    public String iconDay;
    public String iconNight;
    public String backDay;
    public String backNight;

    @NotNull
    @Override
    public String getTable() {
        return "data";
    }

    @Override
    public void parseCursor(Cursor cursor) {
        apiId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_API_ID));
        meaning = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEANING));
        iconDay = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ICON_DAY));
        iconNight = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ICON_NIGHT));
        backDay = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BACK_DAY));
        backNight = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BACK_NIGHT));
    }

    @NotNull
    @Override
    public ContentValues toContentValues() {
        // TODO
        return new ContentValues();
    }

    public int getIcon(Context context) {
        int hours = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return context.getResources().getIdentifier("ic_" + (hours > 4 && hours < 21 ? iconDay :
            iconNight) + "_big", "drawable", context.getPackageName());
    }

    public int getBackground(Context context) {
        int hours = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return context.getResources().getIdentifier("ic_" + (hours > 4 && hours < 21 ? backDay :
            backNight), "drawable", context.getPackageName());
    }

    public Data clone() {
        try {
            return (Data) super.clone();
        } catch( CloneNotSupportedException e) {
            return new Data();
        }
    }
}