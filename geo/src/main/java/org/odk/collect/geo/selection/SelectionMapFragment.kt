package org.odk.collect.geo.selection

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import org.odk.collect.androidshared.livedata.NonNullLiveData
import org.odk.collect.androidshared.ui.ToastUtils
import org.odk.collect.geo.GeoDependencyComponentProvider
import org.odk.collect.geo.R
import org.odk.collect.geo.ReferenceLayerSettingsNavigator
import org.odk.collect.geo.databinding.SelectionMapLayoutBinding
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapFragment.ReadyListener
import org.odk.collect.maps.MapFragmentFactory
import org.odk.collect.maps.MapPoint
import org.odk.collect.material.MaterialProgressDialogFragment
import org.odk.collect.permissions.PermissionsChecker
import javax.inject.Inject

/**
 * Can be used to allow an item to be selected from a map. Items can be provided using an
 * implementation of [SelectionMapData].
 */
class SelectionMapFragment(
    val selectionMapData: SelectionMapData,
    val skipSummary: Boolean = false,
    val showNewItemButton: Boolean = true
) : Fragment() {

    @Inject
    lateinit var mapFragmentFactory: MapFragmentFactory

    @Inject
    lateinit var referenceLayerSettingsNavigator: ReferenceLayerSettingsNavigator

    @Inject
    lateinit var permissionsChecker: PermissionsChecker

    private val selectedFeatureViewModel by viewModels<SelectedFeatureViewModel>()

    private lateinit var map: MapFragment
    private var viewportInitialized = false

    private lateinit var summarySheetBehavior: BottomSheetBehavior<*>
    private lateinit var summarySheet: SelectionSummarySheet

    private val itemsByFeatureId: MutableMap<Int, MappableSelectItem> = mutableMapOf()

    /**
     * Points to be mapped. Note: kept separately from [.itemsByFeatureId] so we can
     * quickly zoom to bounding box.
     */
    private val points: MutableList<MapPoint> = mutableListOf()
    private var itemCount: Int = 0

    private var previousState: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        previousState = savedInstanceState
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val component =
            (context.applicationContext as GeoDependencyComponentProvider).geoDependencyComponent
        component.inject(this)

        if (!permissionsChecker.isPermissionGranted(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            ToastUtils.showLongToast(requireContext(), R.string.not_granted_permission)
            requireActivity().finish()
        }

        MaterialProgressDialogFragment.showOn(
            this,
            selectionMapData.isLoading(),
            childFragmentManager
        ) {
            MaterialProgressDialogFragment().also { dialog ->
                dialog.message = getString(R.string.loading)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return SelectionMapLayoutBinding.inflate(inflater).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = SelectionMapLayoutBinding.bind(view)

        selectionMapData.getMapTitle().observe(viewLifecycleOwner) {
            binding.title.text = it
        }

        selectionMapData.getItemCount().observe(viewLifecycleOwner) {
            itemCount = it
            updateCounts(binding)
        }

        val mapToAdd = mapFragmentFactory.createMapFragment(requireContext().applicationContext)
        if (mapToAdd != null) {
            mapToAdd.addTo(
                childFragmentManager,
                R.id.map_container,
                ReadyListener { newMapFragment ->
                    initMap(newMapFragment, binding)
                },
                MapFragment.ErrorListener { requireActivity().finish() }
            )
        } else {
            requireActivity().finish() // The configured map provider is not available
        }

        setUpSummarySheet(binding)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        if (!::map.isInitialized) {
            // initMap() is called asynchronously, so map can be null if the activity
            // is stopped (e.g. by screen rotation) before initMap() gets to run.
            // In this case, preserve any provided instance state.
            if (previousState != null) {
                outState.putAll(previousState)
            }

            return
        }

        outState.putParcelable(MAP_CENTER_KEY, map.center)
        outState.putDouble(MAP_ZOOM_KEY, map.zoom)
    }

    @SuppressLint("MissingPermission") // Permission handled in Constructor
    private fun initMap(newMapFragment: MapFragment, binding: SelectionMapLayoutBinding) {
        map = newMapFragment

        binding.zoomToLocation.setOnClickListener {
            map.zoomToPoint(map.gpsLocation, true)
        }

        binding.zoomToBounds.setOnClickListener {
            map.zoomToBoundingBox(points, 0.8, false)
        }

        binding.layerMenu.setOnClickListener {
            referenceLayerSettingsNavigator.navigateToReferenceLayerSettings(requireActivity())
        }

        if (showNewItemButton) {
            binding.newItem.setOnClickListener {
                parentFragmentManager.setFragmentResult(
                    REQUEST_SELECT_ITEM,
                    Bundle().also {
                        it.putBoolean(RESULT_CREATE_NEW_ITEM, true)
                    }
                )
            }
        } else {
            binding.newItem.visibility = View.GONE
        }

        map.setGpsLocationEnabled(true)
        map.setGpsLocationListener { point -> onLocationChanged(point) }

        previousState?.let { restoreZoomFromPreviousState(it) }

        map.setFeatureClickListener(::onFeatureClicked)
        map.setClickListener { onClick() }

        selectionMapData.getMappableItems().observe(viewLifecycleOwner) {
            updateItems(it)
            updateCounts(binding)
        }

        selectedFeatureViewModel.getSelectedFeatureId()?.let {
            onFeatureClicked(it)
        }
    }

    private fun updateCounts(binding: SelectionMapLayoutBinding) {
        binding.geometryStatus.text = getString(
            R.string.select_item_count,
            selectionMapData.getItemType(),
            itemCount,
            points.size
        )
    }

    private fun restoreZoomFromPreviousState(state: Bundle) {
        val mapCenter: MapPoint? = state.getParcelable(MAP_CENTER_KEY)
        val mapZoom = state.getDouble(MAP_ZOOM_KEY)

        if (mapCenter != null) {
            map.zoomToPoint(mapCenter, mapZoom, false)
            viewportInitialized = true // avoid recentering as soon as location is received
        }
    }

    private fun setUpSummarySheet(binding: SelectionMapLayoutBinding) {
        summarySheet = binding.summarySheet
        summarySheetBehavior = BottomSheetBehavior.from(summarySheet)
        summarySheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        val onBackPressedCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                summarySheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )

        summarySheetBehavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                val selectedFeatureId = selectedFeatureViewModel.getSelectedFeatureId()
                if (newState == BottomSheetBehavior.STATE_HIDDEN && selectedFeatureId != null) {
                    map.setMarkerIcon(
                        selectedFeatureId,
                        itemsByFeatureId[selectedFeatureId]!!.smallIcon
                    )
                    selectedFeatureViewModel.setSelectedFeatureId(null)
                    onBackPressedCallback.isEnabled = false
                } else {
                    onBackPressedCallback.isEnabled = newState == BottomSheetBehavior.STATE_EXPANDED
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        summarySheet.listener = object : SelectionSummarySheet.Listener {
            override fun selectionAction(id: Long) {
                summarySheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

                parentFragmentManager.setFragmentResult(
                    REQUEST_SELECT_ITEM,
                    Bundle().also {
                        it.putLong(RESULT_SELECTED_ITEM, id)
                    }
                )
            }
        }
    }

    /**
     * Zooms the map to the new location if the map viewport hasn't been initialized yet.
     */
    private fun onLocationChanged(point: MapPoint?) {
        if (!viewportInitialized) {
            map.zoomToPoint(point, true)
            viewportInitialized = true
        }
    }

    private fun onFeatureClicked(featureId: Int) {
        summarySheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        if (!isSummaryForFeatureDisplayed(featureId)) {
            removeEnlargedMarkerIfExist(featureId)

            val item = itemsByFeatureId[featureId]
            if (item != null) {
                if (!skipSummary) {
                    map.zoomToPoint(MapPoint(item.latitude, item.longitude), map.zoom, true)
                    map.setMarkerIcon(featureId, item.largeIcon)
                    summarySheet.setItem(item)
                    summarySheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                    selectedFeatureViewModel.setSelectedFeatureId(featureId)
                } else {
                    parentFragmentManager.setFragmentResult(
                        REQUEST_SELECT_ITEM,
                        Bundle().also {
                            it.putLong(RESULT_SELECTED_ITEM, item.id)
                        }
                    )
                }
            }
        }
    }

    private fun onClick() {
        if (summarySheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            summarySheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private fun updateItems(items: List<MappableSelectItem>) {
        if (!::map.isInitialized) {
            return
        }

        updateFeatures(items)

        val selectedFeatureId =
            itemsByFeatureId.filter { it.value.selected }.map { it.key }.firstOrNull()
        if (selectedFeatureId != null) {
            onFeatureClicked(selectedFeatureId)
            viewportInitialized = true
        } else if (!viewportInitialized && points.isNotEmpty()) {
            map.zoomToBoundingBox(points, 0.8, false)
            viewportInitialized = true
        }
    }

    private fun isSummaryForFeatureDisplayed(featureId: Int): Boolean {
        return selectedFeatureViewModel.getSelectedFeatureId() == featureId && summarySheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN
    }

    private fun removeEnlargedMarkerIfExist(itemId: Int) {
        val selectedFeatureId = selectedFeatureViewModel.getSelectedFeatureId()
        if (selectedFeatureId != null && selectedFeatureId != itemId) {
            map.setMarkerIcon(
                selectedFeatureId,
                itemsByFeatureId[selectedFeatureId]!!.smallIcon
            )
        }
    }

    /**
     * Clears the existing features on the map and places features for the current form's instances.
     */
    private fun updateFeatures(items: List<MappableSelectItem>) {
        points.clear()
        map.clearFeatures()
        itemsByFeatureId.clear()

        for (item in items) {
            val point = MapPoint(item.latitude, item.longitude)
            val featureId = map.addMarker(point, false, MapFragment.BOTTOM)

            map.setMarkerIcon(
                featureId,
                if (featureId == selectedFeatureViewModel.getSelectedFeatureId()) item.largeIcon else item.smallIcon
            )

            itemsByFeatureId[featureId] = item
            points.add(point)
        }
    }

    companion object {
        const val REQUEST_SELECT_ITEM = "select_item"
        const val RESULT_SELECTED_ITEM = "selected_item"
        const val RESULT_CREATE_NEW_ITEM = "create_new_item"

        private const val MAP_CENTER_KEY = "map_center"
        private const val MAP_ZOOM_KEY = "map_zoom"
    }
}

internal class SelectedFeatureViewModel : ViewModel() {

    private var selectedFeatureId: Int? = null

    fun getSelectedFeatureId(): Int? {
        return selectedFeatureId
    }

    fun setSelectedFeatureId(itemId: Int?) {
        selectedFeatureId = itemId
    }
}

interface SelectionMapData {
    fun isLoading(): NonNullLiveData<Boolean>
    fun getMapTitle(): LiveData<String>
    fun getItemType(): String
    fun getItemCount(): LiveData<Int>
    fun getMappableItems(): NonNullLiveData<List<MappableSelectItem>>
}

sealed interface MappableSelectItem {

    val id: Long
    val latitude: Double
    val longitude: Double
    val smallIcon: Int
    val largeIcon: Int
    val name: String
    val properties: List<IconifiedText>
    val selected: Boolean

    data class WithInfo(
        override val id: Long,
        override val latitude: Double,
        override val longitude: Double,
        override val smallIcon: Int,
        override val largeIcon: Int,
        override val name: String,
        override val properties: List<IconifiedText>,
        val info: String,
        override val selected: Boolean = false,
    ) : MappableSelectItem

    data class WithAction(
        override val id: Long,
        override val latitude: Double,
        override val longitude: Double,
        override val smallIcon: Int,
        override val largeIcon: Int,
        override val name: String,
        override val properties: List<IconifiedText>,
        val action: IconifiedText,
        override val selected: Boolean = false
    ) : MappableSelectItem

    data class IconifiedText(val icon: Int?, val text: String)
}
