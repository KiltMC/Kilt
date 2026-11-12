package xyz.bluspring.kilt.gradle

import com.google.gson.JsonParser
import net.minecraftforge.installer.SimpleInstaller
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.slf4j.LoggerFactory
import java.lang.reflect.Modifier
import java.util.jar.JarFile
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.fetchAndIncrement

object LocalVarMappingBuilder {
    private val logger = LoggerFactory.getLogger(LocalVarMappingBuilder::class.java)

    data class LocalInfo(val type: Type, val ordinal: Int, val isParam: Boolean)

    @OptIn(ExperimentalAtomicApi::class)
    fun setupNeo(project: Project) {
        val profile = JsonParser.parseString(SimpleInstaller::class.java.getResource("/install_profile.json")!!.readText()).asJsonObject
        val neoVersion = profile.get("version").asString.split("-")[1]
        val neoFormVersion = profile.getAsJsonObject("data").getAsJsonObject("MCP_VERSION").get("client").asString.removeSurrounding("'")
        val installPath = project.file(".gradle/kilt-cache/neoforge-$neoVersion/")

        val neoJar = installPath.resolve("libraries/net/minecraft/client/$neoFormVersion/client-$neoFormVersion-srg.jar")

        if (!neoJar.exists()) {
            installPath.mkdirs()

            // create fake MC metadata to trick NeoForge's installer into installing the client.
            installPath.resolve("launcher_profiles.json").apply {
                createNewFile()
                writeText("{}")
            }

            SimpleInstaller.main(arrayOf("--installClient", installPath.absolutePath))
        }

        if (!neoJar.exists())
            throw IllegalStateException("Where the fuck did Neo go?")

        // okay, time to grab the LVTs.
        // map: class name -> ((method name, method desc) -> (local name, local var info))
        val localVars = mutableMapOf<String, Map<Pair<String, String>, Map<String, LocalInfo>>>()
        val neoJarFile = JarFile(neoJar)

        neoJarFile.stream().forEach { entry ->
            if (!entry.name.endsWith(".class"))
                return@forEach

            val classReader = ClassReader(neoJarFile.getInputStream(entry))
            val classNode = ClassNode(Opcodes.ASM9)
            classReader.accept(classNode, 0)

            val methodVars = mutableMapOf<Pair<String, String>, Map<String, LocalInfo>>()

            for (methodNode in classNode.methods) {
                if (methodNode.localVariables == null) continue

                val params = Type.getArgumentTypes(methodNode.desc)

                val localVars = mutableMapOf<String, LocalInfo>()
                val localTypeCounts = mutableMapOf<String, AtomicInt>()
                val funnyLocalCounts = mutableMapOf<String, AtomicInt>()

                for (localVar in /*getLocalsAt(classNode, methodNode)*/ methodNode.localVariables.sortedBy { it.index }) {
                    val type = Type.getType(localVar.desc)
                    var localName = localVar.name

                    if (localName.startsWith("$$") && localName.removePrefix("$$").toIntOrNull() != null) {
                        val funnyOrdinal = funnyLocalCounts.computeIfAbsent(localVar.desc) { AtomicInt(0) }.fetchAndIncrement()

                        // follow the same formatting that Neo follows
                        localName = if (type.sort == Type.OBJECT) {
                            type.className.split(".").last().lowercase()
                        } else {
                            type.descriptor.lowercase()
                        }

                        if (funnyOrdinal > 0)
                            localName = "$localName$funnyOrdinal"
                    }

                    if (localVars.contains(localName)) {
                        logger.warn("Duplicate local variable detected: ${classNode.name}#${methodNode.name}${methodNode.desc}: ${localName}:${localVar.desc}")
                        continue
                    }

                    val offset = if (Modifier.isStatic(methodNode.access)) 0 else 1

                    if (offset == 1 && localVar.index == 0 && localName == "this")
                        continue

                    val ordinal = localTypeCounts.computeIfAbsent(localVar.desc) { AtomicInt(0) }.fetchAndIncrement()
                    val isParam = params.lastIndex >= (localVar.index - offset)

                    localVars[localName] = LocalInfo(type, ordinal, isParam)
                }

                if (localVars.isNotEmpty())
                    methodVars[methodNode.name to methodNode.desc] = localVars
            }

            if (methodVars.isNotEmpty())
                localVars[classNode.name] = methodVars
        }

        var lvtData = ""

        for ((className, methodVars) in localVars) {
            if (methodVars.isEmpty())
                continue

            lvtData += "c\t$className\n"

            for ((methodSig, locals) in methodVars) {
                val (methodName, methodDesc) = methodSig
                lvtData += "\tm\t$methodName\t$methodDesc\n"
                for ((localName, local) in locals) {
                    lvtData += "\t\t${if (local.isParam) "p" else "l"}\t$localName\t${local.type.descriptor}\t${local.ordinal}\n"
                }
            }
        }

        val lvtFile = project.file("src/main/resources/local_variable_mappings.txt")
        if (!lvtFile.exists())
            lvtFile.createNewFile()

        lvtFile.writeText(lvtData)
    }

    // the rest for below is all copied from Mixin's Locals class, just modified for here.

    /*private fun getParameterNames(method: MethodNode, isStatic: Boolean): Array<String?> {
        val argTypes = Type.getArgumentTypes(method.desc)
        if (argTypes.size == 0) {
            return arrayOfNulls(0)
        }

        // Always allocate array for all parameters
        val paramNames = arrayOfNulls<String>(argTypes.size)

        // Build map of local variable index to parameter index
        val indexToParam = mutableMapOf<Int, Int>()
        var localIndex = if (isStatic) 0 else 1 // Skip "this" for non-static methods
        for (arg in argTypes.indices) {
            indexToParam[localIndex] = arg
            localIndex += argTypes[arg].size
        }

        // Single pass through local variable table to extract names
        if (method.localVariables != null) {
            for (lvNode in method.localVariables) {
                val paramIndex = indexToParam[lvNode.index]
                if (paramIndex != null) {
                    paramNames[paramIndex] = lvNode.name
                }
            }
        }

        // Merge in names from parameters field where LVT didn't provide them
        if (method.parameters != null) {
            for (i in 0 until min(argTypes.size, method.parameters.size)) {
                if (paramNames[i] == null) {
                    paramNames[i] = method.parameters[i].name
                }
            }
        }

        return paramNames
    }

    fun getInitialMethodLocals(
        method: MethodNode,
        classNode: ClassNode,
        fallbackToLvIndex: Boolean = false
    ): Array<LocalVariableNode?> {
        val isStatic = Bytecode.isStatic(method)
        val argTypes = Type.getArgumentTypes(method.desc)
        val initialFrameSize = Bytecode.getFirstNonArgLocalIndex(method)
        val frame = arrayOfNulls<LocalVariableNode>(initialFrameSize)

        var local = 0

        val paramNames = getParameterNames(method, isStatic)

        // Initialise implicit "this" reference in non-static methods
        if (!isStatic) {
            frame[local++] = LocalVariableNode(
                "this",
                Type.getObjectType(classNode.name).descriptor,
                null,
                null,
                null,
                0
            )
        }

        // Initialise method arguments
        for (index in argTypes.indices) {
            val argType = argTypes[index]
            var paramName = paramNames[index]
            if (paramName == null) {
                paramName = if (fallbackToLvIndex) {
                    "arg$local"
                } else {
                    "arg$index"
                }
            }
            frame[local] = LocalVariableNode(paramName, argType.descriptor, null, null, null, local)
            local += argType.size
        }

        return frame
    }

    @JvmRecord
    private data class FrameData(val index: Int, val type: Int, val locals: Int, val size: Int, val rawSize: Int = size) {
        constructor(index: Int, frameNode: FrameNode, initialFrameSize: Int) : this(index, frameNode.type, frameNode.local?.size ?: 0, rawSize = computeFrameSize(frameNode, 0), size = max(computeFrameSize(frameNode, 0), initialFrameSize))
    }

    private fun getLocalsAt(classNode: ClassNode, method: MethodNode): List<LocalVariableNode> {
//        val classInfo = ClassInfo.forName(classNode.name)
//            ?: throw LVTGeneratorError("Could not load class metadata for " + classNode.name + " generating LVT for " + method.name)
//        val methodInfo: ClassInfo.Method = classInfo.findMethod(method, method.access or ClassInfo.INCLUDE_INITIALISERS)
//            ?: throw LVTGeneratorError("Could not locate method metadata for " + method.name + " generating LVT in " + classNode.name)

        val frames = method.instructions.filterIsInstance<FrameNode>()
            .map {
                FrameData(method.instructions.indexOf(it), it, Bytecode.getFirstNonArgLocalIndex(method))
            }

        // Initialize the frame with "this" and method parameters
        val initialLocals = getInitialMethodLocals(method, classNode)
        val frame = arrayOfNulls<LocalVariableNode>(method.maxLocals)
        System.arraycopy(initialLocals, 0, frame, 0, initialLocals.size)

        val initialFrameSize = initialLocals.size
        var frameSize = initialFrameSize
        var frameIndex = -1
        var lastFrameSize = initialFrameSize
        var knownFrameSize = initialFrameSize
        var storeInsn: VarInsnNode? = null

        run {
            val iter: MutableIterator<AbstractInsnNode> = method.instructions.iterator()
            while (iter.hasNext()) {
                val insn = iter.next()


                // Tick the zombies
                for (l in frame.indices) {
                    if (frame[l] is ZombieLocalVariableNode) {
                        val zombie = frame[l] as ZombieLocalVariableNode
                        zombie.lifetime++
                        if (insn is FrameNode) {
                            zombie.frames++
                        }
                    }
                }

                if (storeInsn != null) {
                    val storedLocal = getLocalVariableAt(classNode, method, insn, storeInsn.`var`)
                    frame[storeInsn.`var`] = storedLocal
                    knownFrameSize = max(knownFrameSize, storeInsn.`var` + 1)
                    if (storedLocal != null && storeInsn.`var` < method.maxLocals - 1
                        && storedLocal.desc != null && Type.getType(storedLocal.desc).size == 2) {
                        frame[storeInsn.`var` + 1] = null // TOP
                        knownFrameSize = max(knownFrameSize, storeInsn.`var` + 2)
                    }
                    storeInsn = null
                }

                run {
                    if (insn is FrameNode) {
                        frameIndex++
                        if (insn.type == Opcodes.F_SAME || insn.type == Opcodes.F_SAME1) {
                            return@run
                        }

                        val frameNodeSize = computeFrameSize(insn, initialFrameSize)
                        val frameData = if (frameIndex < frames.size) frames[frameIndex] else null

                        if (frameData != null) {
                            if (frameData.type == Opcodes.F_FULL) {
                                frameSize = max(initialFrameSize, min(frameNodeSize, frameData.size))
                                lastFrameSize = frameSize
                                knownFrameSize = lastFrameSize
                            } else {
                                frameSize = getAdjustedFrameSize(frameSize, frameData, initialFrameSize)
                            }
                        } else {
                            frameSize = getAdjustedFrameSize(frameSize, insn, initialFrameSize)
                        }


                        // Sanity check
                        check(frameSize >= initialFrameSize) {
                            String.format(
                                "Locals entered an invalid state evaluating %s::%s%s at instruction %d (%s). "
                                    + "Initial frame size is %d, calculated a frame size of %d with %s",
                                classNode.name,
                                method.name,
                                method.desc,
                                method.instructions.indexOf(insn),
                                Bytecode.describeNode(insn, false),
                                initialFrameSize,
                                frameSize,
                                frameData
                            )
                        }

                        if ((frameData == null && (insn.type == Opcodes.F_CHOP || insn.type == Opcodes.F_NEW))
                            || (frameData != null && frameData.type == Opcodes.F_CHOP)
                        ) {
                            for (framePos in frameSize..<frame.size) {
                                frame[framePos] = ZombieLocalVariableNode.of(frame[framePos], ZombieLocalVariableNode.CHOP)
                            }
                            lastFrameSize = frameSize
                            knownFrameSize = lastFrameSize
                            return@run
                        }

                        var framePos = if (insn.type == Opcodes.F_APPEND) lastFrameSize else 0
                        lastFrameSize = frameSize


                        // localPos tracks the location in the frame node's locals list, which doesn't leave space for TOP entries
                        var localPos = 0
                        while (framePos < frame.size) {
                            // Get the local at the current position in the FrameNode's locals list
                            val localType = insn.local.getOrNull(localPos)

                            if (localType is String) { // String refers to a reference type
                                frame[framePos] = getLocalVariableAt(classNode, method, insn, framePos)
                            } else if (localType is Int) { // Integer refers to a primitive type or other marker
                                val isMarkerType = localType == Opcodes.UNINITIALIZED_THIS || localType == Opcodes.NULL
                                val is32bitValue = localType == Opcodes.INTEGER || localType == Opcodes.FLOAT
                                val is64bitValue = localType == Opcodes.DOUBLE || localType == Opcodes.LONG
                                if (localType == Opcodes.TOP) {
                                    // Explicit TOP entries are pretty much always bogus, but depending on our resurrection
                                    // strategy we may want to resurrect eligible zombies here. Real TOP entries are handled below
                                    if (frame[framePos] is ZombieLocalVariableNode /*&& settings.hasFlags(Locals.Settings.RESURRECT_FOR_BOGUS_TOP)*/) {
                                        val zombie = frame[framePos] as ZombieLocalVariableNode
                                        if (zombie.type == ZombieLocalVariableNode.TRIM) {
                                            frame[framePos] = zombie.ancestor
                                        }
                                    }
                                } else if (isMarkerType) {
                                    frame[framePos] = null
                                } else if (is32bitValue || is64bitValue) {
                                    frame[framePos] = getLocalVariableAt(classNode, method, insn, framePos)

                                    if (is64bitValue) {
                                        framePos++
                                        frame[framePos] = null // TOP
                                    }
                                } else {
                                    throw LVTGeneratorError(
                                        ("Unrecognised locals opcode " + localType + " in locals array at position " + localPos
                                            + " in " + classNode.name + "." + method.name + method.desc)
                                    )
                                }
                            } else if (localType == null) {
                                if (framePos >= initialFrameSize && framePos >= frameSize && frameSize > 0) {
                                    if (framePos < knownFrameSize) {
                                        frame[framePos] = getLocalVariableAt(classNode, method, insn, framePos)
                                    } else {
                                        frame[framePos] = ZombieLocalVariableNode.of(
                                            frame[framePos],
                                            ZombieLocalVariableNode.TRIM
                                        )
                                    }
                                }
                            } else if (localType is LabelNode) {
                                // Uninitialised
                            } else {
                                throw LVTGeneratorError(
                                    ("Invalid value " + localType + " in locals array at position " + localPos
                                        + " in " + classNode.name + "." + method.name + method.desc)
                                )
                            }
                            framePos++
                            localPos++
                        }
                    } else if (insn is VarInsnNode) {
                        val isLoad = insn.getOpcode() >= Opcodes.ILOAD && insn.getOpcode() <= Opcodes.SALOAD
                        if (isLoad) {
                            val toLoad = getLocalVariableAt(classNode, method, insn, insn.`var`)
                            frame[insn.`var`] = toLoad
                            val varSize = if (toLoad != null && toLoad.desc != null) Type.getType(frame[insn.`var`]!!.desc).size else 1
                            knownFrameSize = max(knownFrameSize, insn.`var` + varSize)
                            if (/*settings.hasFlags(Locals.Settings.RESURRECT_EXPOSED_ON_LOAD)*/true) {
                                resurrect(frame, knownFrameSize/*, settings*/)
                            }
                        } else {
                            // Update the LVT for the opcode AFTER this one, since we always want to know
                            // the frame state BEFORE the *current* instruction to match the contract of
                            // injection points
                            storeInsn = insn
                        }
                    }
                }
            }
        }


        // Null out any "unknown" or mixin-provided locals
        for (l in frame.indices) {
            if (frame[l] is ZombieLocalVariableNode) {
                val zombie = frame[l] as ZombieLocalVariableNode
                // preserve zombies where the frame node which culled them was immediately prior to
                // the matched instruction, or *was itself* the matched instruction, the returned
                // frame will contain the original node (the zombie ancestor)
                frame[l] = if (zombie.lifetime > 1) null else zombie.ancestor
            }

            if ((frame[l] != null && frame[l]!!.desc == null) || frame[l] is SyntheticLocalVariableNode) {
                frame[l] = null
            }
        }

        return frame.filterNotNull()
    }

    private fun getAdjustedFrameSize(currentSize: Int, frameNode: FrameNode, initialFrameSize: Int): Int {
        return getAdjustedFrameSize(
            currentSize,
            frameNode.type,
            computeFrameSize(frameNode, initialFrameSize),
            initialFrameSize
        )
    }

    fun computeFrameSize(frameNode: FrameNode, initialFrameSize: Int): Int {
        if (frameNode.local == null) {
            return initialFrameSize
        }
        var size = 0
        for (local in frameNode.local) {
            if (local is Int) {
                size += if (local == Opcodes.DOUBLE || local == Opcodes.LONG) 2 else 1
            } else {
                size++
            }
        }
        return max(initialFrameSize, size)
    }

    private fun getAdjustedFrameSize(currentSize: Int, frameData: FrameData, initialFrameSize: Int): Int {
        return getAdjustedFrameSize(currentSize, frameData.type, frameData.size, initialFrameSize)
    }

    private fun getAdjustedFrameSize(currentSize: Int, type: Int, size: Int, initialFrameSize: Int): Int {
        return when (type) {
            Opcodes.F_NEW, Opcodes.F_FULL -> max(initialFrameSize, size)
            Opcodes.F_APPEND -> currentSize + size
            Opcodes.F_CHOP -> max(initialFrameSize, currentSize - size)
            Opcodes.F_SAME, Opcodes.F_SAME1 -> currentSize
            else -> currentSize
        }
    }

    fun getLocalVariableAt(
        classNode: ClassNode,
        method: MethodNode,
        node: AbstractInsnNode?,
        `var`: Int
    ): LocalVariableNode? {
        return getLocalVariableAt(classNode, method, method.instructions.indexOf(node), `var`)
    }

    private val calculatedLocalVariables: MutableMap<String, List<LocalVariableNode>> = mutableMapOf()

    private fun getLocalVariableAt(classNode: ClassNode, method: MethodNode, pos: Int, `var`: Int): LocalVariableNode? {
        var localVariableNode: LocalVariableNode? = null
        var fallbackNode: LocalVariableNode? = null

        for (local in getLocalVariableTable(classNode, method)) {
            if (local.index != `var`) {
                continue
            }
            if (isOpcodeInRange(method.instructions, local, pos)) {
                localVariableNode = local
            } else if (localVariableNode == null) {
                fallbackNode = local
            }
        }

        if (localVariableNode == null && !method.localVariables.isEmpty()) {
            for (local in getGeneratedLocalVariableTable(classNode, method)) {
                if (local.index == `var` && isOpcodeInRange(method.instructions, local, pos)) {
                    localVariableNode = local
                }
            }
        }

        return localVariableNode ?: fallbackNode
    }

    fun getLocalVariableTable(classNode: ClassNode, method: MethodNode): List<LocalVariableNode> {
        if (method.localVariables.isEmpty()) {
            return getGeneratedLocalVariableTable(classNode, method)
        }
        return method.localVariables
    }

    private fun isOpcodeInRange(insns: InsnList, local: LocalVariableNode, pos: Int): Boolean {
        return insns.indexOf(local.start) <= pos && insns.indexOf(local.end) > pos
    }

    /**
     * Gets the generated the local variable table for the specified method.
     *
     * @param classNode Containing class
     * @param method Method
     * @return generated local variable table
     */
    fun getGeneratedLocalVariableTable(classNode: ClassNode, method: MethodNode): List<LocalVariableNode> {
        val methodId = String.format("%s.%s%s", classNode.name, method.name, method.desc)
        var localVars = calculatedLocalVariables[methodId]
        if (localVars != null) {
            return localVars
        }

        localVars = generateLocalVariableTable(classNode, method)
        calculatedLocalVariables[methodId] = localVars
        return localVars
    }

    /**
     * Use ASM Analyzer to generate the local variable table for the specified
     * method
     *
     * @param classNode Containing class
     * @param method Method
     * @return generated local variable table
     */
    fun generateLocalVariableTable(classNode: ClassNode, method: MethodNode): List<LocalVariableNode> {
        var interfaces: MutableList<Type?>? = null
        if (classNode.interfaces != null) {
            interfaces = ArrayList()
            for (iface in classNode.interfaces) {
                interfaces.add(Type.getObjectType(iface))
            }
        }

        var objectType: Type? = null
        if (classNode.superName != null) {
            objectType = Type.getObjectType(classNode.superName)
        }

        // Use Analyzer to generate the bytecode frames
        val analyzer = Analyzer(
            object : SimpleVerifier(ASM9, Type.getObjectType(classNode.name), objectType, interfaces, false) {
                /*override fun isAssignableFrom(type1: Type, type2: Type): Boolean {
                    return type1 == getCommonSupertype(type1, type2)
                }

                private val OBJECT_TYPE: Type = Type.getType(Any::class.java)

                private fun arrayType(type: Type, dimensions: Int): Type {
                    if (dimensions == 0) {
                        return type
                    } else {
                        val descriptor = StringBuilder()
                        for (i in 0 until dimensions) {
                            descriptor.append('[')
                        }
                        descriptor.append(type.descriptor)
                        return Type.getType(descriptor.toString())
                    }
                }

                private fun getCommonSuperClass(type1: Type, type2: Type): Type? {
                    return null
                }

                private fun getCommonSupertype(type1: Type, type2: Type): Type? {
                    if (type1 == type2 || type2 == NULL_TYPE) {
                        return type1
                    }
                    if (type1 == NULL_TYPE) {
                        return type2
                    }
                    if (type1.sort < Type.ARRAY || type2.sort < Type.ARRAY) {
                        // We know they're not the same, so they must be incompatible.
                        return null
                    }
                    if (type1.sort == Type.ARRAY && type2.sort == Type.ARRAY) {
                        val dim1 = type1.getDimensions()
                        val elem1 = type1.getElementType()
                        val dim2 = type2.getDimensions()
                        val elem2 = type2.getElementType()
                        if (dim1 == dim2) {
                            val commonSupertype: Type? = if (elem1 == elem2) {
                                elem1
                            } else if (elem1.sort == Type.OBJECT && elem2.sort == Type.OBJECT) {
                                getCommonSupertype(elem1, elem2)
                            } else {
                                return arrayType(OBJECT_TYPE, dim1 - 1)
                            }

                            return arrayType(commonSupertype!!, dim1)
                        }
                        val smaller: Type
                        var shared: Int
                        if (dim1 < dim2) {
                            smaller = elem1
                            shared = dim1 - 1
                        } else {
                            smaller = elem2
                            shared = dim2 - 1
                        }
                        if (smaller.sort == Type.OBJECT) {
                            shared++
                        }
                        return arrayType(OBJECT_TYPE, shared)
                    }
                    if (type1.sort == Type.ARRAY && type2.sort == Type.OBJECT || type2.sort == Type.ARRAY && type1.sort == Type.OBJECT) {
                        return OBJECT_TYPE
                    }

                    return getCommonSuperClass(type1, type2)
                }*/
            }
//            MixinVerifier(ASM.API_VERSION, Type.getObjectType(classNode.name), objectType, interfaces, false)
        )
        try {
            analyzer.analyze(classNode.name, method)
        } catch (ex: AnalyzerException) {
            ex.printStackTrace()
        }

        // Get frames from the Analyzer
        val frames = analyzer.getFrames()

        // Record the original size of hte method
        val methodSize = method.instructions.size()

        // List of LocalVariableNodes to return
        val localVariables = mutableListOf<LocalVariableNode>()

        val localNodes = arrayOfNulls<LocalVariableNode>(method.maxLocals) // LocalVariableNodes for current frame
        val locals = arrayOfNulls<BasicValue>(method.maxLocals) // locals in previous frame, used to work out what changes between frames
        val labels = arrayOfNulls<LabelNode>(methodSize) // Labels to add to the method, for the markers
        val lastKnownType = arrayOfNulls<String>(method.maxLocals)

        // Traverse the frames and work out when locals begin and end
        for (i in 0 until methodSize) {
            val f = frames[i] ?: continue
            var label: LabelNode? = null

            for (j in 0 until f.locals) {
                val local = f.getLocal(j)
                if (local == null && locals[j] == null)
                    continue

                if (local != null && local == locals[j])
                    continue

                if (label == null) {
                    val existingLabel = method.instructions.get(i)
                    if (existingLabel is LabelNode) {
                        label = existingLabel
                    } else {
                        label = LabelNode()
                        labels[i] = label
                    }
                }

                if (local == null && locals[j] != null) {
                    localVariables.add(localNodes[j]!!)
                    localNodes[j]!!.end = label
                    localNodes[j] = null
                } else if (local != null) {
                    if (locals[j] != null) {
                        localVariables.add(localNodes[j]!!)
                        localNodes[j]!!.end = label
                        localNodes[j] = null
                    }

                    var desc = lastKnownType[j]
                    val localType = local.type
                    if (localType != null) {
                        desc = if (localType.sort >= Type.ARRAY && "null" == localType.internalName)
                            Constants.OBJECT_DESC
                        else
                            localType.descriptor
                    }

                    localNodes[j] = LocalVariableNode("var$j", desc, null, label, null, j)
                    if (desc != null) {
                        lastKnownType[j] = desc
                    }
                }

                locals[j] = local
            }
        }

        // Reached the end of the method so flush all current locals and mark the end
        var label: LabelNode? = null
        for (k in localNodes.indices) {
            if (localNodes[k] != null) {
                if (label == null) {
                    label = LabelNode()
                    method.instructions.add(label)
                }

                localNodes[k]!!.end = label
                localVariables.add(localNodes[k]!!)
            }
        }

        // Insert generated labels into the method body
        for (n in methodSize - 1 downTo 0) {
            if (labels[n] != null) {
                method.instructions.insert(method.instructions.get(n), labels[n])
            }
        }

        return localVariables
    }

    class SyntheticLocalVariableNode(
        name: String?,
        descriptor: String?,
        signature: String?,
        start: LabelNode?,
        end: LabelNode?,
        index: Int
    ) : LocalVariableNode(name, descriptor, signature, start, end, index)

    class ZombieLocalVariableNode internal constructor(
        /**
         * Progenitor of this zombie, to allow "resurrection" (am I stretching
         * this metaphor too far?)
         */
        val ancestor: LocalVariableNode, val type: Char
    ) : LocalVariableNode(
        ancestor.name, ancestor.desc, ancestor.signature, ancestor.start, ancestor.end, ancestor.index
    ) {
        var lifetime: Int = 0
        var frames: Int = 0

        fun checkResurrect(/*settings: Locals.Settings*/): Boolean {
//            val insnThreshold = if (this.type == CHOP) -1 else -1
//            if (insnThreshold > -1 && this.lifetime > insnThreshold) {
//                return false
//            }
            val frameThreshold = if (this.type == CHOP) 1 else -1
            return frameThreshold == -1 || this.frames <= frameThreshold
        }

        override fun toString(): String {
            return String.format("Z(%s,%-2d)", this.type, this.lifetime)
        }

        companion object {
            const val CHOP: Char = 'C'
            const val TRIM: Char = 'X'

            fun of(ancestor: LocalVariableNode?, type: Char): ZombieLocalVariableNode? {
                if (ancestor is ZombieLocalVariableNode) {
                    return ancestor
                }
                return if (ancestor != null) ZombieLocalVariableNode(ancestor, type) else null
            }
        }
    }

    private fun resurrect(frame: Array<LocalVariableNode?>, knownFrameSize: Int) {
        var l = 0
        while (l < knownFrameSize && l < frame.size) {
            if (frame[l] is ZombieLocalVariableNode) {
                val zombie = frame[l] as ZombieLocalVariableNode
                if (zombie.checkResurrect()) {
                    frame[l] = zombie.ancestor
                }
            }

            l++
        }
    }*/
}
