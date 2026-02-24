package org.odk.collect.androidshared.ui

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams

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

    private fun Activity.handleEdgeToEdge(edgeToEdge: Boolean) {
        WindowCompat.enableEdgeToEdge(window)
        if (!edgeToEdge) {
            avoidEdgeToEdge()
        }
    }

    private fun Activity.avoidEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView.findViewById(android.R.id.content)) { v, windowInsets ->
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
}
