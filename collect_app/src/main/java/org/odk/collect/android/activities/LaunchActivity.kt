package org.odk.collect.android.activities

import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.strings.localization.LocalizedActivity
import javax.inject.Inject

class LaunchActivity : LocalizedActivity() {

    @Inject
    lateinit var projectsRepository: ProjectsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        DaggerUtils.getComponent(this).inject(this)

        if (projectsRepository.getAll().isNotEmpty()) {
            ActivityUtils.startActivityAndCloseAllOthers(this, MainMenuActivity::class.java)
        } else {
            ActivityUtils.startActivityAndCloseAllOthers(this, FirstLaunchActivity::class.java)
        }
    }
}
