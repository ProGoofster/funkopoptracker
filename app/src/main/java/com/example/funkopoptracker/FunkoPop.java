package com.example.funkopoptracker;

import android.content.ContentValues;
import android.database.Cursor;

import com.example.funkopoptracker.database.FunkoContentProvider;

import java.util.ArrayList;
import java.util.List;

public class FunkoPop {
    private String name;
    private int number;
    private int rarity;
    private String picture;

    public FunkoPop(String name, int number) {
        this.name = name;
        this.number = number;
    }

    public FunkoPop(String name, int number, int rarity, String picture) {
        this.name = name;
        this.number = number;
        this.rarity = rarity;
        this.picture = picture;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(FunkoContentProvider.COL_NAME, name);
        values.put(FunkoContentProvider.COL_NUMBER, number);
        values.put(FunkoContentProvider.COL_RARITY, rarity);
        values.put(FunkoContentProvider.COL_PICTURE, picture);
        return values;
    }

    public static FunkoPop fromCursor(Cursor cursor) {
        String name = cursor.getString(cursor.getColumnIndexOrThrow(FunkoContentProvider.COL_NAME));
        int number = cursor.getInt(cursor.getColumnIndexOrThrow(FunkoContentProvider.COL_NUMBER));
        int rarity = cursor.getInt(cursor.getColumnIndexOrThrow(FunkoContentProvider.COL_RARITY));
        String picture = cursor.getString(cursor.getColumnIndexOrThrow(FunkoContentProvider.COL_PICTURE));

        return new FunkoPop(name, number, rarity, picture);
    }

    public static List<FunkoPop> allFromCursor(Cursor cursor) {
        List<FunkoPop> list = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(FunkoPop.fromCursor(cursor));
            }
            cursor.close();
        }
        return list;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public int getNumber() {
        return number;
    }
    public String getNumberString() {
        return "#" + number;
    }

    public int getRarity() {
        return rarity;
    }

    public String getPicture() {
        return picture;
    }
}
