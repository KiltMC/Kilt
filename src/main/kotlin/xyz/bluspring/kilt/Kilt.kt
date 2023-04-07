package xyz.bluspring.kilt

import io.github.fabricators_of_create.porting_lib.event.common.LivingEntityEvents
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraftforge.client.ForgeHooksClient
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.ForgeEventFactory
import net.minecraftforge.event.entity.living.LivingDropsEvent
import net.minecraftforge.fml.ModLoadingPhase
import net.minecraftforge.fml.ModLoadingStage
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.server.ServerLifecycleHooks
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.bluspring.kilt.loader.KiltLoader
import java.util.*

class Kilt : ModInitializer {
    override fun onInitialize() {

        registerFabricEvents()
        loader.runPhaseExecutors(ModLoadingPhase.GATHER)

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

        EntitySleepEvents.ALLOW_SLEEPING.register { player, pos ->
            ForgeEventFactory.onPlayerSleepInBed(player, Optional.of(pos))
        }

        EntitySleepEvents.ALLOW_SETTING_SPAWN.register { player, pos ->
            ForgeEventFactory.onPlayerSpawnSet(player, player.level.dimension(), pos, false)
        }

        EntitySleepEvents.ALLOW_SLEEP_TIME.register { player, pos, _ ->
            val ret = ForgeEventFactory.fireSleepingTimeCheck(player, Optional.of(pos))

            if (ret)
                InteractionResult.SUCCESS
            else
                InteractionResult.FAIL
        }

        ServerLifecycleEvents.SERVER_STARTED.register {
            ServerLifecycleHooks.handleServerStarted(it)
        }

        ServerLifecycleEvents.SERVER_STOPPING.register {
            ServerLifecycleHooks.handleServerStopping(it)
        }

        ServerLifecycleEvents.SERVER_STOPPED.register {
            ServerLifecycleHooks.handleServerStopped(it)
        }
    }

    companion object {
        const val MOD_ID = "kilt"

        val logger: Logger = LoggerFactory.getLogger(Kilt::class.java)
        val loader: KiltLoader = KiltLoader()
    }
}