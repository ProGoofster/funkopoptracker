package com.example.funkopoptracker;

import android.content.ContentValues;
import android.database.Cursor;

import com.example.funkopoptracker.database.FunkoContentProvider;

public class FunkoPop {
    private String name;
    private String number;
    private int rarity;
    private String picture;

    public FunkoPop(String name, String number) {
        this.name = name;
        this.number = number;
    }

    public FunkoPop(String name, String number, int rarity, String picture) {
        this.name = name;
        this.number = number;
        this.rarity = rarity;
        this.picture = picture;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(FunkoContentProvider.COL_NAME, name);
        values.put(FunkoContentProvider.COL_NUMBER, Integer.parseInt(number));
        values.put(FunkoContentProvider.COL_RARITY, rarity);
        values.put(FunkoContentProvider.COL_PICTURE, picture);
        return values;
    }

    public static FunkoPop fromCursor(Cursor cursor) {
        String name = cursor.getString(cursor.getColumnIndexOrThrow(FunkoContentProvider.COL_NAME));
        String number = String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(FunkoContentProvider.COL_NUMBER)));
        int rarity = cursor.getInt(cursor.getColumnIndexOrThrow(FunkoContentProvider.COL_RARITY));
        String picture = cursor.getString(cursor.getColumnIndexOrThrow(FunkoContentProvider.COL_PICTURE));

        return new FunkoPop(name, number, rarity, picture);
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public int getRarity() {
        return rarity;
    }

    public String getPicture() {
        return picture;
    }
}
