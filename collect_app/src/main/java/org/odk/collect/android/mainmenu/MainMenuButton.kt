package org.odk.collect.android.mainmenu

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.badge.ExperimentalBadgeUtils
import org.odk.collect.android.R
import org.odk.collect.android.databinding.MainMenuButtonBinding
import org.odk.collect.androidshared.system.ContextUtils.getThemeAttributeValue
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard

class MainMenuButton(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    constructor(context: Context) : this(context, null)

    private val binding = MainMenuButtonBinding.inflate(LayoutInflater.from(context), this, true)
    private val badge: BadgeDrawable
    private var highlightable: Boolean = false

    init {
        context.withStyledAttributes(attrs, R.styleable.MainMenuButton) {
            val buttonIcon = this.getResourceId(R.styleable.MainMenuButton_icon, 0)
            val buttonName = this.getString(R.styleable.MainMenuButton_name)
            highlightable = this.getBoolean(R.styleable.MainMenuButton_highlightable, false)

            binding.icon.setImageResource(buttonIcon)
            binding.name.text = buttonName
        }

        badge = BadgeDrawable.create(context).apply {
            backgroundColor = getThemeAttributeValue(context, androidx.appcompat.R.attr.colorPrimary)
            badgeGravity = BadgeDrawable.BOTTOM_END
        }
    }

    val text: String
        get() = binding.name.text.toString()

    override fun performClick(): Boolean {
        return MultiClickGuard.allowClick(context.getString(R.string.main_menu_screen)) && super.performClick()
    }

    fun setNumberOfForms(number: Int) {
        binding.number.text = if (number < 1) {
            ""
        } else {
            number.toString()
        }

        @ExperimentalBadgeUtils
        if (highlightable) {
            if (number > 0) {
                binding.icon.viewTreeObserver.addOnGlobalLayoutListener {
                    BadgeUtils.attachBadgeDrawable(badge, binding.icon)
                }
                binding.name.setTypeface(binding.name.typeface, Typeface.BOLD)
                binding.number.setTypeface(binding.name.typeface, Typeface.BOLD)
            } else {
                binding.icon.viewTreeObserver.addOnGlobalLayoutListener {
                    BadgeUtils.detachBadgeDrawable(badge, binding.icon)
                }
                binding.name.typeface = Typeface.DEFAULT
                binding.number.typeface = Typeface.DEFAULT
            }
        }
    }
}
