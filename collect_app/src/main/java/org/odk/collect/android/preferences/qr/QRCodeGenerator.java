package org.odk.collect.android.preferences.qr;

import android.graphics.Bitmap;

import org.odk.collect.utilities.Consumer;

import java.util.Collection;

import io.reactivex.Observable;


public interface QRCodeGenerator {

    void generateQRCode(Consumer<String> callback);

    Observable<Bitmap> generateQRCode(Collection<String> selectedPasswordKeys);
}
