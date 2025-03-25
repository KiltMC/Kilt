package cpw.mods.modlauncher.serviceapi

import cpw.mods.jarhandling.SecureJar
import cpw.mods.modlauncher.api.NamedPath
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import java.nio.file.Path
import java.util.*
import java.util.function.Consumer

interface ILaunchPluginService {
    fun name(): String

    enum class Phase {
        BEFORE, AFTER
    }

    object ComputeFlags {
        const val NO_REWRITE = 0
        const val SIMPLE_REWRITE = 0x100
        const val COMPUTE_MAXS = ClassWriter.COMPUTE_MAXS
        const val COMPUTE_FRAMES = ClassWriter.COMPUTE_FRAMES
    }

    fun handlesClass(classType: Type, isEmpty: Boolean): EnumSet<Phase>
    fun handlesClass(classType: Type, isEmpty: Boolean, reason: String): EnumSet<Phase>

    fun processClass(phase: Phase, classNode: ClassNode, classType: Type): Boolean {
        return false
    }

    fun processClass(phase: Phase, classNode: ClassNode, classType: Type, reason: String): Boolean {
        return processClass(phase, classNode, classType)
    }

    fun processClassWithFlags(phase: Phase, classNode: ClassNode, classType: Type, reason: String): Int {
        return if (processClass(phase, classNode, classType, reason))
            ComputeFlags.COMPUTE_FRAMES
        else
            ComputeFlags.NO_REWRITE
    }

    fun offerResource(resource: Path, name: String) {}
    fun addResources(resources: List<SecureJar>) {}
    fun initializeLaunch(transformerLoader: ITransformerLoader, specialPaths: Array<NamedPath>) {}

    fun <T> getExtension(): T? {
        return null
    }

    fun customAuditConsumer(className: String, auditDataAcceptor: Consumer<Array<String>>) {}

    interface ITransformerLoader {
        fun buildTransformedClassNodeFor(className: String): ByteArray
    }
}