package org.odk.collect.maplibre

import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.settings.keys.ProjectKeys.KEY_MAPBOX_MAP_STYLE
import org.odk.collect.strings.R

object Configurations {
    val all = mapOf(
        ProjectKeys.BASEMAP_SOURCE_MAPLIBRE to Configuration(
            name = R.string.basemap_source_maplibre,
            styleSetting = KEY_MAPBOX_MAP_STYLE,
            styleOptions = mapOf(
                "mapbox://styles/mapbox/streets-v11" to StyleOption(
                    name = R.string.streets,
                    BasemapUri.Mapbox("mapbox://styles/mapbox/streets-v11")
                ),
                "mapbox://styles/mapbox/light-v10" to StyleOption(
                    name = R.string.light,
                    BasemapUri.Mapbox("mapbox://styles/mapbox/light-v10")
                ),
                "mapbox://styles/mapbox/dark-v10" to StyleOption(
                    name = R.string.dark,
                    BasemapUri.Mapbox("mapbox://styles/mapbox/dark-v10")
                ),
                "mapbox://styles/mapbox/satellite-v9" to StyleOption(
                    name = R.string.satellite,
                    BasemapUri.Mapbox("mapbox://styles/mapbox/satellite-v9")
                ),
                "mapbox://styles/mapbox/satellite-streets-v11" to StyleOption(
                    name = R.string.hybrid,
                    BasemapUri.Mapbox("mapbox://styles/mapbox/satellite-streets-v11")
                ),
                "mapbox://styles/mapbox/outdoors-v11" to StyleOption(
                    name = R.string.outdoors,
                    BasemapUri.Mapbox("mapbox://styles/mapbox/outdoors-v11")
                )
            )
        ),
        ProjectKeys.BASEMAP_SOURCE_OSM to Configuration(
            name = R.string.basemap_source_osm,
            attribution = "© OpenStreetMap contributors",
            uri = BasemapUri.Raster("https://tile.openstreetmap.org/{z}/{x}/{y}.png")
        ),
        ProjectKeys.BASEMAP_SOURCE_USGS to Configuration(
            name = R.string.basemap_source_usgs,
            attribution = "Map services and data available from U.S. Geological Survey, National Geospatial Program.",
            styleSetting = ProjectKeys.KEY_USGS_MAP_STYLE,
            styleOptions = mapOf(
                "topographic" to StyleOption(
                    name = R.string.topographic,
                    BasemapUri.Raster("https://basemap.nationalmap.gov/arcgis/rest/services/USGSTopo/MapServer/tile/{z}/{y}/{x}")
                ),
                "hybrid" to StyleOption(
                    name = R.string.hybrid,
                    BasemapUri.Raster("https://basemap.nationalmap.gov/arcgis/rest/services/USGSImageryTopo/MapServer/tile/{z}/{y}/{x}")
                ),
                "satellite" to StyleOption(
                    name = R.string.satellite,
                    BasemapUri.Raster("https://basemap.nationalmap.gov/arcgis/rest/services/USGSImageryOnly/MapServer/tile/{z}/{y}/{x}")
                )
            )
        ),
        ProjectKeys.BASEMAP_SOURCE_CARTO to Configuration(
            name = R.string.basemap_source_carto,
            attribution = "© OpenStreetMap contributors, © CARTO",
            styleSetting = ProjectKeys.KEY_CARTO_MAP_STYLE,
            styleOptions = mapOf(
                "positron" to StyleOption(
                    name = R.string.carto_map_style_positron,
                    BasemapUri.Raster("https://1.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png")
                ),
                "dark_matter" to StyleOption(
                    name = R.string.carto_map_style_dark_matter,
                    BasemapUri.Raster("https://1.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}.png")
                )
            )
        ),
    )
}

class Configuration(
    val name: Int,
    val attribution: String? = null,
    val uri: BasemapUri? = null,
    val styleSetting: String? = null,
    val styleOptions: Map<String, StyleOption> = emptyMap()
)

class StyleOption(val name: Int, val uri: BasemapUri)

sealed class BasemapUri(val value: String) {
    class Raster(uri: String) : BasemapUri(uri)
    class Mapbox(uri: String) : BasemapUri(uri)
}
