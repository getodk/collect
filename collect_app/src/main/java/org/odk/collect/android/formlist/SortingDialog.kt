package org.odk.collect.android.formlist

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.odk.collect.android.R
import org.odk.collect.android.adapters.SortDialogAdapter

class SortingDialog(
    context: Context,
    private val options: IntArray,
    private val selectedOption: Int,
    private val onSelectedOptionChanged: (option: Int) -> Unit
) : BottomSheetDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(LayoutInflater.from(context).inflate(R.layout.bottom_sheet, null))

        findViewById<RecyclerView>(R.id.recyclerView)?.apply {
            adapter = SortDialogAdapter(
                context, this, options, selectedOption
            ) { holder, position ->
                holder.updateItemColor(position)
                onSelectedOptionChanged(position)
                dismiss()
            }
            layoutManager = LinearLayoutManager(context)
            itemAnimator = DefaultItemAnimator()
        }
    }
}
