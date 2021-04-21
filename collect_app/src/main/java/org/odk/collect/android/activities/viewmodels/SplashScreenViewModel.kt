package org.odk.collect.android.activities.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.android.projects.ProjectImporter
import org.odk.collect.android.utilities.AppStateProvider
import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.android.utilities.ScreenUtils
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.shared.Settings
import java.io.File

class SplashScreenViewModel(
    private val generalSettings: Settings,
    private val metaSettings: Settings,
    private val projectsRepository: ProjectsRepository,
    private val projectImporter: ProjectImporter,
    private val appStateProvider: AppStateProvider
) : ViewModel() {

    val shouldDisplaySplashScreen
        get() = generalSettings.getBoolean(GeneralKeys.KEY_SHOW_SPLASH)

    val splashScreenLogoFile
        get() = File(generalSettings.getString(GeneralKeys.KEY_SPLASH_PATH) ?: "")

    val scaledSplashScreenLogoBitmap: Bitmap
        get() = FileUtils.getBitmapScaledToDisplay(splashScreenLogoFile, ScreenUtils.getScreenHeight(), ScreenUtils.getScreenWidth())

    val doesLogoFileExist
        get() = splashScreenLogoFile.exists()

    val isFirstLaunch
        get() = appStateProvider.isFreshInstall()

    fun importExistingProjectIfNeeded() {
        if (!isFirstLaunch && projectsRepository.getAll().isEmpty()) {
            projectImporter.importExistingProject()
        }
    }

    open class Factory constructor(
        private val generalSettings: Settings,
        private val metaSettings: Settings,
        private val projectsRepository: ProjectsRepository,
        private val projectImporter: ProjectImporter,
        private val appStateProvider: AppStateProvider
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SplashScreenViewModel(generalSettings, metaSettings, projectsRepository, projectImporter, appStateProvider) as T
        }
    }
}
