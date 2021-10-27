package org.odk.collect.android.support

import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import org.odk.collect.android.utilities.ScreenContext
import java.util.ArrayList

class WidgetTestActivity : FragmentActivity(), ScreenContext {
    @JvmField
    val viewsRegisterForContextMenu: MutableList<View> = ArrayList()

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
