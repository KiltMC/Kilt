package xyz.bluspring.kilt.helpers.mixin;

import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.util.Annotations;

public final class MixinExtensionHelper {
    // This should be executed in the mixin plugin with the corresponding method.
    // This is only separated into this class for anyone who wants to use this code.
    @ApiStatus.Internal
    public static void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        var classNode = mixinInfo.getClassNode(0);

        for (FieldNode fieldNode : classNode.fields) {
            if (Annotations.getVisible(fieldNode, CreateStatic.class) == null)
                continue;

            // Remove private and protected access, and promote to public and static.
            fieldNode.access = fieldNode.access & ~Opcodes.ACC_PRIVATE & ~Opcodes.ACC_PROTECTED | Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC;
        }

        for (MethodNode methodNode : classNode.methods) {
            if (Annotations.getVisible(methodNode, CreateStatic.class) != null) {
                // Remove private and protected access, and promote to public and static.
                methodNode.access = methodNode.access & ~Opcodes.ACC_PRIVATE & ~Opcodes.ACC_PROTECTED | Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC;
            } else if (Annotations.getVisible(methodNode, CreateInitializer.class) != null) {
                // Make sure the method is private and static.
                methodNode.access = methodNode.access & ~Opcodes.ACC_PUBLIC & ~Opcodes.ACC_PROTECTED | Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC;

                var initializer = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", methodNode.desc, methodNode.signature, methodNode.exceptions != null ? methodNode.exceptions.toArray(String[]::new) : null);
                initializer.visitCode();

                for (AbstractInsnNode insnNode : methodNode.instructions) {
                    if (insnNode instanceof MethodInsnNode methodInsn) {
                        // super()/this()
                        if (insnNode.getOpcode() == Opcodes.INVOKESPECIAL) {
                            if (methodInsn.owner.equals(mixinClassName)) { // super()
                                initializer.visitMethodInsn(Opcodes.INVOKESPECIAL, targetClassName, "<init>", methodInsn.desc, false);
                            } else { // this()
                                initializer.visitMethodInsn(Opcodes.INVOKESPECIAL, methodInsn.owner, "<init>", methodInsn.desc, false);
                            }
                        } else {
                            if (methodInsn.owner.equals(mixinClassName)) {
                                methodInsn.owner = targetClassName;
                                initializer.instructions.add(methodInsn);
                            }
                        }
                    } else {
                        initializer.instructions.add(insnNode);
                    }
                }

                initializer.localVariables = methodNode.localVariables;

                // We don't need the method's instructions anymore.
                methodNode.instructions.clear();
                targetClass.methods.add(initializer);
            }
        }
    }
}
