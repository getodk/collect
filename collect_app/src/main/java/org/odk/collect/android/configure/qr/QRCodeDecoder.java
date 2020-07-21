package org.odk.collect.android.configure.qr;

import java.io.InputStream;

public interface QRCodeDecoder {

    String decode(InputStream inputStream) throws InvalidException, NotFoundException;

    class InvalidException extends Exception {
    }

    class NotFoundException extends Exception {
    }
}
