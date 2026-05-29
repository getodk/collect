package org.odk.collect.mapbox

import com.mapbox.maps.Style
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.settings.keys.ProjectKeys.KEY_MAPBOX_MAP_STYLE

object Configurations {
    val all = mapOf(
        ProjectKeys.BASEMAP_SOURCE_MAPBOX to Configuration(
            setting = KEY_MAPBOX_MAP_STYLE,
            uris = mapOf(
                Style.MAPBOX_STREETS to BasemapUri.Mapbox(Style.MAPBOX_STREETS),
                Style.LIGHT to BasemapUri.Mapbox(Style.LIGHT),
                Style.DARK to BasemapUri.Mapbox(Style.DARK),
                Style.SATELLITE to BasemapUri.Mapbox(Style.SATELLITE),
                Style.SATELLITE_STREETS to BasemapUri.Mapbox(Style.SATELLITE_STREETS),
                Style.OUTDOORS to BasemapUri.Mapbox(Style.OUTDOORS)
            )
        ),
        ProjectKeys.BASEMAP_SOURCE_OSM to Configuration(
            attribution = "© OpenStreetMap contributors",
            uri = BasemapUri.Raster("https://tile.openstreetmap.org/{z}/{x}/{y}.png")
        ),
        ProjectKeys.BASEMAP_SOURCE_USGS to Configuration(
            attribution = "Map services and data available from U.S. Geological Survey, National Geospatial Program.",
            setting = ProjectKeys.KEY_USGS_MAP_STYLE,
            uris = mapOf(
                "topographic" to BasemapUri.Raster("https://basemap.nationalmap.gov/arcgis/rest/services/USGSTopo/MapServer/tile/{z}/{y}/{x}"),
                "hybrid" to BasemapUri.Raster("https://basemap.nationalmap.gov/arcgis/rest/services/USGSImageryTopo/MapServer/tile/{z}/{y}/{x}"),
                "satellite" to BasemapUri.Raster("https://basemap.nationalmap.gov/arcgis/rest/services/USGSImageryOnly/MapServer/tile/{z}/{y}/{x}")
            )
        ),
        ProjectKeys.BASEMAP_SOURCE_CARTO to Configuration(
            attribution = "© OpenStreetMap contributors, © CARTO",
            setting = ProjectKeys.KEY_CARTO_MAP_STYLE,
            uris = mapOf(
                "positron" to BasemapUri.Raster("http://1.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png"),
                "dark_matter" to BasemapUri.Raster("http://1.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}.png")
            )
        ),
    )
}

class Configuration(
    val attribution: String? = null,
    val uri: BasemapUri? = null,
    val setting: String? = null,
    val uris: Map<String, BasemapUri> = emptyMap()
)

sealed class BasemapUri(val value: String) {
    class Raster(uri: String) : BasemapUri(uri)
    class Mapbox(uri: String) : BasemapUri(uri)
}