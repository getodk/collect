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
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewmodel.viewModelFactory
import org.odk.collect.androidshared.ui.DialogFragmentUtils.showIfNotShowing
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.async.Scheduler
import org.odk.collect.geo.GeoDependencyComponentProvider
import org.odk.collect.geo.GeoUtils.showCurrentLocation
import org.odk.collect.geo.GeoUtils.showData
import org.odk.collect.geo.GeoUtils.showItemLoading
import org.odk.collect.geo.GeoUtils.toMapPoint
import org.odk.collect.geo.R
import org.odk.collect.geo.geopoint.LocationAccuracy.Improving
import org.odk.collect.geo.items.MappableData
import org.odk.collect.geo.items.MappableItemsDelegate
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

class GeoPointMapFragment(
    val inputPoint: MapPoint? = null,
    val draggable: Boolean = true,
    val readOnly: Boolean = false,
    val retainMockAccuracy: Boolean = false,
    val mappableData: MappableData? = null
) : Fragment() {

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

    private var placeMarkerButton: ImageButton? = null

    private var zoomButton: ImageButton? = null
    private var clearButton: ImageButton? = null

    /**
     * True if a tap on the clear button removed an existing marker and
     * no new marker has been placed.
     */
    private var setClear = false
    /**
     * While true, the point cannot be moved by dragging or long-pressing.
     */
    private var isPointLocked = inputPoint != null

    private val currentLocationDelegate = CurrentLocationDelegate()
    private val mappableItemsDelegate = MappableItemsDelegate(background = true, clickable = false)

    private val geoPointViewModel: GeoPointViewModel by viewModels {
        viewModelFactory {
            addInitializer(GeoPointViewModel::class) {
                GeoPointViewModel(inputPoint)
            }
        }
    }

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
            { cancel() }
        )

        if (mappableData != null) {
            showItemLoading(mappableData)
        }

        geoPointViewModel.isInputPoint.asLiveData().observe(viewLifecycleOwner) {
            locationStatus!!.isVisible = !it
        }
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

        // Flags
        state.putBoolean(SET_CLEAR_KEY, setClear)
        state.putBoolean(IS_POINT_LOCKED_KEY, isPointLocked)

        // UI state
        state.putBoolean(PLACE_MARKER_BUTTON_ENABLED_KEY, placeMarkerButton!!.isEnabled)
        state.putBoolean(ZOOM_BUTTON_ENABLED_KEY, zoomButton!!.isEnabled)
        state.putBoolean(CLEAR_BUTTON_ENABLED_KEY, clearButton!!.isEnabled)
    }

    private fun returnLocation() {
        var result: String? = null

        if (setClear || (readOnly && featureId == -1)) {
            result = ""
        } else {
            val geoPoint = geoPointViewModel.geoPoint.value
            if (geoPoint != null) {
                result = formatResult(geoPoint)
            }
        }

        if (result != null) {
            parentFragmentManager.setFragmentResult(
                REQUEST_GEOPOINT,
                bundleOf(RESULT_GEOPOINT to result)
            )
        } else {
            cancel()
        }
    }

    @SuppressLint("MissingPermission") // Permission handled in Constructor
    private fun initMap(newMapFragment: MapFragment?) {
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
                geoPointViewModel.place(mapPoint)
                map!!.zoomToPoint(map!!.getMarkerPoint(featureId), true)
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
        }

        if (!draggable) {
            // Not Draggable, set text for Map else leave as placement-map text
            locationStatus!!.title =
                getString(org.odk.collect.strings.R.string.geopoint_no_draggable_instruction)
        }

        if (readOnly) {
            clearButton!!.isEnabled = false
        }

        if (previousState != null) {
            restoreFromInstanceState(previousState!!)
        }

        if (inputPoint != null && !map!!.hasCenter()) {
            map!!.setCenter(inputPoint, animate = true)
        }

        map!!.showCurrentLocation(
            locationTracker,
            currentLocationDelegate,
            retainMockAccuracy
        ) { mapPoint: MapPoint? ->
            onLocationChanged(mapPoint)
        }

        if (mappableData != null) {
            (map as MapFragment).showData(mappableData, mappableItemsDelegate)
        }

        geoPointViewModel.geoPoint.asLiveData().observe(viewLifecycleOwner) {
            updateMarker(it)
        }
    }

    private fun restoreFromInstanceState(state: Bundle) {
        setClear = state.getBoolean(SET_CLEAR_KEY, false)
        isPointLocked = state.getBoolean(IS_POINT_LOCKED_KEY, false)

        placeMarkerButton!!.isEnabled = state.getBoolean(PLACE_MARKER_BUTTON_ENABLED_KEY, false)
        zoomButton!!.isEnabled = state.getBoolean(ZOOM_BUTTON_ENABLED_KEY, false)
        clearButton!!.isEnabled = state.getBoolean(CLEAR_BUTTON_ENABLED_KEY, false)
    }

    private fun onLocationChanged(point: MapPoint?) {
        if (setClear) {
            placeMarkerButton!!.isEnabled = true
        }

        if (point != null) {
            enableZoomButton()

            if (!geoPointViewModel.hasGeoPoint() && !setClear) {
                geoPointViewModel.place(point)
                placeMarkerButton!!.isEnabled = true
            }

            locationStatus!!.accuracy = Improving(point.accuracy.toFloat())
        }
    }

    private fun formatResult(point: MapPoint): String {
        return String.format(
            "%s %s %s %s",
            point.latitude,
            point.longitude,
            point.altitude,
            point.accuracy
        )
    }

    private fun cancel() {
        parentFragmentManager.setFragmentResult(REQUEST_GEOPOINT, Bundle.EMPTY)
    }

    private fun onDragEnd(draggedFeatureId: Int) {
        if (draggedFeatureId == featureId) {
            setClear = false
            geoPointViewModel.place(map!!.getMarkerPoint(featureId)!!)
        }
    }

    private fun onLongPress(point: MapPoint) {
        if (draggable && !readOnly && !isPointLocked) {
            geoPointViewModel.place(point)
            enableZoomButton()
        }
    }

    private fun enableZoomButton() {
        if (zoomButton != null) {
            zoomButton!!.isEnabled = true
        }
    }

    private fun clear() {
        geoPointViewModel.clear()
        clearButton!!.isEnabled = false
        placeMarkerButton!!.isEnabled = true

        isPointLocked = false
        setClear = true
    }

    private fun updateMarker(point: MapPoint?) {
        if (featureId != -1) {
            map!!.clearFeatures(listOf(featureId))
        }

        if (point == null) {
            return
        }

        val iconDescription = MarkerIconDescription.DrawableResource(
            org.odk.collect.icons.R.drawable.ic_map_marker_with_hole_big,
            MARKER_COLOR.toColorInt()
        )

        featureId = map!!.addMarker(
            MarkerDescription(
                point,
                draggable && !readOnly && !isPointLocked,
                MapFragment.IconAnchor.BOTTOM,
                iconDescription
            )
        )

        if (!readOnly) {
            clearButton!!.isEnabled = true
        }

        setClear = false
    }

    companion object {
        const val SET_CLEAR_KEY: String = "set_clear"
        const val IS_POINT_LOCKED_KEY: String = "is_point_locked"

        const val PLACE_MARKER_BUTTON_ENABLED_KEY: String = "place_marker_button_enabled"
        const val ZOOM_BUTTON_ENABLED_KEY: String = "zoom_button_enabled"
        const val CLEAR_BUTTON_ENABLED_KEY: String = "clear_button_enabled"

        const val MARKER_COLOR: String = "#52C268"
        const val REQUEST_GEOPOINT: String = "geopoint"
        const val RESULT_GEOPOINT: String = "geopoint"
    }
}