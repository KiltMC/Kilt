package xyz.bluspring.kilt.util

import net.minecraftforge.common.IExtensibleEnum
import net.minecraftforge.fml.unsafe.UnsafeHacks
import xyz.bluspring.kilt.Kilt
import java.util.function.Consumer

object EnumUtils {
    @JvmStatic
    fun <T : Enum<*>> addEnumToClass(clazz: Class<T>, values: Array<out T>, name: String, createValue: java.util.function.Function<Int, T>, setValues: Consumer<List<T>>): T {
        val list = values.toMutableList()

        for (recipeBookCategories in values) {
            if (!recipeBookCategories.name.equals(name, ignoreCase = true)) continue
            return recipeBookCategories
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
            val loader = Kilt::class.java.classLoader
            // You would think this can be made easier by just doing
            // ClassLoader::class.java, but you'd be wrong.
            // There's a module block here. Don't trust it.
            val classLoaderClass = Class.forName("java.lang.ClassLoader", true, ClassLoader.getSystemClassLoader())

            val defineClassMethod = classLoaderClass.getDeclaredMethod("defineClass", String::class.java, ByteArray::class.java, Int::class.java, Int::class.java)
            defineClassMethod.isAccessible = true

            return try {
                defineClassMethod.invoke(loader, name, byteArray, 0, byteArray.size) as Class<*>
            } finally {
                defineClassMethod.isAccessible = false
            }
        } catch (e: Exception) {
            Kilt.logger.error("Failed to dynamically load class $name!")
            e.printStackTrace()
            null
        }
    }
}