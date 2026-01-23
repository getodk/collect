package org.odk.collect.geo.geopoly

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import org.odk.collect.androidshared.livedata.LiveDataExt.zip
import org.odk.collect.androidshared.ui.DialogFragmentUtils.showIfNotShowing
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.androidshared.ui.SnackbarUtils
import org.odk.collect.androidshared.ui.ToastUtils.showShortToastInMiddle
import org.odk.collect.async.Scheduler
import org.odk.collect.geo.GeoActivityUtils.requireLocationPermissions
import org.odk.collect.geo.GeoDependencyComponentProvider
import org.odk.collect.geo.GeoUtils
import org.odk.collect.geo.GeoUtils.toLocation
import org.odk.collect.geo.R
import org.odk.collect.geo.databinding.GeopolyLayoutBinding
import org.odk.collect.geo.geopoint.LocationAccuracy.Improving
import org.odk.collect.geo.geopoint.LocationAccuracy.Unacceptable
import org.odk.collect.geo.geopoly.GeoPolySettingsDialogFragment.SettingsDialogCallback
import org.odk.collect.location.tracker.LocationTracker
import org.odk.collect.maps.LineDescription
import org.odk.collect.maps.MapConsts
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapFragmentFactory
import org.odk.collect.maps.MapPoint
import org.odk.collect.maps.PolygonDescription
import org.odk.collect.maps.layers.OfflineMapLayersPickerBottomSheetDialogFragment
import org.odk.collect.maps.layers.ReferenceLayerRepository
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.webpage.WebPageService
import javax.inject.Inject

class GeoPolyFragment @JvmOverloads constructor(
    val onBackPressedDispatcher: () -> OnBackPressedDispatcher,
    val outputMode: OutputMode = OutputMode.GEOTRACE,
    val readOnly: Boolean = false,
    val retainMockAccuracy: Boolean = false,
    val inputPolygon: List<MapPoint> = emptyList(),
    val invalidMessage: LiveData<String?> = MutableLiveData(null)
) : Fragment(R.layout.geopoly_layout), SettingsDialogCallback {

    @Inject
    lateinit var mapFragmentFactory: MapFragmentFactory

    @Inject
    lateinit var locationTracker: LocationTracker

    @Inject
    lateinit var referenceLayerRepository: ReferenceLayerRepository

    @Inject
    lateinit var scheduler: Scheduler

    @Inject
    lateinit var settingsProvider: SettingsProvider

    @Inject
    lateinit var webPageService: WebPageService

    private var previousState: Bundle? = null

    private var map: MapFragment? = null
    private var featureId = -1 // will be a positive featureId once map is ready
    private var originalPoly: List<MapPoint>? = null

    private var inputActive = false // whether we are ready for the user to add points
    private var recordingEnabled =
        false // whether points are taken from GPS readings (if not, placed by tapping)
    private var recordingAutomatic =
        false // whether GPS readings are taken at regular intervals (if not, only when user-directed)

    private var intervalIndex: Int = DEFAULT_INTERVAL_INDEX

    private var accuracyThresholdIndex: Int = DEFAULT_ACCURACY_THRESHOLD_INDEX

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!readOnly && map != null && originalPoly != viewModel.points.value) {
                    showBackDialog()
                } else {
                    cancel()
                }
            }
        }

    private val viewModel: GeoPolyViewModel by viewModels {
        viewModelFactory {
            addInitializer(GeoPolyViewModel::class) {
                GeoPolyViewModel(
                    outputMode,
                    inputPolygon,
                    retainMockAccuracy,
                    locationTracker,
                    scheduler
                )
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context.applicationContext as GeoDependencyComponentProvider)
            .geoDependencyComponent.inject(this)

        getChildFragmentManager().fragmentFactory = FragmentFactoryBuilder()
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
            .forClass(GeoPolySettingsDialogFragment::class.java) {
                GeoPolySettingsDialogFragment(
                    this
                )
            }
            .build()

        requireLocationPermissions(requireActivity())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        previousState = savedInstanceState

        if (savedInstanceState != null) {
            inputActive = savedInstanceState.getBoolean(INPUT_ACTIVE_KEY, false)
            recordingEnabled = savedInstanceState.getBoolean(RECORDING_ENABLED_KEY, false)
            recordingAutomatic = savedInstanceState.getBoolean(RECORDING_AUTOMATIC_KEY, false)
            intervalIndex = savedInstanceState.getInt(INTERVAL_INDEX_KEY, DEFAULT_INTERVAL_INDEX)
            accuracyThresholdIndex = savedInstanceState.getInt(
                ACCURACY_THRESHOLD_INDEX_KEY, DEFAULT_ACCURACY_THRESHOLD_INDEX
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mapFragment: MapFragment =
            (view.findViewById<View?>(R.id.map_container) as FragmentContainerView).getFragment()
        mapFragment.init({ initMap(it, GeopolyLayoutBinding.bind(view)) }, { this.cancel() })

        val snackbar = SnackbarUtils.make(requireView(), "", Snackbar.LENGTH_INDEFINITE)
        invalidMessage.observe(viewLifecycleOwner) {
            if (it != null) {
                snackbar.setText(it)
                SnackbarUtils.show(snackbar)
            } else {
                snackbar.dismiss()
            }
        }

        onBackPressedDispatcher().addCallback(viewLifecycleOwner, onBackPressedCallback)
    }

    override fun onSaveInstanceState(state: Bundle) {
        super.onSaveInstanceState(state)
        if (map == null) {
            // initMap() is called asynchronously, so map can be null if the activity
            // is stopped (e.g. by screen rotation) before initMap() gets to run.
            // In this case, preserve any provided instance state.
            if (previousState != null) {
                state.putAll(previousState)
            }
            return
        }
        state.putBoolean(INPUT_ACTIVE_KEY, inputActive)
        state.putBoolean(RECORDING_ENABLED_KEY, recordingEnabled)
        state.putBoolean(RECORDING_AUTOMATIC_KEY, recordingAutomatic)
        state.putInt(INTERVAL_INDEX_KEY, intervalIndex)
        state.putInt(ACCURACY_THRESHOLD_INDEX_KEY, accuracyThresholdIndex)
    }

    fun initMap(newMapFragment: MapFragment?, binding: GeopolyLayoutBinding) {
        map = newMapFragment

        binding.clear.setOnClickListener { showClearDialog() }
        binding.pause.setOnClickListener {
            viewModel.stopRecording()
            inputActive = false
            updateUi()
        }

        binding.backspace.setOnClickListener { removeLastPoint() }
        binding.save.setOnClickListener {
            if (!viewModel.points.value.isEmpty()) {
                if (outputMode == OutputMode.GEOTRACE) {
                    saveAsPolyline()
                } else {
                    saveAsPolygon()
                }
            } else {
                setResult()
            }
        }

        binding.play.setOnClickListener {
            if (viewModel.points.value.isEmpty()) {
                showIfNotShowing<GeoPolySettingsDialogFragment>(
                    GeoPolySettingsDialogFragment::class.java,
                    getChildFragmentManager()
                )
            } else {
                startInput()
            }
        }

        binding.recordButton.setOnClickListener { recordPoint(map!!.getGpsLocation()) }
        binding.layers.setOnClickListener {
            showIfNotShowing(
                OfflineMapLayersPickerBottomSheetDialogFragment::class.java,
                getChildFragmentManager()
            )
        }

        binding.zoom.setOnClickListener {
            map!!.zoomToCurrentLocation(
                map!!.getGpsLocation()
            )
        }

        originalPoly = inputPolygon

        map!!.setClickListener(this::onClick)
        // Also allow long press to place point to match prior versions
        map!!.setLongPressListener(this::onClick)
        map!!.setGpsLocationEnabled(true)
        map!!.setGpsLocationListener(this::onGpsLocation)
        map!!.setRetainMockAccuracy(retainMockAccuracy)
        map!!.setDragEndListener {
            viewModel.update(map!!.getPolyPoints(it))
        }

        if (!map!!.hasCenter()) {
            if (viewModel.points.value.isNotEmpty()) {
                map!!.zoomToBoundingBox(viewModel.points.value, 0.6, false)
            } else {
                map!!.runOnGpsLocationReady { this.onGpsLocationReady(it) }
            }
        }

        val pointsAndInvalid = viewModel.points.asLiveData().zip(invalidMessage.map { it != null })
        pointsAndInvalid.observe(viewLifecycleOwner) { (points, invalid) ->
            val color = if (invalid) {
                MapConsts.DEFAULT_ERROR_COLOR
            } else {
                MapConsts.DEFAULT_STROKE_COLOR
            }

            if (outputMode == OutputMode.GEOSHAPE) {
                val polygonDescription = PolygonDescription(
                    points,
                    draggable = !readOnly,
                    strokeColor = color,
                    fillColor = color,
                    highlightLastPoint = !invalid
                )

                if (featureId == -1) {
                    featureId = map!!.addPolygon(polygonDescription)
                } else {
                    map!!.updatePolygon(featureId, polygonDescription)
                }
            } else {
                val lineDescription = LineDescription(
                    points,
                    draggable = !readOnly,
                    strokeColor = color,
                    highlightLastPoint = !invalid
                )

                if (featureId == -1) {
                    featureId = map!!.addPolyLine(lineDescription)
                } else {
                    map!!.updatePolyLine(featureId, lineDescription)
                }
            }

            updateUi()
            setChangeResult()
        }
    }

    private fun saveAsPolyline() {
        if (viewModel.points.value.size > 1) {
            setResult()
        } else {
            showShortToastInMiddle(
                requireActivity(),
                getString(org.odk.collect.strings.R.string.polyline_validator)
            )
        }
    }

    private fun saveAsPolygon() {
        if (viewModel.points.value.size > 2) {
            setResult()
        } else {
            showShortToastInMiddle(
                requireActivity(),
                getString(org.odk.collect.strings.R.string.polygon_validator)
            )
        }
    }

    private fun setChangeResult() {
        val points = viewModel.points.value
        val geoString = if (outputMode == OutputMode.GEOSHAPE && points.size < 3) {
            ""
        } else if (points.size < 2) {
            ""
        } else {
            getGeoString(points)
        }

        getParentFragmentManager().setFragmentResult(
            REQUEST_GEOPOLY,
            bundleOf(RESULT_GEOPOLY_CHANGE to geoString)
        )
    }

    private fun setResult() {
        val points = viewModel.points.value
        getParentFragmentManager().setFragmentResult(
            REQUEST_GEOPOLY,
            bundleOf(RESULT_GEOPOLY to getGeoString(points))
        )
    }

    private fun getGeoString(points: List<MapPoint>): String? {
        return GeoUtils.formatPointsResultString(
            points.toMutableList(),
            outputMode == OutputMode.GEOSHAPE
        )
    }

    override fun startInput() {
        inputActive = true
        if (recordingEnabled && recordingAutomatic) {
            locationTracker.warm(map!!.getGpsLocation()?.toLocation())
            viewModel.startRecording(
                ACCURACY_THRESHOLD_OPTIONS[accuracyThresholdIndex],
                INTERVAL_OPTIONS[intervalIndex].toLong() * 1000
            )
        }
        updateUi()
    }

    override fun updateRecordingMode(id: Int) {
        recordingEnabled = id != R.id.placement_mode
        recordingAutomatic = id == R.id.automatic_mode
    }

    override fun getCheckedId(): Int {
        return if (recordingEnabled) {
            if (recordingAutomatic) R.id.automatic_mode else R.id.manual_mode
        } else {
            R.id.placement_mode
        }
    }

    override fun getIntervalIndex(): Int {
        return intervalIndex
    }

    override fun getAccuracyThresholdIndex(): Int {
        return accuracyThresholdIndex
    }

    override fun setIntervalIndex(intervalIndex: Int) {
        this.intervalIndex = intervalIndex
    }

    override fun setAccuracyThresholdIndex(accuracyThresholdIndex: Int) {
        this.accuracyThresholdIndex = accuracyThresholdIndex
    }

    private fun cancel() {
        getParentFragmentManager().setFragmentResult(REQUEST_GEOPOLY, Bundle.EMPTY)
    }

    private fun discard() {
        val geoString = GeoUtils.formatPointsResultString(
            (originalPoly ?: emptyList()).toMutableList(),
            outputMode == OutputMode.GEOSHAPE
        )

        val bundle = Bundle()
        bundle.putString(RESULT_GEOPOLY, geoString)
        getParentFragmentManager().setFragmentResult(REQUEST_GEOPOLY, bundle)
    }

    private fun onClick(point: MapPoint) {
        if (inputActive && !recordingEnabled) {
            viewModel.add(point)
        }
    }

    private fun onGpsLocationReady(map: MapFragment) {
        // Don't zoom to current location if a user is manually entering points
        if (requireActivity().window.isActive && (!inputActive || recordingEnabled)) {
            map.zoomToCurrentLocation(map.getGpsLocation())
        }
        updateUi()
    }

    private fun onGpsLocation(point: MapPoint?) {
        if (inputActive && recordingEnabled) {
            map!!.setCenter(point, false)
        }
        updateUi()
    }

    private fun recordPoint(point: MapPoint?) {
        if (point != null && isLocationAcceptable(point)) {
            viewModel.add(point)
        }
    }

    private fun isLocationAcceptable(point: MapPoint): Boolean {
        if (!this.isAccuracyThresholdActive) {
            return true
        }
        return point.accuracy <= ACCURACY_THRESHOLD_OPTIONS[accuracyThresholdIndex]
    }

    private val isAccuracyThresholdActive: Boolean
        get() {
            val meters: Int =
                ACCURACY_THRESHOLD_OPTIONS[accuracyThresholdIndex]
            return recordingEnabled && recordingAutomatic && meters > 0
        }

    private fun removeLastPoint() {
        if (featureId != -1) {
            viewModel.removeLast()
        }
    }

    private fun clear() {
        inputActive = false
        viewModel.update(emptyList())
    }

    /** Updates the state of various UI widgets to reflect internal state.  */
    private fun updateUi() {
        val binding = GeopolyLayoutBinding.bind(requireView())

        val numPoints = viewModel.points.value.size
        val location = map!!.getGpsLocation()

        // Visibility state
        binding.play.isVisible = !inputActive
        binding.pause.isVisible = inputActive
        binding.recordButton.isVisible = inputActive && recordingEnabled && !recordingAutomatic

        // Enabled state
        binding.zoom.isEnabled = location != null
        binding.backspace.isEnabled = numPoints > 0
        binding.clear.isEnabled = !inputActive && numPoints > 0

        if (readOnly) {
            binding.play.isEnabled = false
            binding.backspace.isEnabled = false
            binding.clear.isEnabled = false
            binding.save.isEnabled = false
        }

        // Settings dialog

        // GPS status
        val usingThreshold = this.isAccuracyThresholdActive
        val acceptable = location != null && isLocationAcceptable(location)
        val seconds: Int = INTERVAL_OPTIONS[intervalIndex]
        val minutes = seconds / 60
        val meters: Int = ACCURACY_THRESHOLD_OPTIONS[accuracyThresholdIndex]

        if (location != null) {
            if (usingThreshold && !acceptable) {
                binding.locationStatus.accuracy = Unacceptable(location.accuracy.toFloat())
            } else {
                binding.locationStatus.accuracy = Improving(location.accuracy.toFloat())
            }
        }

        binding.collectionStatus.text = if (!inputActive) {
            getString(org.odk.collect.strings.R.string.collection_status_paused, numPoints)
        } else {
            if (!recordingEnabled) {
                getString(
                    org.odk.collect.strings.R.string.collection_status_placement,
                    numPoints
                )
            } else {
                if (!recordingAutomatic) {
                    getString(
                        org.odk.collect.strings.R.string.collection_status_manual,
                        numPoints
                    )
                } else {
                    if (!usingThreshold) {
                        if (minutes > 0) {
                            getString(
                                org.odk.collect.strings.R.string.collection_status_auto_minutes,
                                numPoints,
                                minutes
                            )
                        } else {
                            getString(
                                org.odk.collect.strings.R.string.collection_status_auto_seconds,
                                numPoints,
                                seconds
                            )
                        }
                    } else {
                        if (minutes > 0) {
                            getString(
                                org.odk.collect.strings.R.string.collection_status_auto_minutes_accuracy,
                                numPoints,
                                minutes,
                                meters
                            )
                        } else {
                            getString(
                                org.odk.collect.strings.R.string.collection_status_auto_seconds_accuracy,
                                numPoints,
                                seconds,
                                meters
                            )
                        }
                    }
                }
            }
        }
    }

    private fun showClearDialog() {
        if (!viewModel.points.value.isEmpty()) {
            MaterialAlertDialogBuilder(requireContext())
                .setMessage(org.odk.collect.strings.R.string.geo_clear_warning)
                .setPositiveButton(org.odk.collect.strings.R.string.clear) { _, _ -> clear() }
                .setNegativeButton(org.odk.collect.strings.R.string.cancel, null)
                .show()
        }
    }

    private fun showBackDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(getString(org.odk.collect.strings.R.string.geo_exit_warning))
            .setPositiveButton(org.odk.collect.strings.R.string.discard) { _, _ -> discard() }
            .setNegativeButton(org.odk.collect.strings.R.string.cancel, null)
            .show()
    }

    enum class OutputMode {
        GEOTRACE, GEOSHAPE
    }

    companion object {
        const val REQUEST_GEOPOLY: String = "geopoly"
        const val RESULT_GEOPOLY: String = "geopoly"
        const val RESULT_GEOPOLY_CHANGE: String = "geopoly_change"

        const val INPUT_ACTIVE_KEY: String = "input_active"
        const val RECORDING_ENABLED_KEY: String = "recording_enabled"
        const val RECORDING_AUTOMATIC_KEY: String = "recording_automatic"
        const val INTERVAL_INDEX_KEY: String = "interval_index"
        const val ACCURACY_THRESHOLD_INDEX_KEY: String = "accuracy_threshold_index"
        val INTERVAL_OPTIONS = intArrayOf(
            1, 5, 10, 20, 30, 60, 300, 600, 1200, 1800
        )
        const val DEFAULT_INTERVAL_INDEX = 3 // default is 20 seconds

        private val ACCURACY_THRESHOLD_OPTIONS = intArrayOf(
            0, 3, 5, 10, 15, 20
        )
        private const val DEFAULT_ACCURACY_THRESHOLD_INDEX = 3 // default is 10 meters
    }
}
