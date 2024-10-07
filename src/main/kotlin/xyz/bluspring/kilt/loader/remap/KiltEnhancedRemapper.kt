package xyz.bluspring.kilt.loader.remap

import net.minecraftforge.fart.api.ClassProvider
import net.minecraftforge.fart.internal.EnhancedRemapper
import net.minecraftforge.srgutils.IMappingFile
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import java.util.function.Consumer

class KiltEnhancedRemapper(provider: ClassProvider, file: IMappingFile, log: Consumer<String>) : EnhancedRemapper(provider, file, log) {
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
        val classOpt = this.classProvider.getClassBytes(owner)

        if (mappedNames.contains(owner)) {
            return mappedNames[owner]!!
        }

        if (owner.contains("java/lang/Object"))
            return null

        if (classOpt.isPresent) {
            val classReader = ClassReader(classOpt.get())
            val classNode = ClassNode(Opcodes.ASM9)
            classReader.accept(classNode, 0)

            val tryFindFromSuper = tryFindMethodName(classNode.superName, mappedNames)
            if (tryFindFromSuper != null)
                return tryFindFromSuper

            for (interfaceName in classNode.interfaces) {
                return tryFindMethodName(interfaceName, mappedNames) ?: continue
            }
        }

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