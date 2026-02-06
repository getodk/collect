package org.odk.collect.geo.geopoly

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import org.odk.collect.androidshared.ui.DialogFragmentUtils.showIfNotShowing
import org.odk.collect.androidshared.ui.DisplayString
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.androidshared.ui.SnackbarUtils
import org.odk.collect.androidshared.ui.SnackbarUtils.showSnackbar
import org.odk.collect.androidshared.ui.ToastUtils.showShortToastInMiddle
import org.odk.collect.async.Scheduler
import org.odk.collect.geo.GeoActivityUtils.requireLocationPermissions
import org.odk.collect.geo.GeoDependencyComponentProvider
import org.odk.collect.geo.GeoUtils
import org.odk.collect.geo.GeoUtils.toMapPoint
import org.odk.collect.geo.R
import org.odk.collect.geo.databinding.GeopolyLayoutBinding
import org.odk.collect.geo.geopoint.LocationAccuracy.Improving
import org.odk.collect.geo.geopoint.LocationAccuracy.Unacceptable
import org.odk.collect.geo.geopoly.GeoPolySettingsDialogFragment.SettingsDialogCallback
import org.odk.collect.location.tracker.LocationTracker
import org.odk.collect.location.tracker.getCurrentLocation
import org.odk.collect.maps.LineDescription
import org.odk.collect.maps.MapConsts
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapFragmentFactory
import org.odk.collect.maps.MapPoint
import org.odk.collect.maps.PolygonDescription
import org.odk.collect.maps.layers.OfflineMapLayersPickerBottomSheetDialogFragment
import org.odk.collect.maps.layers.ReferenceLayerRepository
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.strings.R.string
import org.odk.collect.webpage.WebPageService
import javax.inject.Inject

class GeoPolyFragment @JvmOverloads constructor(
    val onBackPressedDispatcher: () -> OnBackPressedDispatcher,
    val outputMode: OutputMode = OutputMode.GEOTRACE,
    val readOnly: Boolean = false,
    val retainMockAccuracy: Boolean = false,
    val inputPolygon: List<MapPoint> = emptyList(),
    val invalidMessage: LiveData<DisplayString?> = MutableLiveData(null)
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

    private var featureId = -1 // will be a positive featureId once map is ready
    private var originalPoly: List<MapPoint>? = null
    private var intervalIndex: Int = DEFAULT_INTERVAL_INDEX
    private var accuracyThresholdIndex: Int = DEFAULT_ACCURACY_THRESHOLD_INDEX
    private var mapInitialized = false

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!readOnly && originalPoly != viewModel.points.value) {
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
                    scheduler,
                    invalidMessage
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

        viewModel.points.asLiveData().observe(this) {
            setChangeResult(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        previousState = savedInstanceState

        if (savedInstanceState != null) {
            intervalIndex = savedInstanceState.getInt(INTERVAL_INDEX_KEY, DEFAULT_INTERVAL_INDEX)
            accuracyThresholdIndex = savedInstanceState.getInt(
                ACCURACY_THRESHOLD_INDEX_KEY, DEFAULT_ACCURACY_THRESHOLD_INDEX
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = GeopolyLayoutBinding.bind(view)
        val mapFragment: MapFragment = binding.mapContainer.getFragment()
        mapFragment.init({ initMap(it, binding) }, { this.cancel() })

        onBackPressedDispatcher().addCallback(viewLifecycleOwner, onBackPressedCallback)

        viewModel.fixedAlerts.showSnackbar(viewLifecycleOwner, view) {
            SnackbarUtils.SnackbarDetails(getString(string.error_fixed))
        }
    }

    override fun onSaveInstanceState(state: Bundle) {
        super.onSaveInstanceState(state)
        if (!mapInitialized) {
            // initMap() is called asynchronously, so map can be null if the activity
            // is stopped (e.g. by screen rotation) before initMap() gets to run.
            // In this case, preserve any provided instance state.
            if (previousState != null) {
                state.putAll(previousState)
            }
            return
        }
        state.putInt(INTERVAL_INDEX_KEY, intervalIndex)
        state.putInt(ACCURACY_THRESHOLD_INDEX_KEY, accuracyThresholdIndex)
    }

    fun initMap(map: MapFragment, binding: GeopolyLayoutBinding) {
        mapInitialized = true

        binding.info.setOnClickListener { showInfoDialog(false) }
        binding.clear.setOnClickListener { showClearDialog() }
        binding.pause.setOnClickListener {
            viewModel.stopRecording()
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

        binding.recordButton.setOnClickListener {
            viewModel.recordPoint()
        }

        binding.layers.setOnClickListener {
            showIfNotShowing(
                OfflineMapLayersPickerBottomSheetDialogFragment::class.java,
                getChildFragmentManager()
            )
        }

        binding.zoom.setOnClickListener {
            map.zoomToCurrentLocation(locationTracker.getCurrentLocation()?.toMapPoint())
        }

        originalPoly = inputPolygon

        map.setClickListener(this::onClick)
        // Also allow long press to place point to match prior versions
        map.setLongPressListener(this::onClick)
        map.setDragEndListener {
            viewModel.update(map.getPolyPoints(it))
        }

        if (!map.hasCenter()) {
            if (viewModel.points.value.isNotEmpty()) {
                map.zoomToBoundingBox(viewModel.points.value, 0.6, false)
            }
        }

        val snackbar = SnackbarUtils.make(
            requireView(),
            "",
            Snackbar.LENGTH_INDEFINITE,
            action = SnackbarUtils.Action(getString(string.how_to_modify)) {
                showInfoDialog(true)
            },
            displayDismissButton = true
        )

        locationTracker.getLocation().asLiveData().observe(viewLifecycleOwner) { location ->
            binding.zoom.isEnabled = location != null
            val shouldFollowLocation =
                viewModel.inputActive && viewModel.recordingMode != GeoPolyViewModel.RecordingMode.PLACEMENT
            if (!map.hasCenter() || shouldFollowLocation) {
                map.setCenter(location?.toMapPoint(), false)
            }
        }

        viewModel.viewData.observe(viewLifecycleOwner) { (points, invalidMessage) ->
            val isValid = invalidMessage == null
            if (!isValid) {
                snackbar.setText(invalidMessage.getString(requireContext()))
                SnackbarUtils.show(snackbar)
            } else {
                snackbar.dismiss()
            }

            binding.save.isEnabled = !readOnly && isValid

            val color = if (isValid) {
                MapConsts.DEFAULT_STROKE_COLOR
            } else {
                MapConsts.DEFAULT_ERROR_COLOR
            }

            if (outputMode == OutputMode.GEOSHAPE) {
                val polygonDescription = PolygonDescription(
                    points,
                    draggable = !readOnly,
                    strokeColor = color,
                    fillColor = color,
                    highlightLastPoint = isValid
                )

                if (featureId == -1) {
                    featureId = map.addPolygon(polygonDescription)
                } else {
                    map.updatePolygon(featureId, polygonDescription)
                }
            } else {
                val lineDescription = LineDescription(
                    points,
                    draggable = !readOnly,
                    strokeColor = color,
                    highlightLastPoint = isValid
                )

                if (featureId == -1) {
                    featureId = map.addPolyLine(lineDescription)
                } else {
                    map.updatePolyLine(featureId, lineDescription)
                }
            }

            updateUi()
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

    private fun setChangeResult(points: List<MapPoint>) {
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
        viewModel.enableInput()
        if (viewModel.recordingMode == GeoPolyViewModel.RecordingMode.AUTOMATIC) {
            viewModel.startRecording(
                ACCURACY_THRESHOLD_OPTIONS[accuracyThresholdIndex],
                INTERVAL_OPTIONS[intervalIndex].toLong() * 1000
            )
        }
        updateUi()
    }

    override fun updateRecordingMode(id: Int) {
        when (id) {
            R.id.placement_mode -> viewModel.setRecordingMode(GeoPolyViewModel.RecordingMode.PLACEMENT)
            R.id.manual_mode -> viewModel.setRecordingMode(GeoPolyViewModel.RecordingMode.MANUAL)
            R.id.automatic_mode -> viewModel.setRecordingMode(GeoPolyViewModel.RecordingMode.AUTOMATIC)
        }
    }

    override fun getCheckedId(): Int {
        return when (viewModel.recordingMode) {
            GeoPolyViewModel.RecordingMode.PLACEMENT -> R.id.placement_mode
            GeoPolyViewModel.RecordingMode.MANUAL -> R.id.manual_mode
            GeoPolyViewModel.RecordingMode.AUTOMATIC -> R.id.automatic_mode
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
        if (viewModel.inputActive && viewModel.recordingMode == GeoPolyViewModel.RecordingMode.PLACEMENT) {
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
            return viewModel.recordingMode == GeoPolyViewModel.RecordingMode.AUTOMATIC && meters > 0
        }

    private fun removeLastPoint() {
        if (featureId != -1) {
            viewModel.removeLast()
        }
    }

    private fun clear() {
        viewModel.update(emptyList())
    }

    /** Updates the state of various UI widgets to reflect internal state.  */
    private fun updateUi() {
        val binding = GeopolyLayoutBinding.bind(requireView())

        val numPoints = viewModel.points.value.size
        val location = locationTracker.getCurrentLocation()?.toMapPoint()

        // Visibility state
        binding.play.isVisible = !viewModel.inputActive
        binding.pause.isVisible = viewModel.inputActive
        binding.recordButton.isVisible =
            viewModel.inputActive && viewModel.recordingMode == GeoPolyViewModel.RecordingMode.MANUAL

        // Enabled state
        binding.backspace.isEnabled = numPoints > 0
        binding.clear.isEnabled = !viewModel.inputActive && numPoints > 0

        if (readOnly) {
            binding.play.isEnabled = false
            binding.backspace.isEnabled = false
            binding.clear.isEnabled = false
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

        binding.collectionStatus.text = if (!viewModel.inputActive) {
            getString(org.odk.collect.strings.R.string.collection_status_paused, numPoints)
        } else {
            if (viewModel.recordingMode == GeoPolyViewModel.RecordingMode.PLACEMENT) {
                getString(
                    org.odk.collect.strings.R.string.collection_status_placement,
                    numPoints
                )
            } else {
                if (viewModel.recordingMode == GeoPolyViewModel.RecordingMode.MANUAL) {
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

    private fun showInfoDialog(fromSnackbar: Boolean) {
        InfoDialog.show(requireContext(), viewModel, fromSnackbar)
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
