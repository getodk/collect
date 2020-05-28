package org.odk.collect.android.preferences.qr;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.odk.collect.android.R;
import org.odk.collect.android.preferences.PreferencesProvider;
import org.odk.collect.android.utilities.FileUtils;

import java.util.Collection;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.odk.collect.android.preferences.AdminKeys.KEY_ADMIN_PW;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_PASSWORD;

class QRCodeViewModel extends ViewModel {

    private final QRCodeGenerator qrCodeGenerator;
    private final SharedPreferences generalSharedPreferences;
    private final SharedPreferences adminSharedPreferences;
    private final MutableLiveData<String> qrCodeFilePath = new MutableLiveData<>(null);
    private final MutableLiveData<Bitmap> qrCodeBitmap = new MutableLiveData<>(null);
    private final MutableLiveData<Integer> warning = new MutableLiveData<>();
    private Collection<String> includedKeys = asList(KEY_ADMIN_PW, KEY_PASSWORD);

    QRCodeViewModel(QRCodeGenerator qrCodeGenerator, SharedPreferences generalSharedPreferences, SharedPreferences adminSharedPreferences) {
        this.qrCodeGenerator = qrCodeGenerator;
        this.generalSharedPreferences = generalSharedPreferences;
        this.adminSharedPreferences = adminSharedPreferences;
    }

    public LiveData<String> getFilePath() {
        generateQRCode();
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
        new AsyncTask<Void, Void, Pair<String, Bitmap>>() {

            @Override
            protected Pair<String, Bitmap> doInBackground(Void... voids) {
                try {
                    String filePath = qrCodeGenerator.generateQRCode(emptyList());

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = FileUtils.getBitmap(filePath, options);

                    return new Pair<>(filePath, bitmap);
                } catch (Exception ignored) {
                    // Ignored
                }

                return null;
            }

            @Override
            protected void onPostExecute(Pair<String, Bitmap> qrCode) {
                qrCodeFilePath.setValue(qrCode.first);
                qrCodeBitmap.setValue(qrCode.second);

                boolean serverPasswordSet = !generalSharedPreferences.getString(KEY_PASSWORD, "").isEmpty();
                boolean adminPasswordSet = adminSharedPreferences.contains(KEY_ADMIN_PW);

                if (serverPasswordSet && includedKeys.contains(KEY_PASSWORD)) {
                    warning.setValue(R.string.qrcode_with_server_password);
                } else if (adminPasswordSet && includedKeys.contains(KEY_ADMIN_PW)) {
                    warning.setValue(R.string.qrcode_with_admin_password);
                } else {
                    warning.setValue(R.string.qrcode_without_passwords);
                }
            }
        }.execute();
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final QRCodeGenerator qrCodeGenerator;
        private final PreferencesProvider preferencesProvider;

        Factory(QRCodeGenerator qrCodeGenerator, PreferencesProvider preferencesProvider) {
            this.qrCodeGenerator = qrCodeGenerator;
            this.preferencesProvider = preferencesProvider;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new QRCodeViewModel(
                    qrCodeGenerator,
                    preferencesProvider.getGeneralSharedPreferences(),
                    preferencesProvider.getAdminSharedPreferences()
            );
        }
    }
}
