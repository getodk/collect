package org.odk.collect.android.preferences.qr;

import android.graphics.Bitmap;

import com.google.zxing.WriterException;

import org.odk.collect.utilities.Consumer;

import java.io.IOException;
import java.util.Collection;

import io.reactivex.Observable;


public interface QRCodeGenerator {

    void generateQRCode(Consumer<String> callback);

    Observable<Bitmap> generateQRCode(Collection<String> selectedPasswordKeys);

    Bitmap generateQRBitMap(String data, int sideLength) throws IOException, WriterException;

    String getQRCodeFilepath();

    String getMd5CachePath();
}
