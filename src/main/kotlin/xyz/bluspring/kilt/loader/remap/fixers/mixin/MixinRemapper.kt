package xyz.bluspring.kilt.loader.remap.fixers.mixin

import kotlinx.atomicfu.locks.synchronized
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.gen.Accessor
import org.spongepowered.asm.mixin.gen.Invoker
import xyz.bluspring.kilt.loader.mixin.modifications.KiltMixinModifications
import xyz.bluspring.kilt.loader.remap.KiltEnhancedRemapper
import xyz.bluspring.kilt.loader.remap.KiltRemapper
import xyz.bluspring.kilt.loader.remap.MixinRefmap
import xyz.bluspring.kilt.loader.remap.fixers.mixin.MixinAdditionalRemapper.MIXIN_TYPE
import xyz.bluspring.kilt.util.KiltHelper
import java.util.Collections

// Remaps all mixins and their associated refmaps
object MixinRemapper {
    private val ACCESSOR_TYPE = Type.getType(Accessor::class.java)
    private val INVOKER_TYPE = Type.getType(Invoker::class.java)

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
            fun remapMixinAnnotations(annotations: MutableList<AnnotationNode>) {
                for (annotationNode in annotations) {
                    if (annotationNode.values == null)
                        continue

                    val values = KiltMixinModifications.annotationValuesToMap(annotationNode.values).toMutableMap()

                    // Remap accessor/invoker
                    if (annotationNode.desc == ACCESSOR_TYPE.descriptor || annotationNode.desc == INVOKER_TYPE.descriptor) {
                        if (!values.contains("value"))
                            continue

                        val value = values["value"] as String

                        if (mixinMapping.contains(value)) {
                            if (!alreadyRefmapped.add(value))
                                continue

                            val mapped = remapTargetString(mixinMapping[value]!!, classTargets, remapper)
                            mixinMapping[value] = mapped
                        } else {
                            values["value"] = remapTargetString(value, classTargets, remapper)
                            annotationNode.values = KiltMixinModifications.mapToAnnotationValues(values)
                        }

                        continue
                    }

                    fun tryRemapMixinRefmap(value: String): String {
                        // Do NOT remap this - this typically indicates MixinSquared or other extensions.
                        if (value.startsWith("@"))
                            return value

                        return if (mixinMapping.contains(value)) {
                            if (!alreadyRefmapped.add(value))
                                return value

                            val original = mixinMapping[value]!!
                            val mapped = remapTargetString(original, classTargets, remapper)

                            mixinMapping[value] = mapped

                            value
                        } else {
                            val mapped = remapTargetString(value, classTargets, remapper)

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

                        if (methodValue is String) {
                            values["method"] = tryRemapMixinRefmap(methodValue)
                        } else if (methodValue is List<*>) {
                            values["method"] = methodValue.map {
                                tryRemapMixinRefmap(it as String)
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
                            KiltRemapper.logger.error("!! Tell BluSpring to stop being lazy. ${classNode.name}")
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
                                sliceValues["from"] = remapAtValue(sliceValues["from"] as AnnotationNode)
                            }

                            if (sliceValues.contains("to") && sliceValues["to"] is AnnotationNode) {
                                sliceValues["to"] = remapAtValue(sliceValues["to"] as AnnotationNode)
                            }
                        }

                        val sliceValue = values["slice"]!!

                        if (sliceValue is AnnotationNode) {
                            remapSliceValue(sliceValue)
                        } else if (sliceValue is List<*>) {
                            val newSliceValues = mutableListOf<Any?>()

                            for (sliceValue in sliceValue) {
                                if (sliceValue is AnnotationNode) {
                                    remapSliceValue(sliceValue)
                                } else {
                                    newSliceValues.add(sliceValue)
                                }
                            }

                            values["slice"] = newSliceValues
                        }
                    }

                    // Now to change the values list
                    annotationNode.values = KiltMixinModifications.mapToAnnotationValues(values)
                }
            }

            if (method.visibleAnnotations != null) {
                remapMixinAnnotations(method.visibleAnnotations!!.toMutableList())
            }

            if (method.invisibleAnnotations != null) {
                remapMixinAnnotations(method.invisibleAnnotations!!.toMutableList())
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
    // however, some mods also completely disregard this format, so we have to keep that in mind.
    // i cannot remember what cursed formats they used though, is the problem....
    fun remapTargetString(value: String, classTargets: Collection<String>, remapper: KiltEnhancedRemapper): String {
        // Class reference, we can just return it directly.
        if (value.contains("/") && !value.startsWith("L") && !value.contains(";")) {
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
        else
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

            if (classDescriptor.isBlank()) {
                // Guesswork time! Thankfully, with fields it's much safer for SRG.
                val mapped = KiltRemapper.srgMappedFields[member]?.second
                if (mapped != null)
                    return "$mapped:$mappedDescriptor".breakpoint()
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
            if (KiltRemapper.srgMappedFields.contains(member)) {
                return "$mappedClassDescriptor${KiltRemapper.srgMappedFields[member]!!.second}".breakpoint()
            }

            if (KiltRemapper.srgMappedMethods.contains(member)) {
                for ((ownerClass, mappedName) in KiltRemapper.srgMappedMethods[member]!!) {
                    if (classTargets.contains(ownerClass))
                        return "$mappedClassDescriptor$mappedName".breakpoint()
                }

                return KiltRemapper.srgMappedMethods[member]!!.values.first()
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
}