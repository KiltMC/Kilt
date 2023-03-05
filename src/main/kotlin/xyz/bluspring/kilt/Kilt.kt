package xyz.bluspring.kilt

import io.github.fabricators_of_create.porting_lib.event.common.LivingEntityEvents
import net.fabricmc.api.ModInitializer
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraftforge.client.ForgeHooksClient
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.living.LivingDropsEvent
import net.minecraftforge.fml.ModLoadingPhase
import net.minecraftforge.fml.ModLoadingStage
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.bluspring.kilt.loader.KiltLoader

class Kilt : ModInitializer {
    override fun onInitialize() {
        registerFabricEvents()
        loader.runPhaseExecutors(ModLoadingPhase.GATHER)

        ForgeHooksClient::class.java.declaredMethods.forEach {
            println("${it.name} ${it.parameterCount}")
        }

        ForgeHooksClient::class.java.methods.forEach {
            println("${it.name} ${it.parameterCount}")
        }

        // config load should be here
        loader.runPhaseExecutors(ModLoadingPhase.LOAD)

        loader.mods.forEach { mod ->
            mod.eventBus.post(FMLCommonSetupEvent(mod, ModLoadingStage.COMMON_SETUP))
        }

        loader.runPhaseExecutors(ModLoadingPhase.COMPLETE)
    }

    private fun registerFabricEvents() {
        LivingEntityEvents.DROPS.register { entity, source, drops ->
            val lootingLevel = EnchantmentHelper.getMobLooting(entity)
            MinecraftForge.EVENT_BUS.post(LivingDropsEvent(entity, source, drops, lootingLevel, true))
        }
    }

    companion object {
        const val MOD_ID = "kilt"

        val logger: Logger = LoggerFactory.getLogger(Kilt::class.java)
        val loader: KiltLoader = KiltLoader()
    }
}