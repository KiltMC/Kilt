package net.minecraftforge.fml.util

import cpw.mods.modlauncher.api.INameMappingService
import net.fabricmc.loader.api.FabricLoader
import org.objectweb.asm.Type
import org.slf4j.LoggerFactory
import xyz.bluspring.kilt.loader.remap.KiltRemapper
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.*

object ObfuscationReflectionHelper {
    private val LOGGER = LoggerFactory.getLogger(ObfuscationReflectionHelper::class.java)

    private val fabricRemapper = FabricLoader.getInstance().mappingResolver
    private val srgIntermediaryTree = KiltRemapper.srgIntermediaryMapping

    private val srgMappedFields = KiltRemapper.srgMappedFields
    private val srgMappedMethods = KiltRemapper.srgMappedMethods

    @JvmStatic
    fun remapName(domain: INameMappingService.Domain, name: String): String {
        return when (domain) {
            INameMappingService.Domain.CLASS -> {
                fabricRemapper.mapClassName("intermediary", KiltRemapper.enhancedRemapper.map(name))
            }

            INameMappingService.Domain.FIELD -> {
                srgMappedFields[name]?.second ?: name
            }

            INameMappingService.Domain.METHOD -> {
                srgMappedMethods[name]?.values?.firstOrNull() ?: name
            }
        }
    }

    @JvmStatic
    fun <T, E> getPrivateValue(classToAccess: Class<in E>, instance: E, fieldName: String): T {
        return try {
            findField(classToAccess, fieldName).get(instance) as T
        } catch (e: UnableToFindFieldException) {
            LOGGER.error("Unable to locate field $fieldName (${remapName(INameMappingService.Domain.FIELD, fieldName)}) on type ${classToAccess.name}", e)
            throw e
        } catch (e: IllegalAccessException) {
            LOGGER.error("Unable to access field $fieldName (${remapName(INameMappingService.Domain.FIELD, fieldName)}) on type ${classToAccess.name}", e)
            throw UnableToAccessFieldException(e)
        }
    }

    @JvmStatic
    fun <T, E> setPrivateValue(classToAccess: Class<in T>, instance: T, value: E, fieldName: String) {
        try {
            findField(classToAccess, fieldName).set(instance, value)
        } catch (e: UnableToFindFieldException) {
            LOGGER.error("Unable to locate any field $fieldName on type ${classToAccess.name}", e)
            throw e
        } catch (e: IllegalAccessException) {
            LOGGER.error("Unable to access any field $fieldName on type ${classToAccess.name}", e)
            throw UnableToAccessFieldException(e)
        }
    }

    @JvmStatic
    fun findMethod(clazz: Class<*>, methodName: String, vararg parameterTypes: Class<*>): Method {
        return try {
            val methodName = if (KiltRemapper.srgMappedMethods.containsKey(methodName)) {
                val descriptor = Type.getMethodDescriptor(Type.VOID_TYPE, *parameterTypes.map { Type.getType(it) }.toTypedArray()).removeSuffix("V")

                val mapped = KiltRemapper.enhancedRemapper.mapMethodNamePrefixDesc(clazz.typeName.replace(".", "/"), methodName, descriptor)

                if ((mapped == methodName || mapped == null) && (methodName.startsWith("m_") || methodName.startsWith("f_")) && methodName.endsWith("_"))
                    KiltRemapper.srgMappedMethods[methodName]!!.values.first()
                else mapped
            } else methodName

            val m = clazz.getDeclaredMethod(methodName, *parameterTypes)
            m.isAccessible = true
            m
        } catch (e: Exception) {
            throw UnableToFindMethodException(e)
        }
    }

    @JvmStatic
    fun <T> findConstructor(clazz: Class<T>, vararg parameterTypes: Class<*>): Constructor<T> {
        return try {
            val constructor = clazz.getDeclaredConstructor(*parameterTypes)
            constructor.isAccessible = true
            constructor
        } catch (e: NoSuchMethodException) {
            val desc = StringBuilder()
            desc.append(clazz.simpleName)

            val joiner = StringJoiner(", ", "(", ")")
            for (type in parameterTypes) {
                joiner.add(type.simpleName)
            }

            desc.append(joiner)

            throw UnknownConstructorException("Could not find constructor '$desc' in $clazz")
        }
    }

    @JvmStatic
    fun <T> findField(clazz: Class<in T>, fieldName: String): Field {
        return try {
            val f = clazz.getDeclaredField(remapName(INameMappingService.Domain.FIELD, fieldName))
            f.isAccessible = true
            f
        } catch (e: Exception) {
            throw UnableToFindFieldException(e)
        }
    }

    open class UnableToAccessFieldException internal constructor(e: Exception) : RuntimeException(e)

    open class UnableToFindFieldException internal constructor(e: Exception) : RuntimeException(e)

    open class UnableToFindMethodException(failed: Throwable?) : RuntimeException(failed)

    open class UnknownConstructorException(message: String?) : RuntimeException(message)
}