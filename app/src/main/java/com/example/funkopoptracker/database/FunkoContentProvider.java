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

    public static final String TABLE_NAME = "funkoTab;e";
    public static final String DB_NAME = "funkoDB";

    public final static String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + " (" +
            "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
//            Examples (based on pokemon homework):
//            COL_NATNUM + " INTEGER, " +
//            COL_NAME + " TEXT, " +
//            COL_SPECIES + " TEXT, " +
//            COL_GENDER + " TEXT, " +
//            COL_HEIGHT + " REAL, " +
//            COL_WEIGHT + " REAL, " +
//            COL_LEVEL + " INTEGER, " +
//            COL_HP + " INTEGER, " +
//            COL_ATTACK + " INTEGER, " +
//            COL_DEFENSE + " INTEGER" +
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
//        String columnExample = values.getAsString(COL_);
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
