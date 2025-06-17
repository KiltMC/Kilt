package xyz.bluspring.kilt.loader.asm

import de.florianmichael.asmfabricloader.api.event.InstrumentationEntrypoint
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
import java.lang.instrument.ClassFileTransformer
import java.lang.instrument.Instrumentation
import java.security.ProtectionDomain

class KiltInstrumentationHandler : InstrumentationEntrypoint {
    override fun onGetInstrumentation(instrumentation: Instrumentation) {
        val mixinPreProcessorClass = Class.forName("org.spongepowered.asm.mixin.transformer.MixinPreProcessorStandard", false, Thread.currentThread().contextClassLoader)

        instrumentation.addTransformer(object : ClassFileTransformer {
            override fun transform(
                loader: ClassLoader,
                className: String,
                classBeingRedefined: Class<*>,
                protectionDomain: ProtectionDomain,
                classfileBuffer: ByteArray
            ): ByteArray {
                // Basically, makes sure that all overwrites are able to upgrade to a higher visibility method where possible.
                // I know, I know, I should *not* be doing this, but at this rate it's honestly easier.
                if (classBeingRedefined == mixinPreProcessorClass) {
                    val classReader = ClassReader(classfileBuffer)
                    val classNode = ClassNode(Opcodes.ASM9)
                    classReader.accept(classNode, 0)

                    val conformVisibilityMethod = classNode.methods.first { it.name == "conformVisibility" }
                    val instructions = conformVisibilityMethod.instructions

                    run {
                        val method = classNode.visitMethod(Opcodes.ACC_PRIVATE or Opcodes.ACC_STATIC or Opcodes.ACC_SYNTHETIC, "kilt\$checkShouldConformOverwriteVisibility", "(Lorg/spongepowered/asm/mixin/extensibility/IMixinConfig;)Z", null, null)
                        val l0 = Label()
                        val l1 = Label()

                        method.visitCode()

                        /*
                        private static boolean kilt$checkShouldConformOverwriteVisibility(IMixinConfig config) {
                            return true;
                        }
                         */

                        method.visitLabel(l0)
                        method.visitInsn(Opcodes.ICONST_1)
                        method.visitInsn(Opcodes.IRETURN)

                        method.visitLabel(l1)
                        method.visitLocalVariable("mixinConfig", "Lorg/spongepowered/asm/mixin/extensibility/IMixinConfig;", null, l0, l1, 0)
                        method.visitMaxs(0, 0)

                        method.visitEnd()
                    }

                    run {
                        val conformVisibilityInsn = instructions.firstOrNull { it is MethodInsnNode && it.opcode == Opcodes.INVOKEVIRTUAL && it.owner == "org/spongepowered/asm/mixin/transformer/MixinConfig" && it.name == "conformOverwriteVisibility" && it.desc == "()Z" } ?: return@run

                        instructions.insert(conformVisibilityInsn, MethodInsnNode(Opcodes.INVOKESTATIC, "org/spongepowered/asm/mixin/transformer/MixinPreProcessorStandard", "kilt\$checkShouldConformOverwriteVisibility", "(Lorg/spongepowered/asm/mixin/extensibility/IMixinConfig;)Z"))
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
}