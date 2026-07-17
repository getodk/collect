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

class ChoicesRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : RecyclerView(context, attrs) {

    private var maxHeight = 0

    fun initRecyclerView(adapter: AbstractSelectListAdapter, isFlex: Boolean, maxHeight: Int) {
        this.maxHeight = maxHeight
        if (isFlex) {
            enableFlexboxLayout()
        } else {
            enableGridLayout(adapter.numColumns)
        }
        setAdapter(adapter)
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val boundedHeightSpec = if (maxHeight > 0) {
            MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST)
        } else {
            heightSpec
        }
        super.onMeasure(widthSpec, boundedHeightSpec)
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

    internal class FlexItemDecoration(private val margin: Int) : ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: State) {
            outRect.bottom = margin
            outRect.right = margin
        }
    }
}
