package xyz.bluspring.knit.loader.util

import java.io.InputStream
import java.security.MessageDigest

object HashUtils {
    @JvmStatic fun md5Hash(stream: InputStream): String = hash(stream, "MD5")
    @JvmStatic fun sha1Hash(stream: InputStream): String = hash(stream, "SHA1")
    @JvmStatic fun sha256Hash(stream: InputStream): String = hash(stream, "SHA256")

    @OptIn(ExperimentalStdlibApi::class)
    @JvmStatic
    fun hash(stream: InputStream, algorithm: String = "MD5"): String {
        val digest = MessageDigest.getInstance(algorithm)
        val buffer = ByteArray(1024)

        var numRead: Int

        do {
            numRead = stream.read(buffer)
            if (numRead > 0) {
                digest.update(buffer, 0, numRead)
            }
        } while (numRead != -1)

        stream.close()

        return digest.digest().toHexString()
    }
}