package org.odk.collect.geo

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.androidshared.ui.DialogFragmentUtils
import org.odk.collect.async.Cancellable
import org.odk.collect.async.Scheduler
import org.odk.collect.externalapp.ExternalAppUtils
import org.odk.collect.location.Location
import org.odk.collect.strings.localization.LocalizedActivity
import javax.inject.Inject

class GeoPointActivityNew : LocalizedActivity(), GeoPointDialogFragment.Listener {

    @Inject
    lateinit var geoPointViewModelFactory: GeoPointViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as GeoDependencyComponentProvider).geoDependencyComponent.inject(this)

        val viewModel =
            ViewModelProvider(this, geoPointViewModelFactory).get(GeoPointViewModel::class.java)

        viewModel.accuracyThreshold =
            this.intent.getDoubleExtra(
                EXTRA_ACCURACY_THRESHOLD,
                Double.MAX_VALUE
            )

        DialogFragmentUtils.showIfNotShowing(
            GeoPointDialogFragment::class.java,
            supportFragmentManager
        )
    }

    override fun onLocationAvailable(location: Location) {
        ExternalAppUtils.returnSingleValue(this, GeoUtils.formatLocationResultString(location))
    }

    companion object {
        const val EXTRA_ACCURACY_THRESHOLD = "extra_accuracy_threshold"
    }
}

class GeoPointDialogFragment : DialogFragment() {

    @Inject
    lateinit var geoPointViewModelFactory: GeoPointViewModelFactory

    @Inject
    lateinit var scheduler: Scheduler

    var listener: Listener? = null

    private lateinit var viewModel: GeoPointViewModel
    private lateinit var repeat: Cancellable

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val component =
            (context.applicationContext as GeoDependencyComponentProvider).geoDependencyComponent
        component.inject(this)

        listener = context as? Listener

        viewModel =
            ViewModelProvider(
                requireActivity(),
                geoPointViewModelFactory
            ).get(GeoPointViewModel::class.java)
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
            listener?.onLocationAvailable(it)
        }
    }

    interface Listener {
        fun onLocationAvailable(location: Location)
    }
}
