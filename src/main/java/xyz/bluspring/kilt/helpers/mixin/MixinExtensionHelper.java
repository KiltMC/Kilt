package xyz.bluspring.kilt.helpers.mixin;

import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public final class MixinExtensionHelper {
    private static final Type extensionType = Type.getType(MixinExtensionHelper.class);
    private static final Type initializerAnnotation = Type.getType(CreateInitializer.class);
    private static final Type staticAnnotation = Type.getType(CreateStatic.class);

    // This should be executed in the mixin plugin with the corresponding method.
    // This is only separated into this class for anyone who wants to use this code.
    @ApiStatus.Internal
    public static void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        var classNode = mixinInfo.getClassNode(0);

        for (FieldNode fieldNode : classNode.fields) {
            if (fieldNode.visibleAnnotations == null)
                continue;

            var foundAnnotation = false;

            for (AnnotationNode annotationNode : fieldNode.visibleAnnotations) {
                if (annotationNode.desc.equals(staticAnnotation.getDescriptor())) {
                    foundAnnotation = true;
                    break;
                }
            }

            if (!foundAnnotation)
                continue;

            // Remove private and protected access, and promote to public and static.
            fieldNode.access = fieldNode.access & ~Opcodes.ACC_PRIVATE & ~Opcodes.ACC_PROTECTED | Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC;
        }

        for (MethodNode methodNode : classNode.methods) {
            if (methodNode.visibleAnnotations == null)
                continue;

            var foundStaticAnnotation = false;
            var foundInitAnnotation = false;

            for (AnnotationNode annotationNode : methodNode.visibleAnnotations) {
                if (annotationNode.desc.equals(staticAnnotation.getDescriptor())) {
                    foundStaticAnnotation = true;
                    break;
                } else if (annotationNode.desc.equals(initializerAnnotation.getDescriptor())) {
                    foundInitAnnotation = true;
                    break;
                }
            }

            if (foundStaticAnnotation) {
                // Remove private and protected access, and promote to public and static.
                methodNode.access = methodNode.access & ~Opcodes.ACC_PRIVATE & ~Opcodes.ACC_PROTECTED | Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC;
            } else if (foundInitAnnotation) {
                // Make sure the method is private and static.
                methodNode.access = methodNode.access & ~Opcodes.ACC_PUBLIC & ~Opcodes.ACC_PROTECTED | Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC;

                var initializer = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", methodNode.desc, methodNode.signature, methodNode.exceptions != null ? methodNode.exceptions.toArray(String[]::new) : null);
                initializer.visitCode();

                var nodesUntilInitializer = 0;
                var alreadyRun = false;
                for (AbstractInsnNode insnNode : methodNode.instructions) {
                    nodesUntilInitializer++;

                    if (insnNode instanceof MethodInsnNode methodInsn) {
                        // super()/this()
                        if (insnNode.getOpcode() == Opcodes.INVOKESTATIC && methodInsn.owner.equals(extensionType.getClassName())) {
                            if (alreadyRun) {
                                throw new IllegalStateException("Cannot run two initializers in one method!");
                            }

                            alreadyRun = true;

                            if (methodInsn.name.equals("initSuper")) { // super()
                                initializer.visitMethodInsn(Opcodes.INVOKESPECIAL, targetClass.superName, "<init>", methodInsn.desc, false);
                            } else if (methodInsn.name.equals("initThis")) { // this()
                                initializer.visitMethodInsn(Opcodes.INVOKESPECIAL, targetClassName, "<init>", methodInsn.desc, false);
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

    public static void initSuper(Object... args) {
        throw new IllegalStateException("Super initializer call failed! This should not be called outside of an @CreateInitializer annotated method.");
    }

    public static void initThis(Object... args) {
        throw new IllegalStateException("Local initializer call failed! This should not be called outside of an @CreateInitializer annotated method.");
    }
}
