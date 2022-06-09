package org.odk.collect.android.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayoutManager;

import org.odk.collect.android.R;
import org.odk.collect.android.adapters.AbstractSelectListAdapter;
import org.odk.collect.androidshared.utils.ScreenUtils;
import org.odk.collect.android.utilities.ThemeUtils;

public class ChoicesRecyclerView extends RecyclerView {
    /**
     * A list of choices can have thousands of items. To increase loading and scrolling performance,
     * a RecyclerView is used. Because it is nested inside a ScrollView, by default, all of
     * the RecyclerView's items are loaded and there is no performance benefit over a ListView.
     * This constant is used to bound the number of items loaded. The value 40 was chosen because
     * it is around the maximum number of elements that can be shown on a large tablet.
     */
    private static final int MAX_ITEMS_WITHOUT_SCREEN_BOUND = 40;

    public ChoicesRecyclerView(@NonNull Context context) {
        super(context);
    }

    public ChoicesRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void initRecyclerView(AbstractSelectListAdapter adapter, boolean isFlex) {
        if (isFlex) {
            enableFlexboxLayout();
        } else {
            enableGridLayout(adapter.getNumColumns());
        }
        setAdapter(adapter);
        adjustRecyclerViewSize();
    }

    private void enableFlexboxLayout() {
        setLayoutManager(new FlexboxLayoutManager(getContext()));
    }

    private void enableGridLayout(int numColumns) {
        if (numColumns == 1) {
            enableDivider();
        }

        setLayoutManager(new GridLayoutManager(getContext(), numColumns));
    }

    private void enableDivider() {
        DividerItemDecoration divider = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.inset_divider_64dp);

        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            DrawableCompat.setTint(DrawableCompat.wrap(drawable), new ThemeUtils(getContext()).getColorOnSurface());
        }

        divider.setDrawable(drawable);
        addItemDecoration(divider);
    }

    private void adjustRecyclerViewSize() {
        if (getAdapter().getItemCount() > MAX_ITEMS_WITHOUT_SCREEN_BOUND) {
            // Only let the RecyclerView take up 90% of the screen height in order to speed up loading if there are many items
            getLayoutParams().height = (int) (ScreenUtils.getScreenHeight(getContext()) * 0.9);
        } else {
            setNestedScrollingEnabled(false);
        }
    }
}