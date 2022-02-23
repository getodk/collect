package org.odk.collect.geo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import org.odk.collect.androidshared.ui.ToastUtils
import org.odk.collect.geo.databinding.SelectionSummarySheetLayoutBinding
import org.odk.collect.geo.maps.MapFragment
import org.odk.collect.geo.maps.MapFragment.ReadyListener
import org.odk.collect.geo.maps.MapFragmentFactory
import org.odk.collect.geo.maps.MapPoint
import org.odk.collect.permissions.PermissionsProvider
import org.odk.collect.strings.localization.LocalizedActivity
import javax.inject.Inject

abstract class SelectionMapActivity : LocalizedActivity() {

    @Inject
    lateinit var mapFragmentFactory: MapFragmentFactory

    @Inject
    lateinit var permissionsProvider: PermissionsProvider

    lateinit var referenceLayerSettingsNavigator: ReferenceLayerSettingsNavigator

    protected val viewModel: SelectionMapViewModel by viewModels()

    private lateinit var map: MapFragment
    private var previousState: Bundle? = null
    private var viewportInitialized = false

    private lateinit var summarySheetBehavior: BottomSheetBehavior<*>
    private lateinit var summarySheet: SelectionSummarySheet

    private val itemsByFeatureId: MutableMap<Int, MappableSelectItem> = mutableMapOf()

    /**
     * Points to be mapped. Note: kept separately from [.itemsByFeatureId] so we can
     * quickly zoom to bounding box.
     */
    private val points: MutableList<MapPoint> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        previousState = savedInstanceState

        val component = (application as GeoDependencyComponentProvider).geoDependencyComponent
        component.inject(this)

        // Subclassing with Dagger is weird and makes the subclass need available bindings
        referenceLayerSettingsNavigator = component.referenceLayerSettingsNavigator
    }

    // Allow subclass to initialization before we set everything up
    protected fun init() {
        if (permissionsProvider.areLocationPermissionsGranted()) {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.selection_map_layout)

            val titleView = findViewById<TextView>(R.id.title)
            titleView.text = getMapTitle()

            val mapToAdd = mapFragmentFactory.createMapFragment(applicationContext)
            if (mapToAdd != null) {
                mapToAdd.addTo(
                    this,
                    R.id.map_container,
                    ReadyListener { newMapFragment ->
                        initMap(newMapFragment)
                    },
                    MapFragment.ErrorListener { finish() }
                )
            } else {
                finish() // The configured map provider is not available
            }

            setUpSummarySheet()
        } else {
            ToastUtils.showLongToast(this, R.string.not_granted_permission)
            finish()
        }
    }

    @SuppressLint("MissingSuperCall") // Super is being called. Bug in Android Studio?
    override fun onSaveInstanceState(state: Bundle) {
        super.onSaveInstanceState(state)

        if (!::map.isInitialized) {
            // initMap() is called asynchronously, so map can be null if the activity
            // is stopped (e.g. by screen rotation) before initMap() gets to run.
            // In this case, preserve any provided instance state.
            if (previousState != null) {
                state.putAll(previousState)
            }
            return
        }

        state.putParcelable(MAP_CENTER_KEY, map.center)
        state.putDouble(MAP_ZOOM_KEY, map.zoom)
    }

    override fun onResume() {
        super.onResume()
        update()
    }

    override fun onBackPressed() {
        if (summarySheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            summarySheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN)
        } else {
            super.onBackPressed()
        }
    }

    protected abstract fun getMapTitle(): String
    protected abstract fun getItemCount(): Int
    protected abstract fun getMappableItems(): List<MappableSelectItem>
    protected abstract fun getNewItemIntent(): Intent

    @SuppressLint("MissingPermission") // Permission handled in Constructor
    open fun initMap(newMapFragment: MapFragment) {
        map = newMapFragment

        findViewById<View>(R.id.zoom_to_location).setOnClickListener {
            map.zoomToPoint(map.gpsLocation, true)
        }

        findViewById<View>(R.id.zoom_to_bounds).setOnClickListener {
            map.zoomToBoundingBox(points, 0.8, false)
        }

        findViewById<View>(R.id.layer_menu).setOnClickListener {
            referenceLayerSettingsNavigator.navigateToReferenceLayerSettings(this)
        }

        findViewById<View>(R.id.new_instance).setOnClickListener {
            startActivity(getNewItemIntent())
        }

        map.setGpsLocationEnabled(true)
        map.setGpsLocationListener { point -> onLocationChanged(point) }

        previousState?.let { restoreFromInstanceState(it) }

        map.setFeatureClickListener { featureId -> onFeatureClicked(featureId) }
        map.setClickListener { onClick() }

        update()

        if (viewModel.getSelectedItemId() != -1) {
            onFeatureClicked(viewModel.getSelectedItemId())
        }
    }

    private fun setUpSummarySheet() {
        summarySheet = findViewById(R.id.submission_summary)
        summarySheetBehavior = BottomSheetBehavior.from(summarySheet)
        summarySheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        summarySheetBehavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                val selectedSubmissionId = viewModel.getSelectedItemId()
                if (newState == BottomSheetBehavior.STATE_HIDDEN && selectedSubmissionId != -1) {
                    map.setMarkerIcon(
                        selectedSubmissionId,
                        itemsByFeatureId[selectedSubmissionId]!!.smallIcon
                    )
                    viewModel.setSelectedItemId(-1)
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        summarySheet.listener = object : SelectionSummarySheet.Listener {
            override fun selectionAction(id: Long) {
                summarySheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

                setResult(
                    RESULT_OK,
                    Intent().also {
                        it.putExtra(EXTRA_SELECTED_ID, id)
                    }
                )
                finish()
            }
        }
    }

    /**
     * Zooms the map to the new location if the map viewport hasn't been initialized yet.
     */
    open fun onLocationChanged(point: MapPoint?) {
        if (!viewportInitialized) {
            map.zoomToPoint(point, true)
            viewportInitialized = true
        }
    }

    protected open fun restoreFromInstanceState(state: Bundle) {
        val mapCenter: MapPoint? = state.getParcelable(MAP_CENTER_KEY)
        val mapZoom = state.getDouble(MAP_ZOOM_KEY)

        if (mapCenter != null) {
            map.zoomToPoint(mapCenter, mapZoom, false)
            viewportInitialized = true // avoid recentering as soon as location is received
        }
    }

    fun onFeatureClicked(featureId: Int) {
        summarySheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN)
        if (!isSummaryForGivenSubmissionDisplayed(featureId)) {
            removeEnlargedMarkerIfExist(featureId)

            val item = itemsByFeatureId.get(featureId)
            if (item != null) {
                map.zoomToPoint(MapPoint(item.latitude, item.longitude), map.zoom, true)
                map.setMarkerIcon(featureId, item.largeIcon)
                summarySheet.setItem(item)
                summarySheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED)
            }

            viewModel.setSelectedItemId(featureId)
        }
    }

    private fun onClick() {
        if (summarySheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            summarySheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN)
        }
    }

    private fun update() {
        if (map == null) {
            return
        }

        updateFeatures()

        if (!viewportInitialized && !points.isEmpty()) {
            map.zoomToBoundingBox(points, 0.8, false)
            viewportInitialized = true
        }

        val statusView = findViewById<TextView>(R.id.geometry_status)
        statusView.text = getString(R.string.geometry_status, getItemCount(), points.size)
    }

    private fun isSummaryForGivenSubmissionDisplayed(newSubmissionId: Int): Boolean {
        return viewModel.getSelectedItemId() == newSubmissionId && summarySheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN
    }

    private fun removeEnlargedMarkerIfExist(newSubmissionId: Int) {
        val selectedSubmissionId = viewModel.getSelectedItemId()
        if (selectedSubmissionId != -1 && selectedSubmissionId != newSubmissionId) {
            map.setMarkerIcon(
                selectedSubmissionId,
                itemsByFeatureId[selectedSubmissionId]!!.smallIcon
            )
        }
    }

    /**
     * Clears the existing features on the map and places features for the current form's instances.
     */
    private fun updateFeatures() {
        points.clear()
        map.clearFeatures()
        itemsByFeatureId.clear()

        val items = getMappableItems()
        for (item in items) {
            val point = MapPoint(item.latitude, item.longitude)
            val featureId = map.addMarker(point, false, MapFragment.BOTTOM)

            map.setMarkerIcon(
                featureId,
                if (featureId == viewModel.getSelectedItemId()) item.largeIcon else item.smallIcon
            )

            itemsByFeatureId.put(featureId, item)
            points.add(point)
        }
    }

    companion object {
        const val EXTRA_SELECTED_ID = "selected_id"

        private const val MAP_CENTER_KEY = "map_center"
        private const val MAP_ZOOM_KEY = "map_zoom"
    }
}

class SelectionMapViewModel : ViewModel() {

    private var selectedItemId = -1

    fun getSelectedItemId(): Int {
        return selectedItemId
    }

    fun setSelectedItemId(itemId: Int) {
        selectedItemId = itemId
    }
}

abstract class SelectItemFromMap<T> : ActivityResultContract<T, Long?>() {

    override fun parseResult(resultCode: Int, intent: Intent?): Long? {
        return if (resultCode == Activity.RESULT_OK) {
            intent?.getLongExtra(SelectionMapActivity.EXTRA_SELECTED_ID, -1)
        } else {
            null
        }
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

class SelectionSummarySheet(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    val binding =
        SelectionSummarySheetLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    var listener: Listener? = null

    private var itemId: Long? = null

    init {
        binding.action.setOnClickListener(::onActionClick)
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

    private fun onActionClick(view: View) {
        itemId?.let { listener?.selectionAction(it) }
    }

    interface Listener {
        fun selectionAction(id: Long)
    }
}
