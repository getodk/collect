package org.odk.collect.geo

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import org.odk.collect.androidshared.ui.ToastUtils
import org.odk.collect.geo.databinding.SelectionMapLayoutBinding
import org.odk.collect.geo.databinding.SelectionSummarySheetLayoutBinding
import org.odk.collect.geo.maps.MapFragment
import org.odk.collect.geo.maps.MapFragment.ReadyListener
import org.odk.collect.geo.maps.MapFragmentFactory
import org.odk.collect.geo.maps.MapPoint
import org.odk.collect.permissions.PermissionsProvider
import javax.inject.Inject

class SelectionMapFragment : Fragment() {

    @Inject
    lateinit var mapFragmentFactory: MapFragmentFactory

    @Inject
    lateinit var referenceLayerSettingsNavigator: ReferenceLayerSettingsNavigator

    @Inject
    lateinit var permissionsProvider: PermissionsProvider

    private val selectionMapViewModel: SelectionMapViewModel by activityViewModels()

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

        if (!permissionsProvider.areLocationPermissionsGranted()) {
            ToastUtils.showLongToast(requireContext(), R.string.not_granted_permission)
            requireActivity().finish()
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

        selectionMapViewModel.getMapTitle().observe(viewLifecycleOwner) {
            binding.title.text = it
        }

        selectionMapViewModel.getItemCount().observe(viewLifecycleOwner) {
            binding.geometryStatus.text = getString(R.string.geometry_status, it, points.size)
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
            referenceLayerSettingsNavigator.navigateToReferenceLayerSettings(requireActivity() as AppCompatActivity)
        }

        binding.newInstance.setOnClickListener {
            parentFragmentManager.setFragmentResult(
                REQUEST_SELECT_ITEM,
                Bundle().also {
                    it.putBoolean(RESULT_CREATE_NEW_ITEM, true)
                }
            )
        }

        map.setGpsLocationEnabled(true)
        map.setGpsLocationListener { point -> onLocationChanged(point) }

        previousState?.let { restoreZoomFromPreviousState(it) }

        map.setFeatureClickListener { featureId -> onFeatureClicked(featureId) }
        map.setClickListener { onClick() }

        selectionMapViewModel.getMappableItems().observe(viewLifecycleOwner) {
            update(it)
        }

        selectionMapViewModel.getSelectedItemId()?.let {
            onFeatureClicked(it)
        }
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
        summarySheet = binding.submissionSummary
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
                val selectedSubmissionId = selectionMapViewModel.getSelectedItemId()
                if (newState == BottomSheetBehavior.STATE_HIDDEN && selectedSubmissionId != null) {
                    map.setMarkerIcon(
                        selectedSubmissionId,
                        itemsByFeatureId[selectedSubmissionId]!!.smallIcon
                    )
                    selectionMapViewModel.setSelectedItemId(-1)
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

    fun onFeatureClicked(featureId: Int) {
        summarySheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        if (!isSummaryForGivenSubmissionDisplayed(featureId)) {
            removeEnlargedMarkerIfExist(featureId)

            val item = itemsByFeatureId[featureId]
            if (item != null) {
                map.zoomToPoint(MapPoint(item.latitude, item.longitude), map.zoom, true)
                map.setMarkerIcon(featureId, item.largeIcon)
                summarySheet.setItem(item)
                summarySheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }

            selectionMapViewModel.setSelectedItemId(featureId)
        }
    }

    private fun onClick() {
        if (summarySheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            summarySheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private fun update(items: List<MappableSelectItem>) {
        if (!::map.isInitialized) {
            return
        }

        updateFeatures(items)

        if (!viewportInitialized && points.isNotEmpty()) {
            map.zoomToBoundingBox(points, 0.8, false)
            viewportInitialized = true
        }
    }

    private fun isSummaryForGivenSubmissionDisplayed(newSubmissionId: Int): Boolean {
        return selectionMapViewModel.getSelectedItemId() == newSubmissionId && summarySheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN
    }

    private fun removeEnlargedMarkerIfExist(newSubmissionId: Int) {
        val selectedSubmissionId = selectionMapViewModel.getSelectedItemId()
        if (selectedSubmissionId != null && selectedSubmissionId != newSubmissionId) {
            map.setMarkerIcon(
                selectedSubmissionId,
                itemsByFeatureId[selectedSubmissionId]!!.smallIcon
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
                if (featureId == selectionMapViewModel.getSelectedItemId()) item.largeIcon else item.smallIcon
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

class SelectionMapViewModel : ViewModel() {

    private var mapTitle = MutableLiveData<String>()
    private var mappableItems = MutableLiveData<List<MappableSelectItem>>(emptyList())
    private var itemCount = MutableLiveData(0)
    private var selectedItemId: Int? = null

    fun getMapTitle(): LiveData<String> {
        return mapTitle
    }

    fun getSelectedItemId(): Int? {
        return selectedItemId
    }

    fun setSelectedItemId(itemId: Int?) {
        selectedItemId = itemId
    }

    fun getItemCount(): LiveData<Int> {
        return itemCount
    }

    fun getMappableItems(): LiveData<List<MappableSelectItem>> {
        return mappableItems
    }

    fun setItems(itemCount: Int, mappableItems: List<MappableSelectItem>) {
        this.mappableItems.value = mappableItems
        this.itemCount.value = itemCount
    }

    fun setMapTitle(title: String) {
        this.mapTitle.value = title
    }
}

data class MappableSelectItem(
    val id: Long,
    val latitude: Double,
    val longitude: Double,
    val smallIcon: Int,
    val largeIcon: Int,
    val name: String,
    val status: IconifiedText,
    val info: String?,
    val action: IconifiedText?
) {
    data class IconifiedText(val icon: Int, val text: String)
}

internal class SelectionSummarySheet(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    val binding =
        SelectionSummarySheetLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    var listener: Listener? = null

    private var itemId: Long? = null

    init {
        binding.action.setOnClickListener {
            itemId?.let { listener?.selectionAction(it) }
        }
    }

    fun setItem(item: MappableSelectItem) {
        itemId = item.id

        binding.name.text = item.name

        binding.statusIcon.setImageDrawable(ContextCompat.getDrawable(context, item.status.icon))
        binding.statusIcon.background = null
        binding.statusText.text = item.status.text

        if (item.info != null) {
            binding.info.text = item.info
            binding.info.visibility = View.VISIBLE
            binding.action.visibility = View.GONE
        } else if (item.action != null) {
            binding.action.text = item.action.text
            binding.action.chipIcon = ContextCompat.getDrawable(context, item.action.icon)
            binding.action.visibility = View.VISIBLE
            binding.info.visibility = View.GONE
        }
    }

    interface Listener {
        fun selectionAction(id: Long)
    }
}
