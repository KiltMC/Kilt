package net.minecraftforge.coremod.api

import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode
import java.util.stream.Stream

/**
 * Specifies the target type for the [ITransformer.Target]. Note that the type of the transformer T
 * dictates what are acceptable targets for this transformer.
 */
class TargetType<T> private constructor(val name: String, val nodeType: Class<T>) {
    companion object {
        /**
         * Target a class, before field and method transforms operate. SHOULD ONLY BE USED to "replace" a complete class
         * The [ITransformer] T variable must refer to [org.objectweb.asm.tree.ClassNode]
         */
        val PRE_CLASS: TargetType<ClassNode> = TargetType(
            "PRE_CLASS",
            ClassNode::class.java
        )

        /**
         * Target a class. The [ITransformer] T variable must refer to [org.objectweb.asm.tree.ClassNode]
         */
        val CLASS: TargetType<ClassNode> = TargetType(
            "CLASS",
            ClassNode::class.java
        )

        /**
         * Target a method. The [ITransformer] T variable must refer to [org.objectweb.asm.tree.MethodNode]
         */
        val METHOD: TargetType<MethodNode> = TargetType(
            "METHOD",
            MethodNode::class.java
        )

        /**
         * Target a field. The [ITransformer] T variable must refer to [org.objectweb.asm.tree.FieldNode]
         */
        val FIELD: TargetType<FieldNode> = TargetType(
            "FIELD",
            FieldNode::class.java
        )

        val VALUES: Array<TargetType<*>> = arrayOf(PRE_CLASS, CLASS, METHOD, FIELD)

        fun byName(name: String): TargetType<*> {
            return Stream.of(*VALUES)
                .filter { type: TargetType<*> -> type.name == name }
                .findFirst()
                .orElseThrow {
                    IllegalArgumentException(
                        "No TargetType of name $name found"
                    )
                }
        }
    }
}