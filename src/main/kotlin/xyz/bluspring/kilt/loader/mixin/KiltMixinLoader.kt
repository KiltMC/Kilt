package xyz.bluspring.kilt.loader.mixin

import net.fabricmc.loader.impl.ModContainerImpl
import net.fabricmc.loader.impl.launch.FabricLauncherBase
import org.slf4j.LoggerFactory
import org.spongepowered.asm.mixin.FabricUtil
import org.spongepowered.asm.mixin.MixinEnvironment
import org.spongepowered.asm.mixin.Mixins
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.loader.ForgeMod
import kotlin.io.path.toPath

object KiltMixinLoader {
    private val logger = LoggerFactory.getLogger("KiltMixinLoader")

    fun init(mods: List<ForgeMod>) {
        try {
            // Forge mixin refmaps need to be remapped,
            System.setProperty("mixin.env.remapRefMap", "true")

            val remapper = MixinSrgRemapper()
            MixinEnvironment.getDefaultEnvironment().remappers.add(remapper)
            logger.info("Loaded Kilt SRG to Intermediary mappings for mixin remapper!")
        } catch (e: Exception) {
            logger.error("Kilt Mixin setup error - Game will likely crash!")
            e.printStackTrace()
        }

        val configToModMap = mutableMapOf<String, ModContainerImpl>()

        mods.forEach { mod ->
            if (mod.manifest == null)
                return@forEach

            Kilt.loader.addModToFabric(mod)
            FabricLauncherBase.getLauncher().addToClassPath(mod.remappedModFile.toURI().toPath())

            val configs = mod.manifest!!.mainAttributes.getValue("MixinConfigs") ?: return@forEach
            configs.split(",").forEach {
                configToModMap[it] = mod.container.fabricModContainer

                Mixins.addConfiguration(it)
            }
        }

        Mixins.getConfigs().forEach { rawConfig ->
            val mod = configToModMap[rawConfig.name] ?: return@forEach

            val config = rawConfig.config
            config.decorate(FabricUtil.KEY_MOD_ID, mod.metadata.id)
            config.decorate(FabricUtil.KEY_COMPATIBILITY, FabricUtil.COMPATIBILITY_LATEST)
        }
    }
}