package xyz.bluspring.kilt.loader.asm

import de.florianmichael.asmfabricloader.api.event.InstrumentationEntrypoint
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
import org.spongepowered.asm.mixin.extensibility.IMixinConfig
import java.lang.instrument.ClassFileTransformer
import java.lang.instrument.Instrumentation
import java.security.ProtectionDomain

class KiltInstrumentationHandler : InstrumentationEntrypoint {
    override fun onGetInstrumentation(instrumentation: Instrumentation) {
        val mixinPreProcessorClass = Class.forName("org.spongepowered.asm.mixin.transformer.MixinPreProcessorStandard", false, Thread.currentThread().contextClassLoader)

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
                if (mixinPreProcessorClass == classBeingRedefined) {
                    val classReader = ClassReader(classfileBuffer)
                    val classNode = ClassNode(Opcodes.ASM9)
                    classReader.accept(classNode, 0)

                    val conformVisibilityMethod = classNode.methods.first { it.name == "conformVisibility" }
                    val instructions = conformVisibilityMethod.instructions

                    run {
                        val conformVisibilityInsn = instructions.firstOrNull { it is MethodInsnNode && it.opcode == Opcodes.INVOKEVIRTUAL && it.owner == "org/spongepowered/asm/mixin/transformer/MixinConfig" && it.name == "conformOverwriteVisibility" && it.desc == "()Z" } ?: return@run

                        instructions.insert(conformVisibilityInsn, MethodInsnNode(Opcodes.INVOKESTATIC, "xyz/bluspring/kilt/loader/asm/KiltInstrumentationHandler", "checkShouldConformOverwriteVisibility", "(Lorg/spongepowered/asm/mixin/extensibility/IMixinConfig;)Z"))
                        instructions.remove(conformVisibilityInsn)
                    }

                    conformVisibilityMethod.instructions = instructions

                    val writer = ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)
                    classNode.accept(writer)

                    return writer.toByteArray()
                }

                return super.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer)
            }
        }, true)

        instrumentation.retransformClasses(mixinPreProcessorClass)
    }

    companion object {
        @Suppress("unused")
        @JvmStatic
        fun checkShouldConformOverwriteVisibility(mixinConfig: IMixinConfig): Boolean {
            return true
        }
    }
}