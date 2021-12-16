package org.odk.collect.geo

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.geo.databinding.GeopointDialogNewBinding
import java.text.DecimalFormat
import javax.inject.Inject

class GeoPointDialogFragment : DialogFragment() {

    @Inject
    lateinit var geoPointViewModelFactory: GeoPointViewModelFactory

    var listener: Listener? = null

    private lateinit var binding: GeopointDialogNewBinding
    private lateinit var viewModel: GeoPointViewModel

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

        viewModel.currency.observe(this) {
            binding.currentAccuracy.text = if (it == null) {
                "--"
            } else {
                "${DecimalFormat("#.##").format(it)}m"
            }
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setCancelable(false)
            .setNegativeButton(R.string.cancel) { _, _ -> listener?.onCancel() }
            .create()
    }

    interface Listener {
        fun onCancel()
    }
}
