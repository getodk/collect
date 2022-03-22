package org.odk.collect.android.widgets.items

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.odk.collect.android.databinding.SelectOneFromMapDialogLayoutBinding
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.androidshared.livedata.NonNullLiveData
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.geo.MappableSelectItem
import org.odk.collect.geo.SelectionMapData
import org.odk.collect.geo.SelectionMapFragment
import org.odk.collect.material.MaterialFullScreenDialogFragment

class SelectOneFromMapDialogFragment : MaterialFullScreenDialogFragment() {

    override fun onAttach(context: Context) {
        super.onAttach(context)
        childFragmentManager.fragmentFactory = FragmentFactoryBuilder()
            .forClass(SelectionMapFragment::class.java) {
                SelectionMapFragment(SelectChoicesMapData())
            }
            .build()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = SelectOneFromMapDialogLayoutBinding.inflate(inflater)
        return binding.root
    }

    override fun getToolbar(): Toolbar? {
        return null
    }

    override fun onBackPressed() {
        dismiss()
    }

    override fun onCloseClicked() {
        // No toolbar so not relevant
    }

    companion object {
        const val ARG_FORM_INDEX = "form_index"
    }
}

class SelectChoicesMapData : SelectionMapData {

    override fun getMapTitle(): LiveData<String> {
        return MutableLiveData()
    }

    override fun getItemCount(): LiveData<Int> {
        return MutableLiveData()
    }

    override fun getMappableItems(): NonNullLiveData<List<MappableSelectItem>> {
        return MutableNonNullLiveData(emptyList())
    }
}
