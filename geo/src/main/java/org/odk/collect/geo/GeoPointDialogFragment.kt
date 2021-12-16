package org.odk.collect.geo

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.async.Cancellable
import org.odk.collect.async.Scheduler
import org.odk.collect.geo.databinding.GeopointDialogNewBinding
import org.odk.collect.location.Location
import java.text.DecimalFormat
import javax.inject.Inject

class GeoPointDialogFragment : DialogFragment() {

    @Inject
    lateinit var geoPointViewModelFactory: GeoPointViewModelFactory

    @Inject
    lateinit var scheduler: Scheduler

    var listener: Listener? = null

    private lateinit var binding: GeopointDialogNewBinding
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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = GeopointDialogNewBinding.inflate(LayoutInflater.from(context))

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
    }

    override fun onResume() {
        super.onResume()
        repeat = scheduler.repeat(
            {
                binding.currentAccuracy.text = viewModel.currentAccuracy.let {
                    if (it == null) {
                        "--"
                    } else {
                        "${DecimalFormat("#.##").format(viewModel.currentAccuracy)}m"
                    }
                }

                checkLocation()
            },
            1000
        )
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
