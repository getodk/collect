package org.odk.collect.android.support

import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import org.odk.collect.android.utilities.ScreenContext

class WidgetTestActivity : FragmentActivity(), ScreenContext {
    @JvmField
    val viewsRegisterForContextMenu = mutableListOf<View>()

    override fun getActivity(): FragmentActivity {
        return this
    }

    override fun getViewLifecycle(): LifecycleOwner {
        return this
    }

    override fun registerForContextMenu(view: View) {
        super.registerForContextMenu(view)
        viewsRegisterForContextMenu.add(view)
    }
}
