package org.odk.collect.android.preferences.qr;

import android.graphics.Bitmap;

import java.util.Collection;

import io.reactivex.Observable;


public interface QRCodeGenerator {
    Observable<Bitmap> generateQRCode(Collection<String> selectedPasswordKeys);
}
