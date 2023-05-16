package org.odk.collect.android.mainmenu

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.android.R
import org.odk.collect.android.activities.ActivityUtils
import org.odk.collect.android.activities.CrashHandlerActivity
import org.odk.collect.android.activities.DeleteSavedFormActivity
import org.odk.collect.android.activities.FirstLaunchActivity
import org.odk.collect.android.activities.FormDownloadListActivity
import org.odk.collect.android.activities.InstanceChooserList
import org.odk.collect.android.activities.InstanceUploaderListActivity
import org.odk.collect.android.activities.WebViewActivity
import org.odk.collect.android.activities.viewmodels.CurrentProjectViewModel
import org.odk.collect.android.application.MapboxClassInstanceCreator.createMapBoxInitializationFragment
import org.odk.collect.android.application.MapboxClassInstanceCreator.isMapboxAvailable
import org.odk.collect.android.databinding.MainMenuBinding
import org.odk.collect.android.formlists.blankformlist.BlankFormListActivity
import org.odk.collect.android.gdrive.GoogleDriveActivity
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.projects.ProjectIconView
import org.odk.collect.android.projects.ProjectSettingsDialog
import org.odk.collect.android.utilities.ApplicationConstants
import org.odk.collect.android.utilities.PlayServicesChecker
import org.odk.collect.android.utilities.ThemeUtils
import org.odk.collect.androidshared.ui.DialogFragmentUtils.showIfNotShowing
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard.allowClick
import org.odk.collect.crashhandler.CrashHandler
import org.odk.collect.permissions.PermissionListener
import org.odk.collect.permissions.PermissionsProvider
import org.odk.collect.projects.Project.Saved
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.strings.localization.LocalizedActivity
import javax.inject.Inject

class MainMenuActivity : LocalizedActivity() {

    @Inject
    lateinit var viewModelFactory: MainMenuViewModel.Factory

    @Inject
    lateinit var currentProjectViewModelFactory: CurrentProjectViewModel.Factory

    @Inject
    lateinit var settingsProvider: SettingsProvider

    @Inject
    lateinit var permissionsProvider: PermissionsProvider

    private lateinit var binding: MainMenuBinding
    private lateinit var mainMenuViewModel: MainMenuViewModel
    private lateinit var currentProjectViewModel: CurrentProjectViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        initSplashScreen()
        super.onCreate(savedInstanceState)
        binding = MainMenuBinding.inflate(layoutInflater)

        CrashHandler.getInstance(this)?.also {
            if (it.hasCrashed(this)) {
                ActivityUtils.startActivityAndCloseAllOthers(this, CrashHandlerActivity::class.java)
                return
            }
        }

        ThemeUtils(this).setDarkModeForCurrentProject()
        DaggerUtils.getComponent(this).inject(this)

        mainMenuViewModel = ViewModelProvider(this, viewModelFactory)[MainMenuViewModel::class.java]
        currentProjectViewModel = ViewModelProvider(
            this,
            currentProjectViewModelFactory
        )[CurrentProjectViewModel::class.java]

        if (!currentProjectViewModel.hasCurrentProject()) {
            ActivityUtils.startActivityAndCloseAllOthers(this, FirstLaunchActivity::class.java)
            return
        }

        setContentView(binding.root)

        currentProjectViewModel.currentProject.observe(this) { (_, name): Saved ->
            invalidateOptionsMenu()
            title = name
        }

        initToolbar()
        initMapbox()
        initButtons()
        initAppName()

        permissionsProvider.requestPermissions(
            this,
            object : PermissionListener {
                override fun granted() { }
            },
            Manifest.permission.POST_NOTIFICATIONS
        )
    }

    override fun onResume() {
        super.onResume()
        currentProjectViewModel.refresh()
        mainMenuViewModel.refreshInstances()
        setButtonsVisibility()
        manageGoogleDriveDeprecationBanner()
    }

    private fun setButtonsVisibility() {
        binding.reviewData.visibility =
            if (mainMenuViewModel.shouldEditSavedFormButtonBeVisible()) View.VISIBLE else View.GONE
        binding.sendData.visibility =
            if (mainMenuViewModel.shouldSendFinalizedFormButtonBeVisible()) View.VISIBLE else View.GONE
        binding.viewSentForms.visibility =
            if (mainMenuViewModel.shouldViewSentFormButtonBeVisible()) View.VISIBLE else View.GONE
        binding.getForms.visibility =
            if (mainMenuViewModel.shouldGetBlankFormButtonBeVisible()) View.VISIBLE else View.GONE
        binding.manageForms.visibility =
            if (mainMenuViewModel.shouldDeleteSavedFormButtonBeVisible()) View.VISIBLE else View.GONE
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val projectsMenuItem = menu.findItem(R.id.projects)
        (projectsMenuItem.actionView as ProjectIconView).apply {
            project = currentProjectViewModel.currentProject.value
            setOnClickListener { onOptionsItemSelected(projectsMenuItem) }
            contentDescription = getString(R.string.projects)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (!allowClick(javaClass.name)) {
            return true
        }
        if (item.itemId == R.id.projects) {
            showIfNotShowing(ProjectSettingsDialog::class.java, supportFragmentManager)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initSplashScreen() {
        /*
        We don't need the `installSplashScreen` call on Android 12+ (the system handles the
        splash screen for us) and it causes problems if we later switch between dark/light themes
        with the ThemeUtils#setDarkModeForCurrentProject call.
        */
        if (Build.VERSION.SDK_INT < 31) {
            installSplashScreen()
        } else {
            setTheme(R.style.Theme_Collect)
        }
    }

    private fun initToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    private fun initMapbox() {
        if (isMapboxAvailable()) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.map_box_initialization_fragment, createMapBoxInitializationFragment()!!)
                .commit()
        }
    }

    private fun initButtons() {
        binding.enterData.setOnClickListener {
            startActivity(Intent(this, BlankFormListActivity::class.java))
        }

        binding.reviewData.setOnClickListener {
            startActivity(
                Intent(this, InstanceChooserList::class.java).apply {
                    putExtra(
                        ApplicationConstants.BundleKeys.FORM_MODE,
                        ApplicationConstants.FormModes.EDIT_SAVED
                    )
                }
            )
        }

        binding.sendData.setOnClickListener {
            startActivity(Intent(this, InstanceUploaderListActivity::class.java))
        }

        binding.viewSentForms.setOnClickListener {
            startActivity(
                Intent(this, InstanceChooserList::class.java).apply {
                    putExtra(
                        ApplicationConstants.BundleKeys.FORM_MODE,
                        ApplicationConstants.FormModes.VIEW_SENT
                    )
                }
            )
        }

        binding.getForms.setOnClickListener(
            View.OnClickListener {
                val protocol =
                    settingsProvider.getUnprotectedSettings().getString(ProjectKeys.KEY_PROTOCOL)
                val intent =
                    if (protocol.equals(ProjectKeys.PROTOCOL_GOOGLE_SHEETS, ignoreCase = true)) {
                        if (PlayServicesChecker().isGooglePlayServicesAvailable(this@MainMenuActivity)) {
                            Intent(
                                applicationContext,
                                GoogleDriveActivity::class.java
                            )
                        } else {
                            PlayServicesChecker().showGooglePlayServicesAvailabilityErrorDialog(this@MainMenuActivity)
                            return@OnClickListener
                        }
                    } else {
                        Intent(
                            applicationContext,
                            FormDownloadListActivity::class.java
                        )
                    }
                startActivity(intent)
            }
        )

        binding.manageForms.setOnClickListener {
            startActivity(Intent(this, DeleteSavedFormActivity::class.java))
        }

        mainMenuViewModel.sendableInstancesCount.observe(this) { finalized: Int ->
            binding.sendData.setNumberOfForms(finalized)
        }
        mainMenuViewModel.editableInstancesCount.observe(this) { unsent: Int ->
            binding.reviewData.setNumberOfForms(unsent)
        }
        mainMenuViewModel.sentInstancesCount.observe(this) { sent: Int ->
            binding.viewSentForms.setNumberOfForms(sent)
        }
    }

    private fun initAppName() {
        binding.appName.text = String.format(
            "%s %s",
            getString(R.string.collect_app_name),
            mainMenuViewModel.version
        )

        val versionSHA = mainMenuViewModel.versionCommitDescription
        if (versionSHA != null) {
            binding.versionSha.text = versionSHA
        } else {
            binding.versionSha.visibility = View.GONE
        }
    }

    private fun manageGoogleDriveDeprecationBanner() {
        val unprotectedSettings = settingsProvider.getUnprotectedSettings()
        val protocol = unprotectedSettings.getString(ProjectKeys.KEY_PROTOCOL)
        if (ProjectKeys.PROTOCOL_GOOGLE_SHEETS == protocol) {
            val gdBannerAlreadyDismissed =
                unprotectedSettings.getBoolean(ProjectKeys.GOOGLE_DRIVE_DEPRECATION_BANNER_DISMISSED)
            if (!gdBannerAlreadyDismissed) {
                binding.googleDriveDeprecationBanner.root.visibility =
                    View.VISIBLE
                val gdLearnMoreAlreadyClicked =
                    unprotectedSettings.getBoolean(ProjectKeys.GOOGLE_DRIVE_DEPRECATION_LEARN_MORE_CLICKED)
                if (gdLearnMoreAlreadyClicked) {
                    binding.googleDriveDeprecationBanner.dismissButton.visibility = View.VISIBLE
                } else {
                    binding.googleDriveDeprecationBanner.dismissButton.visibility = View.GONE
                }
                binding.googleDriveDeprecationBanner.learnMoreButton.setOnClickListener {
                    val intent = Intent(this, WebViewActivity::class.java)
                    intent.putExtra("url", "https://forum.getodk.org/t/40097")
                    startActivity(intent)
                    unprotectedSettings.save(
                        ProjectKeys.GOOGLE_DRIVE_DEPRECATION_LEARN_MORE_CLICKED,
                        true
                    )
                }
                binding.googleDriveDeprecationBanner.dismissButton.setOnClickListener {
                    binding.googleDriveDeprecationBanner.root.visibility =
                        View.GONE
                    unprotectedSettings.save(
                        ProjectKeys.GOOGLE_DRIVE_DEPRECATION_BANNER_DISMISSED,
                        true
                    )
                }
            }
        } else {
            binding.googleDriveDeprecationBanner.root.visibility = View.GONE
        }
    }
}
