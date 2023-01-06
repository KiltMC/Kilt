package net.minecraftforge.common.capabilities

import org.objectweb.asm.Type
import java.lang.reflect.ParameterizedType

abstract class CapabilityToken<T : Any> {
    internal fun getType(): String {
        val type = this::class.java.genericSuperclass as ParameterizedType
        return Type.getInternalName(type.actualTypeArguments[0] as Class<T>)
    }
}