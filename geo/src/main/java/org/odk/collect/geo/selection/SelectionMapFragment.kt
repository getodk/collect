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
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.androidshared.ui.ToastUtils
import org.odk.collect.androidshared.ui.multiclicksafe.setMultiClickSafeOnClickListener
import org.odk.collect.geo.GeoDependencyComponentProvider
import org.odk.collect.geo.R
import org.odk.collect.geo.ReferenceLayerSettingsNavigator
import org.odk.collect.geo.databinding.SelectionMapLayoutBinding
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapFragmentFactory
import org.odk.collect.maps.MapPoint
import org.odk.collect.maps.markers.MarkerDescription
import org.odk.collect.maps.markers.MarkerIconDescription
import org.odk.collect.material.BottomSheetBehavior
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
    val zoomToFitItems: Boolean = true,
    val showNewItemButton: Boolean = true,
    val onBackPressedDispatcher: (() -> OnBackPressedDispatcher)? = null,
) : Fragment() {

    @Inject
    lateinit var mapFragmentFactory: MapFragmentFactory

    @Inject
    lateinit var referenceLayerSettingsNavigator: ReferenceLayerSettingsNavigator

    @Inject
    lateinit var permissionsChecker: PermissionsChecker

    private val selectedFeatureViewModel by viewModels<SelectedFeatureViewModel>()

    private lateinit var map: MapFragment
    private lateinit var summarySheetBehavior: BottomSheetBehavior<*>
    private lateinit var summarySheet: SelectionSummarySheet
    private lateinit var bottomSheetCallback: BottomSheetCallback

    private val itemsByFeatureId: MutableMap<Int, MappableSelectItem> = mutableMapOf()

    /**
     * Points to be mapped. Note: kept separately from [.itemsByFeatureId] so we can
     * quickly zoom to bounding box.
     */
    private val points: MutableList<MapPoint> = mutableListOf()
    private var itemCount: Int = 0

    private var previousState: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        childFragmentManager.fragmentFactory = FragmentFactoryBuilder()
            .forClass(MapFragment::class.java) {
                mapFragmentFactory.createMapFragment() as Fragment
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
        savedInstanceState: Bundle?,
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
            map.zoomToPoint(map.gpsLocation, true)
        }

        binding.zoomToBounds.setMultiClickSafeOnClickListener {
            map.zoomToBoundingBox(points, 0.8, false)
        }

        binding.layerMenu.setMultiClickSafeOnClickListener {
            referenceLayerSettingsNavigator.navigateToReferenceLayerSettings(requireActivity())
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

        map.setGpsLocationEnabled(true)

        map.setFeatureClickListener(::onFeatureClicked)
        map.setClickListener { onClick() }

        selectionMapData.getMappableItems().observe(viewLifecycleOwner) {
            if (it != null) {
                updateItems(it)
                updateCounts(binding)
            }
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
                val selectedFeatureId = selectedFeatureViewModel.getSelectedFeatureId()
                if (newState == STATE_HIDDEN && selectedFeatureId != null) {
                    selectedFeatureViewModel.setSelectedFeatureId(null)
                    resetIcon(selectedFeatureId)

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

    private fun onFeatureClicked(featureId: Int, maintainZoom: Boolean = true) {
        val selectedFeatureId = selectedFeatureViewModel.getSelectedFeatureId()
        if (selectedFeatureId != null && selectedFeatureId != featureId) {
            resetIcon(selectedFeatureId)
        }

        val item = itemsByFeatureId[featureId]
        if (item != null) {
            if (!skipSummary) {
                if (maintainZoom) {
                    map.zoomToPoint(MapPoint(item.latitude, item.longitude), map.zoom, true)
                } else {
                    map.zoomToPoint(MapPoint(item.latitude, item.longitude), true)
                }

                map.setMarkerIcon(
                    featureId,
                    MarkerIconDescription(item.largeIcon, item.color, item.symbol)
                )

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

    private fun onClick() {
        summarySheetBehavior.state = STATE_HIDDEN
    }

    private fun updateItems(items: List<MappableSelectItem>) {
        if (!::map.isInitialized) {
            return
        }

        updateFeatures(items)

        val previouslySelectedItem =
            itemsByFeatureId.filter { it.value.selected }.map { it.key }.firstOrNull()
        val selectedFeatureId = selectedFeatureViewModel.getSelectedFeatureId()

        if (selectedFeatureId != null) {
            onFeatureClicked(selectedFeatureId)
        } else if (previouslySelectedItem != null) {
            onFeatureClicked(previouslySelectedItem, maintainZoom = false)
        } else if (!map.hasCenter()) {
            if (zoomToFitItems && points.isNotEmpty()) {
                map.zoomToBoundingBox(points, 0.8, false)
            } else {
                map.setGpsLocationListener { point ->
                    map.zoomToPoint(point, true)
                    map.setGpsLocationListener(null)
                }
            }
        }
    }

    private fun resetIcon(selectedFeatureId: Int) {
        val item = itemsByFeatureId[selectedFeatureId]!!
        map.setMarkerIcon(
            selectedFeatureId,
            MarkerIconDescription(item.smallIcon, item.color, item.symbol)
        )
    }

    /**
     * Clears the existing features on the map and places features for the current form's instances.
     */
    private fun updateFeatures(items: List<MappableSelectItem>) {
        points.clear()
        map.clearFeatures()
        itemsByFeatureId.clear()

        val markerDescriptions = items.map {
            MarkerDescription(
                MapPoint(it.latitude, it.longitude),
                false,
                MapFragment.BOTTOM,
                MarkerIconDescription(it.smallIcon, it.color, it.symbol)
            )
        }

        val featureIds = map.addMarkers(markerDescriptions)
        items.zip(featureIds).forEach { (item, featureId) ->
            itemsByFeatureId[featureId] = item
            points.add(MapPoint(item.latitude, item.longitude))
        }
    }

    companion object {
        const val REQUEST_SELECT_ITEM = "select_item"
        const val RESULT_SELECTED_ITEM = "selected_item"
        const val RESULT_CREATE_NEW_ITEM = "create_new_item"
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
    fun getMapTitle(): LiveData<String?>
    fun getItemType(): String
    fun getItemCount(): NonNullLiveData<Int>
    fun getMappableItems(): LiveData<List<MappableSelectItem>?>
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
    val color: String?
    val symbol: String?

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
        override val color: String? = null,
        override val symbol: String? = null
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
        override val selected: Boolean = false,
        override val color: String? = null,
        override val symbol: String? = null
    ) : MappableSelectItem

    data class IconifiedText(val icon: Int?, val text: String)
}
