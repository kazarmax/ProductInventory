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
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
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
    private ListView mProductListView;

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
        mProductListView = (ListView) findViewById(R.id.list);

        mProductCursorAdapter = new ProductCursorAdapter(this, null);

        // Attach cursor adapter to the ListView
        mProductListView.setAdapter(mProductCursorAdapter);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyProductListView = findViewById(R.id.empty_list_view);
        mProductListView.setEmptyView(emptyProductListView);

        mProductListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(ProductListActivity.this, ProductDetailsActivity.class);
                intent.setData(ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id));
                startActivity(intent);
            }
        });

        mProductListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mProductListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position,
                                                  long id, boolean checked) {
                mode.setTitle(String.valueOf(mProductListView.getCheckedItemCount()));

            }

            // Called when the action mode is created; startActionMode() was called
            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                // Inflate the menu for the CAB
                MenuInflater inflater = actionMode.getMenuInflater();
                inflater.inflate(R.menu.menu_list_context_actions, menu);
                return true;
            }

            // Called each time the action mode is shown. Always called after onCreateActionMode, but
            // may be called multiple times if the mode is invalidated.
            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            // Called when the user selects a contextual menu item
            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.context_action_delete:
                        showDeleteConfirmationDialog(mProductListView.getCheckedItemIds());
                        //actionMode.finish(); // Action picked, so close the CAB
                        return true;
                    default:
                        return false;
                }
            }

            // Called when the user exits the action mode
            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
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
                showDeleteConfirmationDialog(null);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Prompt the user to confirm that they want to delete products
     */
    private void showDeleteConfirmationDialog(final long[] checkedItemIds) {

        int messageResourceId = 0;

        if (checkedItemIds == null) {
            messageResourceId = R.string.delete_all_products_dialog_msg;
        } else if (checkedItemIds.length == 1) {
            messageResourceId = R.string.delete_selected_product_dialog_msg;
        } else if (checkedItemIds.length > 1) {
            messageResourceId = R.string.delete_selected_products_dialog_msg;
        }

        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(messageResourceId);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete products.
                deleteProducts(checkedItemIds);
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
    private void deleteProducts(long[] checkedItemIds) {

        int rowsDeleted;
        if (checkedItemIds == null) {
            rowsDeleted = getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);
        } else {
            String selection = ProductEntry._ID + " IN (" + getSelectedIdsInString(checkedItemIds) + ")";
            Log.v(LOG_TAG, "SELECTION CLAUSE = " + selection);
            rowsDeleted = getContentResolver().delete(ProductEntry.CONTENT_URI, selection, null);
        }

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

    private String getSelectedIdsInString(long[] selectedIds) {
        StringBuilder idSequence = new StringBuilder();
        for (int i = 0; i < selectedIds.length; i++) {
            idSequence.append(selectedIds[i]);
            if (i != selectedIds.length - 1) {
                idSequence.append(", ");
            }
        }
        return idSequence.toString();
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
