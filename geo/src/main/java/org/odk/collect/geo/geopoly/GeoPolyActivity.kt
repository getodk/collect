/*
 * Copyright (C) 2018 Nafundi
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
package org.odk.collect.geo.geopoly

import android.os.Bundle
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.externalapp.ExternalAppUtils
import org.odk.collect.geo.Constants.EXTRA_READ_ONLY
import org.odk.collect.geo.Constants.EXTRA_RETAIN_MOCK_ACCURACY
import org.odk.collect.geo.R
import org.odk.collect.geo.geopoly.GeoPolyFragment.REQUEST_GEOPOLY
import org.odk.collect.strings.localization.LocalizedActivity

class GeoPolyActivity : LocalizedActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        supportFragmentManager.fragmentFactory = FragmentFactoryBuilder()
            .forClass(GeoPolyFragment::class) {
                GeoPolyFragment(
                    intent.getSerializableExtra(OUTPUT_MODE_KEY) as OutputMode?,
                    intent.getBooleanExtra(EXTRA_READ_ONLY, false),
                    intent.getBooleanExtra(EXTRA_RETAIN_MOCK_ACCURACY, false),
                    intent.getParcelableArrayListExtra(EXTRA_POLYGON)
                )
            }
            .build()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_geopoly_layout)

        supportFragmentManager.setFragmentResultListener(REQUEST_GEOPOLY, this) { _, bundle ->
            val geotrace = bundle.getString(GeoPolyFragment.RESULT_GEOTRACE)
            if (geotrace != null) {
                ExternalAppUtils.returnSingleValue(this, geotrace)
            } else {
                finish()
            }
        }
    }

    enum class OutputMode {
        GEOTRACE, GEOSHAPE
    }

    companion object {
        const val EXTRA_POLYGON: String = "answer"
        const val OUTPUT_MODE_KEY: String = "output_mode"
    }
}
