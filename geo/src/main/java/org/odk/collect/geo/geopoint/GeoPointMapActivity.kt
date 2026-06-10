/*
 * Copyright (C) 2011 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.odk.collect.geo.geopoint

import android.os.Bundle
import android.view.Window
import org.odk.collect.androidshared.system.BundleExt.getParcelableExtraCompat
import org.odk.collect.androidshared.ui.EdgeToEdge.setView
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.androidshared.ui.ToastUtils.showShortToast
import org.odk.collect.async.Scheduler
import org.odk.collect.externalapp.ExternalAppUtils.returnSingleValue
import org.odk.collect.geo.Constants.EXTRA_DRAGGABLE_ONLY
import org.odk.collect.geo.Constants.EXTRA_READ_ONLY
import org.odk.collect.geo.Constants.EXTRA_RETAIN_MOCK_ACCURACY
import org.odk.collect.geo.GeoActivityUtils.requireLocationPermissions
import org.odk.collect.geo.GeoDependencyComponentProvider
import org.odk.collect.geo.R
import org.odk.collect.maps.MapFragmentFactory
import org.odk.collect.maps.MapPoint
import org.odk.collect.maps.layers.ReferenceLayerRepository
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.strings.localization.LocalizedActivity
import org.odk.collect.webpage.WebPageService
import timber.log.Timber.Forest.e
import javax.inject.Inject

class GeoPointMapActivity : LocalizedActivity() {

    @Inject
    lateinit var mapFragmentFactory: MapFragmentFactory

    @Inject
    lateinit var referenceLayerRepository: ReferenceLayerRepository

    @Inject
    lateinit var scheduler: Scheduler

    @Inject
    lateinit var settingsProvider: SettingsProvider

    @Inject
    lateinit var webPageService: WebPageService

    public override fun onCreate(savedInstanceState: Bundle?) {
        (application as GeoDependencyComponentProvider).geoDependencyComponent.inject(this)
        supportFragmentManager.fragmentFactory = FragmentFactoryBuilder()
            .forClass(GeoPointMapFragment::class) {
                GeoPointMapFragment(
                    inputPoint = intent.extras?.getParcelableExtraCompat<MapPoint>(EXTRA_LOCATION),
                    draggable = intent.getBooleanExtra(EXTRA_DRAGGABLE_ONLY, false),
                    readOnly = intent.getBooleanExtra(EXTRA_READ_ONLY, false),
                    retainMockAccuracy = intent.getBooleanExtra(EXTRA_RETAIN_MOCK_ACCURACY, false)
                )
            }
            .build()

        super.onCreate(savedInstanceState)

        requireLocationPermissions(this)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        try {
            this.setView(R.layout.geopoint_map_activity_layout, false)
        } catch (e: NoClassDefFoundError) {
            e(e, "Google maps not accessible due to: %s ", e.message)
            showShortToast(org.odk.collect.strings.R.string.google_play_services_error_occured)
            finish()
            return
        }

        supportFragmentManager.setFragmentResultListener(
            GeoPointMapFragment.REQUEST_GEOPOINT,
            this
        ) { _, result ->
            val geopoint = result.getString(GeoPointMapFragment.RESULT_GEOPOINT)
            if (geopoint != null) {
                returnSingleValue(this, geopoint)
            } else {
                finish()
            }
        }
    }

    companion object {
        const val EXTRA_LOCATION: String = "gp"
    }
}
