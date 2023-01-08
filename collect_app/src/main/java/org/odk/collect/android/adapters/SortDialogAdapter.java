/*
 * Copyright (C) 2017 Shobhit
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.odk.collect.android.R;
import org.odk.collect.android.listeners.RecyclerViewClickListener;
import org.odk.collect.android.location.LocationProvider;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.ThemeUtils;

import timber.log.Timber;

public class SortDialogAdapter extends RecyclerView.Adapter<SortDialogAdapter.ViewHolder> {
    private final RecyclerViewClickListener listener;
    private LocationProvider locationProvider;
    private int selectedSortingOrder;
    private final ThemeUtils themeUtils;
    private final int[] sortList;

    public SortDialogAdapter(Context context, int[] sortList, int selectedSortingOrder, RecyclerViewClickListener recyclerViewClickListener, LocationProvider locationProvider) {
        this.themeUtils = new ThemeUtils(context);
        this.sortList = sortList;
        this.selectedSortingOrder = selectedSortingOrder;
        this.listener = recyclerViewClickListener;
        this.locationProvider = locationProvider;
    }

    @NonNull
    @Override
    public SortDialogAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sort_item_layout, parent, false);
        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        Context context = viewHolder.itemView.getContext();

        int sortTextId = sortList[position];
        viewHolder.txtViewTitle.setText(sortTextId);

        Location location = locationProvider.getLastLocation();

        if (location == null
                && (sortTextId == R.string.sort_by_distance_asc
                || sortTextId == R.string.sort_by_distance_desc)) {
            viewHolder.itemView.setEnabled(false);

            int disabledColor = ResourcesCompat.getColor(
                    context.getResources(),
                    R.color.disabled_view,
                    context.getTheme()
            );
            setImageView(viewHolder.imgViewIcon, position, ColorStateList.valueOf(disabledColor));
            tintTextView(viewHolder.txtViewTitle, disabledColor);

        } else {
            viewHolder.itemView.setEnabled(true);
            int color = position == selectedSortingOrder ? themeUtils.getAccentColor()
                    : themeUtils.getColorOnSurface();
            setImageView(viewHolder.imgViewIcon, position, position == selectedSortingOrder ? ColorStateList.valueOf(color) : null);
            tintTextView(viewHolder.txtViewTitle, color);
        }
    }

    // Return the size of your itemsData (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return sortList.length;
    }

    public void updateSelectedPosition(int newSelectedPos) {
        this.selectedSortingOrder = newSelectedPos;
        notifyDataSetChanged();
    }

    private void setImageView(ImageView imageView, int position, ColorStateList color) {
        try {
            int iconId = ApplicationConstants.getSortLabelToIconMap().get(sortList[position]);
            imageView.setImageResource(iconId);
            imageView.setImageDrawable(DrawableCompat.wrap(imageView.getDrawable()).mutate());
            DrawableCompat.setTintList(imageView.getDrawable(), color);
        } catch (NullPointerException e) {
            Timber.i(e);
        }
    }

    private void tintTextView(TextView textView, int color) {
        textView.setTextColor(color);
    }

    // inner class to hold a reference to each item of RecyclerView
    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtViewTitle;
        ImageView imgViewIcon;

        ViewHolder(final View itemLayoutView) {
            super(itemLayoutView);
            txtViewTitle = itemLayoutView.findViewById(R.id.title);
            imgViewIcon = itemLayoutView.findViewById(R.id.icon);

            itemLayoutView.setOnClickListener(
                    v -> listener.onItemClicked(SortDialogAdapter.this, getLayoutPosition())
            );
        }
    }
}
