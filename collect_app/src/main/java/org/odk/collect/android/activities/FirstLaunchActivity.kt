package org.odk.collect.android.activities

import android.os.Bundle
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.R
import org.odk.collect.android.analytics.AnalyticsEvents
import org.odk.collect.android.databinding.FirstLaunchLayoutBinding
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.android.projects.ManualProjectCreatorDialog
import org.odk.collect.android.projects.QrCodeProjectCreatorDialog
import org.odk.collect.android.version.VersionInformation
import org.odk.collect.androidshared.ui.DialogFragmentUtils
import org.odk.collect.androidshared.ui.GroupClickListener.addOnClickListener
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import javax.inject.Inject

class FirstLaunchActivity : CollectAbstractActivity() {

    @Inject
    lateinit var projectsRepository: ProjectsRepository

    @Inject
    lateinit var versionInformation: VersionInformation

    @Inject
    lateinit var currentProjectProvider: CurrentProjectProvider

    private lateinit var binding: FirstLaunchLayoutBinding

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FirstLaunchLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        DaggerUtils.getComponent(this).inject(this)

        binding.configureViaQrButton.setOnClickListener {
            DialogFragmentUtils.showIfNotShowing(
                QrCodeProjectCreatorDialog::class.java,
                supportFragmentManager
            )
        }

        binding.configureManuallyButton.setOnClickListener {
            DialogFragmentUtils.showIfNotShowing(
                ManualProjectCreatorDialog::class.java,
                supportFragmentManager
            )
        }

        binding.appName.text = String.format(
            "%s %s",
            getString(R.string.collect_app_name),
            versionInformation.versionToDisplay
        )

        binding.configureLater.addOnClickListener {
            Analytics.log(AnalyticsEvents.TRY_DEMO)

            projectsRepository.save(Project.DEMO_PROJECT)
            currentProjectProvider.setCurrentProject(Project.DEMO_PROJECT_ID)

            ActivityUtils.startActivityAndCloseAllOthers(this, MainMenuActivity::class.java)
        }
    }
}
