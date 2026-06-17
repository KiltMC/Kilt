package xyz.bluspring.kilt.loader.remap.fixers.mixin

import kotlinx.atomicfu.locks.synchronized
import net.fabricmc.loader.api.FabricLoader
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor
import org.spongepowered.asm.mixin.gen.Invoker
import org.spongepowered.asm.mixin.injection.Inject
import xyz.bluspring.kilt.loader.mixin.modifications.KiltMixinModifications
import xyz.bluspring.kilt.loader.remap.KiltEnhancedRemapper
import xyz.bluspring.kilt.loader.remap.KiltRemapper
import xyz.bluspring.kilt.loader.remap.MixinRefmap
import xyz.bluspring.kilt.util.KiltHelper
import java.util.*

// Remaps all mixins and their associated refmaps
object MixinRemapper {
    private val targetClassNodeCache = Collections.synchronizedMap<String, ClassNode>(mutableMapOf())

    @JvmField val MIXIN_TYPE: Type = Type.getType(Mixin::class.java)
    private val ACCESSOR_TYPE = Type.getType(Accessor::class.java)
    private val INVOKER_TYPE = Type.getType(Invoker::class.java)
    private val INJECT_TYPE = Type.getType(Inject::class.java)

    fun remapMixinAnnotation(
        annotationNode: AnnotationNode,
        remapper: KiltEnhancedRemapper,
        classTargets: Collection<String>,
        mixinClassName: String?,
        methodName: String, methodDesc: String,
        mixinMapping: MutableMap<String, String> = mutableMapOf(),
        alreadyRefmapped: MutableSet<String> = Collections.synchronizedSet(mutableSetOf<String>()),
        refmap: MixinRefmap? = null
    ): AnnotationNode {
        val values = KiltMixinModifications.annotationValuesToMap(annotationNode.values ?: emptyList()).toMutableMap()

        // Remap accessor/invoker
        if (annotationNode.desc == ACCESSOR_TYPE.descriptor || annotationNode.desc == INVOKER_TYPE.descriptor) {
            val value = (values["value"] as? String ?: methodName.run {
                val value = (if (this.startsWith("get"))
                    this.removePrefix("get")
                else if (this.startsWith("is"))
                    this.removePrefix("is")
                else if (this.startsWith("set"))
                    this.removePrefix("set")
                else if (this.startsWith("invoke"))
                    this.removePrefix("invoke")
                else if (this.startsWith("call"))
                    this.removePrefix("call")
                else
                    this)

                if (value.uppercase() == value) // don't lowercase the first char if it's all uppercase
                    value
                else
                    value.replaceFirstChar { it.lowercaseChar() }
            }) + (if (annotationNode.desc == ACCESSOR_TYPE.descriptor)
                ":${if (methodDesc.endsWith(")V"))
                    methodDesc.removePrefix("(").removeSuffix(")V")
                else
                    methodDesc.removePrefix("()")}"
            else
                methodDesc
                )

            if (mixinMapping.contains(value)) {
                if (!alreadyRefmapped.add(value))
                    return annotationNode

                val mapped = remapTargetString(mixinMapping[value]!!, classTargets, remapper)
                mixinMapping[value] = mapped.replaceAfter(":", "").removeSuffix(":").replaceAfter("(", "").removeSuffix("(")
            } else {
                values["value"] = remapTargetString(value, classTargets, remapper).replaceAfter(":", "").removeSuffix(":").replaceAfter("(", "").removeSuffix("(")
                annotationNode.values = KiltMixinModifications.mapToAnnotationValues(values)
            }

            return annotationNode
        }

        fun tryRemapMixinRefmap(value: String, isTarget: Boolean = true): String {
            // Do NOT remap this - this typically indicates MixinSquared or other extensions.
            if (value.startsWith("@"))
                return value

            return if (mixinMapping.contains(value)) {
                if (!alreadyRefmapped.add(value))
                    return value

                val original = mixinMapping[value]!!
                val mapped = remapTargetString(original, classTargets, remapper, isTarget)

                mixinMapping[value] = mapped

                value
            } else {

                val mapped = remapTargetString(value, classTargets, remapper, isTarget, if (annotationNode.desc == INJECT_TYPE.descriptor) methodDesc else null)

                if (refmap != null) {
                    mixinMapping[value] = mapped
                    return value
                }

                mapped
            }
        }

        // We cannot search for mixin annotations specifically, because there's a chance
        // we might miss custom annotations.
        // Usually this can be a good indicator of a mixin annotation.
        if (values.containsKey("method")) {
            val methodValue = values["method"]!!

            fun remapMethodTarget(methodValue: String): Collection<String> {
                if (methodValue.contains("*")) { // Need to remap wildcards
                    if (methodValue == "*")
                        return listOf("*")

                    val split = methodValue.split("*")
                    if (split.size == 1 && methodValue.startsWith("*")) {
                        return listOf(tryRemapMixinRefmap(split[0], false))
                    } else {
                        val targets = mutableSetOf<String>()

                        for (classTarget in classTargets) {
                            val possible = KiltRemapper.mojMappedMethods.filter { it.value.contains(classTarget) && it.key.startsWith(split[0]) }
                                .mapValues { it.value.getValue(classTarget) }
                                .values
                                .flatten()

                            for ((methodName, methodDesc) in possible) {
                                targets.add("$methodName$methodDesc")
                            }
                        }

                        return targets.toList()
                    }
                } else {
                    return listOf(tryRemapMixinRefmap(methodValue, false))
                }
            }

            if (methodValue is String) {
                values["method"] = remapMethodTarget(methodValue)
            } else if (methodValue is List<*>) {
                values["method"] = methodValue.flatMap {
                    remapMethodTarget(it as String)
                }
            }
        }

        fun remapAtValue(atValue: AnnotationNode) {
            if (atValue.values == null)
                return

            val atValues = KiltMixinModifications.annotationValuesToMap(atValue.values).toMutableMap()

            // Remap targets
            if (atValues.contains("target")) {
                val targetValue = atValues["target"]!!

                if (targetValue is String) {
                    atValues["target"] = tryRemapMixinRefmap(targetValue)
                }
            }

            if (atValues.contains("desc")) {
                // TODO: i'm not even going to try.
                KiltRemapper.logger.error("!! Tell BluSpring to stop being lazy. $mixinClassName")
            }

            atValue.values = KiltMixinModifications.mapToAnnotationValues(atValues)
        }

        // This can also be a very good indicator, but it's a little weird
        if (values.containsKey("at")) {
            val atValue = values["at"]!!

            if (atValue is AnnotationNode) {
                remapAtValue(atValue)
            } else if (atValue is List<*>) {
                val newList = mutableListOf<Any?>()

                for (atValueIndiv in atValue) {
                    if (atValueIndiv is AnnotationNode)
                        remapAtValue(atValueIndiv)

                    newList.add(atValueIndiv)
                }

                values["at"] = newList
            }
        }

        // This one's also a lot stranger, but needs to be handled
        if (values.contains("slice")) {
            fun remapSliceValue(sliceValue: AnnotationNode) {
                if (sliceValue.values == null)
                    return

                val sliceValues = KiltMixinModifications.annotationValuesToMap(sliceValue.values).toMutableMap()

                if (sliceValues.contains("from") && sliceValues["from"] is AnnotationNode) {
                    remapAtValue(sliceValues["from"] as AnnotationNode)
                }

                if (sliceValues.contains("to") && sliceValues["to"] is AnnotationNode) {
                    remapAtValue(sliceValues["to"] as AnnotationNode)
                }

                sliceValue.values = KiltMixinModifications.mapToAnnotationValues(sliceValues)
            }

            val sliceValue = values["slice"]!!

            if (sliceValue is AnnotationNode) {
                remapSliceValue(sliceValue)
            } else if (sliceValue is List<*>) {
                val newSliceValues = mutableListOf<Any?>()

                for (value in sliceValue) {
                    if (value is AnnotationNode) {
                        remapSliceValue(value)
                    }

                    newSliceValues.add(value)
                }

                values["slice"] = newSliceValues
            }
        }

        // Now to change the values list
        annotationNode.values = KiltMixinModifications.mapToAnnotationValues(values)
        return annotationNode
    }

    fun remapMixinAnnotations(
        annotations: MutableList<AnnotationNode>,
        remapper: KiltEnhancedRemapper,
        classTargets: Collection<String>,
        mixinClassName: String?,
        methodName: String = "", methodDesc: String = "",
        mixinMapping: MutableMap<String, String> = mutableMapOf(),
        alreadyRefmapped: MutableSet<String> = Collections.synchronizedSet(mutableSetOf<String>()),
        refmap: MixinRefmap? = null
    ) {
        for (annotationNode in annotations) {
            remapMixinAnnotation(
                annotationNode, remapper, classTargets, mixinClassName, methodName,
                methodDesc, mixinMapping, alreadyRefmapped, refmap
            )
        }
    }

    private fun tryRemapMixinAnnotations(annotations: MutableList<AnnotationNode>): MutableList<AnnotationNode> {
        val updated = mutableListOf<AnnotationNode>()

        for (annotationNode in annotations) {
            if (annotationNode.desc == MIXIN_TYPE.descriptor) {
                val values = KiltMixinModifications.annotationValuesToMap(annotationNode.values).toMutableMap()
                if (values.contains("targets")) {
                    val targets = values["targets"]!!

                    if (targets is String) {
                        values["targets"] = KiltRemapper.remapClass(targets.replace(".", "/").removeSurrounding("L", ";"))
                        updated.add(AnnotationNode(Opcodes.ASM9, MIXIN_TYPE.descriptor).apply {
                            this.values = KiltMixinModifications.mapToAnnotationValues(values)
                        })
                        continue
                    } else if (targets is List<*>) {
                        values["targets"] = targets.map { KiltRemapper.remapClass((it as String).replace(".", "/").removeSurrounding("L", ";")) }
                        updated.add(AnnotationNode(Opcodes.ASM9, MIXIN_TYPE.descriptor).apply {
                            this.values = KiltMixinModifications.mapToAnnotationValues(values)
                        })
                        continue
                    }
                }
            }

            updated.add(annotationNode)
        }

        return updated
    }

    fun remapClass(classNode: ClassNode, remapper: KiltEnhancedRemapper, refmaps: Collection<MixinRefmap>) {
        val alreadyRefmapped = Collections.synchronizedSet(mutableSetOf<String>())

        val classTargets = getMixinClassTargets(classNode)

        // Find the refmap associated with this mixin class.
        val refmap = synchronized(refmaps) {
            refmaps.firstOrNull { refmap -> refmap.mappings.contains(classNode.name) }
        }

        // Then, get the mappings that exist with this mixin class.
        val mixinMapping = refmap?.mappings?.get(classNode.name)
            // If one does not exist, just provide an empty map.
            ?: mutableMapOf()

        // The idea is if we don't have a matching mapping, we need to remap directly on the string instead.

        for (method in classNode.methods) {
            if (method.visibleAnnotations != null) {
                remapMixinAnnotations(
                    method.visibleAnnotations!!.toMutableList(), remapper, classTargets,
                    classNode.name, method.name, method.desc,
                    mixinMapping, alreadyRefmapped, refmap
                )
            }

            if (method.invisibleAnnotations != null) {
                remapMixinAnnotations(
                    method.invisibleAnnotations!!.toMutableList(), remapper, classTargets,
                    classNode.name, method.name, method.desc,
                    mixinMapping, alreadyRefmapped, refmap
                )
            }
        }

        // Remap class targets
        run {
            if (classNode.visibleAnnotations != null) {
                classNode.visibleAnnotations = tryRemapMixinAnnotations(classNode.visibleAnnotations)
            }

            if (classNode.invisibleAnnotations != null) {
                classNode.invisibleAnnotations = tryRemapMixinAnnotations(classNode.invisibleAnnotations)
            }
        }

        // Have another pass specifically to remap things that were missed in the original remapping.
        if (refmap != null) {
            synchronized(mixinMapping) {
                for ((key, original) in mixinMapping) {
                    if (!alreadyRefmapped.add(key))
                        continue

                    val mapped = remapTargetString(original, classTargets, remapper)
                    mixinMapping[key] = mapped
                }
            }

            // Add the already refmapped data as a list, for us to use after.
            if (refmap.alreadyRefmapped.contains(classNode.name)) {
                // If there already exists one, let's try to merge them.
                refmap.alreadyRefmapped[classNode.name]!!.addAll(alreadyRefmapped)
            } else {
                // If not, just assign it.
                refmap.alreadyRefmapped[classNode.name] = alreadyRefmapped
            }
        }
    }

    fun remapUnmappedRefmaps(refmaps: Collection<MixinRefmap>, remapper: KiltEnhancedRemapper) {
        for (refmap in refmaps) {
            synchronized(refmap.mappings) {
                for ((className, mapping) in refmap.mappings) {
                    val existing = refmap.alreadyRefmapped[className] ?: emptyList()

                    synchronized(mapping) {
                        for ((key, original) in mapping) {
                            if (existing.contains(key))
                                continue

                            mapping[key] = remapTargetString(original, emptyList(), remapper)
                        }
                    }
                }
            }
        }
    }

    // Usually, the format is:
    //  - class: pkg/to/ClassName
    //  - field:
    //      - fieldName:I
    //      - Lpkg/to/ClassName;fieldName:I
    //  - method:
    //      - methodName(ILother/descriptor/Stuff;)V
    //      - Lpkg/to/ClassName;methodName(ILother/descriptor/Stuff;)V
    //      - pkg/to/ClassName.methodName(IL/other/descriptor/Stuff;)V // this is the cursed one.
    //      - pkg/to/ClassName/methodName(IL/other/descriptor/Stuff;)V // another one?? seriously???
    // however, some mods also completely disregard this format, so we have to keep that in mind.
    // i cannot remember what cursed formats they used though, is the problem....
    fun remapTargetString(
        value: String,
        classTargets: Collection<String>,
        remapper: KiltEnhancedRemapper,
        isTarget: Boolean = true,
        descriptorHint: String? = null // if this is an @Inject, the descriptor of the injector method itself
    ): String {
        if (FabricLoader.getInstance().isDevelopmentEnvironment && !KiltRemapper.forceProductionRemap && !value.startsWith("lambda$"))
            return value

        // Class reference, we can just return it directly.
        if (value.contains("/") && !value.startsWith("L") && !value.contains(";") && !value.contains("(")) {
            return KiltRemapper.remapClass(value, ignoreWorkaround = true).breakpoint()
        }

        // Special case for NEW target in @At, the target becomes a descriptor if there's a constructor reference.
        if (value.startsWith("(") && value.endsWith(";")) {
            return KiltRemapper.remapDescriptor(value).breakpoint()
        }

        // Usually the format consists of a descriptor at the beginning for the target class.
        // However, some mixins may also not have that.
        val classDescriptor = if (value.startsWith("L") && value.contains(";"))
            value.replaceAfter(';', "")
        else if (value.contains(".")) // ah, here's the cursed format.
            "L${value.replaceAfter(".", "").removeSuffix(".")};"
        else if (value.contains("/")) { // oh no, there's another cursed format in town.
            val split = value.split("/")
            if (split.last().contains("("))
                "L${split.dropLast(1).joinToString("/")};"
            else ""
        } else
            ""

        val mappedClassDescriptor = KiltRemapper.remapDescriptor(classDescriptor)
        val className = classDescriptor.removeSurrounding("L", ";")

        // Member + descriptor is pretty easy to get
        val memberWithDescriptor = if (value.contains("."))
            value.removePrefix("$className.")
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

        val mappedDescriptor = KiltRemapper.remapDescriptor(descriptor)

        // Some special overrides
        if ((className == "net/minecraft/client/particle/ParticleEngine" || classTargets.contains("net/minecraft/client/particle/ParticleEngine")) && member == "f_107293_") {
            // what the fuck is an interpolation string
            return $$"$${mappedClassDescriptor}kilt$providers:$$mappedDescriptor"
        }

        if (isField) {
            // We can safely use both member + descriptor here, because we know each and every component.
            if (!className.isBlank()) {
                val mapped = remapper.mapFieldName(className, member, descriptor)
                // If we found a target that actually remaps, we can safely assume it works correctly.
                if (mapped != member)
                    return "$mappedClassDescriptor$mapped:$mappedDescriptor".breakpoint()
            }

            for (target in classTargets) {
                val mapped = remapper.mapFieldName(target, member, descriptor)
                // If we found a target that actually remaps, we can safely assume it works correctly.
                if (mapped != member)
                    return "$mappedClassDescriptor$mapped:$mappedDescriptor".breakpoint()
            }

            // If not, let's return the member but with remapped descriptors
            return "$mappedClassDescriptor$member:$mappedDescriptor".breakpoint()
        } else if (isMethod) {
            if (member == "<init>" || member == "<clinit>") {
                // We can safely escape here.
                return "$mappedClassDescriptor$member$mappedDescriptor".breakpoint()
            }

            // Same with over here, we can safely use both member + descriptor here.
            if (!className.isBlank()) {
                val mapped = remapper.mapMethodName(className, member, descriptor)
                // If we found a target that actually remaps, we can safely assume it works correctly.
                if (mapped != member)
                    return "$mappedClassDescriptor$mapped$mappedDescriptor".breakpoint()
            }

            for (target in classTargets) {
                val mapped = remapper.mapMethodName(target, member, descriptor)
                // If we found a target that actually remaps, we can safely assume it works correctly.
                if (mapped != member)
                    return "$mappedClassDescriptor$mapped$mappedDescriptor".breakpoint()
            }
        }

        // Time to guess, we don't have access to most information. But also, a lot of these are essentially "just to be safe" fallbacks.

        // If the descriptor is blank, we cannot safely determine what it actually is.
        // So, guesswork time. Hopefully this doesn't come up too often, if at all.
        if (descriptor.isBlank()) {
            if (KiltRemapper.mojMappedMethods.contains(member)) {
                for ((ownerClass, mappedPairs) in KiltRemapper.mojMappedMethods[member]!!) {
                    if (classTargets.contains(ownerClass)) {
                        val classNode = this.getAllTargetClassNodes(remapper, listOf(ownerClass)).firstOrNull()
                            ?: return "$mappedClassDescriptor${mappedPairs.first().first}".breakpoint()

                        var bestCandidate: String? = null

                        // Assume there's no @Coerce involved
                        // if there's @Coerce involved, god help us
                        val descHint = descriptorHint?.split("Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo")?.firstOrNull()

                        for ((methodName, methodDesc) in mappedPairs) {
                            val methodNode = classNode.methods.firstOrNull { it.name == member && remapper.mapMethodDesc(it.desc) == methodDesc }
                                ?: continue

                            // Ignore all bridge method nodes, just so we don't screw up.
                            if ((methodNode.access and Opcodes.ACC_BRIDGE) != 0)
                                continue

                            if (bestCandidate == null) {
                                bestCandidate = "$mappedClassDescriptor$methodName"
                            }

                            if (descHint != null && methodNode.desc.startsWith(descHint)) {
                                // this looks like it'll inject successfully, no better we can do
                                bestCandidate = "$mappedClassDescriptor$methodName"
                                break
                            }
                        }

                        if (bestCandidate == null) {
                            bestCandidate = "$mappedClassDescriptor${mappedPairs.first().first}"
                        }

                        return bestCandidate.breakpoint()
                    }
                }

                if (isTarget) {
                    // If we're dealing with target methods, we can probably just use the first-name remap.
                    return KiltRemapper.mojMappedMethods[member]!!.values.first().first().first.breakpoint()
                }
            }

            if (!isTarget) {
                // Can't really do first-name remaps safely here, we need to iterate through the class targets
                // to figure out which method we *are* targeting.
                for (target in classTargets) {
                    val currentClass = remapper.getClass(target).orElse(null) ?: continue

                    for (methodOpt in currentClass.methods) {
                        val method = methodOpt.orElse(null) ?: continue

                        val declaringClass = method.declaringClass
                        if (method.name == member && currentClass == declaringClass) {
                            // oh hey look, we found one
                            return "$mappedClassDescriptor${remapper.mapMethodName(target, method.name, method.descriptor)}".breakpoint()
                        }
                    }
                }
            }

            if (KiltRemapper.mojMappedFields.contains(member)) {
                for ((ownerClass, mappedName) in KiltRemapper.mojMappedFields[member]!!) {
                    if (classTargets.contains(ownerClass))
                        return "$mappedClassDescriptor$mappedName".breakpoint()
                }

                return "$mappedClassDescriptor${KiltRemapper.mojMappedFields[member]!!.values.first()}".breakpoint()
            }
        } else if (classDescriptor.isBlank()) {
            // If the class descriptor is blank, we're going to struggle to find any information we need, but we can still use some information to find stuff.
            // We can try to use class targets for this.
            for (classTarget in classTargets) {
                val mappedField = remapper.mapFieldName(classTarget, member, descriptor)
                if (mappedField != member)
                    return "L${remapper.map(classTarget)};$mappedField:$mappedDescriptor".breakpoint()

                val mappedMethod = remapper.mapMethodName(classTarget, member, descriptor)
                if (mappedMethod != member)
                    return "L${remapper.map(classTarget)};$mappedMethod$mappedDescriptor".breakpoint()
            }
        } else {
            // I think we can properly remap, just guess I guess.
            val mappedField = remapper.mapFieldName(classDescriptor.removeSurrounding("L", ";"), member, descriptor)
            if (mappedField != member)
                return "$mappedClassDescriptor$mappedField:$mappedDescriptor".breakpoint()

            val mappedMethod = remapper.mapMethodName(classDescriptor.removeSurrounding("L", ";"), member, descriptor)
            if (mappedMethod != member)
                return "$mappedClassDescriptor$mappedMethod$mappedDescriptor".breakpoint()
        }

        // if all else fails, we can return this, as the descriptors and class descriptors need to be remapped too.
        return "$mappedClassDescriptor$member$mappedDescriptor".breakpoint()
    }

    private fun String.breakpoint(): String {
        return this
    }

    fun getMixinClassTargets(
        classNode: ClassNode,
        mixinAnnotation: AnnotationNode = KiltHelper.mergeNullableCollections(classNode.visibleAnnotations, classNode.invisibleAnnotations)
            .firstOrNull { it.desc == MIXIN_TYPE.descriptor }
            ?: throw IllegalStateException("Failed to locate mixin annotations!"),
        values: Map<String, Any> = KiltMixinModifications.annotationValuesToMap(mixinAnnotation.values)
    ): Collection<String> {
        val targetClassNames = mutableListOf<String>()

        if (values.contains("value")) {
            if (values["value"] is List<*>) {
                targetClassNames.addAll((values["value"] as List<Type>).map { it.internalName })
            } else if (values["value"] is Type) {
                targetClassNames.add((values["value"] as Type).internalName)
            }
        }

        if (values.contains("targets")) {
            if (values["targets"] is List<*>) {
                targetClassNames.addAll((values["targets"] as List<String>).map { it.replace(".", "/").removeSurrounding("L", ";") })
            } else if (values["targets"] is String) {
                targetClassNames.add((values["targets"] as String).replace(".", "/").removeSurrounding("L", ";"))
            }
        }

        return targetClassNames
    }

    fun clearCache() {
        this.targetClassNodeCache.clear()
    }

    fun getAllTargetClassNodes(remapper: KiltEnhancedRemapper, targetClassNames: Collection<String>): Collection<ClassNode> {
        val targetClassNodes = mutableListOf<ClassNode>()

        for (className in targetClassNames) {
            val normalizedClassName = className.replace(".", "/").removeSurrounding("L", ";")

            kotlin.synchronized(targetClassNodeCache) {
                if (targetClassNodeCache.contains(normalizedClassName)) {
                    targetClassNodes.add(targetClassNodeCache[normalizedClassName]!!)
                    continue
                }
            }

            val targetClassStream = remapper.provider.getClassStream(normalizedClassName)
                ?: continue

            val classReader = ClassReader(targetClassStream)
            val classNode = ClassNode(Opcodes.ASM9)
            classReader.accept(classNode, 0)

            targetClassNodes.add(classNode)
            this.targetClassNodeCache[normalizedClassName] = classNode
        }

        return targetClassNodes
    }
}
