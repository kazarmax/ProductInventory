package com.example.android.inventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class ProductContract {

    /**
     * The "Content authority" is a name for the entire content provider
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.inventory";

    /**
     * The base of all URI's which apps will use to contact the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URIs)
     */
    public static final String PATH_PRODUCTS = "products";

    private ProductContract() {}

    /**
     * Inner class that defines constant values for the products database table.
     * Each entry in the table represents a single product.
     */
    public static final class ProductEntry implements BaseColumns {

        /** The content URI to access the product data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of products.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single product.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        /** Name of database table for products */
        public static final String TABLE_NAME = "products";

        /**
         * Unique ID number for the product (only for use in the database table).
         *
         * Type: INTEGER
         */
        public static final String _ID = BaseColumns._ID;

        /**
         * Name of the product.
         *
         * Type: TEXT
         */
        public static final String COLUMN_PRODUCT_NAME = "name";

        /**
         * Quantity of the product items available.
         *
         * Type: INTEGER
         */
        public static final String COLUMN_PRODUCT_QUANTITY = "quantity";

        /**
         * Price of the product.
         *
         * Type: INTEGER
         */
        public static final String COLUMN_PRODUCT_PRICE = "price";

        /**
         * Name of the product supplier.
         *
         * Type: TEXT
         */
        public static final String COLUMN_PRODUCT_SUPPLIER_NAME = "supplier_name";

        /**
         * Email of the product supplier.
         *
         * Type: TEXT
         */
        public static final String COLUMN_PRODUCT_SUPPLIER_EMAIL = "supplier_email";

        /**
         * Image of the product.
         *
         * Type: BLOB
         */
        public static final String COLUMN_PRODUCT_IMAGE = "image";

    }

}
