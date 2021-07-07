package org.odk.collect.android.projects

import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.doOnTextChanged
import org.odk.collect.android.R
import org.odk.collect.android.activities.ActivityUtils
import org.odk.collect.android.activities.MainMenuActivity
import org.odk.collect.android.configure.qr.AppConfigurationGenerator
import org.odk.collect.android.databinding.ManualProjectCreatorDialogLayoutBinding
import org.odk.collect.android.gdrive.GoogleAccountsManager
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.listeners.PermissionListener
import org.odk.collect.android.permissions.PermissionsProvider
import org.odk.collect.android.utilities.SoftKeyboardController
import org.odk.collect.android.utilities.ToastUtils
import org.odk.collect.material.MaterialFullScreenDialogFragment
import javax.inject.Inject

class ManualProjectCreatorDialog : MaterialFullScreenDialogFragment() {
    @Inject
    lateinit var projectCreator: ProjectCreator

    @Inject
    lateinit var appConfigurationGenerator: AppConfigurationGenerator

    @Inject
    lateinit var softKeyboardController: SoftKeyboardController

    @Inject
    lateinit var currentProjectProvider: CurrentProjectProvider

    @Inject
    lateinit var permissionsProvider: PermissionsProvider

    @Inject
    lateinit var googleAccountsManager: GoogleAccountsManager

    private lateinit var binding: ManualProjectCreatorDialogLayoutBinding

    val googleAccountResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        val resultData = result.data

        if (result.resultCode == Activity.RESULT_OK && resultData != null && resultData.extras != null) {
            val accountName = resultData.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            googleAccountsManager.selectAccount(accountName)

            val settingsJson = appConfigurationGenerator.getAppConfigurationAsJsonWithGoogleDriveDetails(
                accountName
            )

            projectCreator.createNewProject(settingsJson)
            ActivityUtils.startActivityAndCloseAllOthers(activity, MainMenuActivity::class.java)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ManualProjectCreatorDialogLayoutBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()

        binding.urlInputText.doOnTextChanged { text, _, _, _ ->
            binding.addButton.isEnabled = !text.isNullOrBlank()
        }

        binding.urlInputText.post {
            softKeyboardController.showSoftKeyboard(binding.urlInputText)
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        binding.addButton.setOnClickListener {
            handleAddingNewProject()
        }

        binding.gdrive.setOnClickListener {
            configureGoogleAccount()
        }
    }

    override fun onCloseClicked() {
    }

    override fun onBackPressed() {
        dismiss()
    }

    override fun getToolbar(): Toolbar {
        return binding.toolbar
    }

    private fun setUpToolbar() {
        toolbar.setTitle(R.string.add_project)
        toolbar.navigationIcon = null
    }

    private fun handleAddingNewProject() {
        val settingsJson = appConfigurationGenerator.getAppConfigurationAsJsonWithServerDetails(
            binding.urlInputText.text?.trim().toString(),
            binding.usernameInputText.text?.trim().toString(),
            binding.passwordInputText.text?.trim().toString()
        )

        projectCreator.createNewProject(settingsJson)
        ActivityUtils.startActivityAndCloseAllOthers(activity, MainMenuActivity::class.java)
        ToastUtils.showLongToast(getString(R.string.switched_project, currentProjectProvider.getCurrentProject().name))
    }

    private fun configureGoogleAccount() {
        permissionsProvider.requestGetAccountsPermission(
            activity,
            object : PermissionListener {
                override fun granted() {
                    val intent: Intent = googleAccountsManager.accountChooserIntent
                    googleAccountResultLauncher.launch(intent)
                }

                override fun denied() {
                    // nothing
                }
            }
        )
    }
}
