package xyz.bluspring.kilt.loader.asm.coremod

import com.chocohead.mm.api.ClassTinkerers
import net.minecraftforge.coremod.api.TargetType
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import xyz.bluspring.kilt.loader.KiltFlags
import xyz.bluspring.kilt.loader.KiltLoader
import xyz.bluspring.kilt.loader.asm.NashornHelper
import xyz.bluspring.kilt.loader.mod.ForgeMod
import xyz.bluspring.kilt.loader.remap.KiltRemapper
import java.nio.file.StandardOpenOption
import javax.script.Bindings
import javax.script.Invocable
import javax.script.ScriptEngine
import kotlin.io.path.*

class CoreMod(val mod: ForgeMod, val id: String, val file: String) {
    private val data = String(mod.getFile(file)!!.readAllBytes())
    private var bindings: Map<String, out Bindings> = mapOf()
    private var loaded = false
    private val logger = LoggerFactory.getLogger("CoreMod: ${mod.modId} $id")

    val engine: ScriptEngine = NashornScriptEngineFactory().getScriptEngine(arrayOf("--language=es6"), CoreModLoader::class.java.classLoader) {
        CoreModLoader.ALLOWED_CLASSES.contains(it) || (it.lastIndexOf('.') != -1 && CoreModLoader.ALLOWED_PACKAGES.contains(it.substring(0, it.lastIndexOf('.'))))
    }

    init {
        val ctx = engine.context

        ctx.removeAttribute("load", ctx.getAttributesScope("load"))
        ctx.removeAttribute("quit", ctx.getAttributesScope("quit"))
        ctx.removeAttribute("loadWithNewGlobal", ctx.getAttributesScope("loadWithNewGlobal"))
        ctx.removeAttribute("exit", ctx.getAttributesScope("exit"))
    }

    fun init() {
        engine.eval(modifyScript(this))

        tracked = this
        bindings = (engine as Invocable).invokeFunction("initializeCoreMod") as Map<String, out Bindings>
        tracked = null
        loaded = true

        logger.debug("Loading coremod $id from mod ${mod.displayName} (${mod.modId})")
        for ((name, data) in bindings) {
            val targetData = data["target"] as Map<String, Any?>
            val type = TargetType.byName(targetData["type"] as String)
            val function = data["transformer"] as Bindings

            logger.debug("Loading binding $name")

            when (type) {
                TargetType.CLASS -> {
                    val targets = if (targetData.contains("names")) {
                        val names = NashornHelper.getFunction<Map<String, Any?>, Map<String, Any?>>(targetData["names"] as Bindings)
                        names.apply(targetData).values.map { it as String }
                    } else {
                        listOf(targetData["name"] as String)
                    }

                    for (target in targets) {
                        logger.debug("Binding $name: Added class $target as target")

                        ClassTinkerers.addTransformation(KiltRemapper.remapClass(target, ignoreWorkaround = true)) {
                            try {
                                NashornHelper.getFunction<ClassNode, ClassNode>(function).apply(it)
                            } catch (e: Throwable) {
                                throw RuntimeException("[CoreMod: ${mod.modId}/${this.id}/$name] Failed to bind class $target!", e)
                            }
                        }
                    }
                }

                TargetType.FIELD -> {
                    val className = targetData["class"] as String
                    val fieldName = targetData["fieldName"] as String
                    val mappedFieldName = KiltRemapper.srgMappedFields[fieldName]?.second ?: fieldName

                    logger.debug("Binding $name: Added field $fieldName / $mappedFieldName from class $className as target")
                    ClassTinkerers.addTransformation(KiltRemapper.remapClass(className, ignoreWorkaround = true)) { classNode ->
                        val field = classNode.fields.firstOrNull { it.name == mappedFieldName } ?: return@addTransformation
                        try {
                            NashornHelper.getFunction<FieldNode, FieldNode>(function).apply(field)
                        } catch (e: Throwable) {
                            throw RuntimeException("[CoreMod: ${mod.modId}/${this.id}/$name] Failed to bind field $fieldName / $mappedFieldName for class $className!", e)
                        }
                    }
                }

                TargetType.METHOD -> {
                    val className = targetData["class"] as String
                    val methodName = targetData["methodName"] as String
                    val descName = targetData["methodDesc"] as String

                    val mappedMethodName = KiltRemapper.srgMappedMethods[methodName]?.get(className) ?: KiltRemapper.srgMappedMethods[methodName]?.values?.firstOrNull() ?: methodName
                    val mappedDescName = KiltRemapper.remapDescriptor(descName)

                    logger.debug("Binding $name: Added method $methodName$mappedDescName / $mappedMethodName$mappedDescName from class $className as target")
                    ClassTinkerers.addTransformation(KiltRemapper.remapClass(className, ignoreWorkaround = true)) { classNode ->
                        val method = classNode.methods.firstOrNull { it.name == mappedMethodName && it.desc == mappedDescName } ?: return@addTransformation
                        try {
                            NashornHelper.getFunction<MethodNode, MethodNode>(function).apply(method)
                        } catch (e: Throwable) {
                            throw RuntimeException("[CoreMod: ${mod.modId}/${this.id}/$name] Failed to bind method $methodName$mappedDescName / $mappedMethodName$mappedDescName for class $className!", e)
                        }
                    }
                }
            }
        }
    }

    fun loadAdditionalFile(name: String): Boolean {
        if (loaded)
            return false

        val additional = mod.getFile(name)?.bufferedReader() ?: return false
        engine.eval(additional)
        return true
    }

    fun loadAdditionalData(name: String): Any? {
        if (loaded)
            return null

        val additional = mod.getFile(name)?.bufferedReader()?.readText() ?: return null
        return engine.eval("tmp_json_loading_variable = $additional;")
    }

    fun logMessage(level: String, message: String, args: Array<out Any?>) {
        logger.info(MarkerFactory.getMarker(level), message, args)
    }

    companion object {
        private val modifiedScriptPath = (KiltLoader.kiltCacheDir / "modifiedCoreMods").apply {
            runCatching {
                this.deleteIfExists()
                if (KiltFlags.STORE_MODIFIED_COREMODS)
                    this.createDirectories()
            }
        }
        private val currentLocalCoreMod: ThreadLocal<CoreMod?> = ThreadLocal.withInitial { null }
        var tracked: CoreMod?
            get() = currentLocalCoreMod.get()
            set(value) = currentLocalCoreMod.set(value)

        val remappedNames = mapOf(
            "new FieldInsnNode" to "new KiltMC_RemappingFieldInsnNode",
            "new MethodInsnNode" to "new KiltMC_RemappingMethodInsnNode",
        )

        private fun modifyScript(coreMod: CoreMod): String {
            var currentScript = coreMod.data

            // Add CoreModHelper to all scripts for the sake of handling stuff better
            currentScript = "var KiltMC_CoreModHelper = Java.type('xyz.bluspring.kilt.loader.asm.coremod.CoreModHelper');\n$currentScript"

            // Add remapping instructions imports
            currentScript = "var KiltMC_RemappingFieldInsnNode = Java.type('xyz.bluspring.kilt.loader.asm.coremod.RemappingFieldInsnNode');\n$currentScript"
            currentScript = "var KiltMC_RemappingMethodInsnNode = Java.type('xyz.bluspring.kilt.loader.asm.coremod.RemappingMethodInsnNode');\n$currentScript"

            // Remap whatever names to their fixed equivalents
            for ((original, remapped) in remappedNames) {
                currentScript = currentScript.replace(original, remapped)
            }

            // Remap classes for coremods like Twilight Forest that check class names
            val lines = currentScript.lines().toMutableList()

            for ((index, line) in currentScript.lines().withIndex()) {
                val classNamesToRemap = mutableSetOf<String>()
                var currentParsed = ""
                var stringChar = '\u0000'

                for ((i, c) in line.withIndex()) {
                    if ((c == '\'' || c == '"' || c == '`') && (i == 0 || line[i - 1] != '\\')) { // Check is in string
                        if (stringChar != '\u0000' && c == stringChar) {
                            currentParsed += c

                            val assumedClassName = currentParsed.removeSurrounding("$stringChar")
                            if (assumedClassName.startsWith("net/minecraft/") || assumedClassName.startsWith("com/mojang/")) {
                                classNamesToRemap.add(currentParsed)
                            }

                            currentParsed = ""
                            stringChar = '\u0000'
                        } else if (stringChar == '\u0000') {
                            stringChar = c
                        }
                    }

                    if (stringChar != '\u0000') {
                        currentParsed += c
                    }
                }

                for (classString in classNamesToRemap) {
                    lines[index] = lines[index].replace(classString, "KiltMC_CoreModHelper.remapClass($classString)")
                }
            }

            currentScript = lines.joinToString("\n")

            // Store modified coremods
            if (modifiedScriptPath.exists() && KiltFlags.STORE_MODIFIED_COREMODS) {
                val coreModPath = modifiedScriptPath / coreMod.mod.modId

                if (!coreModPath.exists())
                    runCatching { coreModPath.createDirectories() }

                val scriptPath = coreModPath / coreMod.file.split("/").last()

                if (!scriptPath.exists())
                    scriptPath.createFile()

                scriptPath.writeText(currentScript, Charsets.UTF_8,  StandardOpenOption.WRITE)
            }

            return currentScript
        }
    }
}