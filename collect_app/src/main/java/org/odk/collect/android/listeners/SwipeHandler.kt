package org.odk.collect.android.listeners

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.core.widget.NestedScrollView
import org.odk.collect.android.utilities.FlingRegister
import org.odk.collect.androidshared.utils.ScreenUtils
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.settings.Settings
import kotlin.math.abs
import kotlin.math.atan2

class SwipeHandler(context: Context, generalSettings: Settings) {
    val gestureDetector: GestureDetector
    private val onSwipe: OnSwipeListener
    private var view: View? = null
    private var allowSwiping = true
    private var beenSwiped = false
    private val generalSettings: Settings

    interface OnSwipeListener {
        fun onSwipeBackward()
        fun onSwipeForward()
    }

    init {
        gestureDetector = GestureDetector(context, GestureListener())
        onSwipe = context as OnSwipeListener
        this.generalSettings = generalSettings
    }

    fun setView(view: View?) {
        this.view = view
    }

    fun setAllowSwiping(allowSwiping: Boolean) {
        this.allowSwiping = allowSwiping
    }

    fun setBeenSwiped(beenSwiped: Boolean) {
        this.beenSwiped = beenSwiped
    }

    fun beenSwiped() = beenSwiped

    inner class GestureListener : GestureDetector.OnGestureListener {
        override fun onDown(event: MotionEvent) = false
        override fun onSingleTapUp(e: MotionEvent) = false

        override fun onShowPress(e: MotionEvent) = Unit
        override fun onLongPress(e: MotionEvent) = Unit

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            // The onFling() captures the 'up' event so our view thinks it gets long pressed. We don't want that, so cancel it.
            view?.cancelLongPress()
            return false
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            if (view == null) {
                return false
            }

            FlingRegister.flingDetected()

            if (generalSettings.getString(ProjectKeys.KEY_NAVIGATION)!!.contains(ProjectKeys.NAVIGATION_SWIPE) && allowSwiping) {
                // Looks for user swipes. If the user has swiped, move to the appropriate screen.

                // For all screens a swipe is left/right of at least .25" and up/down of less than .25" OR left/right of > .5"
                val xpixellimit = (ScreenUtils.xdpi(view!!.context) * .25).toInt()
                val ypixellimit = (ScreenUtils.ydpi(view!!.context) * .25).toInt()

                if (view != null && view!!.shouldSuppressFlingGesture()) {
                    return false
                }

                if (beenSwiped) {
                    return false
                }

                val diffX = abs(e1.x - e2.x)
                val diffY = abs(e1.y - e2.y)

                if (view != null && canScrollVertically() && getGestureAngle(diffX, diffY) > 30) {
                    return false
                }

                if (diffX > xpixellimit && diffY < ypixellimit || diffX > xpixellimit * 2) {
                    beenSwiped = true
                    if (e1.x > e2.x) {
                        onSwipe.onSwipeForward()
                    } else {
                        onSwipe.onSwipeBackward()
                    }
                    return true
                }
            }
            return false
        }

        private fun getGestureAngle(diffX: Float, diffY: Float): Double {
            return Math.toDegrees(atan2(diffY.toDouble(), diffX.toDouble()))
        }

        private fun canScrollVertically(): Boolean {
            val scrollView = view!!.verticalScrollView

            return if (scrollView != null) {
                val screenHeight = scrollView.height
                val viewHeight = scrollView.getChildAt(0).height
                viewHeight > screenHeight
            } else {
                false
            }
        }
    }

    abstract class View(context: Context) : FrameLayout(context) {
        abstract fun shouldSuppressFlingGesture(): Boolean
        abstract val verticalScrollView: NestedScrollView?
    }
}
