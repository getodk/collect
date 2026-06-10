/*
 * Copyright (C) 2011 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.odk.collect.geo.geopoint

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import org.odk.collect.androidshared.system.BundleExt.getParcelableExtraCompat
import org.odk.collect.androidshared.ui.DialogFragmentUtils.showIfNotShowing
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.async.Scheduler
import org.odk.collect.externalapp.ExternalAppUtils.returnSingleValue
import org.odk.collect.geo.Constants.EXTRA_DRAGGABLE_ONLY
import org.odk.collect.geo.Constants.EXTRA_READ_ONLY
import org.odk.collect.geo.Constants.EXTRA_RETAIN_MOCK_ACCURACY
import org.odk.collect.geo.GeoDependencyComponentProvider
import org.odk.collect.geo.GeoUtils.showCurrentLocation
import org.odk.collect.geo.GeoUtils.toMapPoint
import org.odk.collect.geo.R
import org.odk.collect.geo.geopoint.GeoPointMapActivity.Companion.EXTRA_LOCATION
import org.odk.collect.geo.geopoint.LocationAccuracy.Improving
import org.odk.collect.location.tracker.LocationTracker
import org.odk.collect.location.tracker.getCurrentLocation
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapFragmentFactory
import org.odk.collect.maps.MapPoint
import org.odk.collect.maps.addMarker
import org.odk.collect.maps.circles.CurrentLocationDelegate
import org.odk.collect.maps.layers.OfflineMapLayersPickerBottomSheetDialogFragment
import org.odk.collect.maps.layers.ReferenceLayerRepository
import org.odk.collect.maps.markers.MarkerDescription
import org.odk.collect.maps.markers.MarkerIconDescription
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.webpage.WebPageService
import javax.inject.Inject

class GeoPointMapFragment : Fragment() {

    @Inject
    lateinit var mapFragmentFactory: MapFragmentFactory

    @Inject
    lateinit var referenceLayerRepository: ReferenceLayerRepository

    @Inject
    lateinit var scheduler: Scheduler

    @Inject
    lateinit var settingsProvider: SettingsProvider

    @Inject
    lateinit var webPageService: WebPageService

    @Inject
    lateinit var locationTracker: LocationTracker

    private var previousState: Bundle? = null

    private var map: MapFragment? = null
    private var featureId = -1 // will be a positive featureId once map is ready

    private var locationStatus: AccuracyStatusView? = null

    private var location: MapPoint? = null
    private var placeMarkerButton: ImageButton? = null

    private var isDragged = false

    private var zoomButton: ImageButton? = null
    private var clearButton: ImageButton? = null

    private var captureLocation = false

    /**
     * True if a tap on the clear button removed an existing marker and
     * no new marker has been placed.
     */
    private var setClear = false

    /**
     * True if the current point came from the intent.
     */
    private var pointFromIntent = false

    /**
     * True if the intent requested for the point to be read-only.
     */
    private var intentReadOnly = false

    /**
     * True if the intent requested for the marker to be draggable.
     */
    private var intentDraggable = false

    /**
     * While true, the point cannot be moved by dragging or long-pressing.
     */
    private var isPointLocked = false

    private val currentLocationDelegate = CurrentLocationDelegate()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context.applicationContext as GeoDependencyComponentProvider)
            .geoDependencyComponent.inject(this)

        childFragmentManager.fragmentFactory = FragmentFactoryBuilder()
            .forClass(MapFragment::class.java) { mapFragmentFactory.createMapFragment() as Fragment }
            .forClass(OfflineMapLayersPickerBottomSheetDialogFragment::class.java) {
                OfflineMapLayersPickerBottomSheetDialogFragment(
                    requireActivity().activityResultRegistry,
                    referenceLayerRepository,
                    scheduler,
                    settingsProvider,
                    webPageService
                )
            }
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        previousState = savedInstanceState
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.geopoint_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationStatus = view.findViewById(R.id.status_section)
            ?: throw IllegalStateException("Status section not found")
        placeMarkerButton = view.findViewById(R.id.place_marker)
        zoomButton = view.findViewById(R.id.zoom)

        val mapFragment: MapFragment =
            (view.findViewById<View?>(R.id.map_container) as FragmentContainerView).getFragment()
        mapFragment.init(
            { newMapFragment: MapFragment -> this.initMap(newMapFragment) },
            { requireActivity().finish() }
        )
    }

    override fun onSaveInstanceState(state: Bundle) {
        super.onSaveInstanceState(state)
        if (map == null) {
            // initMap() is called asynchronously, so map can be null if the fragment
            // is stopped (e.g. by screen rotation) before initMap() gets to run.
            // In this case, preserve any provided instance state.
            if (previousState != null) {
                state.putAll(previousState)
            }
            return
        }

        state.putParcelable(POINT_KEY, map!!.getMarkerPoint(featureId))

        // Flags
        state.putBoolean(IS_DRAGGED_KEY, isDragged)
        state.putBoolean(CAPTURE_LOCATION_KEY, captureLocation)
        state.putBoolean(SET_CLEAR_KEY, setClear)
        state.putBoolean(POINT_FROM_INTENT_KEY, pointFromIntent)
        state.putBoolean(INTENT_READ_ONLY_KEY, intentReadOnly)
        state.putBoolean(INTENT_DRAGGABLE_KEY, intentDraggable)
        state.putBoolean(IS_POINT_LOCKED_KEY, isPointLocked)

        // UI state
        state.putBoolean(PLACE_MARKER_BUTTON_ENABLED_KEY, placeMarkerButton!!.isEnabled)
        state.putBoolean(ZOOM_BUTTON_ENABLED_KEY, zoomButton!!.isEnabled)
        state.putBoolean(CLEAR_BUTTON_ENABLED_KEY, clearButton!!.isEnabled)
        state.putInt(LOCATION_STATUS_VISIBILITY_KEY, locationStatus!!.visibility)
    }

    fun returnLocation() {
        var result: String? = null

        if (setClear || (intentReadOnly && featureId == -1)) {
            result = ""
        } else if (isDragged || intentReadOnly || pointFromIntent) {
            result = formatResult(map!!.getMarkerPoint(featureId)!!)
        } else if (location != null) {
            result = formatResult(location!!)
        }

        if (result != null) {
            returnSingleValue(requireActivity(), result)
        } else {
            requireActivity().finish()
        }
    }

    @SuppressLint("MissingPermission") // Permission handled in Constructor
    fun initMap(newMapFragment: MapFragment?) {
        map = newMapFragment
        map!!.setDragEndListener { draggedFeatureId: Int ->
            this.onDragEnd(draggedFeatureId)
        }
        map!!.setLongPressListener { point: MapPoint -> this.onLongPress(point) }

        val acceptLocation = view?.findViewById<ImageButton>(R.id.accept_location)
        acceptLocation?.setOnClickListener { returnLocation() }

        placeMarkerButton!!.isEnabled = false
        placeMarkerButton!!.setOnClickListener {
            val currentLocation = locationTracker.getCurrentLocation()
            if (currentLocation != null) {
                val mapPoint = currentLocation.toMapPoint()
                placeMarker(mapPoint)
                zoomToMarker(true)
            }
        }

        // Focuses on marked location
        zoomButton!!.isEnabled = false
        zoomButton!!.setOnClickListener {
            currentLocationDelegate.zoomToCurrentLocation(map!!)
        }

        // Menu Layer Toggle
        view?.findViewById<View>(R.id.layer_menu)?.setOnClickListener {
            showIfNotShowing(
                OfflineMapLayersPickerBottomSheetDialogFragment::class.java,
                childFragmentManager
            )
        }

        clearButton =
            view?.findViewById(R.id.clear) ?: throw IllegalStateException("Clear button not found")
        clearButton!!.isEnabled = false
        clearButton!!.setOnClickListener {
            clear()
            locationStatus!!.visibility = View.VISIBLE
            pointFromIntent = false
        }

        val intent = requireActivity().intent
        if (intent != null && intent.extras != null) {
            intentDraggable = intent.getBooleanExtra(EXTRA_DRAGGABLE_ONLY, false)
            if (!intentDraggable) {
                // Not Draggable, set text for Map else leave as placement-map text
                locationStatus!!.title =
                    getString(org.odk.collect.strings.R.string.geopoint_no_draggable_instruction)
            }

            intentReadOnly = intent.getBooleanExtra(EXTRA_READ_ONLY, false)
            if (intentReadOnly) {
                captureLocation = true
                clearButton!!.isEnabled = false
            }

            if (intent.hasExtra(EXTRA_LOCATION)) {
                val point = intent.extras?.getParcelableExtraCompat<MapPoint>(EXTRA_LOCATION)

                // If the point is initially set from the intent, the "place marker"
                // button, dragging, and long-pressing are all initially disabled.
                // To enable them, the user must clear the marker and add a new one.
                isPointLocked = true
                placeMarker(point!!)
                placeMarkerButton!!.isEnabled = false

                captureLocation = true
                pointFromIntent = true
                locationStatus!!.visibility = View.GONE
                zoomButton!!.isEnabled = true
                zoomToMarker(false)
            }
        }

        if (previousState != null) {
            restoreFromInstanceState(previousState!!)
        }

        val retainMockAccuracy =
            requireActivity().intent?.getBooleanExtra(EXTRA_RETAIN_MOCK_ACCURACY, false) ?: false
        map!!.showCurrentLocation(
            locationTracker,
            currentLocationDelegate,
            retainMockAccuracy
        ) { mapPoint: MapPoint? ->
            onLocationChanged(mapPoint)
        }
    }

    private fun restoreFromInstanceState(state: Bundle) {
        isDragged = state.getBoolean(IS_DRAGGED_KEY, false)
        captureLocation = state.getBoolean(CAPTURE_LOCATION_KEY, false)
        setClear = state.getBoolean(SET_CLEAR_KEY, false)
        pointFromIntent = state.getBoolean(POINT_FROM_INTENT_KEY, false)
        intentReadOnly = state.getBoolean(INTENT_READ_ONLY_KEY, false)
        intentDraggable = state.getBoolean(INTENT_DRAGGABLE_KEY, false)
        isPointLocked = state.getBoolean(IS_POINT_LOCKED_KEY, false)

        // Restore the marker and dialog after the flags, because they use some of them.
        val point = state.getParcelableExtraCompat<MapPoint>(POINT_KEY)
        if (point != null) {
            placeMarker(point)
        }

        // Restore the flags again, because placeMarker() and clear() modify some of them.
        isDragged = state.getBoolean(IS_DRAGGED_KEY, false)
        captureLocation = state.getBoolean(CAPTURE_LOCATION_KEY, false)
        setClear = state.getBoolean(SET_CLEAR_KEY, false)
        pointFromIntent = state.getBoolean(POINT_FROM_INTENT_KEY, false)
        intentReadOnly = state.getBoolean(INTENT_READ_ONLY_KEY, false)
        intentDraggable = state.getBoolean(INTENT_DRAGGABLE_KEY, false)
        isPointLocked = state.getBoolean(IS_POINT_LOCKED_KEY, false)

        placeMarkerButton!!.isEnabled = state.getBoolean(PLACE_MARKER_BUTTON_ENABLED_KEY, false)
        zoomButton!!.isEnabled = state.getBoolean(ZOOM_BUTTON_ENABLED_KEY, false)
        clearButton!!.isEnabled = state.getBoolean(CLEAR_BUTTON_ENABLED_KEY, false)

        locationStatus!!.visibility = state.getInt(LOCATION_STATUS_VISIBILITY_KEY, View.GONE)
    }

    fun onLocationChanged(point: MapPoint?) {
        if (setClear) {
            placeMarkerButton!!.isEnabled = true
        }

        if (point != null) {
            enableZoomButton()

            if (!captureLocation && !setClear) {
                placeMarker(point)
                placeMarkerButton!!.isEnabled = true
            }

            locationStatus!!.accuracy = Improving(point.accuracy.toFloat())
        }
    }

    fun formatResult(point: MapPoint): String {
        return String.format(
            "%s %s %s %s",
            point.latitude,
            point.longitude,
            point.altitude,
            point.accuracy
        )
    }

    fun onDragEnd(draggedFeatureId: Int) {
        if (draggedFeatureId == featureId) {
            isDragged = true
            captureLocation = true
            setClear = false
            map!!.setCenter(map!!.getMarkerPoint(featureId), true)
        }
    }

    fun onLongPress(point: MapPoint) {
        if (intentDraggable && !intentReadOnly && !isPointLocked) {
            placeMarker(point)
            enableZoomButton()
            isDragged = true
        }
    }

    private fun enableZoomButton() {
        if (zoomButton != null) {
            zoomButton!!.isEnabled = true
        }
    }

    fun zoomToMarker(animate: Boolean) {
        map!!.zoomToPoint(map!!.getMarkerPoint(featureId), animate)
    }

    private fun clear() {
        map!!.clearFeatures(listOf(featureId))
        featureId = -1
        clearButton!!.isEnabled = false
        placeMarkerButton!!.isEnabled = true

        isPointLocked = false
        isDragged = false
        captureLocation = false
        setClear = true
    }

    /**
     * Places the marker and enables the button to remove it.
     */
    private fun placeMarker(point: MapPoint) {
        this.location = point

        if (featureId != -1) {
            map!!.clearFeatures(listOf(featureId))
        }

        val iconDescription = MarkerIconDescription.DrawableResource(
            org.odk.collect.icons.R.drawable.ic_map_marker_with_hole_big,
            MARKER_COLOR.toColorInt()
        )

        featureId = map!!.addMarker(
            MarkerDescription(
                point,
                intentDraggable && !intentReadOnly && !isPointLocked,
                MapFragment.IconAnchor.BOTTOM,
                iconDescription
            )
        )
        if (!intentReadOnly) {
            clearButton!!.isEnabled = true
        }
        captureLocation = true
        setClear = false
    }

    companion object {
        const val POINT_KEY: String = "point"

        const val IS_DRAGGED_KEY: String = "is_dragged"
        const val CAPTURE_LOCATION_KEY: String = "capture_location"
        const val SET_CLEAR_KEY: String = "set_clear"
        const val POINT_FROM_INTENT_KEY: String = "point_from_intent"
        const val INTENT_READ_ONLY_KEY: String = "intent_read_only"
        const val INTENT_DRAGGABLE_KEY: String = "intent_draggable"
        const val IS_POINT_LOCKED_KEY: String = "is_point_locked"

        const val PLACE_MARKER_BUTTON_ENABLED_KEY: String = "place_marker_button_enabled"
        const val ZOOM_BUTTON_ENABLED_KEY: String = "zoom_button_enabled"
        const val CLEAR_BUTTON_ENABLED_KEY: String = "clear_button_enabled"
        const val LOCATION_STATUS_VISIBILITY_KEY: String = "location_status_visibility"

        const val MARKER_COLOR: String = "#52C268"
    }
}