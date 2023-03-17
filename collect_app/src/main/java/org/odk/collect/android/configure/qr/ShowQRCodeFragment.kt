/*
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
package org.odk.collect.android.configure.qr

import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.OnHierarchyChangeListener
import androidx.appcompat.widget.AppCompatCheckedTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.android.R
import org.odk.collect.android.databinding.ShowQrcodeFragmentBinding
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.async.Scheduler
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.settings.keys.ProtectedProjectKeys
import javax.inject.Inject

class ShowQRCodeFragment : Fragment() {
    @Inject
    lateinit var qrCodeGenerator: QRCodeGenerator

    @Inject
    lateinit var settingsProvider: SettingsProvider

    @Inject
    lateinit var scheduler: Scheduler

    @Inject
    lateinit var appConfigurationGenerator: AppConfigurationGenerator

    lateinit var binding: ShowQrcodeFragmentBinding

    private lateinit var qrCodeViewModel: QRCodeViewModel

    private val checkedItems = booleanArrayOf(true, true)
    private val passwordsSet = booleanArrayOf(true, true)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ShowQrcodeFragmentBinding.inflate(inflater)
        binding.tvPasswordWarning.setOnClickListener { passwordWarningClicked() }

        setHasOptionsMenu(true)
        passwordsSet[0] = settingsProvider.getProtectedSettings().getString(ProtectedProjectKeys.KEY_ADMIN_PW)!!.isNotEmpty()
        passwordsSet[1] = settingsProvider.getUnprotectedSettings().getString(ProjectKeys.KEY_PASSWORD)!!.isNotEmpty()

        qrCodeViewModel.bitmap.observe(this.viewLifecycleOwner) { bitmap: Bitmap? ->
            if (bitmap != null) {
                binding.circularProgressBar.visibility = View.GONE
                binding.ivQRcode.visibility = View.VISIBLE
                binding.ivQRcode.setImageBitmap(bitmap)
            } else {
                binding.circularProgressBar.visibility = View.VISIBLE
                binding.ivQRcode.visibility = View.GONE
            }
        }

        qrCodeViewModel.warning.observe(this.viewLifecycleOwner) { warning: Int? ->
            if (warning != null) {
                binding.tvPasswordWarning.setText(warning)
                binding.status.visibility = View.VISIBLE
            } else {
                binding.status.visibility = View.GONE
            }
        }
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)

        qrCodeViewModel = ViewModelProvider(
            requireActivity(),
            QRCodeViewModel.Factory(
                qrCodeGenerator,
                appConfigurationGenerator,
                settingsProvider,
                scheduler
            )
        )[QRCodeViewModel::class.java]
    }

    private fun passwordWarningClicked() {
        val items = arrayOf(
            getString(R.string.admin_password),
            getString(R.string.server_password)
        )
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(R.string.include_password_dialog)
            .setMultiChoiceItems(
                items,
                checkedItems
            ) { _: DialogInterface?, which: Int, isChecked: Boolean ->
                checkedItems[which] = isChecked
            }
            .setCancelable(false)
            .setPositiveButton(R.string.generate) { dialog: DialogInterface, _: Int ->
                qrCodeViewModel.setIncludedKeys(
                    selectedPasswordKeys
                )
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
            .create()
            .apply {
                // disable checkbox if password not set
                listView.setOnHierarchyChangeListener(
                    object : OnHierarchyChangeListener {
                        override fun onChildViewAdded(parent: View, child: View) {
                            val text = (child as AppCompatCheckedTextView).text
                            val itemIndex = listOf(*items).indexOf(text)
                            if (!passwordsSet[itemIndex]) {
                                child.setEnabled(passwordsSet[itemIndex])
                                child.setOnClickListener(null)
                            }
                        }

                        override fun onChildViewRemoved(view: View, view1: View) {}
                    }
                )
            }
            .show()
    }

    // adding the selected password keys
    private val selectedPasswordKeys: Collection<String>
        get() {
            val keys: MutableCollection<String> = ArrayList()

            // adding the selected password keys
            if (checkedItems[0]) {
                keys.add(ProtectedProjectKeys.KEY_ADMIN_PW)
            }
            if (checkedItems[1]) {
                keys.add(ProjectKeys.KEY_PASSWORD)
            }
            return keys
        }
}
