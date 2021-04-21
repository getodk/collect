package org.odk.collect.android.activities.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.android.preferences.keys.MetaKeys
import org.odk.collect.android.preferences.source.Settings
import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.android.utilities.ScreenUtils
import java.io.File

class SplashScreenViewModel(
    private val generalSettings: Settings,
    private val metaSettings: Settings,
) : ViewModel() {

    val shouldDisplaySplashScreen
        get() = generalSettings.getBoolean(GeneralKeys.KEY_SHOW_SPLASH)

    val splashScreenLogoFile
        get() = File(generalSettings.getString(GeneralKeys.KEY_SPLASH_PATH) ?: "")

    val scaledSplashScreenLogoBitmap: Bitmap
        get() = FileUtils.getBitmapScaledToDisplay(splashScreenLogoFile, ScreenUtils.getScreenHeight(), ScreenUtils.getScreenWidth())

    val doesLogoFileExist
        get() = splashScreenLogoFile.exists()

    fun shouldFirstLaunchDialogBeDisplayed(): Boolean {
        val isFirstLaunch = !metaSettings.contains(MetaKeys.KEY_FIRST_LAUNCH)
        metaSettings.save(MetaKeys.KEY_FIRST_LAUNCH, false)
        return isFirstLaunch
    }

    open class Factory constructor(private val generalSettings: Settings, private val metaSettings: Settings) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SplashScreenViewModel(generalSettings, metaSettings) as T
        }
    }
}
