package org.odk.collect.android.configure.qr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.odk.collect.android.R;
import org.odk.collect.android.utilities.ImageFileUtils;
import org.odk.collect.shared.Settings;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.async.Scheduler;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.odk.collect.android.preferences.keys.ProtectedProjectKeys.KEY_ADMIN_PW;
import static org.odk.collect.android.preferences.keys.ProjectKeys.KEY_PASSWORD;

class QRCodeViewModel extends ViewModel {

    private final QRCodeGenerator qrCodeGenerator;
    private final AppConfigurationGenerator appConfigurationGenerator;
    private final Settings generalSettings;
    private final Settings adminSettings;
    private final Scheduler scheduler;
    private final MutableLiveData<String> qrCodeFilePath = new MutableLiveData<>(null);
    private final MutableLiveData<Bitmap> qrCodeBitmap = new MutableLiveData<>(null);
    private final MutableLiveData<Integer> warning = new MutableLiveData<>();
    private Collection<String> includedKeys = asList(KEY_ADMIN_PW, KEY_PASSWORD);

    QRCodeViewModel(QRCodeGenerator qrCodeGenerator, AppConfigurationGenerator appConfigurationGenerator,
                    Settings generalSettings, Settings adminSettings, Scheduler scheduler) {
        this.qrCodeGenerator = qrCodeGenerator;
        this.appConfigurationGenerator = appConfigurationGenerator;
        this.generalSettings = generalSettings;
        this.adminSettings = adminSettings;
        this.scheduler = scheduler;

        generateQRCode();
    }

    public LiveData<String> getFilePath() {
        return qrCodeFilePath;
    }

    public LiveData<Bitmap> getBitmap() {
        return qrCodeBitmap;
    }

    public LiveData<Integer> getWarning() {
        return warning;
    }

    public void setIncludedKeys(Collection<String> includedKeys) {
        this.includedKeys = includedKeys;
        generateQRCode();
    }

    private void generateQRCode() {
        scheduler.immediate(
                () -> {
                    try {
                        String filePath = qrCodeGenerator.generateQRCode(includedKeys, appConfigurationGenerator);

                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        Bitmap bitmap = ImageFileUtils.getBitmap(filePath, options);

                        return new Pair<>(filePath, bitmap);
                    } catch (Exception ignored) {
                        // Ignored
                    }

                    return null;
                },
                qrCode -> {
                    qrCodeFilePath.setValue(qrCode.first);
                    qrCodeBitmap.setValue(qrCode.second);

                    boolean serverPasswordSet = !generalSettings.getString(KEY_PASSWORD).isEmpty();
                    boolean adminPasswordSet = !adminSettings.getString(KEY_ADMIN_PW).isEmpty();

                    if (serverPasswordSet || adminPasswordSet) {
                        if (serverPasswordSet && includedKeys.contains(KEY_PASSWORD) && adminPasswordSet && includedKeys.contains(KEY_ADMIN_PW)) {
                            warning.setValue(R.string.qrcode_with_both_passwords);
                        } else if (serverPasswordSet && includedKeys.contains(KEY_PASSWORD)) {
                            warning.setValue(R.string.qrcode_with_server_password);
                        } else if (adminPasswordSet && includedKeys.contains(KEY_ADMIN_PW)) {
                            warning.setValue(R.string.qrcode_with_admin_password);
                        } else {
                            warning.setValue(R.string.qrcode_without_passwords);
                        }
                    } else {
                        warning.setValue(null);
                    }
                }
        );
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final QRCodeGenerator qrCodeGenerator;
        private final AppConfigurationGenerator appConfigurationGenerator;
        private final SettingsProvider settingsProvider;
        private final Scheduler scheduler;

        Factory(QRCodeGenerator qrCodeGenerator, AppConfigurationGenerator appConfigurationGenerator,
                SettingsProvider settingsProvider, Scheduler scheduler) {
            this.qrCodeGenerator = qrCodeGenerator;
            this.appConfigurationGenerator = appConfigurationGenerator;
            this.settingsProvider = settingsProvider;
            this.scheduler = scheduler;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new QRCodeViewModel(
                    qrCodeGenerator,
                    appConfigurationGenerator,
                    settingsProvider.getUnprotectedSettings(),
                    settingsProvider.getProtectedSettings(),
                    scheduler
            );
        }
    }
}
