package org.odk.collect.maps

data class LineDescription(
    override val points: List<MapPoint> = emptyList(),
    private val strokeWidth: String? = null,
    private val strokeColor: Int? = null,
    override val highlightLastPoint: Boolean = false,
    val draggable: Boolean = false,
    @Deprecated("Use PolygonDescription instead") val closed: Boolean = false
) : TraceDescription {
    override fun getStrokeWidth(): Float {
        return try {
            strokeWidth?.toFloat()?.let {
                if (it >= 0) {
                    it
                } else {
                    MapConsts.DEFAULT_STROKE_WIDTH
                }
            } ?: MapConsts.DEFAULT_STROKE_WIDTH
        } catch (e: NumberFormatException) {
            MapConsts.DEFAULT_STROKE_WIDTH
        }
    }

    override fun getStrokeColor(): Int {
        return strokeColor ?: MapConsts.DEFAULT_STROKE_COLOR
    }
}
