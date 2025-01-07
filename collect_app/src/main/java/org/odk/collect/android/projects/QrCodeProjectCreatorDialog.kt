package org.odk.collect.android.projects

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import com.google.zxing.client.android.BeepManager
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CaptureManager
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.R
import org.odk.collect.android.activities.ActivityUtils
import org.odk.collect.android.analytics.AnalyticsEvents
import org.odk.collect.android.databinding.QrCodeProjectCreatorDialogLayoutBinding
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.mainmenu.MainMenuActivity
import org.odk.collect.android.utilities.CodeCaptureManagerFactory
import org.odk.collect.android.views.BarcodeViewDecoder
import org.odk.collect.androidshared.system.IntentLauncher
import org.odk.collect.androidshared.ui.DialogFragmentUtils
import org.odk.collect.androidshared.ui.ToastUtils
import org.odk.collect.androidshared.ui.ToastUtils.showShortToast
import org.odk.collect.androidshared.ui.enableIconsVisibility
import org.odk.collect.androidshared.utils.CompressionUtils
import org.odk.collect.material.MaterialFullScreenDialogFragment
import org.odk.collect.permissions.PermissionListener
import org.odk.collect.permissions.PermissionsProvider
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.qrcode.QRCodeDecoder
import org.odk.collect.settings.ODKAppSettingsImporter
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.importing.SettingsImportingResult
import timber.log.Timber
import javax.inject.Inject

class QrCodeProjectCreatorDialog :
    MaterialFullScreenDialogFragment(),
    DuplicateProjectConfirmationDialog.DuplicateProjectConfirmationListener {

    @Inject
    lateinit var codeCaptureManagerFactory: CodeCaptureManagerFactory

    @Inject
    lateinit var barcodeViewDecoder: BarcodeViewDecoder

    @Inject
    lateinit var permissionsProvider: PermissionsProvider

    @Inject
    lateinit var projectCreator: ProjectCreator

    @Inject
    lateinit var projectsDataService: ProjectsDataService

    @Inject
    lateinit var projectsRepository: ProjectsRepository

    @Inject
    lateinit var settingsProvider: SettingsProvider

    lateinit var settingsConnectionMatcher: SettingsConnectionMatcher

    private var capture: CaptureManager? = null

    private lateinit var beepManager: BeepManager
    lateinit var binding: QrCodeProjectCreatorDialogLayoutBinding

    @Inject
    lateinit var qrCodeDecoder: QRCodeDecoder

    @Inject
    lateinit var settingsImporter: ODKAppSettingsImporter

    @Inject
    lateinit var intentLauncher: IntentLauncher

    private var savedInstanceState: Bundle? = null

    private val imageQrCodeImportResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val imageUri: Uri? = result.data?.data
            if (imageUri != null) {
                permissionsProvider.requestReadUriPermission(
                    requireActivity(),
                    imageUri,
                    requireActivity().contentResolver,
                    object : PermissionListener {
                        override fun granted() {
                            // Do not call from a fragment that does not exist anymore https://github.com/getodk/collect/issues/4741
                            if (isAdded) {
                                requireActivity().contentResolver.openInputStream(imageUri).use {
                                    val settingsJson = try {
                                        qrCodeDecoder.decode(it)
                                    } catch (e: QRCodeDecoder.QRCodeInvalidException) {
                                        showShortToast(
                                            requireContext(),
                                            org.odk.collect.strings.R.string.invalid_qrcode
                                        )
                                        ""
                                    } catch (e: QRCodeDecoder.QRCodeNotFoundException) {
                                        showShortToast(
                                            requireContext(),
                                            org.odk.collect.strings.R.string.qr_code_not_found
                                        )
                                        ""
                                    }
                                    createProjectOrError(settingsJson)
                                }
                            }
                        }
                    }
                )
            }
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)

        settingsConnectionMatcher = SettingsConnectionMatcher(projectsRepository, settingsProvider)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        this.savedInstanceState = savedInstanceState

        binding = QrCodeProjectCreatorDialogLayoutBinding.inflate(inflater)
        binding.toolbarLayout.toolbar.setTitle(org.odk.collect.strings.R.string.add_project)

        configureMenu()

        binding.configureManuallyButton.setOnClickListener {
            DialogFragmentUtils.showIfNotShowing(
                ManualProjectCreatorDialog::class.java,
                requireActivity().supportFragmentManager
            )
        }
        binding.cancelButton.setOnClickListener {
            dismiss()
        }
        beepManager = BeepManager(requireActivity())

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        permissionsProvider.requestCameraPermission(
            requireActivity(),
            object : PermissionListener {
                override fun granted() {
                    // Do not call from a fragment that does not exist anymore https://github.com/getodk/collect/issues/4741
                    if (isAdded) {
                        startScanning(savedInstanceState)
                    }
                }
            }
        )
    }

    private fun configureMenu() {
        val toolbar = binding.toolbarLayout.toolbar
        toolbar.inflateMenu(R.menu.qr_code_scan_menu)

        val menu = toolbar.menu
        menu.enableIconsVisibility()

        menu.removeItem(R.id.menu_item_share)

        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_item_scan_sd_card -> {
                    val photoPickerIntent = Intent(Intent.ACTION_GET_CONTENT)
                    photoPickerIntent.type = "image/*"
                    intentLauncher.launchForResult(
                        imageQrCodeImportResultLauncher,
                        photoPickerIntent
                    ) {
                        showShortToast(
                            requireContext(),
                            getString(
                                org.odk.collect.strings.R.string.activity_not_found,
                                getString(org.odk.collect.strings.R.string.choose_image)
                            )
                        )
                        Timber.w(
                            getString(
                                org.odk.collect.strings.R.string.activity_not_found,
                                getString(org.odk.collect.strings.R.string.choose_image)
                            )
                        )
                    }
                }
            }
            false
        }
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

    override fun getToolbar(): Toolbar? {
        return binding.toolbarLayout.toolbar
    }

    private fun startScanning(savedInstanceState: Bundle?) {
        capture = codeCaptureManagerFactory.getCaptureManager(
            requireActivity(),
            binding.barcodeView,
            savedInstanceState,
            listOf(IntentIntegrator.QR_CODE)
        )

        barcodeViewDecoder.waitForBarcode(binding.barcodeView).observe(
            viewLifecycleOwner
        ) { barcodeResult: BarcodeResult ->
            try {
                beepManager.playBeepSoundAndVibrate()
            } catch (e: Exception) {
                // ignore because beeping isn't essential and this can crash the whole app
            }

            val settingsJson = try {
                CompressionUtils.decompress(barcodeResult.text)
            } catch (e: Exception) {
                showShortToast(
                    requireContext(),
                    getString(org.odk.collect.strings.R.string.invalid_qrcode)
                )
                ""
            }
            createProjectOrError(settingsJson)
        }
    }

    private fun createProjectOrError(settingsJson: String) {
        settingsConnectionMatcher.getProjectWithMatchingConnection(settingsJson)?.let { uuid ->
            val confirmationArgs = Bundle()
            confirmationArgs.putString(
                DuplicateProjectConfirmationKeys.SETTINGS_JSON,
                settingsJson
            )
            confirmationArgs.putString(DuplicateProjectConfirmationKeys.MATCHING_PROJECT, uuid)
            DialogFragmentUtils.showIfNotShowing(
                DuplicateProjectConfirmationDialog::class.java,
                confirmationArgs,
                childFragmentManager
            )
        } ?: run {
            createProject(settingsJson)
        }
    }

    override fun createProject(settingsJson: String) {
        when (projectCreator.createNewProject(settingsJson)) {
            SettingsImportingResult.SUCCESS -> {
                Analytics.log(AnalyticsEvents.QR_CREATE_PROJECT)

                ActivityUtils.startActivityAndCloseAllOthers(activity, MainMenuActivity::class.java)
                ToastUtils.showLongToast(
                    requireContext(),
                    getString(
                        org.odk.collect.strings.R.string.switched_project,
                        projectsDataService.getCurrentProject().name
                    )
                )
            }

            SettingsImportingResult.INVALID_SETTINGS -> ToastUtils.showLongToast(
                requireContext(),
                getString(
                    org.odk.collect.strings.R.string.invalid_qrcode
                )
            )

            SettingsImportingResult.GD_PROJECT -> ToastUtils.showLongToast(
                requireContext(),
                getString(
                    org.odk.collect.strings.R.string.settings_with_gd_protocol
                )
            )
        }
    }

    override fun switchToProject(uuid: String) {
        projectsDataService.setCurrentProject(uuid)
        ActivityUtils.startActivityAndCloseAllOthers(activity, MainMenuActivity::class.java)
        ToastUtils.showLongToast(
            requireContext(),
            getString(
                org.odk.collect.strings.R.string.switched_project,
                projectsDataService.getCurrentProject().name
            )
        )
    }
}
