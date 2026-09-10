package org.odk.collect.maplibre

import android.content.Context
import android.os.Handler
import android.os.Looper
import org.maplibre.android.plugins.annotation.OnSymbolClickListener
import org.maplibre.android.plugins.annotation.OnSymbolDragListener
import org.maplibre.android.plugins.annotation.Symbol
import org.maplibre.android.plugins.annotation.SymbolManager
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapPoint
import org.odk.collect.maps.markers.MarkerIconDescription

/** A symbol that can optionally be dragged by the user. */
class MarkerFeature(
    val context: Context,
    private val styleIcons: StyleIcons,
    private val symbolManager: SymbolManager,
    private val symbol: Symbol,
    val featureId: Int,
    private val featureClickListener: MapFragment.FeatureListener?,
    private val featureDragEndListener: MapFragment.FeatureListener?,
    var point: MapPoint
) : MapFeature {
    private val clickListener = ClickListener()
    private val dragListener = DragListener()
    private val mainHandler = Handler(Looper.getMainLooper())

    init {
        symbolManager.apply {
            addClickListener(clickListener)
            addDragListener(dragListener)
        }
    }

    fun setIcon(markerIconDescription: MarkerIconDescription) {
        symbol.iconImage = styleIcons.add(context, markerIconDescription)
        symbolManager.update(symbol)
    }

    override fun dispose() {
        symbolManager.apply {
            removeClickListener(clickListener)
            removeDragListener(dragListener)
            delete(symbol)
        }
    }

    private inner class ClickListener : OnSymbolClickListener {
        override fun onAnnotationClick(annotation: Symbol): Boolean {
            if (annotation.id == symbol.id && featureClickListener != null) {
                featureClickListener.onFeature(featureId)
                return true
            }
            return false
        }
    }

    private inner class DragListener : OnSymbolDragListener {
        override fun onAnnotationDragStarted(annotation: Symbol) = Unit

        override fun onAnnotationDrag(annotation: Symbol) {
            if (annotation.id == symbol.id) {
                point = MapUtils.mapPointFromSymbol(annotation)
            }
        }

        override fun onAnnotationDragFinished(annotation: Symbol) {
            onAnnotationDrag(annotation)
            if (annotation.id == symbol.id && featureDragEndListener != null) {
                // Prevents listener from accidentally interfering with features while MapLibre
                // performs updates which can cause a `ConcurrentModificationException`
                mainHandler.post {
                    featureDragEndListener.onFeature(featureId)
                }
            }
        }
    }
}
