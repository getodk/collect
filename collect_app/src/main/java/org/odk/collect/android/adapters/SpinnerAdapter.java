package org.odk.collect.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.odk.collect.android.R;
import org.odk.collect.android.utilities.ThemeUtils;

public class SpinnerAdapter extends ArrayAdapter<String> {
    Context context;
    String[] items = new String[]{};
    int textUnit;
    float textSize;
    private int selectedPosition;
    private ThemeUtils themeUtils;

    public SpinnerAdapter(final Context context, final int textViewResourceId,
                          final String[] objects, int textUnit, float textSize, int selectedPosition) {
        super(context, textViewResourceId, objects);
        this.items = objects;
        this.context = context;
        this.textUnit = textUnit;
        this.textSize = textSize;
        this.selectedPosition = selectedPosition;
        themeUtils = new ThemeUtils(context);
    }

    @Override
    // Defines the text view parameters for the drop down list entries
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }

        TextView tv = convertView.findViewById(android.R.id.text1);
        tv.setTextSize(textUnit, textSize);
        tv.setPadding(20, 10, 10, 10);

        if (themeUtils.isDarkTheme()) {
            convertView.setBackgroundColor(context.getResources().getColor(R.color.darkPopupDialogColor));
        }

        if (position == items.length - 1) {
            tv.setText(parent.getContext().getString(R.string.clear_answer));
        } else {
            tv.setText(items[position]);
        }

        if (position == (items.length - 1) && selectedPosition == position) {
            tv.setEnabled(false);
        } else {
            tv.setTextColor(selectedPosition == position ? themeUtils.getAccentColor() : themeUtils.getPrimaryTextColor());
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
        }

        TextView tv = convertView.findViewById(android.R.id.text1);
        tv.setTextSize(textUnit, textSize);
        tv.setPadding(10, 10, 10, 10);
        tv.setText(items[position]);

        return convertView;
    }
}
