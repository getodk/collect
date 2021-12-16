package org.odk.collect.geo

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.async.Cancellable
import org.odk.collect.async.Scheduler
import org.odk.collect.externalapp.ExternalAppUtils
import org.odk.collect.strings.localization.LocalizedActivity
import javax.inject.Inject

class GeoPointActivityNew : LocalizedActivity() {

    @Inject
    lateinit var geoPointViewModelFactory: GeoPointViewModelFactory

    @Inject
    lateinit var scheduler: Scheduler

    private lateinit var viewModel: GeoPointViewModel
    private lateinit var repeat: Cancellable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as GeoDependencyComponentProvider).geoDependencyComponent.inject(this)

        viewModel =
            ViewModelProvider(this, geoPointViewModelFactory).get(GeoPointViewModel::class.java)

        viewModel.accuracyThreshold =
            intent.getDoubleExtra(EXTRA_ACCURACY_THRESHOLD, Double.MAX_VALUE)
    }

    override fun onResume() {
        super.onResume()
        repeat = scheduler.repeat(::checkLocation, 1000)
    }

    override fun onPause() {
        super.onPause()
        repeat.cancel()
    }

    private fun checkLocation() {
        viewModel.location?.let {
            ExternalAppUtils.returnSingleValue(
                this,
                GeoUtils.formatLocationResultString(it)
            )
        }
    }

    companion object {
        const val EXTRA_ACCURACY_THRESHOLD = "extra_accuracy_threshold"
    }
}
