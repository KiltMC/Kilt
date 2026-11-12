package xyz.bluspring.kilt.loader.remap.fixers.mixin

import net.fabricmc.loader.api.FabricLoader
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.loader.remap.KiltEnhancedRemapper
import xyz.bluspring.kilt.loader.remap.MixinHelpers
import xyz.bluspring.kilt.loader.remap.MixinTypes
import xyz.bluspring.kilt.util.KiltHelper

object MixinNamedLocalFixer {
    private var wasInit = false
    // class:
    //    <method name, method desc>:
    //       NeoForge local var name:
    //          local variable data
    private val localMappings: MutableMap<Type, Map<Pair<String, String>, Map<String, LocalVarData>>> by lazy {
        wasInit = true
        val mappings = mutableMapOf<Type, Map<Pair<String, String>, Map<String, LocalVarData>>>()

        var currentClass = Type.VOID_TYPE
        var currentMethod = "" to ""
        val currentMethodMappings = mutableMapOf<Pair<String, String>, Map<String, LocalVarData>>()
        val currentLocalMappings = mutableMapOf<String, LocalVarData>()

        MixinNamedLocalFixer::class.java.getResourceAsStream("/local_variable_mappings.txt")!!.bufferedReader()
            .forEachLine { line ->
                val split = line.trim().split("\t")

                if (split.isEmpty())
                    return@forEachLine

                when (split[0]) {
                    "c" -> {
                        if (currentLocalMappings.isNotEmpty())
                            currentMethodMappings[currentMethod] = currentLocalMappings.toMap()

                        if (currentMethodMappings.isNotEmpty())
                            mappings[currentClass] = currentMethodMappings.toMap()

                        currentClass = Type.getObjectType(split[1])
                        currentLocalMappings.clear()
                        currentMethodMappings.clear()
                    }

                    "m" -> {
                        if (currentLocalMappings.isNotEmpty())
                            currentMethodMappings[currentMethod] = currentLocalMappings.toMap()

                        currentMethod = split[1] to split[2]
                        currentLocalMappings.clear()
                    }

                    "p" -> {
                        currentLocalMappings[split[1]] = LocalVarData(true, Type.getType(split[2]), split[3].toInt())
                    }

                    "l" -> {
                        currentLocalMappings[split[1]] = LocalVarData(false, Type.getType(split[2]), split[3].toInt())
                    }
                }
            }

        if (currentLocalMappings.isNotEmpty())
            currentMethodMappings[currentMethod] = currentLocalMappings.toMap()

        if (currentMethodMappings.isNotEmpty())
            mappings[currentClass] = currentMethodMappings.toMap()

        mappings
    }

    @JvmRecord
    private data class LocalVarData(val isParam: Boolean, val type: Type, val ordinal: Int)

    // to handle:
    // - @ModifyVariable
    // - @Local
    // any Neo-added locals cannot be safely remapped, use InjectedShareAccessModifiers for this instead, which will be entirely manual.
    fun fixClass(classNode: ClassNode, remapper: KiltEnhancedRemapper) {
        // We should only be targeting Minecraft classes. Anything else is futile.
        val targetClasses = MixinHelpers.getMixinClassTargets(classNode).filter {
            val split = it.className.split(".")
            (split.size > 3 && split[0] == "com" && split[1] == "mojang" && (split[2] == "blaze3d" || split[2] == "math" || split[2] == "realmsclient"))
                || (split.size > 2 && split[0] == "net" && split[1] == "minecraft")
        }

        if (targetClasses.isEmpty())
            return

        var hasModified = false
        for (methodNode in classNode.methods) {
            run {
                val annotations = KiltHelper.mergeNullableCollections(methodNode.visibleAnnotations, methodNode.invisibleAnnotations)

                if (annotations.any { it.desc == MixinTypes.MODIFY_VARIABLE.descriptor }) {
                    for (annotationNode in annotations.filter { it.desc == MixinTypes.MODIFY_VARIABLE.descriptor }) {
                        if (tryMapLocal(annotationNode, targetClasses, methodNode, remapper))
                            hasModified = true
                    }
                }

                MixinHelpers.nestedModifyAnnotations(annotations.filter { it.desc == MixinTypes.DEFINITIONS.descriptor || it.desc == MixinTypes.DEFINITION.descriptor }.toMutableList()) { annotationNode ->
                    if (annotationNode.desc == MixinTypes.LOCAL.descriptor) {
                        if (tryMapLocal(annotationNode, targetClasses, methodNode, remapper))
                            hasModified = true
                    }
                }
            }

            run {
                if (methodNode.visibleParameterAnnotations != null) {
                    for (annotations in methodNode.visibleParameterAnnotations) {
                        if (annotations == null) continue

                        MixinHelpers.nestedModifyAnnotations(annotations.toMutableList()) { annotationNode ->
                            if (annotationNode.desc == MixinTypes.LOCAL.descriptor) {
                                if (tryMapLocal(annotationNode, targetClasses, methodNode, remapper))
                                    hasModified = true
                            }
                        }
                    }
                }

                if (methodNode.invisibleParameterAnnotations != null) {
                    for (annotations in methodNode.invisibleParameterAnnotations) {
                        if (annotations == null) continue

                        MixinHelpers.nestedModifyAnnotations(annotations.toMutableList()) { annotationNode ->
                            if (annotationNode.desc == MixinTypes.LOCAL.descriptor) {
                                if (tryMapLocal(annotationNode, targetClasses, methodNode, remapper))
                                    hasModified = true
                            }
                        }
                    }
                }
            }
        }

        if (hasModified) {
            println("Fixed locals for ${classNode.name}")
        }
    }

    private fun println(text: String) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment)
            Kilt.logger.debug(text)
    }

    private fun findMethodPairs(methodNode: MethodNode, remapper: KiltEnhancedRemapper, targetClasses: Collection<Type>): Collection<Pair<String, String>> {
        val pairs = mutableSetOf<Pair<String, String>>()

        val annotations = KiltHelper.mergeNullableCollections(methodNode.visibleAnnotations, methodNode.invisibleAnnotations)
        for (annotation in annotations) {
            if (annotation.values == null || annotation.values.isEmpty())
                continue

            val values = MixinHelpers.annotationValuesToMap(annotation.values)

            if (values.contains("method")) {
                val methodValues = values["method"]!!.let {
                    when (it) {
                        is String -> listOf(it)
                        is List<*> -> it as List<String>
                        else -> throw IllegalStateException()
                    }
                }

                // special case disable MixinSquared from this equation
                if (methodValues.contains("@MixinSquared:Handler"))
                    continue

                for (methodValue in methodValues) {
                    val (_, member, descriptor, _, _) = MixinHelpers.findMemberData(methodValue)

                    if (descriptor.isBlank() && annotation.desc == MixinTypes.INJECT.descriptor) {
                        // Assume there's no @Coerce involved
                        // if there's @Coerce involved, god help us
                        val descHintSplit = methodNode.desc.split("Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo")
                        val descHint = descHintSplit
                            .firstOrNull()

                        if (descHint != null) {
                            // educated adventure time
                            for (type in targetClasses) {
                                remapper.getClass(type.className)
                                    .ifPresent { cls ->
                                        for (optional in cls.methods) {
                                            if (optional.isPresent) {
                                                val method = optional.orElseThrow()

                                                if (method.name == methodNode.name && methodNode.desc.startsWith("$descHint)"))
                                                    pairs += method.name to method.descriptor
                                            }
                                        }
                                    }
                            }

                            // let's also just build it based on what we know too.

                            if (descHintSplit.size > 1) {
                                if (descHintSplit[1].startsWith(";")) // likely to be CallbackInfo
                                    pairs += member to "$descHint)V"
                                else if (methodNode.signature != null) {
                                    // okay, this is likely CallbackInfoReturnable.
                                    val signatures = MixinHelpers.splitSignature(methodNode.signature)

                                    for (sig in signatures) {
                                        if (sig.startsWith(MixinTypes.CALLBACK_INFO_RETURNABLE.descriptor)) {
                                            val genericParts = sig.removePrefix(MixinTypes.CALLBACK_INFO_RETURNABLE.descriptor)
                                            val assumedReturnType = genericParts.removeSurrounding("<+", ">")
                                            pairs += member to "$descHint)$assumedReturnType"
                                            when (assumedReturnType) {
                                                // primitives
                                                "Ljava/lang/Integer;" -> pairs += member to "$descHint)I"
                                                "Ljava/lang/Float;" -> pairs += member to "$descHint)F"
                                                "Ljava/lang/Long;" -> pairs += member to "$descHint)J"
                                                "Ljava/lang/Double;" -> pairs += member to "$descHint)D"
                                                "Ljava/lang/Boolean;" -> pairs += member to "$descHint)Z"
                                                "Ljava/lang/Short;" -> pairs += member to "$descHint)S"
                                                "Ljava/lang/Byte;" -> pairs += member to "$descHint)B"

                                                // array
                                                "[Ljava/lang/Integer;" -> pairs += member to "$descHint)[I"
                                                "[Ljava/lang/Float;" -> pairs += member to "$descHint)[F"
                                                "[Ljava/lang/Long;" -> pairs += member to "$descHint)[J"
                                                "[Ljava/lang/Double;" -> pairs += member to "$descHint)[D"
                                                "[Ljava/lang/Boolean;" -> pairs += member to "$descHint)[Z"
                                                "[Ljava/lang/Short;" -> pairs += member to "$descHint)[S"
                                                "[Ljava/lang/Byte;" -> pairs += member to "$descHint)[B"
                                            }

                                            break
                                        }
                                    }
                                }
                            } else {
                                pairs += member to descHint
                            }
                        }
                    }

                    if (descriptor.isBlank()) {
                        // welp, time to adventure!
                        for (type in targetClasses) {
                            remapper.getClass(type.className)
                                .ifPresent { cls ->
                                    for (optional in cls.methods) {
                                        if (optional.isPresent) {
                                            val method = optional.orElseThrow()

                                            if (method.name == methodNode.name)
                                                pairs += method.name to method.descriptor
                                        }
                                    }
                                }
                        }
                    } else {
                        pairs += member to descriptor
                    }
                }
            }
        }

        return pairs
    }

    private fun tryMapLocal(annotationNode: AnnotationNode, targetClasses: Collection<Type>, methodNode: MethodNode, remapper: KiltEnhancedRemapper): Boolean {
        if (annotationNode.values == null) return false

        val values = MixinHelpers.annotationValuesToMap(annotationNode.values).toMutableMap()
        var newOrdinal = -1

        if (values.contains("name") && !values.contains("ordinal") && !values.contains("index")) {
            val names = if (values["name"] is List<*>)
                values["name"] as List<String>
            else
                listOf(values["name"] as String)

            val methodPairs = findMethodPairs(methodNode, remapper, targetClasses)

            for (name in names) {
                for (type in targetClasses) {
                    for (methodPair in methodPairs) {
                        val localData = localMappings[type]?.get(methodPair)?.get(name)

                        if (localData != null) {
                            newOrdinal = localData.ordinal
                        }
                    }
                }
            }
        }

        if (newOrdinal != -1) {
            values.remove("name")
            values["ordinal"] = newOrdinal

            annotationNode.values = MixinHelpers.mapToAnnotationValues(values)
            return true
        }

        return false
    }

    fun discard() {
        if (this.wasInit) {
            this.localMappings.clear()
        }
    }
}
