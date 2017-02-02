package org.odk.collect.android.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI;

/**
 * This adapter will help to show Form title, sent time, deletion time (if the submission deleted) and
 * the visibility off icon in the right(if form is encrypted, submision form is deleted and blank form is deleted) *
 * Created by Amit Sahoo (amit@sdrc.co.in) on 24-01-2017.
 */

public class ViewSentListAdapter extends SimpleCursorAdapter {

    private Cursor c = null;
    private Context context;

    public ViewSentListAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
        super(context, layout, c, from, to);
        this.c = c;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = super.getView(position, convertView, parent);
        TextView sent = (TextView) view.findViewById(R.id.text2);
        TextView visibilityOffCause = (TextView) view.findViewById(R.id.text4);
        ImageView visibleOff = (ImageView) view.findViewById(R.id.visible_off);
        String status = c.getString(c.getColumnIndex(InstanceProviderAPI.InstanceColumns.STATUS));
        /**
         * If the form is sent and deleted show the delete time text
         * show visibility off icon
         */
        if (status.equals(InstanceProviderAPI.STATUS_SUBMITTED_AND_DELETED)) {
            visibilityOffCause.setVisibility(View.VISIBLE);
            visibleOff.setVisibility(View.VISIBLE);

        } else {

            /**
             * if the form is only sent then check for encrypted or not
             */

            String[] selectionArgs = {c.getString(c.getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_FORM_ID))};
            Cursor cursor = context.getContentResolver().query(FormsProviderAPI.FormsColumns.CONTENT_URI, null,
                    InstanceProviderAPI.InstanceColumns.JR_FORM_ID + " =? ", selectionArgs, null);
            //If blank form is not present then the size will be 0
            if(cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                String encryption = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.BASE64_RSA_PUBLIC_KEY));
                if (encryption != null) {
                    visibleOff.setVisibility(View.VISIBLE);
                    visibilityOffCause.setVisibility(View.VISIBLE);
                    visibilityOffCause.setText(context.getString(R.string.encrypted_form));
                }

            }else{
                visibleOff.setVisibility(View.VISIBLE);
                visibilityOffCause.setVisibility(View.VISIBLE);
                visibilityOffCause.setText(context.getString(R.string.blank_form_deleted));
            }
            if(cursor != null) {
                cursor.close();
            }

        }
        return view;
    }
}
