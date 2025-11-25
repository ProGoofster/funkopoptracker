package com.example.funkopoptracker;

import android.content.ContentValues;
import android.database.Cursor;

import com.example.funkopoptracker.database.FunkoContentProvider;

import java.util.ArrayList;
import java.util.List;

public class FunkoPop {
    private int id;
    private final String name;
    private final int number;
    private final int rarity;
    private final String picture;
    private final double price;

    public FunkoPop(String name, int number) {
        this.name = name;
        this.number = number;
        rarity = 0;
        picture = null;
        price = 0.0;
    }

    public FunkoPop(String name, int number, int rarity, String picture, double price) {
        this.name = name;
        this.number = number;
        this.rarity = rarity;
        this.picture = picture;
        this.price = price;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(FunkoContentProvider.COL_NAME, name);
        values.put(FunkoContentProvider.COL_NUMBER, number);
        values.put(FunkoContentProvider.COL_RARITY, rarity);
        values.put(FunkoContentProvider.COL_PICTURE, picture);
        values.put(FunkoContentProvider.COL_PRICE, price);
        return values;
    }

    public static FunkoPop fromCursor(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(FunkoContentProvider.COL_NAME));
        int number = cursor.getInt(cursor.getColumnIndexOrThrow(FunkoContentProvider.COL_NUMBER));
        int rarity = cursor.getInt(cursor.getColumnIndexOrThrow(FunkoContentProvider.COL_RARITY));
        String picture = cursor.getString(cursor.getColumnIndexOrThrow(FunkoContentProvider.COL_PICTURE));
        double price = cursor.getDouble(cursor.getColumnIndexOrThrow(FunkoContentProvider.COL_PRICE));

        FunkoPop pop = new FunkoPop(name, number, rarity, picture, price);
        pop.id = id;
        return pop;
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

    public double getPrice() {
        return price;
    }

    public int getId() {
        return id;
    }
}
