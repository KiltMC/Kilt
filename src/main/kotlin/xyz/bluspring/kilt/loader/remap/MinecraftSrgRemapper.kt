package xyz.bluspring.kilt.loader.remap

import com.chocohead.mm.api.ClassTinkerers
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.impl.launch.FabricLauncherBase
import net.fabricmc.mapping.tree.ClassDef
import net.fabricmc.mapping.tree.TinyTree
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.InputStream
import java.util.jar.JarFile

class MinecraftSrgRemapper(
    val srgIntermediaryTree: TinyTree,
    val intermediaryDevTree: TinyTree
) {
    private val isDev = FabricLoader.getInstance().isDevelopmentEnvironment
    private val launcher = FabricLauncherBase.getLauncher()
    private val fabricNamespace = if (isDev) launcher.mappingConfiguration.targetNamespace else "intermediary"

    fun remap() {
        intermediaryDevTree.classes.forEach {
            remapMinecraftClass(it.getName(fabricNamespace))
        }
    }

    private fun remapMinecraftClass(className: String) {
        val fabricClassDef = getFabricClassDef(className) ?: return
        val srgClassDef = getSrgClassDef(className) ?: return

        ClassTinkerers.addTransformation(className) {
            val classRemapper = MinecraftSrgClassRemapper(fabricNamespace, fabricClassDef, srgClassDef)
            it.accept(classRemapper)
        }
    }

    // Gets a class def that no matter what will always point to the Fabric equivalent.
    private fun getFabricClassDef(name: String): ClassDef? {
        return if (isDev) {
            intermediaryDevTree.classes.firstOrNull {
                it.getName(launcher.mappingConfiguration.targetNamespace) == name
            }
        } else {
            srgIntermediaryTree.classes.firstOrNull {
                it.getName("intermediary") == name
            }
        }
    }

    // Gets a class def that no matter what will always point to the SRG equivalent.
    private fun getSrgClassDef(name: String): ClassDef? {
        val intermediaryName = if (isDev) {
            intermediaryDevTree.classes.firstOrNull {
                it.getName(launcher.mappingConfiguration.targetNamespace) == name
            }?.getName("intermediary")
        } else name

        if (intermediaryName == null)
            return null

        return srgIntermediaryTree.classes.firstOrNull {
            it.getName("intermediary") == intermediaryName
        }
    }
}