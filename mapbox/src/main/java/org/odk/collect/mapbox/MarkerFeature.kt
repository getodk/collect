package org.odk.collect.mapbox

import android.content.Context
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationDragListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapPoint
import org.odk.collect.maps.markers.MarkerIconCreator
import org.odk.collect.maps.markers.MarkerIconDescription

/** A point annotation that can optionally be dragged by the user. */
class MarkerFeature(
    val context: Context,
    private val pointAnnotationManager: PointAnnotationManager,
    private val pointAnnotation: PointAnnotation,
    val featureId: Int,
    private val featureClickListener: MapFragment.FeatureListener?,
    private val featureDragEndListener: MapFragment.FeatureListener?,
    var point: MapPoint
) : MapFeature {
    private val clickListener = ClickListener()
    private val dragListener = DragListener()

    init {
        pointAnnotationManager.apply {
            addClickListener(clickListener)
            addDragListener(dragListener)
        }
    }

    fun setIcon(markerIconDescription: MarkerIconDescription) {
        pointAnnotation.iconImageBitmap = MarkerIconCreator.getMarkerIconBitmap(context, markerIconDescription)
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
                return true
            }
            return false
        }
    }

    private inner class DragListener : OnPointAnnotationDragListener {
        override fun onAnnotationDragStarted(annotation: com.mapbox.maps.plugin.annotation.Annotation<*>) = Unit

        override fun onAnnotationDrag(annotation: com.mapbox.maps.plugin.annotation.Annotation<*>) {
            if (annotation.id == pointAnnotation.id) {
                point = MapUtils.mapPointFromPointAnnotation(annotation as PointAnnotation)
            }
        }

        override fun onAnnotationDragFinished(annotation: com.mapbox.maps.plugin.annotation.Annotation<*>) {
            onAnnotationDrag(annotation)
            if (annotation.id == pointAnnotation.id && featureDragEndListener != null) {
                featureDragEndListener.onFeature(featureId)
            }
        }
    }
}
