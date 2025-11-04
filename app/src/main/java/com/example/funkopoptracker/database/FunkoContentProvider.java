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

    public static final String TABLE_NAME = "funkoTable";
    public static final String DB_NAME = "funkoDB";

    public static final String COL_NAME = "NAME";
    public static final String COL_NUMBER = "NUMBER";
    public static final String COL_RARITY = "RARITY";
    public static final String COL_PICTURE = "PICTURE";


    public final static String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + " (" +
            "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_NAME + " TEXT, " +
            COL_NUMBER + " INTEGER, " +
            COL_RARITY + " INTEGER, " +
            COL_PICTURE + " TEXT " +
            ")";


    public static final Uri CONTENT_URI = Uri.parse("content://com.example.funkotracker.provider");

    MainDatabaseHelper mHelper;

    protected final class MainDatabaseHelper extends SQLiteOpenHelper {


        public MainDatabaseHelper(Context context) {
            super(context, DB_NAME, null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return mHelper.getWritableDatabase().delete(TABLE_NAME, selection, selectionArgs);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        long id = mHelper.getWritableDatabase().insert(TABLE_NAME, null, values);
        return uri.withAppendedPath(uri, id + "");
    }

    @Override
    public boolean onCreate() {
        mHelper = new MainDatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return mHelper.getReadableDatabase().query(TABLE_NAME, projection, selection,
                selectionArgs, null, null, sortOrder);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
