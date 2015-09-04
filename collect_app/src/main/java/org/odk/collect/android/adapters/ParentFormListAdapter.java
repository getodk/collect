package org.odk.collect.android.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.odk.collect.android.database.FormRelationsDb;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;

/**
 * Created by jpringle on 9/4/15.
 */
public class ParentFormListAdapter extends SimpleCursorAdapter {

    public ParentFormListAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
        super(context, layout, c, from, to);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);

        TextView t = (TextView) view.findViewById(R.id.text2);
        long id = cursor.getLong(cursor.getColumnIndex(InstanceColumns._ID));

        long[] children = FormRelationsDb.getChildren(id);
        if (children.length > 0) {

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < children.length; i++) {
                sb.append(InstanceColumns._ID + " = " + children[i]);
                if (i != children.length - 1) {
                    sb.append(" OR ");
                }
            }

            String selection = "( " + InstanceColumns.STATUS + " = ? OR " +
                    InstanceColumns.STATUS
                    + " = ?)" + " AND (" + sb.toString() + ")";
            String[] selectionArgs = {
                    InstanceProviderAPI.STATUS_SUBMITTED,
                    InstanceProviderAPI.STATUS_COMPLETE
            };
            String sortOrder = InstanceColumns.STATUS + " DESC, " + InstanceColumns.DISPLAY_NAME
                    + " ASC";
            Cursor c = context.getContentResolver().query(InstanceColumns.CONTENT_URI, null,
                    selection, selectionArgs, sortOrder);
            int count = c.getCount();
            c.close();
            t.setText("Completed: " + count + "/" + children.length);
        } else {
            String saveText = cursor.getString(cursor.getColumnIndex(InstanceColumns.DISPLAY_SUBTEXT));
            t.setText(saveText);
        }
    }

    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.two_item, null);
        return view;
    }
}
