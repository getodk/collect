package org.odk.collect.android.preferences.qr;

import android.graphics.Bitmap;

import com.google.zxing.WriterException;

import java.io.IOException;
import java.util.Collection;

import io.reactivex.Observable;


public interface QRCodeGenerator {
    Bitmap generateQRBitMap(String data, int sideLength) throws IOException, WriterException;

    Observable<Bitmap> generateQRCode(Collection<String> selectedPasswordKeys);

    String getQrCodeFilepath();

    String getMd5CachePath();
}
