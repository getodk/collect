package org.odk.collect.android.support

import androidx.fragment.app.FragmentActivity
import org.odk.collect.android.audio.AudioControllerView.SwipableParent

class SwipableParentActivity : FragmentActivity(), SwipableParent {
    var isSwipingAllowed = false
        private set

    override fun allowSwiping(allowSwiping: Boolean) {
        isSwipingAllowed = allowSwiping
    }
}
