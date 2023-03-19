package org.odk.collect.android.configure.qr

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.util.Pair
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.android.R
import org.odk.collect.androidshared.bitmap.ImageFileUtils
import org.odk.collect.async.Scheduler
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.settings.keys.ProtectedProjectKeys
import org.odk.collect.shared.settings.Settings

class QRCodeViewModel(
    private val qrCodeGenerator: QRCodeGenerator,
    private val appConfigurationGenerator: AppConfigurationGenerator,
    private val generalSettings: Settings,
    private val adminSettings: Settings,
    private val scheduler: Scheduler
) : ViewModel() {
    private val qrCodeFilePath = MutableLiveData<String?>(null)
    private val qrCodeBitmap = MutableLiveData<Bitmap?>(null)
    private val _warning = MutableLiveData<Int?>()
    val warning: LiveData<Int?> = _warning
    private var includedKeys: Collection<String> = listOf(ProtectedProjectKeys.KEY_ADMIN_PW, ProjectKeys.KEY_PASSWORD)

    val filePath: LiveData<String?>
        get() = qrCodeFilePath
    val bitmap: LiveData<Bitmap?>
        get() = qrCodeBitmap

    init {
        generateQRCode()
    }

    fun setIncludedKeys(includedKeys: Collection<String>) {
        this.includedKeys = includedKeys
        generateQRCode()
    }

    private fun generateQRCode() {
        scheduler.immediate(
            {
                try {
                    val filePath =
                        qrCodeGenerator.generateQRCode(includedKeys, appConfigurationGenerator)
                    val options = BitmapFactory.Options()
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888
                    val bitmap = ImageFileUtils.getBitmap(filePath, options)
                    return@immediate Pair(filePath, bitmap)
                } catch (ignored: Exception) {
                    // Ignored
                }
                null
            }
        ) { qrCode: Pair<String, Bitmap>? ->
            qrCodeFilePath.value = qrCode!!.first
            qrCodeBitmap.value = qrCode.second
            val serverPasswordSet = generalSettings.getString(ProjectKeys.KEY_PASSWORD)!!.isNotEmpty()
            val adminPasswordSet = adminSettings.getString(ProtectedProjectKeys.KEY_ADMIN_PW)!!.isNotEmpty()
            if (serverPasswordSet || adminPasswordSet) {
                if (serverPasswordSet && includedKeys.contains(ProjectKeys.KEY_PASSWORD) && adminPasswordSet && includedKeys.contains(
                        ProtectedProjectKeys.KEY_ADMIN_PW
                    )
                ) {
                    _warning.setValue(R.string.qrcode_with_both_passwords)
                } else if (serverPasswordSet && includedKeys.contains(ProjectKeys.KEY_PASSWORD)) {
                    _warning.setValue(R.string.qrcode_with_server_password)
                } else if (adminPasswordSet && includedKeys.contains(ProtectedProjectKeys.KEY_ADMIN_PW)) {
                    _warning.setValue(R.string.qrcode_with_admin_password)
                } else {
                    _warning.setValue(R.string.qrcode_without_passwords)
                }
            } else {
                _warning.setValue(null)
            }
        }
    }

    class Factory constructor(
        private val qrCodeGenerator: QRCodeGenerator,
        private val appConfigurationGenerator: AppConfigurationGenerator,
        private val settingsProvider: SettingsProvider,
        private val scheduler: Scheduler
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return QRCodeViewModel(
                qrCodeGenerator,
                appConfigurationGenerator,
                settingsProvider.getUnprotectedSettings(),
                settingsProvider.getProtectedSettings(),
                scheduler
            ) as T
        }
    }
}
