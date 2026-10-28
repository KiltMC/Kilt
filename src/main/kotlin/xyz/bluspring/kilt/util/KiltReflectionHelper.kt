package xyz.bluspring.kilt.util

import net.neoforged.fml.util.ObfuscationReflectionHelper
import org.objectweb.asm.Type
import xyz.bluspring.kilt.loader.remap.KiltRemapper
import java.lang.reflect.Field
import java.lang.reflect.Method

object KiltReflectionHelper {
    @JvmStatic
    fun getMojFieldName(field: Field): String {
        return KiltRemapper.enhancedRemapper.mapFieldName(Type.getInternalName(field.declaringClass), field.name, Type.getDescriptor(field.type))
    }

    @JvmStatic
    fun getMojFieldDesc(field: Field): String {
        return KiltRemapper.remapDescriptor(Type.getDescriptor(field.type), true)
    }

    @JvmStatic
    fun getMojMethodName(method: Method): String {
        return KiltRemapper.enhancedRemapper.mapMethodName(Type.getInternalName(method.declaringClass), method.name,
            Type.getMethodDescriptor(Type.getReturnType(method), *Type.getArgumentTypes(method)))
    }

    @JvmStatic
    fun getMojMethodDesc(method: Method): String {
        return KiltRemapper.remapDescriptor(Type.getMethodDescriptor(Type.getReturnType(method), *Type.getArgumentTypes(method)), true)
    }

    @JvmStatic
    fun getClass(name: String): Class<*> {
        return Class.forName(KiltRemapper.remapClass(name).replace('/', '.'))
    }

    @JvmStatic
    fun getClass(module: Module, name: String): Class<*> {
        return Class.forName(module, KiltRemapper.remapClass(name).replace('/', '.'))
    }

    @JvmStatic
    fun getClass(name: String, initialize: Boolean, classLoader: ClassLoader): Class<*> {
        return Class.forName(KiltRemapper.remapClass(name).replace('/', '.'), initialize, classLoader)
    }

    @JvmStatic
    fun loadClass(classLoader: ClassLoader, name: String): Class<*> {
        return classLoader.loadClass(KiltRemapper.remapClass(name).replace('/', '.'))
    }

    @JvmStatic
    fun getField(clazz: Class<*>, name: String): Field {
        return ObfuscationReflectionHelper.findField(clazz, name)
    }

    @JvmStatic
    fun getDeclaredField(clazz: Class<*>, name: String): Field {
        return ObfuscationReflectionHelper.findField(clazz, name)
    }

    @JvmStatic
    fun getMethod(clazz: Class<*>, name: String, vararg args: Class<*>): Method {
        return clazz.getMethod(KiltRemapper.enhancedRemapper.mapMethodNamePrefixDesc(Type.getInternalName(clazz), name,
            args.joinToString("") { Type.getDescriptor(it) }) ?: name, *args)
    }

    @JvmStatic
    fun getDeclaredMethod(clazz: Class<*>, name: String, vararg args: Class<*>): Method {
        return clazz.getDeclaredMethod(KiltRemapper.enhancedRemapper.mapMethodNamePrefixDesc(Type.getInternalName(clazz), name,
            args.joinToString("") { Type.getDescriptor(it) }) ?: name, *args)
    }
}
