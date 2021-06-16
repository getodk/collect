package org.odk.collect.android.configure.qr;

import com.google.zxing.WriterException;

import org.json.JSONException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;


public interface QRCodeGenerator {

    String generateQRCode(Collection<String> selectedPasswordKeys, AppConfigurationGenerator appConfigurationGenerator) throws JSONException, NoSuchAlgorithmException, IOException, WriterException;
}
