package org.odk.collect.android.activities

import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.core.text.color
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.analytics.AnalyticsEvents
import org.odk.collect.android.databinding.FirstLaunchLayoutBinding
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.mainmenu.MainMenuActivity
import org.odk.collect.android.projects.ManualProjectCreatorDialog
import org.odk.collect.android.projects.ProjectsDataService
import org.odk.collect.android.projects.QrCodeProjectCreatorDialog
import org.odk.collect.android.version.VersionInformation
import org.odk.collect.androidshared.system.ContextUtils.getThemeAttributeValue
import org.odk.collect.androidshared.ui.DialogFragmentUtils
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.strings.localization.LocalizedActivity
import javax.inject.Inject

class FirstLaunchActivity : LocalizedActivity() {

    @Inject
    lateinit var projectsRepository: ProjectsRepository

    @Inject
    lateinit var versionInformation: VersionInformation

    @Inject
    lateinit var projectsDataService: ProjectsDataService

    @Inject
    lateinit var settingsProvider: SettingsProvider

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerUtils.getComponent(this).inject(this)

        FirstLaunchLayoutBinding.inflate(layoutInflater).apply {
            setContentView(this.root)

            configureViaQrButton.setOnClickListener {
                DialogFragmentUtils.showIfNotShowing(
                    QrCodeProjectCreatorDialog::class.java,
                    supportFragmentManager
                )
            }

            configureManuallyButton.setOnClickListener {
                DialogFragmentUtils.showIfNotShowing(
                    ManualProjectCreatorDialog::class.java,
                    supportFragmentManager
                )
            }

            appName.text = String.format(
                "%s %s",
                getString(org.odk.collect.strings.R.string.collect_app_name),
                versionInformation.versionToDisplay
            )

            dontHaveServer.apply {
                text = SpannableStringBuilder()
                    .append(getString(org.odk.collect.strings.R.string.dont_have_project))
                    .append(" ")
                    .color(getThemeAttributeValue(context, com.google.android.material.R.attr.colorAccent)) {
                        append(getString(org.odk.collect.strings.R.string.try_demo))
                    }

                setOnClickListener {
                    Analytics.log(AnalyticsEvents.TRY_DEMO)

                    projectsRepository.save(Project.DEMO_PROJECT)
                    projectsDataService.setCurrentProject(Project.DEMO_PROJECT_ID)

                    ActivityUtils.startActivityAndCloseAllOthers(
                        this@FirstLaunchActivity,
                        MainMenuActivity::class.java
                    )
                }
            }
        }
    }
}
