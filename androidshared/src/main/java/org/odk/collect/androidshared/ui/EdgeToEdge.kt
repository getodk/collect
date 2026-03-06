package org.odk.collect.androidshared.ui

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import org.odk.collect.androidshared.system.ContextExt.isDarkTheme

object EdgeToEdge {

    @JvmStatic
    fun Activity.setView(@LayoutRes layout: Int, edgeToEdge: Boolean) {
        handleEdgeToEdge(edgeToEdge)
        setContentView(layout)
    }

    @JvmStatic
    fun Activity.setView(view: View, edgeToEdge: Boolean) {
        handleEdgeToEdge(edgeToEdge)
        setContentView(view)
    }

    @JvmStatic
    private fun Activity.avoidEdgeToEdge() {
        val contentView = window.decorView.findViewById<View>(android.R.id.content)
        contentView.addSystemBarInsetMargins()
    }

    @JvmStatic
    fun View.addSystemBarInsetMargins() {
        ViewCompat.setOnApplyWindowInsetsListener(this) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
                bottomMargin = insets.bottom

                leftMargin = insets.left
                rightMargin = insets.right
            }

            WindowInsetsCompat.CONSUMED
        }
    }

    private fun Activity.handleEdgeToEdge(edgeToEdge: Boolean) {
        WindowCompat.enableEdgeToEdge(window)
        WindowCompat.getInsetsController(window, window.decorView).let {
            val darkTheme = isDarkTheme()
            it.isAppearanceLightStatusBars = !darkTheme
            it.isAppearanceLightNavigationBars = !darkTheme
        }


        if (!edgeToEdge) {
            avoidEdgeToEdge()
        }
    }
}
