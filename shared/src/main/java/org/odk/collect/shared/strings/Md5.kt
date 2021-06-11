package org.odk.collect.shared.strings

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object Md5 {

    @JvmStatic
    @JvmOverloads
    fun getMd5Hash(string: String, bufSize: Int = 16 * 1024): String? {
        return getMd5Hash(string.byteInputStream(), bufSize)
    }

    @JvmStatic
    @JvmOverloads
    fun getMd5Hash(file: File, bufSize: Int = 16 * 1024): String? {
        val inputStream: InputStream = try {
            FileInputStream(file)
        } catch (e: FileNotFoundException) {
            return null
        }

        return getMd5Hash(inputStream, bufSize)
    }

    @JvmStatic
    @JvmOverloads
    fun getMd5Hash(inputStream: InputStream, bufSize: Int = 16 * 1024): String? {
        return try {
            val md = MessageDigest.getInstance("MD5")
            val buffer = ByteArray(bufSize)
            while (true) {
                val result = inputStream.read(buffer, 0, bufSize)
                if (result == -1) {
                    break
                }
                md.update(buffer, 0, result)
            }
            val md5 = StringBuilder(BigInteger(1, md.digest()).toString(16))
            while (md5.length < 32) {
                md5.insert(0, "0")
            }
            inputStream.close()
            md5.toString()
        } catch (e: NoSuchAlgorithmException) {
            null
        } catch (e: IOException) {
            null
        }
    }
}
