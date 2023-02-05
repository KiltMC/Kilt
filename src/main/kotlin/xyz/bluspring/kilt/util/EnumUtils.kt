package xyz.bluspring.kilt.util

import net.minecraftforge.common.IExtensibleEnum
import net.minecraftforge.fml.unsafe.UnsafeHacks
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
}