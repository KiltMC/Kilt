package net.minecraftforge.fml.loading

import org.apache.logging.log4j.core.lookup.StrSubstitutor
import java.net.MalformedURLException
import java.net.URL
import java.util.stream.Stream


object StringUtils {
    @JvmStatic
    fun toLowerCase(str: String): String {
        return str.lowercase()
    }

    @JvmStatic
    fun toUpperCase(str: String): String {
        return str.uppercase()
    }

    @JvmStatic
    fun endsWith(search: String, vararg endings: String): Boolean {
        val lowerSearch = toLowerCase(search)
        return Stream.of(*endings).anyMatch { suffix ->
            lowerSearch.endsWith(
                suffix!!
            )
        }
    }

    @JvmStatic
    fun toURL(string: String?): URL? {
        return if (string == null || string.trim { it <= ' ' }
                .isEmpty() || string.contains("myurl.me") || string.contains("example.invalid"))
            null
        else try {
            URL(string)
        } catch (e: MalformedURLException) {
            throw RuntimeException(e)
        }
    }

    @JvmStatic
    fun parseStringFormat(input: String, properties: Map<String, String>): String {
        return StrSubstitutor.replace(input, properties)
    }

    @JvmStatic
    fun binToHex(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (i in bytes.indices) {
            sb.append(Integer.toHexString(bytes[i].toInt() and 0xf0 shr 4))
            sb.append(Integer.toHexString(bytes[i].toInt() and 0x0f))
        }
        return sb.toString()
    }
}