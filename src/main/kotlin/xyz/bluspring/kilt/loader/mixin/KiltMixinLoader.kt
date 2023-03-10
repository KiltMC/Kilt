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
    private val logger = LoggerFactory.getLogger("Kilt Mixin Loader")

    fun init(mods: List<ForgeMod>) {
        val configToModMap = mutableMapOf<String, ModContainerImpl>()

        try {
            MixinEnvironment.getDefaultEnvironment().remappers.add(SrgMixinRemapper())
            logger.info("Loaded Forge SRG mappings for mixin remapper!")
        } catch (e: Exception) {
            logger.error("Failed to set up Forge SRG mixins, the game is very likely going to crash!")
            e.printStackTrace()
        }

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