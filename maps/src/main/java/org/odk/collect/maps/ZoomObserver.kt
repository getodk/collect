package org.odk.collect.maps

import androidx.lifecycle.Observer

abstract class ZoomObserver : Observer<Zoom?> {

    abstract fun onZoomToPoint(zoom: Zoom.Point)
    abstract fun onZoomToBox(zoom: Zoom.Box)

    private var isFirstChange = true

    override fun onChanged(value: Zoom?) {
        if (value == null) {
            return
        }

        /**
         * Ignore zooms from the user unless it's the first change that this observer is dealing
         * with. In that case, we always want to use the value as we're rendering a fresh map.
         */
        if (!value.user || isFirstChange) {
            when (value) {
                is Zoom.Box -> onZoomToBox(value)
                is Zoom.Point -> onZoomToPoint(value)
            }
        }

        isFirstChange = false
    }
}
