package com.example.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Pantry.db";
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_NAME = "pantry";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "ingredient_name";
    private static final String COLUMN_QUANTITY = "quantity";
    private static final String COLUMN_EXPIRY = "expiry_date";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_QUANTITY + " TEXT, " +
                COLUMN_EXPIRY + " TEXT)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Save ingredient
    public void insertPantryItem(PantryItem item) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, item.getName());
        values.put(COLUMN_QUANTITY, item.getQuantity());
        values.put(COLUMN_EXPIRY, item.getExpiry());

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    // Read all ingredients
    public ArrayList<PantryItem> getAllPantryItems() {

        ArrayList<PantryItem> pantryList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                COLUMN_NAME + " ASC"
        );

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                String quantity = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY));
                String expiry = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPIRY));

                pantryList.add(new PantryItem(name, quantity, expiry));

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return pantryList;
    }
}