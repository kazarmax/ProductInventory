package com.example.android.inventory;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventory.data.ProductContract.ProductEntry;

/**
 * {@link ProductCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of products data as its data source. This adapter knows
 * how to create list items for each row of products data in the {@link Cursor}.
 */
public class ProductCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link ProductCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.product_list_item, parent, false);
    }

    /**
     * This method binds the products data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current product can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView tvProductName = (TextView) view.findViewById(R.id.product_name);
        TextView tvProductQuantity = (TextView) view.findViewById(R.id.product_quantity);
        TextView tvProductPrice = (TextView) view.findViewById(R.id.product_price);
        Button btnSell = (Button) view.findViewById(R.id.saleButton);

        // Extract properties from cursor
        String productName = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_NAME));
        final int productQuantity = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_QUANTITY));
        int productPrice = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_PRICE));
        final int productId = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry._ID));

        // Update the TextViews with the attributes for the current product
        tvProductName.setText(productName);
        tvProductQuantity.setText(String.valueOf(productQuantity));
        tvProductPrice.setText(context.getString(R.string.product_price_in_list, productPrice));

        btnSell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decreaseProductQuantity(context, productId, productQuantity);
            }
        });
    }

    private void decreaseProductQuantity(Context context, int productId, int productQuantity) {
        productQuantity--;
        if (productQuantity < 0) {
            return;
        }

        Uri productUri = Uri.withAppendedPath(ProductEntry.CONTENT_URI, String.valueOf(productId));
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, productQuantity);

        context.getContentResolver().update(productUri, values, null, null);
    }

}
