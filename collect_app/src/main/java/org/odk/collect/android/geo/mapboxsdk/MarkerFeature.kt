package org.odk.collect.android.geo.mapboxsdk

import android.content.Context
import com.mapbox.maps.plugin.annotation.Annotation
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationDragListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import org.odk.collect.android.geo.MapsMarkerCache
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapPoint

/** A point annotation that can optionally be dragged by the user. */
class MarkerFeature(
    private val context: Context,
    private val pointAnnotationManager: PointAnnotationManager,
    private val featureId: Int,
    private val featureClickListener: MapFragment.FeatureListener?,
    private val featureDragEndListener: MapFragment.FeatureListener?,
    var point: MapPoint,
    draggable: Boolean,
    @MapFragment.IconAnchor iconAnchor: String,
    iconDrawableId: Int
) : MapFeature {
    private val clickListener = ClickListener()
    private val dragListener = DragListener()
    private var pointAnnotation = MapUtils.createPointAnnotation(pointAnnotationManager, point, draggable, iconAnchor, iconDrawableId, context)

    init {
        pointAnnotationManager.apply {
            addClickListener(clickListener)
            addDragListener(dragListener)
        }
    }

    fun setIcon(drawableId: Int) {
        pointAnnotation.iconImageBitmap = MapsMarkerCache.getMarkerBitmap(drawableId, context)
        pointAnnotationManager.update(pointAnnotation)
    }

    override fun dispose() {
        pointAnnotationManager.apply {
            removeClickListener(clickListener)
            removeDragListener(dragListener)
            delete(pointAnnotation)
        }
    }

    private inner class ClickListener : OnPointAnnotationClickListener {
        override fun onAnnotationClick(annotation: PointAnnotation): Boolean {
            if (annotation.id == pointAnnotation.id && featureClickListener != null) {
                featureClickListener.onFeature(featureId)
            }
            return true
        }
    }

    private inner class DragListener : OnPointAnnotationDragListener {
        override fun onAnnotationDragStarted(annotation: Annotation<*>) = Unit

        override fun onAnnotationDrag(annotation: Annotation<*>) {
            if (annotation.id == pointAnnotation.id) {
                point = MapUtils.mapPointFromPointAnnotation(annotation as PointAnnotation)
            }
        }

        override fun onAnnotationDragFinished(annotation: Annotation<*>) {
            onAnnotationDrag(annotation)
            if (annotation.id == pointAnnotation.id && featureDragEndListener != null) {
                featureDragEndListener.onFeature(featureId)
            }
        }
    }
}
