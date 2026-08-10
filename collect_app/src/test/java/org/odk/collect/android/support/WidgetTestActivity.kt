package org.odk.collect.android.support

import android.view.ContextMenu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import org.odk.collect.android.application.CollectComposeThemeProvider

class WidgetTestActivity : AppCompatActivity(), CollectComposeThemeProvider {
    @JvmField
    val viewsRegisterForContextMenu = mutableListOf<View>()

    @JvmField
    val viewsWithShownContextMenu = mutableListOf<View>()

    override fun registerForContextMenu(view: View) {
        super.registerForContextMenu(view)
        viewsRegisterForContextMenu.add(view)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        viewsWithShownContextMenu.add(v)
    }
}
