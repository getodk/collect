package org.odk.collect.android.mainmenu

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import org.odk.collect.android.R
import org.odk.collect.android.databinding.MainMenuButtonBinding
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard

class MainMenuButton(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    constructor(context: Context) : this(context, null)

    private val binding = MainMenuButtonBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.MainMenuButton,
            0, 0
        ).apply {
            try {
                val buttonIcon = this.getResourceId(R.styleable.MainMenuButton_icon, 0)
                val buttonName = this.getString(R.styleable.MainMenuButton_name)

                binding.icon.setImageResource(buttonIcon)
                binding.name.text = buttonName
            } finally {
                recycle()
            }
        }
    }

    override fun performClick(): Boolean {
        return MultiClickGuard.allowClick() && super.performClick()
    }

    fun setNumberOfForms(number: Int) {
        binding.number.text = if (number < 1) {
            ""
        } else {
            number.toString()
        }
    }
}
