package org.odk.collect.geo.geopoly

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import org.odk.collect.androidshared.ui.DialogFragmentUtils.showIfNotShowing
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.androidshared.ui.ToastUtils.showShortToastInMiddle
import org.odk.collect.async.Scheduler
import org.odk.collect.geo.GeoActivityUtils.requireLocationPermissions
import org.odk.collect.geo.GeoDependencyComponentProvider
import org.odk.collect.geo.GeoUtils
import org.odk.collect.geo.R
import org.odk.collect.geo.geopoint.AccuracyStatusView
import org.odk.collect.geo.geopoint.LocationAccuracy.Improving
import org.odk.collect.geo.geopoint.LocationAccuracy.Unacceptable
import org.odk.collect.geo.geopoly.GeoPolySettingsDialogFragment.SettingsDialogCallback
import org.odk.collect.location.tracker.LocationTracker
import org.odk.collect.maps.LineDescription
import org.odk.collect.maps.MapConsts
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapFragmentFactory
import org.odk.collect.maps.MapPoint
import org.odk.collect.maps.layers.OfflineMapLayersPickerBottomSheetDialogFragment
import org.odk.collect.maps.layers.ReferenceLayerRepository
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.webpage.WebPageService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GeoPolyFragment @JvmOverloads constructor(
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
    private val executorServiceScheduler: ScheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor()
    private var schedulerHandler: ScheduledFuture<*>? = null

    private var map: MapFragment? = null
    private var featureId = -1 // will be a positive featureId once map is ready
    private var originalPoly: List<MapPoint>? = null

    private var zoomButton: ImageButton? = null
    var playButton: ImageButton? = null
    var clearButton: ImageButton? = null
    private var recordButton: Button? = null
    private var pauseButton: ImageButton? = null
    var backspaceButton: ImageButton? = null
    var saveButton: ImageButton? = null

    private var locationStatus: AccuracyStatusView? = null
    private var collectionStatus: TextView? = null

    private var settingsView: View? = null

    private var inputActive = false // whether we are ready for the user to add points
    private var recordingEnabled =
        false // whether points are taken from GPS readings (if not, placed by tapping)
    private var recordingAutomatic =
        false // whether GPS readings are taken at regular intervals (if not, only when user-directed)

    private var intervalIndex: Int = DEFAULT_INTERVAL_INDEX

    private var accuracyThresholdIndex: Int = DEFAULT_ACCURACY_THRESHOLD_INDEX

    // restored from savedInstanceState
    private var restoredPoints: List<MapPoint>? = null

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!readOnly && map != null && originalPoly != map!!.getPolyLinePoints(featureId)) {
                    showBackDialog()
                } else {
                    cancel()
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
        requireActivity().onBackPressedDispatcher.addCallback(onBackPressedCallback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        previousState = savedInstanceState

        if (savedInstanceState != null) {
            restoredPoints = savedInstanceState.getParcelableArrayList(POINTS_KEY)
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
        mapFragment.init({ initMap(view, it) }, { this.cancel() })

        val snackbar = Snackbar.make(requireView(), "", Snackbar.LENGTH_INDEFINITE)
        invalidMessage.observe(viewLifecycleOwner) {
            if (it != null) {
                snackbar.setText(it)
                snackbar.show()
            } else {
                snackbar.dismiss()
            }
        }
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
        state.putParcelableArrayList(
            POINTS_KEY,
            ArrayList<MapPoint?>(map!!.getPolyLinePoints(featureId))
        )
        state.putBoolean(INPUT_ACTIVE_KEY, inputActive)
        state.putBoolean(RECORDING_ENABLED_KEY, recordingEnabled)
        state.putBoolean(RECORDING_AUTOMATIC_KEY, recordingAutomatic)
        state.putInt(INTERVAL_INDEX_KEY, intervalIndex)
        state.putInt(ACCURACY_THRESHOLD_INDEX_KEY, accuracyThresholdIndex)
    }

    override fun onDestroy() {
        if (schedulerHandler != null && !schedulerHandler!!.isCancelled) {
            schedulerHandler!!.cancel(true)
        }

        locationTracker.stop()
        super.onDestroy()
    }

    fun initMap(view: View, newMapFragment: MapFragment?) {
        map = newMapFragment

        locationStatus = view.findViewById(R.id.location_status)
        collectionStatus = view.findViewById(R.id.collection_status)
        settingsView = getLayoutInflater().inflate(R.layout.geopoly_dialog, null)

        clearButton = view.findViewById(R.id.clear)
        clearButton!!.setOnClickListener { showClearDialog() }

        pauseButton = view.findViewById(R.id.pause)
        pauseButton!!.setOnClickListener {
            inputActive = false
            try {
                schedulerHandler!!.cancel(true)
            } catch (_: Exception) {
                // Do nothing
            }
            updateUi()
        }

        backspaceButton = view.findViewById(R.id.backspace)
        backspaceButton!!.setOnClickListener { removeLastPoint() }

        saveButton = view.findViewById(R.id.save)
        saveButton!!.setOnClickListener {
            if (!map!!.getPolyLinePoints(featureId).isEmpty()) {
                if (outputMode == OutputMode.GEOTRACE) {
                    saveAsPolyline()
                } else {
                    saveAsPolygon()
                }
            } else {
                setResult(RESULT_GEOPOLY)
            }
        }

        playButton = view.findViewById(R.id.play)
        playButton!!.setOnClickListener {
            if (map!!.getPolyLinePoints(featureId).isEmpty()) {
                showIfNotShowing<GeoPolySettingsDialogFragment>(
                    GeoPolySettingsDialogFragment::class.java,
                    getChildFragmentManager()
                )
            } else {
                startInput()
            }
        }

        recordButton = view.findViewById(R.id.record_button)
        recordButton!!.setOnClickListener { recordPoint(map!!.getGpsLocation()) }

        view.findViewById<View>(R.id.layers).setOnClickListener {
            showIfNotShowing(
                OfflineMapLayersPickerBottomSheetDialogFragment::class.java,
                getChildFragmentManager()
            )
        }

        zoomButton = view.findViewById(R.id.zoom)
        zoomButton!!.setOnClickListener {
            map!!.zoomToCurrentLocation(
                map!!.getGpsLocation()
            )
        }

        var points = emptyList<MapPoint>()
        if (!inputPolygon.isEmpty()) {
            if (outputMode == OutputMode.GEOSHAPE) {
                points = inputPolygon.subList(0, inputPolygon.size - 1)
            } else {
                points = inputPolygon
            }
        }

        originalPoly = inputPolygon

        restoredPoints?.also {
            points = it
        }

        featureId = map!!.addPolyLine(
            LineDescription(
                points,
                MapConsts.DEFAULT_STROKE_WIDTH.toString(),
                null,
                !readOnly,
                outputMode == OutputMode.GEOSHAPE
            )
        )

        if (inputActive && !readOnly) {
            startInput()
        }

        map!!.setClickListener(this::onClick)
        // Also allow long press to place point to match prior versions
        map!!.setLongPressListener(this::onClick)
        map!!.setGpsLocationEnabled(true)
        map!!.setGpsLocationListener(this::onGpsLocation)
        map!!.setRetainMockAccuracy(retainMockAccuracy)

        if (!map!!.hasCenter()) {
            if (points.isNotEmpty()) {
                map!!.zoomToBoundingBox(points, 0.6, false)
            } else {
                map!!.runOnGpsLocationReady { this.onGpsLocationReady(it) }
            }
        }

        updateUi()
    }

    private fun saveAsPolyline() {
        if (map!!.getPolyLinePoints(featureId).size > 1) {
            setResult(RESULT_GEOPOLY)
        } else {
            showShortToastInMiddle(
                requireActivity(),
                getString(org.odk.collect.strings.R.string.polyline_validator)
            )
        }
    }

    private fun saveAsPolygon() {
        if (map!!.getPolyLinePoints(featureId).size > 2) {
            // Close the polygon.
            val points = map!!.getPolyLinePoints(featureId)
            val count = points.size
            if (count > 1 && points[0] != points[count - 1]) {
                map!!.appendPointToPolyLine(featureId, points[0])
            }
            setResult(RESULT_GEOPOLY)
        } else {
            showShortToastInMiddle(
                requireActivity(),
                getString(org.odk.collect.strings.R.string.polygon_validator)
            )
        }
    }

    private fun setResult(result: String) {
        val points = map!!.getPolyLinePoints(featureId)
        val geoString = GeoUtils.formatPointsResultString(
            points.toMutableList(),
            outputMode == OutputMode.GEOSHAPE
        )
        val bundle = Bundle()
        bundle.putString(result, geoString)
        getParentFragmentManager().setFragmentResult(REQUEST_GEOPOLY, bundle)
    }

    override fun startInput() {
        inputActive = true
        if (recordingEnabled && recordingAutomatic) {
            locationTracker.start(retainMockAccuracy)

            recordPoint(map!!.getGpsLocation())
            schedulerHandler = executorServiceScheduler.scheduleAtFixedRate(
                {
                    requireActivity().runOnUiThread {
                        val currentLocation = locationTracker.getCurrentLocation()
                        if (currentLocation != null) {
                            val currentMapPoint = MapPoint(
                                currentLocation.latitude,
                                currentLocation.longitude,
                                currentLocation.altitude,
                                currentLocation.accuracy.toDouble()
                            )

                            recordPoint(currentMapPoint)
                        }
                    }
                },
                INTERVAL_OPTIONS[intervalIndex].toLong(),
                INTERVAL_OPTIONS[intervalIndex].toLong(),
                TimeUnit.SECONDS
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

    private fun onClick(point: MapPoint) {
        if (inputActive && !recordingEnabled) {
            appendPointIfNew(point)
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
            appendPointIfNew(point)
        }
    }

    private fun appendPointIfNew(point: MapPoint) {
        val points = map!!.getPolyLinePoints(featureId)
        if (points.isEmpty() || point != points[points.size - 1]) {
            map!!.appendPointToPolyLine(featureId, point)
            updateUi()
        }

        setResult(RESULT_GEOPOLY_CHANGE)
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
            map!!.removePolyLineLastPoint(featureId)
            updateUi()
            setResult(RESULT_GEOPOLY_CHANGE)
        }
    }

    private fun clear() {
        map!!.clearFeatures()
        featureId = map!!.addPolyLine(
            LineDescription(
                emptyList(),
                MapConsts.DEFAULT_STROKE_WIDTH.toString(),
                null,
                !readOnly,
                outputMode == OutputMode.GEOSHAPE
            )
        )
        inputActive = false
        updateUi()
    }

    /** Updates the state of various UI widgets to reflect internal state.  */
    private fun updateUi() {
        val numPoints = map!!.getPolyLinePoints(featureId).size
        val location = map!!.getGpsLocation()

        // Visibility state
        playButton!!.isVisible = !inputActive
        pauseButton!!.isVisible = inputActive
        recordButton!!.isVisible = inputActive && recordingEnabled && !recordingAutomatic

        // Enabled state
        zoomButton!!.isEnabled = location != null
        backspaceButton!!.isEnabled = numPoints > 0
        clearButton!!.isEnabled = !inputActive && numPoints > 0
        settingsView!!.findViewById<View>(R.id.manual_mode).setEnabled(location != null)
        settingsView!!.findViewById<View>(R.id.automatic_mode).setEnabled(location != null)

        if (readOnly) {
            playButton!!.isEnabled = false
            backspaceButton!!.isEnabled = false
            clearButton!!.isEnabled = false
            saveButton!!.isEnabled = false
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
                locationStatus!!.accuracy = Unacceptable(location.accuracy.toFloat())
            } else {
                locationStatus!!.accuracy = Improving(location.accuracy.toFloat())
            }
        }

        collectionStatus!!.text = if (!inputActive) {
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
        if (!map!!.getPolyLinePoints(featureId).isEmpty()) {
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
            .setPositiveButton(org.odk.collect.strings.R.string.discard) { _, _ -> cancel() }
            .setNegativeButton(org.odk.collect.strings.R.string.cancel, null)
            .show()
    }

    enum class OutputMode {
        GEOTRACE, GEOSHAPE
    }

    companion object {
        const val REQUEST_GEOPOLY: String = "geopoly"
        const val RESULT_GEOPOLY: String = "geopoly"
        const val RESULT_GEOPOLY_CHANGE: String = "geotrace_change"

        const val POINTS_KEY: String = "points"
        const val INPUT_ACTIVE_KEY: String = "input_active"
        const val RECORDING_ENABLED_KEY: String = "recording_enabled"
        const val RECORDING_AUTOMATIC_KEY: String = "recording_automatic"
        const val INTERVAL_INDEX_KEY: String = "interval_index"
        const val ACCURACY_THRESHOLD_INDEX_KEY: String = "accuracy_threshold_index"
        private val INTERVAL_OPTIONS = intArrayOf(
            1, 5, 10, 20, 30, 60, 300, 600, 1200, 1800
        )
        private const val DEFAULT_INTERVAL_INDEX = 3 // default is 20 seconds

        private val ACCURACY_THRESHOLD_OPTIONS = intArrayOf(
            0, 3, 5, 10, 15, 20
        )
        private const val DEFAULT_ACCURACY_THRESHOLD_INDEX = 3 // default is 10 meters
    }
}
