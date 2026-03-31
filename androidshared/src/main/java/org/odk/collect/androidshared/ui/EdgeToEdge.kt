package org.odk.collect.androidshared.ui

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.Window
import androidx.annotation.LayoutRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import android.view.ViewGroup
import androidx.core.view.updatePadding
import org.odk.collect.androidshared.system.ContextExt.isDarkTheme

object EdgeToEdge {

    @JvmStatic
    fun Activity.setView(@LayoutRes layout: Int, edgeToEdge: Boolean) {
        window.handleEdgeToEdge(this, edgeToEdge)
        setContentView(layout)
    }

    @JvmStatic
    fun Activity.setView(view: View, edgeToEdge: Boolean) {
        window.handleEdgeToEdge(this, edgeToEdge)
        setContentView(view)
    }

    fun Window.handleEdgeToEdge(context: Context, edgeToEdge: Boolean = false, bottomView: View? = null) {
        WindowCompat.enableEdgeToEdge(this)
        WindowCompat.getInsetsController(this, this.decorView).let {
            val darkTheme = context.isDarkTheme()
            it.isAppearanceLightStatusBars = !darkTheme
            it.isAppearanceLightNavigationBars = !darkTheme
        }

        if (!edgeToEdge) {
            avoidEdgeToEdge(bottomView)
        }
    }

    private fun Window.avoidEdgeToEdge(bottomView: View? = null) {
        val contentView = decorView.findViewById<View>(android.R.id.content)
        contentView.addSystemBarInsetMargins(bottomView)
    }

    private fun View.addSystemBarInsetMargins(bottomView: View? = null) {
        ViewCompat.setOnApplyWindowInsetsListener(this) { v, windowInsets ->
            val systemBarsInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val keyboardInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime())

            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBarsInsets.top
                leftMargin = systemBarsInsets.left
                rightMargin = systemBarsInsets.right
                bottomMargin = if (bottomView == null) systemBarsInsets.bottom else 0
            }

            bottomView?.updatePadding(
                bottom = maxOf(systemBarsInsets.bottom, keyboardInsets.bottom)
            )

            WindowInsetsCompat.CONSUMED
        }
    }
}
