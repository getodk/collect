package org.odk.collect.geo.items

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.odk.collect.geo.support.FakeMapFragment
import org.odk.collect.geo.support.MappableItemsFixtures
import org.odk.collect.maps.MapPoint

class MappableItemsDelegateTest {

    @Test
    fun `#updateFeatures and then #zoomToFitItems zooms to fit updated features`() {
        val mappableItemsDelegate = MappableItemsDelegate()
        val map = FakeMapFragment()

        mappableItemsDelegate.updateFeatures(
            map,
            listOf(MappableItemsFixtures.point(point = MapPoint(0.0, 0.0)))
        )

        mappableItemsDelegate.updateFeatures(
            map,
            listOf(MappableItemsFixtures.point(point = MapPoint(1.0, 1.0)))
        )

        mappableItemsDelegate.zoomToFitItems(map)
        assertThat(map.getZoomBoundingBox()!!.first, equalTo(listOf(MapPoint(1.0, 1.0))))
    }
}