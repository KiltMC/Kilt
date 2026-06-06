package xyz.bluspring.kilt.loader.asm.coremod

import com.google.gson.JsonParser
import cpw.mods.modlauncher.VoteDeadlockException
import cpw.mods.modlauncher.VoteRejectedException
import cpw.mods.modlauncher.api.ITransformer
import cpw.mods.modlauncher.api.ITransformerActivity
import cpw.mods.modlauncher.api.TargetType
import cpw.mods.modlauncher.api.TransformerVoteResult
import net.fabricmc.loader.impl.gui.FabricGuiEntry
import net.minecraftforge.fart.internal.EnhancedClassRemapper
import net.minecraftforge.fart.internal.RenamingTransformer
import net.neoforged.neoforgespi.coremod.ICoreMod
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.signature.SignatureReader
import org.objectweb.asm.signature.SignatureVisitor
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode
import org.slf4j.LoggerFactory
import xyz.bluspring.fork.mm.api.ClassTinkerers
import xyz.bluspring.kilt.loader.KiltFlags
import xyz.bluspring.kilt.loader.mod.NeoForgeMod
import xyz.bluspring.kilt.loader.remap.KiltRemapper
import xyz.bluspring.kilt.loader.remap.KiltRemapper.enhancedRemapper
import java.security.MessageDigest
import java.util.*

// A reimplementation of Forge's coremodding system.
// Mainly utilizes some code from https://github.com/neoforged/CoreMods/blob/main/src/main/java/net/neoforged/coremod/CoreModScriptingEngine.java
// with additional changes to work with Kilt's mod loading process.
object CoreModLoader {
    val ALLOWED_PACKAGES = setOf(
        "java.util",
        "java.util.function",
        "org.objectweb.asm.util"
    )

    val ALLOWED_CLASSES = setOf(
        "net.neoforged.coremod.api.ASMAPI", "org.objectweb.asm.Opcodes",

        // Editing the code of methods
        "org.objectweb.asm.tree.AbstractInsnNode",
        "org.objectweb.asm.tree.FieldInsnNode",
        "org.objectweb.asm.tree.FrameNode",
        "org.objectweb.asm.tree.IincInsnNode",
        "org.objectweb.asm.tree.InsnNode",
        "org.objectweb.asm.tree.IntInsnNode",
        "org.objectweb.asm.tree.InsnList",
        "org.objectweb.asm.tree.InvokeDynamicInsnNode",
        "org.objectweb.asm.tree.JumpInsnNode",
        "org.objectweb.asm.tree.LabelNode",
        "org.objectweb.asm.tree.LdcInsnNode",
        "org.objectweb.asm.tree.LineNumberNode",
        "org.objectweb.asm.tree.LocalVariableAnnotationNode",
        "org.objectweb.asm.tree.LocalVariableNode",
        "org.objectweb.asm.tree.LookupSwitchInsnNode",
        "org.objectweb.asm.tree.MethodInsnNode",
        "org.objectweb.asm.tree.MultiANewArrayInsnNode",
        "org.objectweb.asm.tree.TableSwitchInsnNode",
        "org.objectweb.asm.tree.TryCatchBlockNode",
        "org.objectweb.asm.tree.TypeAnnotationNode",
        "org.objectweb.asm.tree.TypeInsnNode",
        "org.objectweb.asm.tree.VarInsnNode",

        // Adding new fields to classes
        "org.objectweb.asm.tree.FieldNode",

        // Adding new methods to classes
        "org.objectweb.asm.tree.MethodNode",
        "org.objectweb.asm.tree.ParameterNode",

        // Misc stuff referenced in above classes that's probably useful
        "org.objectweb.asm.Attribute",
        "org.objectweb.asm.Handle",
        "org.objectweb.asm.Label",
        "org.objectweb.asm.Type",
        "org.objectweb.asm.TypePath",
        "org.objectweb.asm.TypeReference",

        // Kilt: Provide access to the remapper for ourselves
        "xyz.bluspring.kilt.loader.asm.coremod.CoreModHelper",
        "xyz.bluspring.kilt.loader.asm.coremod.RemappingFieldInsnNode",
        "xyz.bluspring.kilt.loader.asm.coremod.RemappingMethodInsnNode"
    )

    val loadedCoreMods = mutableListOf<CoreMod>()
    val enableCoreMods = KiltFlags.DISABLE_COREMODS

    fun scanAndLoadCoreMods(mod: NeoForgeMod) {
        if (!enableCoreMods)
            return

        //Kilt.logger.warn("Coremods have been enabled! Be advised that this may cause severe incompatibility issues!")

        try {
            val entry = mod.getFile("META-INF/coremods.json")

            if (entry != null) {
                val json = JsonParser.parseReader(entry.bufferedReader()).asJsonObject

                for (key in json.keySet()) {
                    val filePath = json.get(key).asString
                    val coreMod = CoreMod(mod, key, filePath)

                    coreMod.init()

                    mod.coreMods.add(coreMod)
                    loadedCoreMods.add(coreMod)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            FabricGuiEntry.displayError("Failed to load coremods in ${mod.displayName} (${mod.modId})!", e, {
                val tab = it.addTab("Kilt Error")

                it.tabs.removeIf { t -> t != tab }
            }, true)
        }
    }

    private val logger = LoggerFactory.getLogger("Kilt CoreMod Loader")

    fun loadJavaCoreMods() {
        if (!enableCoreMods)
            return

        val coremods = ServiceLoader.load(ICoreMod::class.java)
        val mergedTransformers = mutableMapOf<String, MutableList<ITransformer<*>>>()

        // Group by class targets
        for (coreMod in coremods) {
            for (transformer in coreMod.transformers) {
                for (target in transformer.targets()) {
                    mergedTransformers.computeIfAbsent(target.className) { mutableListOf() }
                        .add(transformer)
                }
            }
        }

        for ((className, transformers) in mergedTransformers) {
            val targetedTransformers = transformers.groupBy { it.targetType }

            ClassTinkerers.addPostTransformation(KiltRemapper.remapClass(className)) { classNode ->
                try {
                    val hash by lazy {
                        val digest = MessageDigest.getInstance("SHA-256")
                        val writer = ClassWriter(0)
                        classNode.accept(writer)
                        digest.digest(writer.toByteArray())
                    }
                    val context = TransformerVotingContext(className, true, { hash }, mutableListOf(), ITransformerActivity.CLASSLOADING_REASON)

                    val remappedNode = ClassNode()
                    classNode.accept(EnhancedClassRemapper(remappedNode, KiltRemapper.enhancedInverseRemapper, RenamingTransformer(KiltRemapper.enhancedInverseRemapper, false)))

                    // the voting system is... very confusing. no joke, the only project I can find that actually uses a result that *isn't* TransformerVoteResult.YES
                    // is Sponge.
                    fun <T : Any> transform(node: T, transformers: Collection<ITransformer<T>>): T {
                        var actualNode = node
                        context.node = actualNode

                        val actualTransformers = transformers.toMutableList()
                        do {
                            val votes = transformers.associateWith { it.castVote(context) }
                            if (votes.containsValue(TransformerVoteResult.REJECT)) {
                                throw VoteRejectedException()
                            }

                            if (votes.containsValue(TransformerVoteResult.NO)) {
                                actualTransformers.removeAll(votes.filterValues { it == TransformerVoteResult.NO }.keys)
                            }

                            if (votes.containsValue(TransformerVoteResult.YES)) {
                                val transformer = votes.filterValues { it == TransformerVoteResult.YES }.keys.first()
                                logger.debug("Transforming $node using $transformer (${transformer.targets()})")
                                actualNode = transformer.transform(actualNode, context)
                                actualTransformers.remove(transformer)
                                continue
                            }

                            if (votes.containsValue(TransformerVoteResult.DEFER)) {
                                throw VoteDeadlockException()
                            }
                        } while (actualTransformers.isNotEmpty())

                        return actualNode
                    }

                    val actualClassNode = transform(remappedNode, (targetedTransformers[TargetType.PRE_CLASS] ?: emptyList()) as Collection<ITransformer<ClassNode>>)

                    val fields = ArrayList<FieldNode>(actualClassNode.fields.size)
                    for (field in actualClassNode.fields) {
                        fields.add(transform(field, ((targetedTransformers[TargetType.FIELD] ?: emptyList()) as Collection<ITransformer<FieldNode>>).filter {
                            it.targets().any { f ->
                                f.elementName == field.name && f.elementDescriptor == field.desc
                            }
                        }))
                    }

                    val methods = ArrayList<MethodNode>(actualClassNode.methods.size)
                    for (method in actualClassNode.methods) {
                        methods.add(transform(method, ((targetedTransformers[TargetType.METHOD] ?: emptyList()) as Collection<ITransformer<MethodNode>>).filter {
                            it.targets().any { f ->
                                f.elementName == method.name && f.elementDescriptor == method.desc
                            }
                        }))
                    }

                    actualClassNode.fields = fields
                    actualClassNode.methods = methods

                    val postTransformedClassNode = transform(actualClassNode, (targetedTransformers[TargetType.CLASS] ?: emptyList()) as Collection<ITransformer<ClassNode>>)

                    // Some mods like Twilight Forest implement JVM type signatures that are invalid according to the JVM specification, which
                    // causes issues while remapping.
                    // We need to validate and repair the signatures so we can actually remap shit again, but this ultimately needs to be fixed on
                    // the offending mods' side, in the event that a JVM actually validates these signatures.
                    // Offending signature: https://github.com/TeamTwilight/twilightforest/blob/82f1b4b9c15eedcc4fbaccdd513d0b44d818710c/tf-asm/src/main/java/twilightforest/asm/transformers/beardifier/BeardifierClassTransformer.java#L26
                    // JVM specification reference: https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-TypeArgument
                    if (postTransformedClassNode.signature != null) {
                        postTransformedClassNode.signature = validateAndRepairSignature(postTransformedClassNode.signature, postTransformedClassNode.name, "class", transformers)
                    }

                    for (field in postTransformedClassNode.fields) {
                        if (field.signature != null) {
                            field.signature = validateAndRepairSignature(field.signature, postTransformedClassNode.name, "field ${field.name}:${field.desc}", transformers)
                        }
                    }

                    for (method in postTransformedClassNode.methods) {
                        if (method.signature != null) {
                            method.signature = validateAndRepairSignature(method.signature, postTransformedClassNode.name, "method ${method.name}${method.desc}", transformers)
                        }
                    }

                    val unmappedClassNode = ClassNode()
                    postTransformedClassNode.accept(EnhancedClassRemapper(unmappedClassNode, enhancedRemapper, RenamingTransformer(enhancedRemapper, false)))
                    classNode.fields.clear()
                    classNode.methods.clear()

                    // Validate added fields and methods, some Fabric mods provide accessors for already-existing methods,
                    // which then breaks here because they get applied twice.
                    // We're gonna need to remove 'em ourselves.
                    val fieldsToRemove = mutableListOf<FieldNode>()
                    val methodsToRemove = mutableListOf<MethodNode>()

                    for (fieldNode in unmappedClassNode.fields) {
                        if ((fieldNode.access and Opcodes.ACC_SYNTHETIC != 0) // Just in case, y'know
                            && unmappedClassNode.fields.any { it.name == fieldNode.name && it.desc == fieldNode.desc && (it.access and Opcodes.ACC_SYNTHETIC == 0) }
                        ) {
                            fieldsToRemove.add(fieldNode)
                            logger.warn("Found duplicate field ${fieldNode.name}:${fieldNode.desc} in class ${unmappedClassNode.name}, attempting to remove.")
                        }
                    }

                    for (methodNode in unmappedClassNode.methods) {
                        if ((methodNode.access and Opcodes.ACC_SYNTHETIC != 0) // Most accessors should be marked synthetic.
                            && unmappedClassNode.methods.any { it.name == methodNode.name && it.desc == methodNode.desc && (it.access and Opcodes.ACC_SYNTHETIC == 0) }
                        ) {
                            methodsToRemove.add(methodNode)
                            logger.warn("Found duplicate method ${methodNode.name}${methodNode.desc} in class ${unmappedClassNode.name}, attempting to remove.")
                        }
                    }

                    unmappedClassNode.fields.removeAll(fieldsToRemove)
                    unmappedClassNode.methods.removeAll(methodsToRemove)

                    unmappedClassNode.accept(classNode)
                } catch (e: Throwable) {
                    logger.error("An error occurred in a coremod while transforming $className/${classNode.name}!", e)
                    throw e
                }
            }
        }
    }

    private fun validateAndRepairSignature(signature: String, className: String, location: String, transformers: List<ITransformer<*>>): String {
        try {
            // Validate with ASM directly first, this is our source of truth.
            // That way, if we fucked up our repair code, we won't crash instantly.
            val reader = SignatureReader(signature)
            reader.accept(object : SignatureVisitor(Opcodes.ASM9) {})

            // Nothing wrong, we can continue.
            return signature
        } catch (_: IllegalArgumentException) {
            // Time to warn the user about the invalid signature.
            logger.warn("Found an invalid type signature \"${signature}\" at $location while remapping coremod transformations on class ${className}!")
            logger.warn("Potential offending coremod transformers (${transformers.size}):")
            for (transformer in transformers) {
                logger.warn(" - ${transformer::class.java.name} (targeting ${transformer.targetType})")
            }
        }

        // This is definitely horrendously incorrect,
        var currentSignature = ""
        var signatureLayer = 0
        var builtSignature = ""

        for (ch in signature) {
            if (ch == '<' || ch == '>') {
                if (ch == '<') {
                    signatureLayer++
                    currentSignature += ch
                }

                if (builtSignature.isNotBlank()) {
                    val firstChar = builtSignature[0]
                    currentSignature += if (firstChar != 'L' && firstChar != 'Z' && firstChar != 'C' && firstChar != 'B' && firstChar != 'S' && firstChar != 'I' && firstChar != 'F' && firstChar != 'J' && firstChar != 'D' && firstChar != 'V' && firstChar != '[' && firstChar != 'T') {
                        // Invalid type signature found, we've gotta fix it now.
                        "L${builtSignature.removeSurrounding("L", ";").replace(".", "/")};"
                    } else {
                        builtSignature
                    }

                    builtSignature = ""
                }

                if (ch == '>') {
                    signatureLayer--
                    currentSignature += ch
                }
            } else if (signatureLayer > 0) {
                builtSignature += ch
            } else {
                currentSignature += ch
            }
        }

        return currentSignature
    }
}
