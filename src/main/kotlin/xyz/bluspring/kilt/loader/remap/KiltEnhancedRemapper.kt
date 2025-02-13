package xyz.bluspring.kilt.loader.remap

import com.google.common.cache.CacheBuilder
import net.minecraftforge.fart.api.ClassProvider
import net.minecraftforge.fart.internal.EnhancedRemapper
import net.minecraftforge.srgutils.IMappingFile
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

class KiltEnhancedRemapper(provider: ClassProvider, file: IMappingFile, log: Consumer<String>) : EnhancedRemapper(provider, file, log) {
    private val cachedLoadedClasses = CacheBuilder.newBuilder()
        .expireAfterAccess(1L, TimeUnit.MINUTES)
        .build<String, ClassNode>()

    override fun mapMethodName(owner: String, name: String, descriptor: String): String {
        if (name.startsWith("m_") && name.endsWith("_")) {
            val mappedNames = KiltRemapper.srgMappedMethods[name] ?: return super.mapMethodName(owner, name, descriptor)

            return mappedNames[owner] ?: run {
                return tryFindMethodName(owner, mappedNames) ?: mappedNames.values.firstOrNull() ?: super.mapMethodName(owner, name, descriptor)
            }
        }

        return super.mapMethodName(owner, name, descriptor)
    }

    private fun tryFindMethodName(owner: String, mappedNames: Map<String, String>): String? {
        val actualOwnerName = if (owner.startsWith("net/minecraft/class_"))
            KiltRemapper.unmapClass(owner)
        else owner

        if (mappedNames.contains(actualOwnerName)) {
            return mappedNames[actualOwnerName]!!
        }

        if (actualOwnerName.contains("java/lang/Object"))
            return null

        try {
            val classNode = cachedLoadedClasses.get(actualOwnerName) {
                val classStream = this.classProvider.getClassStream(actualOwnerName)

                if (classStream != null) {
                    val classReader = ClassReader(classStream)
                    val node = ClassNode(Opcodes.ASM9)
                    classReader.accept(node, 0)

                    node
                } else throw ClassNotFoundException()
            }

            val tryFindFromSuper = tryFindMethodName(classNode.superName, mappedNames)
            if (tryFindFromSuper != null)
                return tryFindFromSuper

            for (interfaceName in classNode.interfaces) {
                return tryFindMethodName(interfaceName, mappedNames) ?: continue
            }
        } catch (_: Exception) {}

        return null
    }

    override fun mapInvokeDynamicMethodName(name: String, descriptor: String): String {
        if (name.startsWith("m_") && name.endsWith("_")) {
            return KiltRemapper.srgMappedMethods[name]?.values?.firstOrNull() ?: super.mapInvokeDynamicMethodName(name, descriptor)
        }

        return super.mapInvokeDynamicMethodName(name, descriptor)
    }

    override fun mapFieldName(owner: String, name: String, descriptor: String): String {
        if (name.startsWith("f_") && name.endsWith("_")) {
            return KiltRemapper.srgMappedFields[name]?.second ?: super.mapFieldName(owner, name, descriptor)
        }

        return super.mapFieldName(owner, name, descriptor)
    }
}