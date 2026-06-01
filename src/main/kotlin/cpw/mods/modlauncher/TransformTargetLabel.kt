package cpw.mods.modlauncher

import cpw.mods.modlauncher.api.ITransformer
import cpw.mods.modlauncher.api.TargetType
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import java.util.*

class TransformTargetLabel private constructor(className: String, val elementName: String, elementDescriptor: String, internal val targetType: TargetType<*>) {
    internal val className: Type = Type.getObjectType(className.replace(".", "/"))
    val elementDescriptor: Type = if (elementDescriptor.isNotEmpty())
        Type.getMethodType(elementDescriptor)
    else Type.VOID_TYPE

    internal constructor(target: ITransformer.Target<*>) : this(target.className, target.elementName, target.elementDescriptor, target.targetType)
    constructor(className: String, fieldName: String) : this(className, fieldName, "", TargetType.FIELD)
    internal constructor(className: String, methodName: String, methodDesc: String) : this(className, methodName, methodDesc, TargetType.METHOD)
    constructor(className: String) : this(className, "", "", TargetType.CLASS)
    constructor(className: String, type: TargetType<ClassNode>) : this(className, "", "", type)

    override fun hashCode(): Int {
        return Objects.hash(this.className, this.elementName, this.elementDescriptor)
    }

    override fun equals(other: Any?): Boolean {
        if (other is TransformTargetLabel) {
            return this.className == other.className && this.elementName == other.elementName && this.elementDescriptor == other.elementDescriptor
        }

        return false
    }

    override fun toString(): String {
        return "Target : $targetType {$className} {$elementName} {$elementDescriptor}"
    }
}
