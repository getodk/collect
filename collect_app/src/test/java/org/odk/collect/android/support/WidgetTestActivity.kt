package org.odk.collect.android.support

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import org.odk.collect.androidshared.ui.CollectComposeThemeProvider

class WidgetTestActivity : AppCompatActivity(), CollectComposeThemeProvider {
    @JvmField
    val viewsRegisterForContextMenu = mutableListOf<View>()

    override fun registerForContextMenu(view: View) {
        super.registerForContextMenu(view)
        viewsRegisterForContextMenu.add(view)
    }
}
