package org.odk.collect.androidshared.utils

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import org.odk.collect.androidshared.R

object AppBarUtils {

    @JvmStatic
    fun setupAppBarLayout(activity: AppCompatActivity, title: CharSequence) {
        val toolbar = activity.findViewById<Toolbar>(R.id.toolbar)
        if (toolbar != null) {
            toolbar.title = title
            activity.setSupportActionBar(toolbar)
        }
    }
}
