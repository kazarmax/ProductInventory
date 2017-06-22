package com.example.android.inventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventory.data.ProductContract.ProductEntry;
import com.example.android.inventory.utils.DbBitmapUtility;

import java.io.IOException;

public class ProductDetailsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = ProductDetailsActivity.class.getSimpleName();

    /** Identifier for the product data loader */
    static final int EXISTING_PRODUCT_LOADER_ID = 0;

    /** Content URI for the existing product (null if it's a new product) */
    private Uri mCurrentProductUri;

    /** EditText field to enter the product's name */
    private EditText mProductNameEditText;

    /** EditText field to enter the product's quantity */
    private EditText mProductQuantityEditText;

    /** EditText field to enter the product's price */
    private EditText mProductPriceEditText;

    /** EditText field to enter the product's supplier name */
    private EditText mProductSupplierNameEditText;

    /** EditText field to enter the product's supplier email */
    private EditText mProductSupplierEmailEditText;

    /** ImageView field to display image of the product */
    private ImageView mProductImageIV;

    /** Quantity of the current product */
    private int mProductQuantity;

    /** Image bitmap of the current product */
    private Bitmap mProductImageBitmap;

    /** Constant for picking image */
    private static final int PICK_IMAGE_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new product or editing an existing one.
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        Log.v(LOG_TAG, "mCurrentProductUri = " + mCurrentProductUri);

        // If the intent DOES NOT contain a product content URI, then we know that we are
        // creating a new product.
        if (mCurrentProductUri == null) {
            // This is a new product, so change the app bar to say "Add a Product"
            setTitle(getString(R.string.editor_activity_title_new_product));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a product that hasn't been created yet.)
            invalidateOptionsMenu();

        } else {
            // Otherwise this is an existing product, so change app bar to say "Edit Product"
            setTitle(getString(R.string.editor_activity_title_edit_product));

            // Initialize a loader to read the product data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER_ID, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mProductNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mProductQuantityEditText = (EditText) findViewById(R.id.edit_product_quantity);
        mProductPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mProductSupplierNameEditText = (EditText) findViewById(R.id.edit_supplier_name);
        mProductSupplierEmailEditText = (EditText) findViewById(R.id.edit_supplier_email);
        mProductImageIV = (ImageView) findViewById(R.id.product_image);

        Button btnDecreaseProductQuantity = (Button) findViewById(R.id.decrease_product_quantity);
        Button btnIncreaseProductQuantity = (Button) findViewById(R.id.increase_product_quantity);

        View.OnClickListener btnChangeQuantityListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeProductQuantity(view);
            }
        };

        btnDecreaseProductQuantity.setOnClickListener(btnChangeQuantityListener);
        btnIncreaseProductQuantity.setOnClickListener(btnChangeQuantityListener);

        // Setting up button for picking image
        Button btnPickImage = (Button) findViewById(R.id.pick_image_button);
        btnPickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImage();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                mProductImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                mProductImageIV.setImageBitmap(mProductImageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void pickImage() {
        Intent intent = new Intent();
        // Show only images, no videos or anything else
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select picture"), PICK_IMAGE_REQUEST);
    }

    private void changeProductQuantity(View view) {

        switch (view.getId()) {

            case R.id.decrease_product_quantity:
                if (mProductQuantity <= 0) {
                    return;
                }
                mProductQuantity--;
                mProductQuantityEditText.setText(String.valueOf(mProductQuantity));
                break;

            case R.id.increase_product_quantity:
                mProductQuantity++;
                mProductQuantityEditText.setText(String.valueOf(mProductQuantity));
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_product_details, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" and "Order more" menu items.
        if (mCurrentProductUri == null) {
            MenuItem menuItemDelete = menu.findItem(R.id.action_delete);
            menuItemDelete.setVisible(false);
            MenuItem menuItemOrderMore = menu.findItem(R.id.action_order_more);
            menuItemOrderMore.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save product to database
                if (saveProduct()) {
                    // Exit activity
                    finish();
                }
                return true;
            // Respond to a click on the "Order more" menu option
            case R.id.action_order_more:
                createOrder();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(ProductDetailsActivity.this);
                return true;
            }
        return super.onOptionsItemSelected(item);
    }

    private void createOrder() {
        String productName = mProductNameEditText.getText().toString().trim();
        String productSupplierName = mProductSupplierNameEditText.getText().toString().trim();
        String productSupplierEmail = mProductSupplierEmailEditText.getText().toString().trim();

        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto",productSupplierEmail, null));
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.order_request_email_subject, productName));
        intent.putExtra(Intent.EXTRA_TEXT, createOrderSummary(productSupplierName, productName));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(intent, "Send email..."));
        }
    }

    private String createOrderSummary(String productSupplierName, String productName) {
        StringBuilder orderSummary = new StringBuilder();
        orderSummary.append("Dear " + productSupplierName + ", \n\n");
        orderSummary.append("Available items of product " + productName + " are close to completion.\n");
        orderSummary.append("So, please send us additional 100 items.\n\n");
        orderSummary.append("Thanks!");
        return orderSummary.toString();
    }

    /**
     * Get user input from editor and save product into database.
     */
    private boolean saveProduct() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String productName = mProductNameEditText.getText().toString().trim();
        String productQuantityString = mProductQuantityEditText.getText().toString().trim();
        String productPriceString = mProductPriceEditText.getText().toString().trim();
        String productSupplierName = mProductSupplierNameEditText.getText().toString().trim();
        String productSupplierEmail = mProductSupplierEmailEditText.getText().toString().trim();
        byte[] productImageData = DbBitmapUtility.getBytes(mProductImageBitmap);

        if (TextUtils.isEmpty(productName)
                || TextUtils.isEmpty(productQuantityString)
                || TextUtils.isEmpty(productPriceString)
                || TextUtils.isEmpty(productSupplierName)
                || TextUtils.isEmpty(productSupplierEmail)
                || productImageData == null) {
            Toast.makeText(this, getString(R.string.editor_save_product_with_empty_fields),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        int productQuantity = 0;
        if (!TextUtils.isEmpty(productQuantityString)) {
            productQuantity = Integer.parseInt(productQuantityString);
        }

        int productPrice = 0;
        if (!TextUtils.isEmpty(productPriceString)) {
            productPrice = Integer.parseInt(productPriceString);
        }

        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, productName);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, productQuantity);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, productPrice);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME, productSupplierName);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL, productSupplierEmail);
        values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, productImageData);

        // Determine if this is a new or existing product by checking if mCurrentProductUri is null or not
        if (mCurrentProductUri == null) {
            // This is a NEW product, so insert a new product into the provider,
            // returning the content URI for the new product.
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
                return false;
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
                return true;
            }
        } else {
            // Otherwise this is an EXISTING product, so update the product with content URI: mCurrentProductUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentProductUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
                return false;
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
                return true;
            }
        }
    }

    /**
     * Prompt the user to confirm that they want to delete this product.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
                finish();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
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
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {

        // Only perform the delete if this is an existing product.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentProductUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the deletion was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the deletion.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the deletion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all product attributes, define a projection that contains
        // all columns from the product table
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL,
                ProductEntry.COLUMN_PRODUCT_IMAGE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,         // Query the content URI for the current product
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of product attributes that we're interested in

            int productNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int productQuantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int productPriceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int productSupplierNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
            int productSupplierEmailColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL);
            int productImageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String productName = cursor.getString(productNameColumnIndex);
            int productQuantity = cursor.getInt(productQuantityColumnIndex);
            int productPrice = cursor.getInt(productPriceColumnIndex);
            String productSupplierName = cursor.getString(productSupplierNameColumnIndex);
            String productSupplierEmail = cursor.getString(productSupplierEmailColumnIndex);
            byte[] productImageData = cursor.getBlob(productImageColumnIndex);
            mProductImageBitmap = DbBitmapUtility.getImage(productImageData);

            mProductQuantity = productQuantity;

            // Update the views on the screen with the values from the database
            mProductNameEditText.setText(productName);
            mProductQuantityEditText.setText(String.valueOf(mProductQuantity));
            mProductPriceEditText.setText(String.valueOf(productPrice));
            mProductSupplierNameEditText.setText(productSupplierName);
            mProductSupplierEmailEditText.setText(productSupplierEmail);
            mProductImageIV.setImageBitmap(mProductImageBitmap);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mProductNameEditText.setText("");
        mProductQuantityEditText.setText("");
        mProductPriceEditText.setText("");
        mProductSupplierNameEditText.setText("");
        mProductSupplierEmailEditText.setText("");
        mProductImageIV.setImageBitmap(null);
    }
}

