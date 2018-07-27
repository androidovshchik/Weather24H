package io.androidovshchik.weather24h.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.github.androidovshchik.models.Row;

import org.jetbrains.annotations.NotNull;

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
        setRowId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ROW_ID)));
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
        ContentValues values = new ContentValues();
        if (getRowId() != NONE) {
            values.put(COLUMN_ROW_ID, getRowId());
        }
        values.put(COLUMN_API_ID, apiId);
        values.put(COLUMN_MEANING, meaning);
        values.put(COLUMN_ICON_DAY, iconDay);
        values.put(COLUMN_ICON_NIGHT, iconNight);
        values.put(COLUMN_BACK_DAY, backDay);
        values.put(COLUMN_BACK_NIGHT, backNight);
        return values;
    }

    public static int getIcon(Context context, String icon) {
        return context.getResources().getIdentifier("ic_" + icon + "_big", "drawable",
            context.getPackageName());
    }

    public static int getBackground(Context context, String icon) {
        return context.getResources().getIdentifier("ic_" + icon, "drawable",
            context.getPackageName());
    }

    public Data clone() {
        try {
            return (Data) super.clone();
        } catch( CloneNotSupportedException e) {
            return new Data();
        }
    }
}