package com.example.android.inventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.inventory.data.ProductContract.ProductEntry;

/**
 * Displays list of products that were entered and stored in the app.
 */
public class ProductListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = ProductListActivity.class.getSimpleName();
    static final int PRODUCT_CURSOR_LOADER_ID = 0;
    private ProductCursorAdapter mProductCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        // Setup FAB to open ProductDetailsActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProductListActivity.this, ProductDetailsActivity.class);
                startActivity(intent);
            }
        });

        // Find ListView to populate
        ListView productListView = (ListView) findViewById(R.id.list);

        mProductCursorAdapter = new ProductCursorAdapter(this, null);

        // Attach cursor adapter to the ListView
        productListView.setAdapter(mProductCursorAdapter);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyProductListView = findViewById(R.id.empty_list_view);
        productListView.setEmptyView(emptyProductListView);

        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(ProductListActivity.this, ProductDetailsActivity.class);
                intent.setData(ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id));
                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(PRODUCT_CURSOR_LOADER_ID, null, this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_product_list.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_product_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertDummyProduct();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Prompt the user to confirm that they want to delete all products.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_products_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete products.
                deleteProducts();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of all products in the database.
     */
    private void deleteProducts() {

        int rowsDeleted = getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);

        // Show a toast message depending on whether or not the deletion was successful.
        if (rowsDeleted == 0) {
            // If no rows were deleted, then there was an error with the deletion.
            Toast.makeText(this, getString(R.string.delete_all_products_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the deletion was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.delete_all_products_successful, rowsDeleted), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {

        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_PRICE
        };

        String sortOrder = ProductEntry._ID + " DESC";

        return new CursorLoader(
                this,
                ProductEntry.CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mProductCursorAdapter.swapCursor(cursor);
        Log.v(LOG_TAG, "num of records in DB = " + cursor.getCount());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mProductCursorAdapter.swapCursor(null);
    }

    private void insertDummyProduct() {

        byte[] imageData = {1, 2, 3, 4, 5};

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, "Chocolate");
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, 17);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, 89);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME, "Nestle");
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL, "info@nestle.com");
        values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, imageData);

        getContentResolver().insert(ProductEntry.CONTENT_URI, values);
    }

}
