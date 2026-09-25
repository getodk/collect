package org.odk.collect.maplibre

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.viewModelFactory
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.gestures.MoveGestureDetector
import org.maplibre.android.gestures.StandardScaleGestureDetector
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMap.OnMapClickListener
import org.maplibre.android.maps.MapLibreMap.OnMapLongClickListener
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.plugins.annotation.CircleManager
import org.maplibre.android.plugins.annotation.FillManager
import org.maplibre.android.plugins.annotation.LineManager
import org.maplibre.android.plugins.annotation.Symbol
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.scalebar.ScaleBarOptions
import org.maplibre.android.plugins.scalebar.ScaleBarPlugin
import org.maplibre.android.style.layers.Layer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.TransitionOptions
import org.maplibre.android.style.sources.Source
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapFragment.ErrorListener
import org.odk.collect.maps.MapFragment.FeatureListener
import org.odk.collect.maps.MapFragment.PointListener
import org.odk.collect.maps.MapFragment.ReadyListener
import org.odk.collect.maps.MapPoint
import org.odk.collect.maps.MapViewModel
import org.odk.collect.maps.MapViewModelMapFragment
import org.odk.collect.maps.Zoom
import org.odk.collect.maps.ZoomObserver
import org.odk.collect.maps.circles.CircleDescription
import org.odk.collect.maps.layers.ReferenceLayerRepository
import org.odk.collect.maps.markers.MarkerDescription
import org.odk.collect.maps.markers.MarkerIconCreator
import org.odk.collect.maps.markers.MarkerIconDescription
import org.odk.collect.maps.traces.LineDescription
import org.odk.collect.maps.traces.PolygonDescription
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys.KEY_MAPBOX_MAP_STYLE
import org.odk.collect.shared.settings.Settings
import java.io.File
import javax.inject.Inject

class MapLibreMapFragment(private val configuration: Configuration) :
    MapViewModelMapFragment(),
    OnMapClickListener,
    OnMapLongClickListener {

    constructor(configuration: String) : this(Configurations.all.getValue(configuration))

    private lateinit var settings: Settings
    private lateinit var mapView: MapView
    private var map: MapLibreMap? = null
    private val styleIcons = StyleIcons { map?.style }

    private var backgroundLineManager: LineManager? = null
    private var lineManager: LineManager? = null
    private var backgroundFillManager: FillManager? = null
    private var fillManager: FillManager? = null
    private var circleManager: CircleManager? = null
    private var symbolManager: SymbolManager? = null
    private var annotationManagersCreated = false
    private var mapReadyListener: ReadyListener? = null

    private var nextFeatureId = 1
    private val features = mutableMapOf<Int, MapFeature>()
    private var clickListener: PointListener? = null
    private var longPressListener: PointListener? = null

    private var featureClickListener: FeatureListener? = null
    private var featureDragEndListener: FeatureListener? = null
    private val referenceLayers = ReferenceLayers()
    private var referenceLayerFile: File? = null
    private var basemapTopLayer: String? = null
    private var awaitingRemoteStyle = false

    private val scaleListener = object : MapLibreMap.OnScaleListener {
        override fun onScaleBegin(detector: StandardScaleGestureDetector) = Unit
        override fun onScale(detector: StandardScaleGestureDetector) = Unit

        override fun onScaleEnd(detector: StandardScaleGestureDetector) {
            getMapViewModel().onUserZoom(getCenter(), getZoom())
        }
    }

    private val moveListener = object : MapLibreMap.OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) = Unit
        override fun onMove(detector: MoveGestureDetector) = Unit

        override fun onMoveEnd(detector: MoveGestureDetector) {
            getMapViewModel().onUserMove(getCenter(), getZoom())
        }
    }

    private val _mapViewModel by viewModels<MapViewModel> {
        viewModelFactory {
            addInitializer(MapViewModel::class) {
                MapViewModel(
                    settingsProvider.getUnprotectedSettings(),
                    settingsProvider.getMetaSettings(),
                    referenceLayerRepository
                )
            }
        }
    }

    @Inject
    lateinit var settingsProvider: SettingsProvider

    @Inject
    lateinit var referenceLayerRepository: ReferenceLayerRepository

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context.applicationContext as MapLibreDependencyComponentProvider)
            .mapLibreDependencyComponent
            .inject(this)
    }

    override fun init(readyListener: ReadyListener?, errorListener: ErrorListener?) {
        mapReadyListener = readyListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        MapLibreSupport.initialize(requireContext())

        mapView = MapView(requireContext())
        mapView.getMapAsync { map -> onMapReady(map) }

        return mapView
    }

    private fun onMapReady(map: MapLibreMap) {
        this.map = map

        map.uiSettings.apply {
            compassGravity = Gravity.TOP or Gravity.START
            setCompassMargins(36, 36, 36, 36)
            isTiltGesturesEnabled = false
        }

        ScaleBarPlugin(mapView, map).create(ScaleBarOptions(requireContext()))

        // MapLibre allows zooming in to 25 by default, while its predecessor Mapbox allowed 22.
        // Allowing more than 22 can cause rendering problems.
        map.setMaxZoomPreference(22.0)

        map.addOnMapClickListener(this)
        map.addOnMapLongClickListener(this)

        map.addOnScaleListener(scaleListener)
        map.addOnMoveListener(moveListener)

        mapView.addOnDidFailLoadingMapListener {
            if (awaitingRemoteStyle) {
                loadEmptyStyle()
            }
        }

        moveOrAnimateCamera(MapFragment.INITIAL_CENTER, false, MapFragment.INITIAL_ZOOM.toDouble())

        getMapViewModel().getSettings(setOf(KEY_MAPBOX_MAP_STYLE)).observe(viewLifecycleOwner) {
            settings = it
            loadStyle(settings)
        }

        getMapViewModel().getReferenceLayer().observe(viewLifecycleOwner) {
            referenceLayerFile = it
            loadStyle(settings)
        }

        getMapViewModel().zoom.observe(
            viewLifecycleOwner,
            object : ZoomObserver() {
                override fun onZoomToPoint(zoom: Zoom.Point) {
                    moveOrAnimateCamera(zoom.point, zoom.animate, zoom.level)
                }

                override fun onZoomToBox(zoom: Zoom.Box) {
                    zoomToBox(zoom)
                }
            }
        )
    }

    override fun onDestroyView() {
        backgroundLineManager?.onDestroy()
        lineManager?.onDestroy()
        backgroundFillManager?.onDestroy()
        fillManager?.onDestroy()
        circleManager?.onDestroy()
        symbolManager?.onDestroy()

        map?.apply {
            removeOnMapClickListener(this@MapLibreMapFragment)
            removeOnMapLongClickListener(this@MapLibreMapFragment)
            removeOnScaleListener(scaleListener)
            removeOnMoveListener(moveListener)
        }

        mapView.onDestroy()
        map = null

        backgroundLineManager = null
        lineManager = null
        backgroundFillManager = null
        fillManager = null
        circleManager = null
        symbolManager = null
        annotationManagersCreated = false

        super.onDestroyView()
    }

    override fun onDestroy() {
        referenceLayers.destroy()
        MarkerIconCreator.clearCache()
        super.onDestroy()
    }

    private fun loadStyle(settings: Settings) {
        awaitingRemoteStyle = false

        when (val uri = configuration.basemapUri(settings)) {
            is BasemapUri.Raster -> {
                map?.setStyle(configuration.rasterBasemapStyle(uri)) {
                    basemapTopLayer = "basemap_layer"
                    onStyleLoaded(it)
                }
            }

            is BasemapUri.Mapbox -> {
                loadRemoteStyle(uri.value)
            }
        }
    }

    private fun loadRemoteStyle(styleUri: String) {
        awaitingRemoteStyle = true

        map?.setStyle(styleUri) {
            awaitingRemoteStyle = false
            basemapTopLayer = it.layers.lastOrNull()?.id
            onStyleLoaded(it)
        }
    }

    private fun loadEmptyStyle() {
        awaitingRemoteStyle = false

        map?.setStyle(Style.Builder()) {
            basemapTopLayer = null
            onStyleLoaded(it)
        }
    }

    private fun onStyleLoaded(style: Style) {
        // Avoid markers flickering while they are added or dragged.
        style.transition = TransitionOptions(style.transition.duration, style.transition.delay, false)

        styleIcons.onStyleLoaded(style)

        if (!annotationManagersCreated) {
            val map = this.map ?: return

            val symbolManager = SymbolManager(mapView, map, style).apply {
                iconAllowOverlap = true
                iconIgnorePlacement = true
            }
            val lineManager = LineManager(mapView, map, style, symbolManager.layerId, null).apply {
                setLineCap(Property.LINE_CAP_ROUND)
            }
            val fillManager = FillManager(mapView, map, style, lineManager.layerId, null)
            val backgroundLineManager =
                LineManager(mapView, map, style, fillManager.layerId, null).apply {
                    setLineCap(Property.LINE_CAP_ROUND)
                }
            val backgroundFillManager =
                FillManager(mapView, map, style, backgroundLineManager.layerId, null)

            val circleManager = CircleManager(mapView, map, style, symbolManager.layerId, null)

            this.symbolManager = symbolManager
            this.circleManager = circleManager
            this.fillManager = fillManager
            this.lineManager = lineManager
            this.backgroundFillManager = backgroundFillManager
            this.backgroundLineManager = backgroundLineManager

            annotationManagersCreated = true

            // The map becomes usable only once the annotation managers exist. The style loads
            // asynchronously, so the fragment could already be detached by now.
            if (activity != null) {
                mapReadyListener?.onReady(this)
            }
        }

        loadReferenceOverlay()
    }

    override fun getCenter(): MapPoint {
        val target = map?.cameraPosition?.target ?: return MapFragment.INITIAL_CENTER
        return MapPoint(target.latitude, target.longitude)
    }

    override fun getZoom(): Double {
        return map?.cameraPosition?.zoom ?: MapFragment.INITIAL_ZOOM.toDouble()
    }

    override fun getMapViewModel(): MapViewModel {
        return _mapViewModel
    }

    override fun updateMarker(
        featureId: Int,
        markerDescription: MarkerDescription
    ) {
        val symbolManager = this.symbolManager ?: return

        features[featureId]?.dispose()
        val symbol = MapUtils.createSymbol(
            symbolManager,
            styleIcons,
            requireContext(),
            markerDescription
        )

        addMarker(featureId, markerDescription, symbol, symbolManager)
    }

    override fun addMarkers(markers: List<MarkerDescription>): List<Int> {
        val symbolManager = this.symbolManager ?: return emptyList()

        val symbols = MapUtils.createSymbols(requireContext(), symbolManager, styleIcons, markers)

        val featureIds = mutableListOf<Int>()
        markers.asSequence()
            .zip(symbols.asSequence())
            .forEach { (marker, symbol) ->
                val featureId = nextFeatureId++
                featureIds.add(featureId)

                addMarker(featureId, marker, symbol, symbolManager)
            }

        return featureIds
    }

    private fun addMarker(
        featureId: Int,
        marker: MarkerDescription,
        symbol: Symbol,
        symbolManager: SymbolManager
    ) {
        val markerFeature = MarkerFeature(
            requireContext(),
            styleIcons,
            symbolManager,
            symbol,
            featureId,
            featureClickListener,
            featureDragEndListener,
            marker.point
        )

        features[featureId] = markerFeature
    }

    override fun setMarkerIcon(featureId: Int, markerIconDescription: MarkerIconDescription) {
        val feature = features[featureId]
        if (feature is MarkerFeature) {
            feature.setIcon(markerIconDescription)
        }
    }

    override fun getMarkerPoint(featureId: Int): MapPoint? {
        val feature = features[featureId]
        return if (feature is MarkerFeature) {
            feature.point
        } else {
            null
        }
    }

    override fun addPolyLine(lineDescription: LineDescription): Int {
        val featureId = nextFeatureId++
        addPolyLine(featureId, lineDescription)
        return featureId
    }

    override fun updatePolyLine(featureId: Int, lineDescription: LineDescription) {
        features[featureId]?.dispose()
        addPolyLine(featureId, lineDescription)
    }

    private fun addPolyLine(
        featureId: Int,
        lineDescription: LineDescription
    ) {
        val lineManager = lineManager(lineDescription.background) ?: return

        if (lineDescription.draggable) {
            val symbolManager = this.symbolManager ?: return

            features[featureId] = DynamicPolyLineFeature(
                requireContext(),
                styleIcons,
                symbolManager,
                lineManager,
                featureId,
                featureClickListener,
                featureDragEndListener,
                lineDescription
            )
        } else {
            features[featureId] = StaticPolyLineFeature(
                lineManager,
                featureId,
                featureClickListener,
                lineDescription
            )
        }
    }

    override fun addPolygon(polygonDescription: PolygonDescription): Int {
        val featureId = nextFeatureId++
        addPolygon(featureId, polygonDescription)

        return featureId
    }

    private fun addPolygon(
        featureId: Int,
        polygonDescription: PolygonDescription
    ) {
        val fillManager = fillManager(polygonDescription.background) ?: return
        val lineManager = lineManager(polygonDescription.background) ?: return

        if (polygonDescription.draggable) {
            val symbolManager = this.symbolManager ?: return

            features[featureId] = DynamicPolygonFeature(
                requireContext(),
                styleIcons,
                symbolManager,
                fillManager,
                lineManager,
                featureId,
                featureClickListener,
                featureDragEndListener,
                polygonDescription
            )
        } else {
            features[featureId] = StaticPolygonFeature(
                fillManager,
                lineManager,
                polygonDescription,
                featureClickListener,
                featureId
            )
        }
    }

    override fun updatePolygon(
        featureId: Int,
        polygonDescription: PolygonDescription
    ) {
        features[featureId]?.dispose()
        addPolygon(featureId, polygonDescription)
    }

    override fun addCircle(circleDescription: CircleDescription): Int {
        val featureId = nextFeatureId++
        addCircle(featureId, circleDescription)
        return featureId
    }

    override fun updateCircle(
        featureId: Int,
        circleDescription: CircleDescription
    ) {
        features[featureId]?.dispose()
        addCircle(featureId, circleDescription)
    }

    private fun addCircle(
        featureId: Int,
        circleDescription: CircleDescription
    ) {
        val map = this.map ?: return
        val circleManager = this.circleManager ?: return

        features[featureId] = CircleFeature(map, circleManager, circleDescription)
    }

    private fun lineManager(background: Boolean): LineManager? {
        return if (background) backgroundLineManager else lineManager
    }

    private fun fillManager(background: Boolean): FillManager? {
        return if (background) backgroundFillManager else fillManager
    }

    override fun getPolyPoints(featureId: Int): List<MapPoint> {
        val feature = features[featureId]
        return if (feature is LineFeature) {
            feature.points
        } else {
            emptyList()
        }
    }

    override fun clearFeatures() {
        for (feature in features.values) {
            feature.dispose()
        }

        features.clear()
        nextFeatureId = 1
    }

    override fun clearFeatures(ids: List<Int>) {
        ids.forEach { features.remove(it)?.dispose() }
    }

    override fun setClickListener(listener: PointListener?) {
        clickListener = listener
    }

    override fun setLongPressListener(listener: PointListener?) {
        longPressListener = listener
    }

    override fun setFeatureClickListener(listener: FeatureListener?) {
        featureClickListener = listener
    }

    override fun setDragEndListener(listener: FeatureListener?) {
        featureDragEndListener = listener
    }

    override fun onMapClick(point: LatLng): Boolean {
        clickListener?.onPoint(MapPoint(point.latitude, point.longitude))

        // onMapClick is called before the annotation click listeners, which means that every click
        // on a marker will also cause a click event on the map. Returning true will consume the
        // event and prevent the marker's listener from ever being called, so we have to return
        // false.
        return false
    }

    override fun onMapLongClick(point: LatLng): Boolean {
        longPressListener?.onPoint(MapPoint(point.latitude, point.longitude))
        return true
    }

    private fun moveOrAnimateCamera(point: MapPoint, animate: Boolean, zoom: Double = getZoom()) {
        val update = CameraUpdateFactory.newLatLngZoom(
            LatLng(point.latitude, point.longitude),
            zoom
        )

        if (animate) {
            map?.animateCamera(update, 300)
        } else {
            map?.moveCamera(update)
        }
    }

    private fun zoomToBox(zoom: Zoom.Box) {
        // MapLibre can't build a bounding box from a single point
        if (zoom.box.size < 2) {
            val point = zoom.box.firstOrNull()
            if (point != null) {
                moveOrAnimateCamera(point, zoom.animate, MapFragment.POINT_ZOOM.toDouble())
            }
        } else {
            val map = this.map ?: return
            val points = zoom.box.map { LatLng(it.latitude, it.longitude) }
            val bounds = LatLngBounds.Builder().includes(points).build()

            zoomToBounds(map, bounds, zoom.scaleFactor, zoom.animate)
        }
    }

    private fun zoomToBounds(
        map: MapLibreMap,
        bounds: LatLngBounds,
        scaleFactor: Double,
        animate: Boolean
    ) {
        // A scaleFactor of 0.8 means the box may occupy at most 80% of the viewport, so 10% of it
        // is left as a margin on each side.
        val horizontalPadding = (mapView.width * (1 - scaleFactor) / 2).toInt()
        val verticalPadding = (mapView.height * (1 - scaleFactor) / 2).toInt()

        val update = CameraUpdateFactory.newLatLngBounds(
            bounds,
            horizontalPadding,
            verticalPadding,
            horizontalPadding,
            verticalPadding
        )

        if (animate) {
            map.animateCamera(update, 300)
        } else {
            map.moveCamera(update)
        }
    }

    private fun loadReferenceOverlay() {
        referenceLayerFile?.let {
            val (source, layers) = referenceLayers.sourceAndLayers(it) ?: return
            addOverlaySource(source)
            layers.forEach { addOverlayLayer(it) }
        }
    }

    private fun addOverlayLayer(layer: Layer) {
        val style = map?.style ?: return
        basemapTopLayer?.let { style.addLayerAbove(layer, it) } ?: style.addLayerAt(layer, 0)
    }

    private fun addOverlaySource(source: Source) {
        val style = map?.style ?: return
        if (style.getSource(source.id) == null) {
            style.addSource(source)
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        mapView.onStop()
        super.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()

        if (::mapView.isInitialized) {
            mapView.onLowMemory()
        }
    }
}
