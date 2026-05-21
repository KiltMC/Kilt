package xyz.bluspring.kilt.helpers.mixin;

import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.util.Annotations;
import org.spongepowered.asm.util.asm.MethodNodeEx;

import java.lang.invoke.LambdaMetafactory;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public final class MixinExtensionHelper {
    public static final String LAMBDA_CLASS_NAME = Type.getInternalName(LambdaMetafactory.class);
    public static final String LAMBDA_METHOD_DESCRIPTOR = "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;";

    // This should be executed in the mixin plugin with the corresponding method.
    // This is only separated into this class for anyone who wants to use this code.
    private static boolean containsOpcode(InsnList list, int opcode) {
        for (AbstractInsnNode node : list) {
            if (node.getOpcode() == opcode)
                return true;
        }

        return false;
    }

    private static boolean containsThisCall(ClassNode classNode, InsnList list) {
        for (AbstractInsnNode node : list) {
            if (node.getOpcode() == Opcodes.INVOKESPECIAL && node instanceof MethodInsnNode methodInsnNode && methodInsnNode.owner.equals(classNode.name) && methodInsnNode.name.equals("<init>"))
                return true;
        }

        return false;
    }

    @ApiStatus.Internal
    public static void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        var classNode = mixinInfo.getClassNode(0);
        var slashedMixinClassName = mixinClassName.replaceAll("\\.", "/");
        var slashedTargetClassName = targetClassName.replaceAll("\\.", "/");

        var extend = Annotations.getVisible(classNode, Extends.class);
        var oldSuper = targetClass.superName;
        if (extend != null) {
            if (targetClass.superName != null && !targetClass.superName.equals("java/lang/Object"))
                throw new IllegalStateException(String.format("Class %s should not already have a super class! (tried extend by %s, has %s)", targetClassName, mixinClassName, classNode.superName));

            var visitor = new AnnotationValueVisitor();
            extend.accept(visitor);
            var className = ((Type) visitor.values.get("value")).getClassName();
            targetClass.superName = className.replace(".", "/");
            if (targetClass.signature != null)
                targetClass.signature = targetClass.signature.replaceFirst("java/lang/Object", targetClass.superName);
        }

        for (MethodNode methodNode : classNode.methods) {
             var createInitializer = Annotations.getVisible(methodNode, CreateInitializer.class);
             if (createInitializer != null) {
                 var initializer = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", methodNode.desc, methodNode.signature, methodNode.exceptions != null ? methodNode.exceptions.toArray(String[]::new) : null);
                 initializer.invisibleAnnotations = new ArrayList<>();
                 initializer.invisibleAnnotations.add(createInitializer);
                 initializer.visitCode();

                 for (AbstractInsnNode insnNode : methodNode.instructions) {
                     if (insnNode instanceof MethodInsnNode methodInsn) {
                         // super()/this()
                         if (insnNode.getOpcode() == Opcodes.INVOKESPECIAL) {
                             if (methodInsn.owner.equals(slashedMixinClassName)) { // this()
                                 initializer.visitMethodInsn(Opcodes.INVOKESPECIAL, slashedTargetClassName, "<init>", methodInsn.desc, false);
                             } else { // super()
                                 // Redirect any super call to the target's actual superclass
                                 var superName = methodInsn.owner.equals(oldSuper) || methodInsn.owner.equals("java/lang/Object")
                                         ? targetClass.superName
                                         : methodInsn.owner;

                                 initializer.visitMethodInsn(Opcodes.INVOKESPECIAL, superName, "<init>", methodInsn.desc, false);
                             }
                         } else if (insnNode.getOpcode() == Opcodes.INVOKEVIRTUAL && methodInsn.name.equals("kilt$mixin$superCall")) { // super()
                             // Find the existing super call we already added and remove it then swap it for our custom one.
                             AbstractInsnNode superCall = null;
                             for (AbstractInsnNode insn : initializer.instructions) {
                                 if (insn.getOpcode() == Opcodes.INVOKESPECIAL && ((MethodInsnNode) insn).name.equals("<init>")) {
                                     superCall = insn;
                                     break;
                                 }
                             }
                             if (superCall != null) {
                                 initializer.instructions.remove(superCall);
                             }

                             initializer.visitMethodInsn(Opcodes.INVOKESPECIAL, targetClass.superName.replace(".", "/"), "<init>", methodInsn.desc, false);
                         } else {
                             if (methodInsn.owner.equals(slashedMixinClassName)) {
                                 methodInsn.owner = slashedTargetClassName;
                             }

                             initializer.instructions.add(methodInsn);
                         }
                     } else if (insnNode instanceof InvokeDynamicInsnNode invokeDynamicInsn) {
                         // Make lambdas actually remap correctly.
                         if (Opcodes.H_INVOKESTATIC == invokeDynamicInsn.bsm.getTag()
                                 && "metafactory".equals(invokeDynamicInsn.bsm.getName())
                                 && LAMBDA_CLASS_NAME.equals(invokeDynamicInsn.bsm.getOwner())
                                 && LAMBDA_METHOD_DESCRIPTOR.equals(invokeDynamicInsn.bsm.getDesc())
                                 && invokeDynamicInsn.bsmArgs != null
                                 && invokeDynamicInsn.bsmArgs.length == 3
                                 && invokeDynamicInsn.bsmArgs[1] instanceof Handle target
                                 && target.getOwner().equals(slashedMixinClassName)
                         ) {
                             initializer.instructions.add(new InvokeDynamicInsnNode(invokeDynamicInsn.name, invokeDynamicInsn.desc, invokeDynamicInsn.bsm,
                                     invokeDynamicInsn.bsmArgs[0],
                                     new Handle(target.getTag(), slashedTargetClassName, target.getName(), target.getDesc().replace(slashedMixinClassName, slashedTargetClassName), target.isInterface()),
                                     invokeDynamicInsn.bsmArgs[2]
                             ));
                         } else {
                             initializer.instructions.add(invokeDynamicInsn);
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

                 var lvt = new ArrayList<>(methodNode.localVariables);
                 for (int i = 0; i < lvt.size(); i++) {
                     LocalVariableNode lv = lvt.get(i);

                     if (lv.desc.contains("L" + slashedMixinClassName + ";")) {
                         var signature = lv.signature;

                         if (signature != null) {
                             signature = signature.replace("L" + slashedMixinClassName + ";", "L" + slashedTargetClassName + ";");
                         }

                         lvt.set(i, new LocalVariableNode(lv.name, lv.desc.replace("L" + slashedMixinClassName + ";", "L" + slashedTargetClassName + ";"), signature, lv.start, lv.end, lv.index));
                     }
                 }

                 initializer.localVariables = lvt;

                 // We don't need the method's instructions anymore.
                 methodNode.instructions.clear();
                 targetClass.methods.add(initializer);
            }
        }
    }

    private static String getMixinModifiedName(ClassNode targetClass, String originalName) {
        for (var method : targetClass.methods) {
            if (method instanceof MethodNodeEx ex) {
                if (ex.getOriginalName().equals(originalName)) {
                    return ex.name;
                }
            }
        }
        return originalName;
    }

    @ApiStatus.Internal
    public static void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        var classNode = mixinInfo.getClassNode(0);
        var slashedMixinClassName = mixinClassName.replaceAll("\\.", "/");
        var slashedTargetClassName = targetClassName.replaceAll("\\.", "/");

        var fieldsToRemove = new ArrayList<FieldNode>();
        var methodsToRemove = new ArrayList<MethodNode>();

        var extend = Annotations.getVisible(classNode, Extends.class);
        List<MethodNode> replacementNodes = new LinkedList<>();

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

                var instructions = new InsnList();

                for (AbstractInsnNode insn : methodNode.instructions) {
                    if (insn instanceof MethodInsnNode methodInsn) {
                        if (methodInsn.owner.equals(slashedMixinClassName))
                            methodInsn.owner = slashedTargetClassName;

                        instructions.add(methodInsn);
                        continue;
                    } else if (insn instanceof FieldInsnNode fieldInsn) {
                        if (fieldInsn.owner.equals(slashedMixinClassName))
                            fieldInsn.owner = slashedTargetClassName;

                        instructions.add(fieldInsn);
                        continue;
                    }

                    instructions.add(insn);
                }

                method.instructions.add(instructions);
                method.localVariables.addAll(methodNode.localVariables);

                method.visitEnd();

                targetClass.methods.add(method);
            } else if (Annotations.getVisible(methodNode, AbstractOverride.class) != null) {
                var originalMethods = targetClass.methods.stream().filter(a -> a.name.equals(methodNode.name) && a.desc.equals(methodNode.desc)).toList();

                if (originalMethods.isEmpty()) {
                    throw new IllegalStateException("Could not find method " + methodNode.name + methodNode.desc + " in class " + targetClass.name);
                }

                var originalMethod = originalMethods.get(0);
                targetClass.methods.remove(originalMethod);

                var node = new MethodNode(originalMethod.access & ~Opcodes.ACC_ABSTRACT, originalMethod.name, originalMethod.desc, originalMethod.signature, methodNode.exceptions != null ? methodNode.exceptions.toArray(String[]::new) : null);
                node.visitCode();

                for (AbstractInsnNode insnNode : methodNode.instructions) {
                    if (insnNode instanceof MethodInsnNode methodInsn) {
                        if (methodInsn.owner.equals(slashedMixinClassName)) {
                            methodInsn.owner = slashedTargetClassName;
                        }

                        node.instructions.add(methodInsn);
                    } else {
                        if (insnNode instanceof FieldInsnNode fieldInsn) {
                            if (fieldInsn.owner.equals(slashedMixinClassName)) {
                                fieldInsn.owner = slashedTargetClassName;
                            }

                            node.instructions.add(fieldInsn);
                        } else {
                            node.instructions.add(insnNode);
                        }
                    }
                }

                node.visitEnd();
                node.localVariables = methodNode.localVariables;

                targetClass.methods.add(node);
            }
        }

        for (MethodNode methodNode : targetClass.methods) {
            var createInitializer = Annotations.getInvisible(methodNode, CreateInitializer.class);
            if (createInitializer != null) {
                methodNode.invisibleAnnotations.remove(createInitializer);
                boolean wasFixed = false;
                InsnList fixedInstructions = new InsnList();
                for (AbstractInsnNode insnNode : methodNode.instructions) {
                    if (insnNode instanceof InvokeDynamicInsnNode invokeDynamicInsn) {
                        // Mixin injection might rename the lambda, we need to update the name accordingly.
                        if (Opcodes.H_INVOKESTATIC == invokeDynamicInsn.bsm.getTag()
                                && "metafactory".equals(invokeDynamicInsn.bsm.getName())
                                && LAMBDA_CLASS_NAME.equals(invokeDynamicInsn.bsm.getOwner())
                                && LAMBDA_METHOD_DESCRIPTOR.equals(invokeDynamicInsn.bsm.getDesc())
                                && invokeDynamicInsn.bsmArgs != null
                                && invokeDynamicInsn.bsmArgs.length == 3
                                && invokeDynamicInsn.bsmArgs[1] instanceof Handle target
                                && target.getOwner().equals(slashedTargetClassName)
                        ) {
                            var name = getMixinModifiedName(targetClass, target.getName());
                            if (!name.equals(target.getName())) {
                                fixedInstructions.add(new InvokeDynamicInsnNode(invokeDynamicInsn.name, invokeDynamicInsn.desc, invokeDynamicInsn.bsm,
                                        invokeDynamicInsn.bsmArgs[0],
                                        new Handle(target.getTag(), target.getOwner(), name, target.getDesc(), target.isInterface()),
                                        invokeDynamicInsn.bsmArgs[2]
                                ));
                                wasFixed = true;
                            } else {
                                fixedInstructions.add(invokeDynamicInsn);
                            }
                        } else {
                            fixedInstructions.add(invokeDynamicInsn);
                        }
                    } else {
                        fixedInstructions.add(insnNode);
                    }
                }
                if (wasFixed) {
                    methodNode.instructions.clear();
                    methodNode.instructions.add(fixedInstructions);
                }
            }
            if (extend != null && methodNode.name.equals("<init>")) {
                if (!containsThisCall(targetClass, methodNode.instructions)) {
                    methodsToRemove.add(methodNode);

                    var instructions = methodNode.instructions;
                    var insnList = new InsnList();

                    var visitor = new AnnotationValueVisitor();
                    extend.accept(visitor);
                    var className = ((Type) visitor.values.get("value")).getClassName();
                    targetClass.superName = className.replace(".", "/");
                    if (targetClass.signature != null)
                        targetClass.signature = targetClass.signature.replaceFirst("java/lang/Object", targetClass.superName);

                    boolean alreadyChanged = false;

                    for (AbstractInsnNode insn : instructions) {
                        if (!alreadyChanged && insn instanceof MethodInsnNode methodInsnNode && insn.getOpcode() == Opcodes.INVOKESPECIAL && methodInsnNode.owner.equals("java/lang/Object")) {
                            insnList.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, targetClass.superName, "<init>", "()V"));
                            alreadyChanged = true;
                        } else {
                            insnList.add(insn);
                        }
                    }

                    var oldThis = methodNode.localVariables.get(0);
                    var newLocals = new ArrayList<>(methodNode.localVariables);
                    newLocals.set(0, new LocalVariableNode(oldThis.name, "L" + targetClass.superName + ";", null, oldThis.start, oldThis.end, oldThis.index));

                    var newNode = new MethodNode(Opcodes.ASM9, methodNode.access, methodNode.name, methodNode.desc, methodNode.signature, methodNode.exceptions.toArray(new String[0]));
                    newNode.instructions = insnList;
                    newNode.localVariables = newLocals;
                    replacementNodes.add(newNode);
                }
            }
        }

        /*if (extend != null) {
            if (targetClass.superName.equals("net/minecraftforge/common/capabilities/CapabilityProvider")) {
                for (MethodNode node : targetClass.methods.stream().filter((node) -> node.name.equals("<init>")).toList()) {
                    if (containsThisCall(targetClass, node.instructions))
                        continue;

                    methodsToRemove.add(node);

                    var instructions = node.instructions;

                    var aload = instructions.get(2);
                    var invoke = instructions.get(3);

                    // remove Object.<init> call
                    instructions.remove(invoke);

                    var insnList = new InsnList();
                    insnList.add(new LdcInsnNode(Type.getObjectType(targetClass.name)));
                    insnList.add(new InsnNode(Opcodes.ICONST_0));
                    insnList.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, targetClass.superName, "<init>", "(Ljava/lang/Class;Z)V"));

                    instructions.insert(aload, insnList);

                    var newNode = new MethodNode(Opcodes.ASM9, node.access, node.name, node.desc, node.signature, node.exceptions.toArray(new String[0]));
                    newNode.instructions = instructions;
                    replacementNodes.add(newNode);
                }
            }
        }*/

        for (FieldNode fieldNode : fieldsToRemove) {
            classNode.fields.remove(fieldNode);
        }

        for (MethodNode methodNode : methodsToRemove) {
            classNode.methods.remove(methodNode);
            targetClass.methods.remove(methodNode);
        }

        targetClass.methods.addAll(replacementNodes);
    }
}
