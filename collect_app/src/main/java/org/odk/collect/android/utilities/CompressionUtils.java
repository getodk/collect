/*
 * Copyright (C) 2017 Shobhit
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.utilities;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import timber.log.Timber;

/**
 * Created by shobhit on 12/4/17.
 */

public class CompressionUtils {
    public static String compress(String data) throws IOException {
        if (data == null || data.length() == 0) {
            return data;
        }

        // Encode string into bytes
        byte[] input = data.getBytes("UTF-8");

        // Compress the bytes
        byte[] output = new byte[input.length];
        Deflater compresser = new Deflater();
        compresser.setInput(input);
        compresser.finish();
        int compressedDataLength = compresser.deflate(output);
        compresser.end();

        // Encode to base64
        String base64String = Base64.encodeBase64String(output);
        Timber.i("Original length : %d", data.length());
        Timber.i("Compressed length : %d", compressedDataLength);
        Timber.i("Compression ratio : %2f", ((data.length() * 1.0) / compressedDataLength) * 100);
        return base64String;
    }

    public static String decompress(String compressedString) throws IOException, DataFormatException {

        // Decode from base64
        byte[] output = Base64.decodeBase64(compressedString);

        // Decompresses the bytes
        Inflater decompresser = new Inflater();
        decompresser.setInput(output);
        byte[] result = compressedString.getBytes();
        int resultLength = decompresser.inflate(result);
        decompresser.end();

        // Decode the bytes into a String
        String outputString = new String(result, 0, resultLength, "UTF-8");
        Timber.i("Compressed length : %d", compressedString.length());
        Timber.i("Decompressed length : %d", resultLength);
        return outputString;
    }
}
