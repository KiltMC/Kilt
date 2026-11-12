package xyz.bluspring.kilt.loader.remap

import org.objectweb.asm.Type
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import xyz.bluspring.kilt.util.KiltHelper

object MixinHelpers {
    fun mapToAnnotationValues(map: Map<String, Any>): List<Any> {
        val values = mutableListOf<Any>()

        for ((key, v) in map) {
            values.add(key)
            values.add(v)
        }

        return values
    }

    fun annotationValuesToMap(values: List<Any>): Map<String, Any> {
        val map = mutableMapOf<String, Any>()

        var currentKey = ""
        for ((index, value) in values.withIndex()) {
            if ((index and 1) == 0) {
                currentKey = value as String
            } else {
                map[currentKey] = value
            }
        }

        return map
    }

    fun getMixinClassTargets(
        classNode: ClassNode,
        mixinAnnotation: AnnotationNode = KiltHelper.mergeNullableCollections(classNode.visibleAnnotations, classNode.invisibleAnnotations)
            .firstOrNull { it.desc == MixinTypes.MIXIN.descriptor }
            ?: throw IllegalStateException("Failed to locate mixin annotations!"),
        values: Map<String, Any> = MixinHelpers.annotationValuesToMap(mixinAnnotation.values)
    ): Collection<Type> {
        val targetClasses = mutableListOf<Type>()

        if (values.contains("value")) {
            if (values["value"] is List<*>) {
                targetClasses.addAll((values["value"] as List<Type>))
            } else if (values["value"] is Type) {
                targetClasses.add((values["value"] as Type))
            }
        }

        if (values.contains("targets")) {
            if (values["targets"] is List<*>) {
                targetClasses.addAll((values["targets"] as List<String>).mapNotNull { tryParseAsType(it) ?: return@mapNotNull null })
            } else if (values["targets"] is String) {
                targetClasses.add(tryParseAsType(values["targets"] as String) ?: return targetClasses)
            }
        }

        return targetClasses
    }

    fun nestedModifyAnnotations(annotations: MutableList<AnnotationNode>, modifier: (AnnotationNode) -> Unit) {
        for (annotation in annotations) {
            modifier(annotation)

            if (annotation.values != null && annotation.values.isNotEmpty()) {
                var hasChanges = false
                val values = annotationValuesToMap(annotation.values).toMutableMap()
                for ((key, value) in values) {
                    if (value is AnnotationNode) {
                        nestedModifyAnnotations(mutableListOf(value), modifier)
                        hasChanges = true
                    } else if (value is List<*> && value.any { it is AnnotationNode }) {
                        val list = (value as List<AnnotationNode>).toMutableList()

                        nestedModifyAnnotations(list, modifier)
                        values[key] = list
                        hasChanges = true
                    }
                }

                if (hasChanges)
                    annotation.values = mapToAnnotationValues(values)
            }
        }
    }

    private fun tryParseAsType(className: String): Type? {
        if (className.isBlank())
            return null

        if (className.startsWith("L") && className.endsWith(";"))
            return Type.getType(className.replace(".", "/"))

        if (className.contains(".") || className.contains("/")) {
            val split = className.split(".", "/")
            // need to try to normalize the class name, otherwise we might not reliably find these classes.

            // we can only trust these sources for competently named classes, so let's try to break through the parsing like that.
            if (split.size > 2 && (split[0] == "com" && split[1] == "mojang") || (split[0] == "net" && (split[1] == "minecraft" || split[1] == "neoforged"))) {
                var normalizedName = split[0]
                var isFirstClass = true
                for (string in split.drop(1)) {
                    normalizedName += if (string.lowercase()[0] == string[0]) {
                        "/" // package
                    } else if (isFirstClass) {
                        isFirstClass = false // class
                        "/"
                    } else {
                        "$" // inner class
                    }

                    normalizedName += string
                }

                return Type.getObjectType(normalizedName)
            }
        }

        // let's just force it here, i guess.
        return Type.getObjectType(className.replace(".", "/"))
    }

    fun findMemberData(value: String): MemberData {
        // Usually the format consists of a descriptor at the beginning for the target class.
        // However, some mixins may also not have that.
        val classDescriptor = if (value.startsWith("L") && value.contains(";"))
            value.replaceAfter(';', "")
        else if (value.contains(".")) // ah, here's the cursed format.
            "L${value.replaceAfterLast(".", "").removeSuffix(".").replace('.', '/')};"
        else if (value.contains("/")) { // oh no, there's another cursed format in town.
            val split = value.split("/")
            if (split.last().contains("("))
                "L${split.dropLast(1).joinToString("/")};"
            else ""
        } else
            ""

        // Member + descriptor is pretty easy to get
        val memberWithDescriptor = if (value.contains("."))
            value.replaceBeforeLast(".", "").removePrefix(".")
        else
            value.removePrefix(classDescriptor)
        val isField = memberWithDescriptor.contains(":") // Full field targets consist of a : - fieldName:Ldescriptor/Here;
        val isMethod = memberWithDescriptor.contains("(") // Full method targets consist of ( : - methodName(Ldescriptor/Here;)V

        val member = if (isField)
        // Field targets usually consist of a :
            memberWithDescriptor.replaceAfter(':', "").removeSuffix(":")
        else if (isMethod)
        // Method targets typically follow the typical methodName()V rule
            memberWithDescriptor.replaceAfter('(', "").removeSuffix("(")
        else
        // In this case, it's much harder to figure out. In this case, we follow two passes.
            memberWithDescriptor

        val descriptor = if (isField)
        // Field descriptor, we can safely remove the member name + :
            memberWithDescriptor.removePrefix("$member:")
        else if (isMethod)
        // This one's even easier, it's just memberName()V
            memberWithDescriptor.removePrefix(member)
        else
        // We don't know what the descriptor is, so assume blank.
            ""

        return MemberData(classDescriptor, member, descriptor, isField, isMethod)
    }

    @JvmRecord
    data class MemberData(
        val classDescriptor: String,
        val member: String,
        val descriptor: String,
        val isField: Boolean,
        val isMethod: Boolean,
    )

//        fun splitDescriptor(descriptor: String): List<String> {
//            val split = mutableListOf<String>()
//
//            var incompleteString = ""
//            var isInArray = false
//            var isInClass = false
//
//            for (ch in descriptor) {
//                incompleteString += ch
//
//                if (ch == '[') {
//                    isInArray = true
//                } else if (ch == 'L') {
//                    isInClass = true
//                } else if (ch == ';' && isInClass) {
//                    isInClass = false
//                    isInArray = false
//                    split.add(incompleteString)
//                    incompleteString = ""
//                } else if (!isInClass) {
//                    if (isInArray)
//                        isInArray = false
//
//                    split.add(incompleteString)
//                    incompleteString = ""
//                }
//            }
//
//            return split
//        }
//
    fun splitSignature(descriptor: String): List<String> {
        val split = mutableListOf<String>()
        val current = mutableListOf<String>()

        var incompleteString = ""
        var isInArray = false
        var isInClass = false
        var genericLayer = 0

        for (ch in descriptor) {
            incompleteString += ch

            if (ch == '<') {
                genericLayer++
            } else if (ch == '>') {
                if (--genericLayer <= 0) {
                    current.add(incompleteString)
                    incompleteString = ""
                    genericLayer = 0
                }
            } else if (ch == '[') {
                isInArray = true
            } else if (ch == 'L') {
                isInClass = true
            } else if (ch == ';' && isInClass && genericLayer <= 0) {
                isInClass = false
                isInArray = false
                current.add(incompleteString)
                split.add(current.joinToString(""))
                current.clear()
                incompleteString = ""
            } else if (!isInClass && genericLayer <= 0) {
                if (isInArray)
                    isInArray = false

                split.add(incompleteString)
                incompleteString = ""
            }
        }

        if (current.isNotEmpty())
            split.add(current.joinToString(""))
        return split
    }
}
