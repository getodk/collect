package org.odk.collect.android.preferences.qr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.odk.collect.android.utilities.FileUtils;

import java.util.ArrayList;
import java.util.Collection;

import static org.odk.collect.android.preferences.AdminKeys.KEY_ADMIN_PW;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_PASSWORD;

class QRCodeViewModel {

    private final QRCodeGenerator qrCodeGenerator;
    private final MutableLiveData<String> qrCodeFilePath = new MutableLiveData<>(null);
    private final MutableLiveData<Bitmap> qrCodeBitmap = new MutableLiveData<>(null);

    QRCodeViewModel(QRCodeGenerator qrCodeGenerator) {
        this.qrCodeGenerator = qrCodeGenerator;
    }

    public LiveData<String> getFilePath() {
        generateQRCode();
        return qrCodeFilePath;
    }

    public LiveData<Bitmap> getBitmap() {
        generateQRCode();
        return qrCodeBitmap;
    }

    private void generateQRCode() {
        new AsyncTask<Void, Void, Pair<String, Bitmap>>() {

            @Override
            protected Pair<String, Bitmap> doInBackground(Void... voids) {
                try {
                    Collection<String> keys = new ArrayList<>();
                    keys.add(KEY_ADMIN_PW);
                    keys.add(KEY_PASSWORD);

                    String filePath = qrCodeGenerator.generateQRCode(keys);

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
            }
        }.execute();
    }

    void setIncludedKeys(Collection<String> includedKeys) {

    }
}
