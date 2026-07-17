package org.odk.collect.android.views

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import org.odk.collect.android.R
import org.odk.collect.android.adapters.AbstractSelectListAdapter
import org.odk.collect.androidshared.utils.ScreenUtils

class ChoicesRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : RecyclerView(context, attrs) {

    fun initRecyclerView(adapter: AbstractSelectListAdapter, isFlex: Boolean) {
        if (isFlex) {
            enableFlexboxLayout()
        } else {
            enableGridLayout(adapter.numColumns)
        }
        setAdapter(adapter)
        adjustRecyclerViewSize(adapter)
    }

    private fun enableFlexboxLayout() {
        layoutManager = FlexboxLayoutManager(context)
        val marginBetweenItems = resources.getDimensionPixelSize(org.odk.collect.androidshared.R.dimen.margin_standard)
        addItemDecoration(FlexItemDecoration(marginBetweenItems))
    }

    private fun enableGridLayout(numColumns: Int) {
        if (numColumns == 1) {
            enableDivider()
        }

        layoutManager = GridLayoutManager(context, numColumns)
    }

    private fun enableDivider() {
        val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        val drawable = ContextCompat.getDrawable(context, R.drawable.inset_divider_64dp)
        if (drawable != null) {
            divider.setDrawable(drawable)
        }
        addItemDecoration(divider)
    }

    private fun adjustRecyclerViewSize(adapter: AbstractSelectListAdapter) {
        if (adapter.itemCount > MAX_ITEMS_WITHOUT_SCREEN_BOUND) {
            // Only let the RecyclerView take up 90% of the screen height in order to speed up loading if there are many items
            layoutParams.height = (ScreenUtils.getScreenHeight(context) * 0.9).toInt()
        }
    }

    internal class FlexItemDecoration(private val margin: Int) : ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: State) {
            outRect.bottom = margin
            outRect.right = margin
        }
    }

    companion object {
        /**
         * A list of choices can have thousands of items. To increase loading and scrolling performance,
         * a RecyclerView is used. Because it is nested inside a ScrollView, by default, all of
         * the RecyclerView's items are loaded and there is no performance benefit over a ListView.
         * This constant is used to bound the number of items loaded. The value 40 was chosen because
         * it is around the maximum number of elements that can be shown on a large tablet.
         */
        private const val MAX_ITEMS_WITHOUT_SCREEN_BOUND = 40
    }
}
