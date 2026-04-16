package org.odk.collect.androidshared.ui

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.LayoutRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.google.android.material.snackbar.Snackbar
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

    fun Window.handleEdgeToEdge(context: Context, edgeToEdge: Boolean = false) {
        WindowCompat.enableEdgeToEdge(this)
        WindowCompat.getInsetsController(this, this.decorView).let {
            val darkTheme = context.isDarkTheme()
            it.isAppearanceLightStatusBars = !darkTheme
            it.isAppearanceLightNavigationBars = !darkTheme
        }

        if (!edgeToEdge) {
            avoidEdgeToEdge()
        }
    }

    @JvmStatic
    fun View.applyBottomBarInsetMargins() {
        ViewCompat.setOnApplyWindowInsetsListener(this) { v, windowInsets ->
            val systemBarsInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val keyboardInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime())

            v.updatePadding(
                bottom = maxOf(0, keyboardInsets.bottom - systemBarsInsets.bottom)
            )

            windowInsets
        }
    }

    @JvmStatic
    fun Snackbar.applySnackbarInsetOffset() {
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, windowInsets ->
            val systemBarsInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val keyboardInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime())

            val offset = maxOf(0, keyboardInsets.bottom - systemBarsInsets.bottom)
            view.translationY = -offset.toFloat()

            windowInsets
        }
        ViewCompat.requestApplyInsets(view)
    }

    private fun Window.avoidEdgeToEdge() {
        val contentView = decorView.findViewById<View>(android.R.id.content)
        contentView.addSystemBarInsetMargins()
    }

    private fun View.addSystemBarInsetMargins() {
        ViewCompat.setOnApplyWindowInsetsListener(this) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
                bottomMargin = insets.bottom

                leftMargin = insets.left
                rightMargin = insets.right
            }

            windowInsets
        }
    }
}
