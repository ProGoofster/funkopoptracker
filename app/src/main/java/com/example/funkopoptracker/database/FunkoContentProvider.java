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
    public static final String TABLE_PRICE_HISTORY = "price_history";
    public static final String DB_NAME = "funkoDB";

    // Column names (same for both tables)
    public static final String COL_NAME = "NAME";
    public static final String COL_NUMBER = "NUMBER";
    public static final String COL_RARITY = "RARITY";
    public static final String COL_PICTURE = "PICTURE";
    public static final String COL_PRICE = "PRICE";

    // price history columns
    public static final String COL_FUNKO_ID = "funko_id";
    public static final String COL_TIMESTAMP = "timestamp";

    // SQL Create statements
    public final static String SQL_CREATE_OWNED = "CREATE TABLE " + TABLE_OWNED + " (" +
            "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_NAME + " TEXT, " +
            COL_NUMBER + " INTEGER, " +
            COL_RARITY + " INTEGER, " +
            COL_PICTURE + " TEXT, " +
            COL_PRICE + " REAL " +
            ")";

    public final static String SQL_CREATE_WISHLIST = "CREATE TABLE " + TABLE_WISHLIST + " (" +
            "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_NAME + " TEXT, " +
            COL_NUMBER + " INTEGER, " +
            COL_RARITY + " INTEGER, " +
            COL_PICTURE + " TEXT, " +
            COL_PRICE + " REAL " +
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
            super(context, DB_NAME, null, 6);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_OWNED);
            db.execSQL(SQL_CREATE_WISHLIST);
            db.execSQL(SQL_CREATE_PRICE_HISTORY);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion < 2) {
                // Rename old table to owned table
                db.execSQL("ALTER TABLE funkoTable RENAME TO " + TABLE_OWNED);
                // Create new wishlist table
                db.execSQL(SQL_CREATE_WISHLIST);
            }
            if (oldVersion < 3) {
                db.execSQL(SQL_CREATE_PRICE_HISTORY);
            }
            if (oldVersion < 4) {
                //add price column
                db.execSQL("ALTER TABLE " + TABLE_OWNED + " ADD COLUMN " + COL_PRICE + " REAL DEFAULT 0");
                db.execSQL("ALTER TABLE " + TABLE_WISHLIST + " ADD COLUMN " + COL_PRICE + " REAL DEFAULT 0");

                //generate prices for existing pops
                regeneratePrices(db, TABLE_OWNED);
                regeneratePrices(db, TABLE_WISHLIST);
            }
            if (oldVersion < 5) {
                //fix column name case from goofster's version
                try {
                    db.execSQL("ALTER TABLE " + TABLE_OWNED + " RENAME COLUMN price TO PRICE");
                } catch (Exception e) {
                    //already uppercase or doesn't exist
                }
                try {
                    db.execSQL("ALTER TABLE " + TABLE_WISHLIST + " RENAME COLUMN price TO PRICE");
                } catch (Exception e) {
                    //already uppercase or doesn't exist
                }
            }
            if (oldVersion < 6) {
                //add rarity column and populate it based on existing prices
                db.execSQL("ALTER TABLE " + TABLE_OWNED + " ADD COLUMN " + COL_RARITY + " INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE " + TABLE_WISHLIST + " ADD COLUMN " + COL_RARITY + " INTEGER DEFAULT 0");

                //regenerate rarity values based on existing prices
                updateRarityFromPrice(db, TABLE_OWNED);
                updateRarityFromPrice(db, TABLE_WISHLIST);
            }
        }

        private void updateRarityFromPrice(SQLiteDatabase db, String tableName) {
            Cursor cursor = db.query(tableName, null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PRICE));

                int rarity = RandomPriceGenerator.calculateRarity(price);

                ContentValues values = new ContentValues();
                values.put(COL_RARITY, rarity);
                db.update(tableName, values, "_id=" + id, null);
            }
            cursor.close();
        }

        private void regeneratePrices(SQLiteDatabase db, String tableName) {
            Cursor cursor = db.query(tableName, null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME));
                int number = cursor.getInt(cursor.getColumnIndexOrThrow(COL_NUMBER));

                double price = RandomPriceGenerator.generatePrice(name, number);
                int rarity = RandomPriceGenerator.calculateRarity(price);

                ContentValues values = new ContentValues();
                values.put(COL_PRICE, price);
                values.put(COL_RARITY, rarity);
                db.update(tableName, values, "_id=" + id, null);
            }
            cursor.close();
        }
    }

    private String getTableName(Uri uri) {
        String path = uri.getPath();
        if (path.contains("wishlist")) {
            return TABLE_WISHLIST;
        } else if (path.contains("price_history")) {
            return TABLE_PRICE_HISTORY;
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
