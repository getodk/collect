package org.odk.collect.android.activities.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.android.utilities.AppStateProvider
import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.android.utilities.ScreenUtils
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.shared.Settings
import java.io.File

class SplashScreenViewModel(
    private val generalSettings: Settings,
    private val appStateProvider: AppStateProvider,
    private val projectsRepository: ProjectsRepository
) : ViewModel() {

    val shouldDisplaySplashScreen
        get() = generalSettings.getBoolean(GeneralKeys.KEY_SHOW_SPLASH)

    val splashScreenLogoFile
        get() = File(generalSettings.getString(GeneralKeys.KEY_SPLASH_PATH) ?: "")

    val scaledSplashScreenLogoBitmap: Bitmap
        get() = FileUtils.getBitmapScaledToDisplay(splashScreenLogoFile, ScreenUtils.getScreenHeight(), ScreenUtils.getScreenWidth())

    val doesLogoFileExist
        get() = splashScreenLogoFile.exists()

    val shouldFirstLaunchScreenBeDisplayed
        get() = appStateProvider.isFreshInstall() || projectsRepository.getAll().isEmpty()

    open class Factory constructor(
        private val generalSettings: Settings,
        private val appStateProvider: AppStateProvider,
        private val projectsRepository: ProjectsRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SplashScreenViewModel(generalSettings, appStateProvider, projectsRepository) as T
        }
    }
}
