package com.example.funkopoptracker.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FunkoContentProvider extends ContentProvider {

    // Table names
    public static final String TABLE_FUNKOS = "funkosTable";
    public static final String TABLE_PRICE_HISTORY = "price_history";
    public static final String DB_NAME = "funkoDB";

    // Column names
    public static final String COL_NAME = "NAME";
    public static final String COL_NUMBER = "NUMBER";
    public static final String COL_RARITY = "RARITY";
    public static final String COL_PICTURE = "PICTURE";
    public static final String COL_PRICE = "PRICE";
    public static final String COL_IS_WISHLIST = "IS_WISHLIST";
    public static final String COL_CONDITION = "CONDITION";
    public static final String COL_NOTES = "NOTES";

    // price history columns
    public static final String COL_FUNKO_ID = "funko_id";
    public static final String COL_TIMESTAMP = "timestamp";

    // SQL Create statements
    public final static String SQL_CREATE_FUNKOS = "CREATE TABLE " + TABLE_FUNKOS + " (" +
            "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_NAME + " TEXT, " +
            COL_NUMBER + " INTEGER, " +
            COL_RARITY + " INTEGER, " +
            COL_PICTURE + " TEXT, " +
            COL_PRICE + " REAL, " +
            COL_IS_WISHLIST + " INTEGER DEFAULT 0 ," +
            COL_CONDITION + " TEXT DEFAULT 'Mint' , " +
            COL_NOTES + " TEXT DEFAULT ''" +
            ")";

    public final static String SQL_CREATE_PRICE_HISTORY = "CREATE TABLE " + TABLE_PRICE_HISTORY + " (" +
            "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_FUNKO_ID + " INTEGER, " +
            COL_PRICE + " REAL, " +
            COL_TIMESTAMP + " INTEGER " +
            ")";

    // Separate URIs for each table
    public static final Uri CONTENT_URI_OWNED = Uri.parse("content://com.example.funkotracker.provider/owned");
    public static final Uri CONTENT_URI_WISHLIST = Uri.parse("content://com.example.funkotracker.provider/wishlist");
    public static final Uri CONTENT_URI_PRICE_HISTORY = Uri.parse("content://com.example.funkotracker.provider/price_history");

    MainDatabaseHelper mHelper;

    protected final class MainDatabaseHelper extends SQLiteOpenHelper {

        public MainDatabaseHelper(Context context) {
            super(context, DB_NAME, null, 8);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_FUNKOS);
            db.execSQL(SQL_CREATE_PRICE_HISTORY);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Drop all existing tables and recreate with current schema
            db.execSQL("DROP TABLE IF EXISTS funkoTable");
            db.execSQL("DROP TABLE IF EXISTS funkoOwnedTable");
            db.execSQL("DROP TABLE IF EXISTS funkoWishlistTable");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_FUNKOS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRICE_HISTORY);
            onCreate(db);
        }
    }

    private boolean isWishlistUri(Uri uri) {
        String path = uri.getPath();
        return path != null && path.contains("wishlist");
    }

    private boolean isPriceHistoryUri(Uri uri) {
        String path = uri.getPath();
        return path != null && path.contains("price_history");
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        if (isPriceHistoryUri(uri)) {
            return mHelper.getWritableDatabase().delete(TABLE_PRICE_HISTORY, selection, selectionArgs);
        }

        // Add wishlist filter to selection
        String wishlistValue = isWishlistUri(uri) ? "1" : "0";
        String finalSelection = COL_IS_WISHLIST + " = " + wishlistValue;
        if (selection != null && !selection.isEmpty()) {
            finalSelection = finalSelection + " AND (" + selection + ")";
        }

        return mHelper.getWritableDatabase().delete(TABLE_FUNKOS, finalSelection, selectionArgs);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (isPriceHistoryUri(uri)) {
            long id = mHelper.getWritableDatabase().insert(TABLE_PRICE_HISTORY, null, values);
            return Uri.withAppendedPath(uri, id + "");
        }

        // Set the wishlist flag based on the URI
        if (values == null) {
            values = new ContentValues();
        }
        values.put(COL_IS_WISHLIST, isWishlistUri(uri) ? 1 : 0);

        long id = mHelper.getWritableDatabase().insert(TABLE_FUNKOS, null, values);
        return Uri.withAppendedPath(uri, id + "");
    }

    @Override
    public boolean onCreate() {
        mHelper = new MainDatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        if (isPriceHistoryUri(uri)) {
            return mHelper.getReadableDatabase().query(TABLE_PRICE_HISTORY, projection, selection,
                    selectionArgs, null, null, sortOrder);
        }

        // Add wishlist filter to selection
        String wishlistValue = isWishlistUri(uri) ? "1" : "0";
        String finalSelection = COL_IS_WISHLIST + " = " + wishlistValue;
        if (selection != null && !selection.isEmpty()) {
            finalSelection = finalSelection + " AND (" + selection + ")";
        }

        return mHelper.getReadableDatabase().query(TABLE_FUNKOS, projection, finalSelection,
                selectionArgs, null, null, sortOrder);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        if (isPriceHistoryUri(uri)) {
            return mHelper.getWritableDatabase().update(TABLE_PRICE_HISTORY, values, selection, selectionArgs);
        }

        // Add wishlist filter to selection
        String wishlistValue = isWishlistUri(uri) ? "1" : "0";
        String finalSelection = COL_IS_WISHLIST + " = " + wishlistValue;
        if (selection != null && !selection.isEmpty()) {
            finalSelection = finalSelection + " AND (" + selection + ")";
        }

        return mHelper.getWritableDatabase().update(TABLE_FUNKOS, values, finalSelection, selectionArgs);
    }
}
