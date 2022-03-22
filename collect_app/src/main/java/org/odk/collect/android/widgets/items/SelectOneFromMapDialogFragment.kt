package org.odk.collect.android.widgets.items

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import org.odk.collect.android.databinding.SelectOneFromMapDialogLayoutBinding
import org.odk.collect.material.MaterialFullScreenDialogFragment

class SelectOneFromMapDialogFragment : MaterialFullScreenDialogFragment() {

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
}
