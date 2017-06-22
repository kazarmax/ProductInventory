package com.example.android.inventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.example.android.inventory.data.ProductContract.ProductEntry;

public class ProductDbHelper extends SQLiteOpenHelper{

    private static final String DATABASE_NAME = "inventory.db";
    private static final int DATABASE_VERSION = 1;

    private static final String LOG_TAG = ProductDbHelper.class.getSimpleName();

    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String COMMA_SEP = ",";

        String SQL_CREATE_PRODUCTS_TABLE =
                "CREATE TABLE " + ProductEntry.TABLE_NAME + "(" +
                        ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
                        ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL" + COMMA_SEP +
                        ProductEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL" + COMMA_SEP +
                        ProductEntry.COLUMN_PRODUCT_PRICE + " INTEGER NOT NULL" + COMMA_SEP +
                        ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME + " TEXT NOT NULL" + COMMA_SEP +
                        ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL + " TEXT NOT NULL" + COMMA_SEP +
                        ProductEntry.COLUMN_PRODUCT_IMAGE + " BLOB NOT NULL" + ");";

        db.execSQL(SQL_CREATE_PRODUCTS_TABLE);

        Log.v(LOG_TAG, "SQL script to create products table = \n" + SQL_CREATE_PRODUCTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        String SQL_DROP_PRODUCTS_TABLE =
                "DROP TABLE IF EXISTS " + ProductEntry.TABLE_NAME;
        db.execSQL(SQL_DROP_PRODUCTS_TABLE);
        onCreate(db);
    }

}
