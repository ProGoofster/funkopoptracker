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
    public static final String TABLE_OWNED = "funkoOwnedTable";
    public static final String TABLE_WISHLIST = "funkoWishlistTable";
    public static final String DB_NAME = "funkoDB";

    // Column names (same for both tables)
    public static final String COL_NAME = "NAME";
    public static final String COL_NUMBER = "NUMBER";
    public static final String COL_PRICE = "PRICE";
    public static final String COL_PICTURE = "PICTURE";

    // SQL Create statements
    public final static String SQL_CREATE_OWNED = "CREATE TABLE " + TABLE_OWNED + " (" +
            "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_NAME + " TEXT, " +
            COL_NUMBER + " INTEGER, " +
            COL_PRICE + " REAL, " +
            COL_PICTURE + " TEXT " +
            ")";

    public final static String SQL_CREATE_WISHLIST = "CREATE TABLE " + TABLE_WISHLIST + " (" +
            "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_NAME + " TEXT, " +
            COL_NUMBER + " INTEGER, " +
            COL_PRICE + " REAL, " +
            COL_PICTURE + " TEXT " +
            ")";

    // Separate URIs for each table
    public static final Uri CONTENT_URI_OWNED = Uri.parse("content://com.example.funkotracker.provider/owned");
    public static final Uri CONTENT_URI_WISHLIST = Uri.parse("content://com.example.funkotracker.provider/wishlist");

    MainDatabaseHelper mHelper;

    protected final class MainDatabaseHelper extends SQLiteOpenHelper {

        public MainDatabaseHelper(Context context) {
            super(context, DB_NAME, null, 4);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_OWNED);
            db.execSQL(SQL_CREATE_WISHLIST);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Drop old tables and recreate with new schema
            db.execSQL("DROP TABLE IF EXISTS funkoTable");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_OWNED);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_WISHLIST);
            onCreate(db);
        }
    }

    private String getTableName(Uri uri) {
        String path = uri.getPath();
        if (path.contains("wishlist")) {
            return TABLE_WISHLIST;
        } else if (path.contains("owned")) {
            return TABLE_OWNED;
        }
        // Default to owned for backward compatibility
        return TABLE_OWNED;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        String tableName = getTableName(uri);
        return mHelper.getWritableDatabase().delete(tableName, selection, selectionArgs);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        String tableName = getTableName(uri);
        long id = mHelper.getWritableDatabase().insert(tableName, null, values);
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
        String tableName = getTableName(uri);
        return mHelper.getReadableDatabase().query(tableName, projection, selection,
                selectionArgs, null, null, sortOrder);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        String tableName = getTableName(uri);
        return mHelper.getWritableDatabase().update(tableName, values, selection, selectionArgs);
    }
}