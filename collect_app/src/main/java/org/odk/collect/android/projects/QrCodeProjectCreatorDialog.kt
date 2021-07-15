package org.odk.collect.android.projects

import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.BUTTON_NEGATIVE
import android.content.DialogInterface.BUTTON_POSITIVE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.google.zxing.client.android.BeepManager
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CaptureManager
import org.odk.collect.android.activities.ActivityUtils
import org.odk.collect.android.activities.MainMenuActivity
import org.odk.collect.android.databinding.QrCodeProjectCreatorDialogLayoutBinding
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.listeners.PermissionListener
import org.odk.collect.android.permissions.PermissionsProvider
import org.odk.collect.android.utilities.CodeCaptureManagerFactory
import org.odk.collect.android.utilities.CompressionUtils
import org.odk.collect.android.utilities.DialogUtils
import org.odk.collect.android.utilities.ToastUtils
import org.odk.collect.android.views.BarcodeViewDecoder
import org.odk.collect.material.MaterialFullScreenDialogFragment
import org.odk.collect.projects.R
import javax.inject.Inject

class QrCodeProjectCreatorDialog : MaterialFullScreenDialogFragment(), DialogInterface.OnClickListener {

    private var lastScannedJson: String? = null
    private var lastMatchingUuid: String? = null

    @Inject
    lateinit var codeCaptureManagerFactory: CodeCaptureManagerFactory

    @Inject
    lateinit var barcodeViewDecoder: BarcodeViewDecoder

    @Inject
    lateinit var permissionsProvider: PermissionsProvider

    @Inject
    lateinit var projectCreator: ProjectCreator

    @Inject
    lateinit var currentProjectProvider: CurrentProjectProvider

    @Inject
    lateinit var settingsConnectionMatcher: SettingsConnectionMatcher

    private var capture: CaptureManager? = null

    private lateinit var beepManager: BeepManager
    lateinit var binding: QrCodeProjectCreatorDialogLayoutBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = QrCodeProjectCreatorDialogLayoutBinding.inflate(inflater)
        setUpToolbar()
        binding.configureManuallyButton.setOnClickListener {
            DialogUtils.showIfNotShowing(ManualProjectCreatorDialog::class.java, requireActivity().supportFragmentManager)
        }
        binding.cancelButton.setOnClickListener {
            dismiss()
        }
        beepManager = BeepManager(requireActivity())
        permissionsProvider.requestCameraPermission(
            requireActivity(),
            object : PermissionListener {
                override fun granted() {
                    startScanning(savedInstanceState)
                }

                override fun denied() {
                    dismiss()
                }
            }
        )
        return binding.root
    }

    override fun onCloseClicked() {
    }

    override fun onBackPressed() {
        dismiss()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        capture?.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        super.onPause()
        binding.barcodeView.pauseAndWait()
        capture?.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.barcodeView.resume()
        capture?.onResume()
    }

    override fun onDestroy() {
        capture?.onDestroy()
        super.onDestroy()
    }

    override fun getToolbar(): Toolbar {
        return binding.toolbar
    }

    private fun setUpToolbar() {
        toolbar.setTitle(R.string.add_project)
        toolbar.navigationIcon = null
    }

    private fun startScanning(savedInstanceState: Bundle?) {
        capture = codeCaptureManagerFactory.getCaptureManager(requireActivity(), binding.barcodeView, savedInstanceState, listOf(IntentIntegrator.QR_CODE))

        barcodeViewDecoder.waitForBarcode(binding.barcodeView).observe(
            viewLifecycleOwner,
            { barcodeResult: BarcodeResult ->
                try {
                    beepManager.playBeepSoundAndVibrate()
                } catch (e: Exception) {
                    // ignore
                }

                try {
                    val settingsJson = CompressionUtils.decompress(barcodeResult.text)
                    lastScannedJson = settingsJson

                    settingsConnectionMatcher.getProjectWithMatchingConnection(settingsJson)?.let { uuid ->
                        lastMatchingUuid = uuid
                        DialogUtils.showIfNotShowing(DuplicateProjectConfirmationDialog::class.java, childFragmentManager)
                    } ?: run {
                        createProject(settingsJson)
                    }
                } catch (e: Exception) {
                    ToastUtils.showShortToast(getString(R.string.invalid_qrcode))
                }
            }
        )
    }

    private fun createProject(settingsJson: String) {
        val projectCreatedSuccessfully = projectCreator.createNewProject(settingsJson)

        if (projectCreatedSuccessfully) {
            ActivityUtils.startActivityAndCloseAllOthers(activity, MainMenuActivity::class.java)
            ToastUtils.showLongToast(getString(R.string.switched_project, currentProjectProvider.getCurrentProject().name))
        } else {
            ToastUtils.showLongToast(getString(R.string.invalid_qrcode))
        }
    }

    private fun switchToProject(uuid: String) {
        currentProjectProvider.setCurrentProject(uuid)
        ActivityUtils.startActivityAndCloseAllOthers(activity, MainMenuActivity::class.java)
        ToastUtils.showLongToast(getString(R.string.switched_project, currentProjectProvider.getCurrentProject().name))
    }

    override fun onClick(dialog: DialogInterface?, buttonClicked: Int) {
        when (buttonClicked) {
            BUTTON_POSITIVE -> createProject(lastScannedJson ?: "")
            BUTTON_NEGATIVE -> lastMatchingUuid?.let { switchToProject(it) }
        }
    }
}
