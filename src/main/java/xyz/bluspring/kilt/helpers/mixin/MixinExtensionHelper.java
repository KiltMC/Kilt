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
        var slashedMixinClassName = mixinClassName.replaceAll("\\.", "/");
        var slashedTargetClassName = targetClassName.replaceAll("\\.", "/");

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
                            if (methodInsn.owner.equals(mixinClassName)) { // this()
                                initializer.visitMethodInsn(Opcodes.INVOKESPECIAL, slashedTargetClassName, "<init>", methodInsn.desc, false);
                            } else { // super()
                                initializer.visitMethodInsn(Opcodes.INVOKESPECIAL, methodInsn.owner, "<init>", methodInsn.desc, false);
                            }
                        } else {
                            if (methodInsn.owner.equals(slashedMixinClassName)) {
                                methodInsn.owner = slashedTargetClassName;
                            }

                            initializer.instructions.add(methodInsn);
                        }
                    } else {
                        if (insnNode instanceof FieldInsnNode fieldInsn) {
                            if (fieldInsn.owner.equals(slashedMixinClassName)) {
                                fieldInsn.owner = slashedTargetClassName;
                            }

                            initializer.instructions.add(fieldInsn);
                        } else {
                            initializer.instructions.add(insnNode);
                        }
                    }
                }

                initializer.visitEnd();

                initializer.localVariables = methodNode.localVariables;

                // We don't need the method's instructions anymore.
                methodNode.instructions.clear();
                targetClass.methods.add(initializer);
            }
        }
    }
}
