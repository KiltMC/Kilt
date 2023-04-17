package xyz.bluspring.kilt.helpers.mixin;

import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.util.Annotations;

import java.util.ArrayList;

public final class MixinExtensionHelper {
    // This should be executed in the mixin plugin with the corresponding method.
    // This is only separated into this class for anyone who wants to use this code.
    @ApiStatus.Internal
    public static void apply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        var classNode = mixinInfo.getClassNode(0);
        var slashedMixinClassName = mixinClassName.replaceAll("\\.", "/");
        var slashedTargetClassName = targetClassName.replaceAll("\\.", "/");

        var fieldsToRemove = new ArrayList<FieldNode>();
        var methodsToRemove = new ArrayList<MethodNode>();

        for (FieldNode fieldNode : classNode.fields) {
            if (Annotations.getVisible(fieldNode, CreateStatic.class) == null)
                continue;

            fieldsToRemove.add(fieldNode);

            targetClass.fields.removeIf((field) -> field.name.equals(fieldNode.name) && field.desc.equals(fieldNode.desc));
            targetClass.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, fieldNode.name, fieldNode.desc, fieldNode.signature, fieldNode.value).visitEnd();
        }

        for (MethodNode methodNode : classNode.methods) {
            if (Annotations.getVisible(methodNode, CreateStatic.class) != null) {
                methodsToRemove.add(methodNode);
                targetClass.methods.removeIf((method) -> method.name.equals(methodNode.name) && method.desc.equals(methodNode.desc));

                var method = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, methodNode.name, methodNode.desc, methodNode.signature, methodNode.exceptions != null ? methodNode.exceptions.toArray(String[]::new) : null);
                method.visitCode();

                method.instructions.add(methodNode.instructions);
                method.localVariables.addAll(methodNode.localVariables);

                method.visitEnd();

                targetClass.methods.add(method);
            } else if (Annotations.getVisible(methodNode, CreateInitializer.class) != null) {
                var initializer = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", methodNode.desc, methodNode.signature, methodNode.exceptions != null ? methodNode.exceptions.toArray(String[]::new) : null);
                initializer.visitCode();

                for (AbstractInsnNode insnNode : methodNode.instructions) {
                    if (insnNode instanceof MethodInsnNode methodInsn) {
                        // super()/this()
                        if (insnNode.getOpcode() == Opcodes.INVOKESPECIAL) {
                            if (methodInsn.owner.equals(slashedMixinClassName)) { // this()
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

        for (FieldNode fieldNode : fieldsToRemove) {
            classNode.fields.remove(fieldNode);
        }

        for (MethodNode methodNode : methodsToRemove) {
            classNode.methods.remove(methodNode);
        }
    }
}
