package org.odk.collect.geo.selection

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import org.odk.collect.androidshared.livedata.NonNullLiveData
import org.odk.collect.androidshared.ui.DialogFragmentUtils
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.androidshared.ui.ToastUtils
import org.odk.collect.androidshared.ui.multiclicksafe.setMultiClickSafeOnClickListener
import org.odk.collect.androidshared.utils.sanitizeToColorInt
import org.odk.collect.async.Scheduler
import org.odk.collect.geo.GeoDependencyComponentProvider
import org.odk.collect.geo.GeoUtils.showCurrentLocation
import org.odk.collect.geo.databinding.SelectionMapLayoutBinding
import org.odk.collect.location.tracker.LocationTracker
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapFragmentFactory
import org.odk.collect.maps.MapPoint
import org.odk.collect.maps.circles.CurrentLocationDelegate
import org.odk.collect.maps.layers.OfflineMapLayersPickerBottomSheetDialogFragment
import org.odk.collect.maps.layers.ReferenceLayerRepository
import org.odk.collect.maps.markers.MarkerDescription
import org.odk.collect.maps.markers.MarkerIconDescription
import org.odk.collect.maps.traces.LineDescription
import org.odk.collect.maps.traces.PolygonDescription
import org.odk.collect.material.BottomSheetBehavior
import org.odk.collect.material.MaterialProgressDialogFragment
import org.odk.collect.permissions.PermissionsChecker
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.webpage.WebPageService
import javax.inject.Inject

/**
 * Can be used to allow an item to be selected from a map. Items can be provided using an
 * implementation of [SelectionMapData].
 */
class SelectionMapFragment(
    val selectionMapData: SelectionMapData,
    val skipSummary: Boolean = false,
    val zoomToFitItems: Boolean = true,
    val showNewItemButton: Boolean = true,
    val onBackPressedDispatcher: (() -> OnBackPressedDispatcher)? = null
) : Fragment() {

    @Inject
    lateinit var mapFragmentFactory: MapFragmentFactory

    @Inject
    lateinit var permissionsChecker: PermissionsChecker

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

    private val selectedItemViewModel by viewModels<SelectedItemViewModel>()

    private lateinit var map: MapFragment
    private lateinit var summarySheetBehavior: BottomSheetBehavior<*>
    private lateinit var summarySheet: SelectionSummarySheet
    private lateinit var bottomSheetCallback: BottomSheetCallback

    private val itemsByFeatureId: MutableMap<Int, MappableItem> = mutableMapOf()

    /**
     * Points to be mapped. Note: kept separately from [.itemsByFeatureId] so we can
     * quickly zoom to bounding box.
     */
    private val points: MutableList<MapPoint> = mutableListOf()
    private var itemCount: Int = 0
    private var featureCount: Int = 0

    private var previousState: Bundle? = null
    private val currentLocationDelegate = CurrentLocationDelegate()

    override fun onCreate(savedInstanceState: Bundle?) {
        childFragmentManager.fragmentFactory = FragmentFactoryBuilder()
            .forClass(MapFragment::class.java) {
                mapFragmentFactory.createMapFragment() as Fragment
            }
            .forClass(OfflineMapLayersPickerBottomSheetDialogFragment::class) {
                OfflineMapLayersPickerBottomSheetDialogFragment(requireActivity().activityResultRegistry, referenceLayerRepository, scheduler, settingsProvider, webPageService)
            }
            .build()

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
            ToastUtils.showLongToast(org.odk.collect.strings.R.string.not_granted_permission)
            requireActivity().finish()
        }

        MaterialProgressDialogFragment.showOn(
            this,
            selectionMapData.isLoading(),
            childFragmentManager
        ) {
            MaterialProgressDialogFragment().also { dialog ->
                dialog.message = getString(org.odk.collect.strings.R.string.loading)
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

        val mapFragment = binding.mapContainer.getFragment<Fragment?>() as MapFragment
        mapFragment.init(
            { newMapFragment -> initMap(newMapFragment, binding) },
            { requireActivity().finish() }
        )

        selectionMapData.getMapTitle().observe(viewLifecycleOwner) {
            binding.title.text = it
        }

        selectionMapData.getItemCount().observe(viewLifecycleOwner) {
            itemCount = it
            updateCounts(binding)
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
    }

    override fun onDestroy() {
        if (this::summarySheetBehavior.isInitialized) {
            summarySheetBehavior.removeBottomSheetCallback(bottomSheetCallback)
        }

        super.onDestroy()
    }

    @SuppressLint("MissingPermission") // Permission handled in Constructor
    private fun initMap(newMapFragment: MapFragment, binding: SelectionMapLayoutBinding) {
        map = newMapFragment

        binding.zoomToLocation.setMultiClickSafeOnClickListener {
            currentLocationDelegate.zoomToCurrentLocation(map)
        }

        binding.zoomToBounds.setMultiClickSafeOnClickListener {
            map.zoomToBoundingBox(points, 0.8, false)
        }

        binding.layerMenu.setMultiClickSafeOnClickListener {
            DialogFragmentUtils.showIfNotShowing(
                OfflineMapLayersPickerBottomSheetDialogFragment::class.java,
                childFragmentManager
            )
        }

        if (showNewItemButton) {
            binding.newItem.setMultiClickSafeOnClickListener {
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

        map.setFeatureClickListener(::onFeatureSelected)
        map.setClickListener { onClick() }

        selectionMapData.getMappableItems().observe(viewLifecycleOwner) {
            if (it != null) {
                updateItems(it)
                updateCounts(binding)
            }
        }

        map.showCurrentLocation(locationTracker, currentLocationDelegate)
    }

    private fun updateCounts(binding: SelectionMapLayoutBinding) {
        binding.geometryStatus.text = getString(
            org.odk.collect.strings.R.string.select_item_count,
            selectionMapData.getItemType(),
            itemCount,
            featureCount
        )
    }

    private fun setUpSummarySheet(binding: SelectionMapLayoutBinding) {
        summarySheet = binding.summarySheet
        summarySheetBehavior = BottomSheetBehavior.from(summarySheet)
        summarySheetBehavior.state = STATE_HIDDEN

        val closeSummarySheet = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                summarySheetBehavior.state = STATE_HIDDEN
            }
        }

        (onBackPressedDispatcher?.invoke() ?: requireActivity().onBackPressedDispatcher).addCallback(
            viewLifecycleOwner,
            closeSummarySheet
        )

        bottomSheetCallback = object : BottomSheetCallback() {
            override fun onStateChanged(onStateChangedbottomSheet: View, newState: Int) {
                val selectedItem = selectedItemViewModel.getSelectedItem()
                if (newState == STATE_HIDDEN && selectedItem != null) {
                    selectedItemViewModel.setSelectedItem(null)
                    if (selectedItem is MappableItem.MappablePoint) {
                        resetIcon(selectedItem)
                    }

                    closeSummarySheet.isEnabled = false
                } else {
                    closeSummarySheet.isEnabled = true
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        }

        summarySheetBehavior.addBottomSheetCallback(bottomSheetCallback)

        summarySheet.listener = object : SelectionSummarySheet.Listener {
            override fun selectionAction(id: Long) {
                summarySheetBehavior.state = STATE_HIDDEN

                parentFragmentManager.setFragmentResult(
                    REQUEST_SELECT_ITEM,
                    Bundle().also {
                        it.putLong(RESULT_SELECTED_ITEM, id)
                    }
                )
            }
        }
    }

    private fun onFeatureSelected(featureId: Int, maintainZoom: Boolean = true, selectedByUser: Boolean = true) {
        val item = itemsByFeatureId[featureId]
        val selectedItem = selectedItemViewModel.getSelectedItem()

        if (item != null) {
            if (selectedItem != null && selectedItem.id != item.id && selectedItem is MappableItem.MappablePoint) {
                resetIcon(selectedItem)
            }

            if (skipSummary && selectedByUser) {
                parentFragmentManager.setFragmentResult(
                    REQUEST_SELECT_ITEM,
                    Bundle().also {
                        it.putLong(RESULT_SELECTED_ITEM, item.id)
                    }
                )
            } else {
                when (item) {
                    is MappableItem.MappableLine -> map.zoomToBoundingBox(item.points, 0.8, true)
                    is MappableItem.MappablePolygon -> map.zoomToBoundingBox(item.points, 0.8, true)
                    is MappableItem.MappablePoint -> {
                        val point = item.point

                        if (maintainZoom) {
                            map.zoomToPoint(MapPoint(point.latitude, point.longitude), map.getZoom(), true)
                        } else {
                            map.zoomToPoint(MapPoint(point.latitude, point.longitude), true)
                        }

                        map.setMarkerIcon(
                            featureId,
                            MarkerIconDescription.DrawableResource(item.largeIcon, item.color, item.symbol)
                        )
                    }
                }

                summarySheet.setItem(item)

                summarySheetBehavior.state = STATE_COLLAPSED
                summarySheet.viewTreeObserver.addOnGlobalLayoutListener(
                    object : ViewTreeObserver.OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            summarySheet.viewTreeObserver.removeOnGlobalLayoutListener(this)
                            summarySheetBehavior.peekHeight = summarySheet.peekHeight
                        }
                    }
                )

                selectedItemViewModel.setSelectedItem(item)
            }
        }
    }

    private fun onClick() {
        summarySheetBehavior.state = STATE_HIDDEN
    }

    private fun updateItems(items: List<MappableItem>) {
        if (!::map.isInitialized) {
            return
        }

        updateFeatures(items)

        val previouslySelectedItem = itemsByFeatureId
                .filter { selectionMapData.isSelected(it.value) }
                .map { it.key }
                .firstOrNull()
        val selectedItem = selectedItemViewModel.getSelectedItem()

        if (selectedItem != null) {
            val featureId = featureIdByItemId(selectedItem.id)
            if (featureId != null) {
                onFeatureSelected(featureId, selectedByUser = false)
            }
        } else if (previouslySelectedItem != null) {
            onFeatureSelected(previouslySelectedItem, maintainZoom = false, selectedByUser = false)
        } else if (!map.hasCenter()) {
            if (zoomToFitItems && points.isNotEmpty()) {
                map.zoomToBoundingBox(points, 0.8, false)
            }
        }
    }

    private fun resetIcon(selectedItem: MappableItem.MappablePoint) {
        val featureId = featureIdByItemId(selectedItem.id)
        if (featureId != null) {
            map.setMarkerIcon(
                featureId,
                MarkerIconDescription.DrawableResource(selectedItem.smallIcon, selectedItem.color, selectedItem.symbol)
            )
        }
    }

    /**
     * Clears the existing features on the map and places features for the current form's instances.
     */
    private fun updateFeatures(items: List<MappableItem>) {
        points.clear()
        map.clearFeatures(itemsByFeatureId.keys.toList())
        itemsByFeatureId.clear()

        val singlePoints = items.filterIsInstance<MappableItem.MappablePoint>()
        val lines = items.filterIsInstance<MappableItem.MappableLine>()
        val polygons = items.filterIsInstance<MappableItem.MappablePolygon>()

        val markerDescriptions = singlePoints.map {
            MarkerDescription(
                MapPoint(it.point.latitude, it.point.longitude),
                false,
                MapFragment.IconAnchor.BOTTOM,
                MarkerIconDescription.DrawableResource(it.smallIcon, it.color, it.symbol)
            )
        }

        val pointIds = map.addMarkers(markerDescriptions)
        val lineIds = lines.fold(listOf<Int>()) { ids, item ->
            ids + map.addPolyLine(
                LineDescription(
                    item.points,
                    item.strokeWidth,
                    item.strokeColor?.sanitizeToColorInt()
                )
            )
        }
        val polygonIds = polygons.fold(listOf<Int>()) { ids, item ->
            ids + map.addPolygon(
                PolygonDescription(
                    item.points,
                    item.strokeWidth,
                    item.strokeColor?.sanitizeToColorInt(),
                    item.fillColor?.sanitizeToColorInt()
                )
            )
        }

        (singlePoints + lines + polygons).zip(pointIds + lineIds + polygonIds).forEach { (item, featureId) ->
            itemsByFeatureId[featureId] = item
            when (item) {
                is MappableItem.MappablePoint -> points.add(item.point)
                is MappableItem.MappableLine -> points.addAll(item.points)
                is MappableItem.MappablePolygon -> points.addAll(item.points)
            }
        }

        featureCount = items.size
    }

    private fun featureIdByItemId(id: Long): Int? {
        return itemsByFeatureId.filter { (_, item) -> item.id == id }.keys.firstOrNull()
    }

    companion object {
        const val REQUEST_SELECT_ITEM = "select_item"
        const val RESULT_SELECTED_ITEM = "selected_item"
        const val RESULT_CREATE_NEW_ITEM = "create_new_item"
    }
}

internal class SelectedItemViewModel : ViewModel() {

    private var selectedItem: MappableItem? = null

    fun getSelectedItem(): MappableItem? {
        return selectedItem
    }

    fun setSelectedItem(item: MappableItem?) {
        selectedItem = item
    }
}

interface SelectionMapData : MappableData {
    fun getMapTitle(): LiveData<String?>
    fun getItemType(): String
    fun getItemCount(): NonNullLiveData<Int>

    fun isSelected(mappableItem: MappableItem): Boolean
}
