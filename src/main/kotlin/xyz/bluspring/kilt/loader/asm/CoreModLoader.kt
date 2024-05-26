package xyz.bluspring.kilt.loader.asm

import com.google.gson.JsonParser
import net.fabricmc.loader.impl.gui.FabricGuiEntry
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.loader.mod.ForgeMod


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
        "net.minecraftforge.coremod.api.ASMAPI", "org.objectweb.asm.Opcodes",

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
        "org.objectweb.asm.TypeReference"
    )

    val loadedCoreMods = mutableListOf<CoreMod>()

    val enableCoreMods = System.getProperty("kilt.enableCoreMods") == "true"

    fun scanAndLoadCoreMods(mod: ForgeMod) {
        if (!enableCoreMods)
            return

        Kilt.logger.warn("Coremods have been enabled! Be advised that this may cause severe incompatibility issues!")

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
}