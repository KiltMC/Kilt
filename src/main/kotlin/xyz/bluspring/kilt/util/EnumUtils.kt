package xyz.bluspring.kilt.util

import com.chocohead.mm.CasualStreamHandler
import com.chocohead.mm.api.ClassTinkerers
import net.minecraftforge.common.IExtensibleEnum
import net.minecraftforge.fml.unsafe.UnsafeHacks
import xyz.bluspring.kilt.Kilt
import java.util.function.Consumer

object EnumUtils {
    @JvmStatic
    fun <T : Enum<*>> addEnumToClass(clazz: Class<T>, values: Array<out T>, name: String, createValue: java.util.function.Function<Int, T>, setValues: Consumer<List<T>>): T {
        val list = values.toMutableList()

        for (enumValue in values) {
            if (!enumValue.name.equals(name, ignoreCase = true)) continue
            return enumValue
        }

        val value = createValue.apply(values.size)

        if (value is IExtensibleEnum)
            value.init()

        list.add(value)
        setValues.accept(list)

        try {
            UnsafeHacks.cleanEnumCache(clazz)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

        return value
    }

    @JvmStatic
    fun loadClass(name: String, byteArray: ByteArray): Class<*>? {
        return try {
            val className = name.replace("/", ".")
            val url = CasualStreamHandler.create(className, byteArray)
            if (ClassTinkerers.addURL(url))
                return Class.forName(className)

            return null
        } catch (e: Exception) {
            Kilt.logger.error("Failed to dynamically load class $name!")
            e.printStackTrace()
            null
        }
    }
}