package xyz.bluspring.kilt.loader.asm

import de.florianmichael.asmfabricloader.api.event.InstrumentationEntrypoint
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnNode
import java.lang.instrument.ClassFileTransformer
import java.lang.instrument.Instrumentation
import java.security.ProtectionDomain

class KiltInstrumentationHandler : InstrumentationEntrypoint {
    override fun onGetInstrumentation(instrumentation: Instrumentation) {
        val mixinConfigClass = Class.forName("org.spongepowered.asm.mixin.transformer.MixinConfig", false, Thread.currentThread().contextClassLoader)

        instrumentation.addTransformer(object : ClassFileTransformer {
            override fun transform(
                loader: ClassLoader?,
                className: String,
                classBeingRedefined: Class<*>?,
                protectionDomain: ProtectionDomain?,
                classfileBuffer: ByteArray
            ): ByteArray? {
                // Basically, makes sure that all overwrites are able to upgrade to a higher visibility method where possible.
                // I know, I know, I should *not* be doing this, but at this rate it's honestly easier.
                if (classBeingRedefined == mixinConfigClass) {
                    val classReader = ClassReader(classfileBuffer)
                    val classNode = ClassNode(Opcodes.ASM9)
                    classReader.accept(classNode, 0)

                    val conformVisibilityMethod = classNode.methods.first { it.name == "conformOverwriteVisibility" }
                    val instructions = conformVisibilityMethod.instructions

                    run {
                        instructions.clear()

                        instructions.add(InsnNode(Opcodes.ICONST_1))
                        instructions.add(InsnNode(Opcodes.IRETURN))
                    }

                    conformVisibilityMethod.instructions = instructions

                    val writer = ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)
                    classNode.accept(writer)

                    return writer.toByteArray()
                }

                return super.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer)
            }
        }, true)

        instrumentation.retransformClasses(mixinConfigClass)
    }
}