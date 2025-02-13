package xyz.bluspring.kilt.loader.asm

import com.chocohead.mm.api.ClassTinkerers
import net.minecraftforge.coremod.api.TargetType
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import xyz.bluspring.kilt.loader.asm.CoreModLoader.ALLOWED_CLASSES
import xyz.bluspring.kilt.loader.asm.CoreModLoader.ALLOWED_PACKAGES
import xyz.bluspring.kilt.loader.mod.ForgeMod
import xyz.bluspring.kilt.loader.remap.KiltRemapper
import javax.script.Bindings
import javax.script.Invocable
import javax.script.ScriptEngine

class CoreMod(val mod: ForgeMod, val id: String, val file: String) {
    private val data = String(mod.getFile(file)!!.readAllBytes())
    private var bindings: Map<String, out Bindings> = mapOf()
    private var loaded = false
    private val logger = LoggerFactory.getLogger("CoreMod: ${mod.modId} $id")

    val engine: ScriptEngine = NashornScriptEngineFactory().getScriptEngine(arrayOf("--language=es6"), CoreModLoader::class.java.classLoader) {
        ALLOWED_CLASSES.contains(it) || (it.lastIndexOf('.') != -1 && ALLOWED_PACKAGES.contains(it.substring(0, it.lastIndexOf('.'))))
    }

    init {
        val ctx = engine.context

        ctx.removeAttribute("load", ctx.getAttributesScope("load"))
        ctx.removeAttribute("quit", ctx.getAttributesScope("quit"))
        ctx.removeAttribute("loadWithNewGlobal", ctx.getAttributesScope("loadWithNewGlobal"))
        ctx.removeAttribute("exit", ctx.getAttributesScope("exit"))
    }

    fun init() {
        engine.eval(data)

        tracked = this
        bindings = (engine as Invocable).invokeFunction("initializeCoreMod") as Map<String, out Bindings>
        tracked = null
        loaded = true

        for ((name, data) in bindings) {
            val targetData = data["target"] as Map<String, Any?>
            val type = TargetType.byName(targetData["type"] as String)
            val function = data["transformer"] as Bindings

            when (type) {
                TargetType.CLASS -> {
                    val targets = if (targetData.contains("names")) {
                        val names = NashornHelper.getFunction<Map<String, Any?>, Map<String, Any?>>(targetData["names"] as Bindings)
                        names.apply(targetData).values.map { it as String }
                    } else {
                        listOf(targetData["name"] as String)
                    }

                    for (target in targets) {
                        ClassTinkerers.addTransformation(KiltRemapper.remapClass(target, ignoreWorkaround = true)) {
                            NashornHelper.getFunction<ClassNode, ClassNode>(function).apply(it)
                        }
                    }
                }

                TargetType.FIELD -> {
                    val className = targetData["class"] as String
                    val fieldName = targetData["fieldName"] as String
                    val mappedFieldName = KiltRemapper.srgMappedFields[fieldName]?.second ?: fieldName

                    ClassTinkerers.addTransformation(KiltRemapper.remapClass(className, ignoreWorkaround = true)) { classNode ->
                        val field = classNode.fields.firstOrNull { it.name == mappedFieldName } ?: return@addTransformation
                        NashornHelper.getFunction<FieldNode, FieldNode>(function).apply(field)
                    }
                }

                TargetType.METHOD -> {
                    val className = targetData["class"] as String
                    val methodName = targetData["methodName"] as String
                    val descName = targetData["methodDesc"] as String

                    val mappedMethodName = KiltRemapper.srgMappedMethods[methodName]?.get(className) ?: KiltRemapper.srgMappedMethods[methodName]?.values?.firstOrNull() ?: methodName
                    val mappedDescName = KiltRemapper.remapDescriptor(descName)

                    ClassTinkerers.addTransformation(KiltRemapper.remapClass(className, ignoreWorkaround = true)) { classNode ->
                        val method = classNode.methods.firstOrNull { it.name == mappedMethodName && it.desc == mappedDescName } ?: return@addTransformation
                        NashornHelper.getFunction<MethodNode, MethodNode>(function).apply(method)
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
        private val currentLocalCoreMod: ThreadLocal<CoreMod?> = ThreadLocal.withInitial { null }
        var tracked: CoreMod?
            get() = currentLocalCoreMod.get()
            set(value) = currentLocalCoreMod.set(value)
    }
}