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
package org.odk.collect.androidshared.utils

import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.IllegalArgumentException
import java.util.zip.DataFormatException
import java.util.zip.Deflater
import java.util.zip.Inflater

object CompressionUtils {

    @Throws(IOException::class)
    fun compress(data: String?): String {
        if (data == null || data.isEmpty()) {
            return ""
        }

        // Encode string into bytes
        val input = data.toByteArray(charset("UTF-8"))
        val deflater = Deflater()
        deflater.setInput(input)

        // Compress the bytes
        val outputStream = ByteArrayOutputStream(data.length)
        deflater.finish()
        val buffer = ByteArray(1024)
        while (!deflater.finished()) {
            val count = deflater.deflate(buffer) // returns the generated code... index
            outputStream.write(buffer, 0, count)
        }
        outputStream.close()
        val output = outputStream.toByteArray()

        // Encode to base64
        return Base64.encodeToString(output, Base64.NO_WRAP)
    }

    @Throws(
        IOException::class,
        DataFormatException::class,
        IllegalArgumentException::class
    )
    fun decompress(compressedString: String?): String {
        if (compressedString == null || compressedString.isEmpty()) {
            return ""
        }

        // Decode from base64
        val output = Base64.decode(compressedString, Base64.NO_WRAP)
        val inflater = Inflater()
        inflater.setInput(output)

        // Decompresses the bytes
        val outputStream = ByteArrayOutputStream(output.size)
        val buffer = ByteArray(1024)
        while (!inflater.finished()) {
            val count = inflater.inflate(buffer)
            outputStream.write(buffer, 0, count)
        }
        outputStream.close()
        inflater.end()
        val result = outputStream.toByteArray()

        // Decode the bytes into a String
        return String(result, charset("UTF-8"))
    }
}
