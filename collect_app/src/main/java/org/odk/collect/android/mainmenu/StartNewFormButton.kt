package org.odk.collect.android.mainmenu

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import org.odk.collect.android.R
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard

class StartNewFormButton(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    constructor(context: Context) : this(context, null)

    init {
        inflate(context, R.layout.start_new_from_button, this)
    }

    val text: String
        get() = findViewById<TextView>(R.id.name).text.toString()

    override fun performClick(): Boolean {
        return MultiClickGuard.allowClick(context.getString(org.odk.collect.androidshared.R.string.main_menu_screen)) && super.performClick()
    }
}
