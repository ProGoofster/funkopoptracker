package com.example.funkopoptracker;

import android.content.ContentValues;
import android.database.Cursor;

import com.example.funkopoptracker.database.FunkoContentProvider;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FunkoPop implements Serializable {
    private int id;
    private String name;
    private int number;
    private final int rarity;
    private final String picture;
    private final double price;
    private String condition;
    private String notes;

    public FunkoPop(String name, int number) {
        this.name = name;
        this.number = number;
        rarity = 0;
        picture = null;
        price = 0.0;
        this.condition = "Mint";
        this.notes = "";
    }

    public FunkoPop(String name, int number, int rarity, String picture, double price) {
        this.name = name;
        this.number = number;
        this.rarity = rarity;
        this.picture = picture;
        this.price = price;
        this.condition = "Mint";
        this.notes = "";
    }

    public FunkoPop(String name, int number, int rarity, String picture, double price, String condition, String notes) {
        this.name = name;
        this.number = number;
        this.rarity = rarity;
        this.picture = picture;
        this.price = price;
        this.condition = condition != null ? condition : "Mint";
        this.notes = notes != null ? notes : "";
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(FunkoContentProvider.COL_NAME, name);
        values.put(FunkoContentProvider.COL_NUMBER, number);
        values.put(FunkoContentProvider.COL_RARITY, rarity);
        values.put(FunkoContentProvider.COL_PICTURE, picture);
        values.put(FunkoContentProvider.COL_PRICE, price);
        values.put(FunkoContentProvider.COL_CONDITION, condition);
        values.put(FunkoContentProvider.COL_NOTES, notes);
        return values;
    }

    public static FunkoPop fromCursor(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(FunkoContentProvider.COL_NAME));
        int number = cursor.getInt(cursor.getColumnIndexOrThrow(FunkoContentProvider.COL_NUMBER));
        int rarity = cursor.getInt(cursor.getColumnIndexOrThrow(FunkoContentProvider.COL_RARITY));
        String picture = cursor.getString(cursor.getColumnIndexOrThrow(FunkoContentProvider.COL_PICTURE));
        double price = cursor.getDouble(cursor.getColumnIndexOrThrow(FunkoContentProvider.COL_PRICE));

        //Handles conditions and notes (for edit button implementation)
        String condition = "Mint";
        String notes = "";
        try{
            condition = cursor.getString(cursor.getColumnIndexOrThrow(FunkoContentProvider.COL_CONDITION));
            notes = cursor.getString(cursor.getColumnIndexOrThrow(FunkoContentProvider.COL_NOTES));
        } catch (Exception e) {
            //not implemented yet
        }

        FunkoPop pop = new FunkoPop(name, number, rarity, picture, price, condition, notes);
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
    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
    public String getCondition() {
        return condition != null ? condition : "Mint";
    }

    public String getNotes() {
        return notes != null ? notes : "";
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
