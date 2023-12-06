package org.odk.collect.android.formlists.sorting

import OfflineMapLayer
import OfflineMapLayersAdapter
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.odk.collect.android.R
import java.util.function.Consumer

class OfflineMapLayersListBottomSheetDialog(
        context: Context,
        private val options: List<OfflineMapLayer>,
        private val selectedOption: Int,
        private val onSelectedOptionChanged: Consumer<Int>
) : BottomSheetDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(LayoutInflater.from(context).inflate(R.layout.bottom_sheet, null))

        findViewById<RecyclerView>(R.id.recyclerView)?.apply {
            adapter = OfflineMapLayersAdapter(
                    options,
                    selectedOption
            ) { position ->
                onSelectedOptionChanged.accept(position)
                dismiss()
            }
            layoutManager = LinearLayoutManager(context)
            itemAnimator = DefaultItemAnimator()
        }
    }

    companion object {
        fun showBottomSheet(activity: Activity, options: List<OfflineMapLayer>, selectedOption: Int, onSelectedOptionChanged: Consumer<Int>) {
            val bottomSheetDialog = OfflineMapLayersListBottomSheetDialog(activity, options, selectedOption, onSelectedOptionChanged)
            bottomSheetDialog.show()
        }
    }
}

