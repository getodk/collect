package org.odk.collect.android.adapters

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import org.odk.collect.android.databinding.HierarchyElementBinding
import org.odk.collect.android.logic.HierarchyElement
import org.odk.collect.android.utilities.HtmlUtils

class HierarchyListItemView(context: Context) : FrameLayout(context) {

    val binding = HierarchyElementBinding.inflate(LayoutInflater.from(context), this, true)

    fun setElement(element: HierarchyElement) {
        val icon = element.icon
        if (icon != null) {
            binding.icon.visibility = VISIBLE
            binding.icon.setImageDrawable(icon)
        } else {
            binding.icon.visibility = GONE
        }

        binding.primaryText.text = HtmlUtils.textToHtml(element.primaryText)

        val secondaryText = element.secondaryText
        if (secondaryText != null && secondaryText.isNotEmpty()) {
            binding.secondaryText.visibility = VISIBLE
            binding.secondaryText.text = HtmlUtils.textToHtml(secondaryText)
        } else {
            binding.secondaryText.visibility = GONE
        }
    }
}
