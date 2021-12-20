package org.odk.collect.geo

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import org.odk.collect.androidshared.system.ContextUtils.getThemeAttributeValue
import org.odk.collect.geo.databinding.AccuracyStatusBinding
import java.text.DecimalFormat

class AccuracyStatusView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    constructor(context: Context) : this(context, null)

    private var binding = AccuracyStatusBinding.inflate(LayoutInflater.from(context), this, true)

    fun setAccuracy(accuracy: Float, accuracyThreshold: Float) {
        binding.root.background =
            ColorDrawable(getThemeAttributeValue(context, R.attr.colorPrimary))

        binding.currentAccuracy.text = formatAccuracy(accuracy)
        binding.currentAccuracy.setTextColor(getThemeAttributeValue(context, R.attr.colorOnPrimary))

        binding.qualitative.text = context.getString(
            R.string.distance_from_accuracy_goal,
            formatAccuracy(accuracy - accuracyThreshold),
            formatAccuracy(accuracyThreshold)
        )
        binding.qualitative.setTextColor(getThemeAttributeValue(context, R.attr.colorOnPrimary))

        binding.action.setTextColor(getThemeAttributeValue(context, R.attr.colorOnPrimary))
    }

    private fun formatAccuracy(accuracy: Float) =
        "${DecimalFormat("#.##").format(accuracy)}m"
}
