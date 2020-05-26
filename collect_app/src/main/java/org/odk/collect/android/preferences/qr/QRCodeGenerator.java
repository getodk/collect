package org.odk.collect.android.preferences.qr;

import android.graphics.Bitmap;

import androidx.core.util.Pair;

import com.google.zxing.WriterException;

import org.json.JSONException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;


public interface QRCodeGenerator {

    Pair<Bitmap, String> getQRCode(Collection<String> selectedPasswordKeys) throws JSONException, NoSuchAlgorithmException, IOException, WriterException;
}
