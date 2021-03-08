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
import org.odk.collect.android.preferences.JsonPreferencesGenerator;
import org.odk.collect.android.preferences.PreferencesDataSource;
import org.odk.collect.android.preferences.PreferencesDataSourceProvider;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.async.Scheduler;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.odk.collect.android.preferences.AdminKeys.KEY_ADMIN_PW;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_PASSWORD;

class QRCodeViewModel extends ViewModel {

    private final QRCodeGenerator qrCodeGenerator;
    private final JsonPreferencesGenerator jsonPreferencesGenerator;
    private final PreferencesDataSource generalPreferences;
    private final PreferencesDataSource adminPreferences;
    private final Scheduler scheduler;
    private final MutableLiveData<String> qrCodeFilePath = new MutableLiveData<>(null);
    private final MutableLiveData<Bitmap> qrCodeBitmap = new MutableLiveData<>(null);
    private final MutableLiveData<Integer> warning = new MutableLiveData<>();
    private Collection<String> includedKeys = asList(KEY_ADMIN_PW, KEY_PASSWORD);

    QRCodeViewModel(QRCodeGenerator qrCodeGenerator, JsonPreferencesGenerator jsonPreferencesGenerator,
                    PreferencesDataSource generalPreferences, PreferencesDataSource adminPreferences, Scheduler scheduler) {
        this.qrCodeGenerator = qrCodeGenerator;
        this.jsonPreferencesGenerator = jsonPreferencesGenerator;
        this.generalPreferences = generalPreferences;
        this.adminPreferences = adminPreferences;
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
                        String filePath = qrCodeGenerator.generateQRCode(includedKeys, jsonPreferencesGenerator);

                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        Bitmap bitmap = FileUtils.getBitmap(filePath, options);

                        return new Pair<>(filePath, bitmap);
                    } catch (Exception ignored) {
                        // Ignored
                    }

                    return null;
                },
                qrCode -> {
                    qrCodeFilePath.setValue(qrCode.first);
                    qrCodeBitmap.setValue(qrCode.second);

                    boolean serverPasswordSet = !generalPreferences.getString(KEY_PASSWORD).isEmpty();
                    boolean adminPasswordSet = !adminPreferences.getString(KEY_ADMIN_PW).isEmpty();

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
        private final JsonPreferencesGenerator jsonPreferencesGenerator;
        private final PreferencesDataSourceProvider preferencesDataSourceProvider;
        private final Scheduler scheduler;

        Factory(QRCodeGenerator qrCodeGenerator, JsonPreferencesGenerator jsonPreferencesGenerator,
                PreferencesDataSourceProvider preferencesDataSourceProvider, Scheduler scheduler) {
            this.qrCodeGenerator = qrCodeGenerator;
            this.jsonPreferencesGenerator = jsonPreferencesGenerator;
            this.preferencesDataSourceProvider = preferencesDataSourceProvider;
            this.scheduler = scheduler;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new QRCodeViewModel(
                    qrCodeGenerator,
                    jsonPreferencesGenerator,
                    preferencesDataSourceProvider.getGeneralPreferences(),
                    preferencesDataSourceProvider.getAdminPreferences(),
                    scheduler
            );
        }
    }
}
