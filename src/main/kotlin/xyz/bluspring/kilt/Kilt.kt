package xyz.bluspring.kilt

import com.google.gson.GsonBuilder
import com.mojang.datafixers.util.Either
import dev.architectury.event.CompoundEventResult
import dev.architectury.event.EventResult
import io.github.fabricators_of_create.porting_lib.core.event.BaseEvent
import io.github.fabricators_of_create.porting_lib.entity.events.EntityEvents
import io.github.fabricators_of_create.porting_lib.entity.events.living.LivingDropsEvent
import io.github.fabricators_of_create.porting_lib.entity.events.player.CriticalHitEvent
import io.github.fabricators_of_create.porting_lib.entity.events.player.PlayerInteractEvent
import io.github.fabricators_of_create.porting_lib.entity.events.tick.PlayerTickEvent
import io.github.fabricators_of_create.porting_lib.event.common.ExplosionEvents
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents
import net.fabricmc.fabric.api.util.TriState
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Unit
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.phys.BlockHitResult
import net.neoforged.bus.api.Event
import net.neoforged.neoforge.common.CommonHooks
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.EventHooks
import net.neoforged.neoforge.event.entity.living.LivingEvent
import net.neoforged.neoforge.event.level.LevelEvent
import net.neoforged.neoforge.server.ServerLifecycleHooks
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.bluspring.kilt.client.KiltClient
import xyz.bluspring.kilt.loader.KiltLoader
import xyz.bluspring.kilt.mixin.MinecraftServerAccessor
import java.util.*

class Kilt : ModInitializer {
    override fun onInitialize() {
        registerFabricEvents()
    }

    @Suppress("removal")
    private fun registerFabricEvents() {
        PlayerInteractEvent.RightClickBlock.EVENT.register { event ->
            val forgeEvent = CommonHooks.onRightClickBlock(event.entity, event.hand, event.pos, event.hitVec)

            event.cancellationResult = forgeEvent.cancellationResult

            if (!forgeEvent.useBlock.isDefault)
                event.useBlock = TriState.of(forgeEvent.useBlock.isTrue)

            if (!forgeEvent.useItem.isDefault)
                event.useItem = TriState.of(forgeEvent.useItem.isTrue)
        }

        PlayerInteractEvent.RightClickItem.EVENT.register { event ->
            event.cancellationResult = CommonHooks.onItemRightClick(event.entity, event.hand)
        }

        PlayerInteractEvent.EntityInteract.EVENT.register { event ->
            event.cancellationResult = CommonHooks.onInteractEntity(event.entity, event.target, event.hand)
        }

        PlayerInteractEvent.EntityInteractSpecific.EVENT.register { event ->
            event.cancellationResult = CommonHooks.onInteractEntityAt(event.entity, event.target, event.localPos, event.hand)
        }

        PlayerInteractEvent.LeftClickBlock.EVENT.register { event ->
            val forgeEvent = CommonHooks.onLeftClickBlock(event.entity, event.pos, event.face, when (event.action) {
                PlayerInteractEvent.LeftClickBlock.Action.START -> ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK
                PlayerInteractEvent.LeftClickBlock.Action.STOP -> ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK
                PlayerInteractEvent.LeftClickBlock.Action.ABORT -> ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK
                else -> ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK
            })

            if (forgeEvent.isCanceled)
                event.isCanceled = true
        }

        PlayerInteractEvent.LeftClickEmpty.EVENT.register { event ->
            CommonHooks.onEmptyLeftClick(event.entity)
        }

        CriticalHitEvent.EVENT.register { event ->
            val forgeEvent = CommonHooks.fireCriticalHit(event.entity, event.entity, event.isVanillaCritical, event.vanillaMultiplier)

            if (forgeEvent.isCriticalHit != event.isCriticalHit)
                event.isCriticalHit = forgeEvent.isCriticalHit

            if (forgeEvent.damageMultiplier != event.damageMultiplier)
                event.damageMultiplier = forgeEvent.damageMultiplier
        }

        PlayerInteractEvent.RightClickEmpty.EVENT.register { event ->
            CommonHooks.onEmptyClick(event.entity, event.hand)
        }

        EntitySleepEvents.ALLOW_SLEEPING.register { player, pos ->
            if (player !is ServerPlayer) // istg
                return@register null

            EventHooks.canPlayerStartSleeping(player, pos, Either.right(Unit.INSTANCE)).left().orElse(null)
        }

        EntitySleepEvents.ALLOW_SETTING_SPAWN.register { player, pos ->
            !EventHooks.onPlayerSpawnSet(player, player.level().dimension(), pos, false)
        }

        ServerLifecycleEvents.SERVER_STARTED.register {
            ServerLifecycleHooks.handleServerStarted(it)
        }

        ServerLifecycleEvents.SERVER_STOPPING.register {
            ServerLifecycleHooks.handleServerStopping(it)
        }

        ServerLifecycleEvents.SERVER_STOPPED.register {
            ServerLifecycleHooks.expectServerStopped()
            ServerLifecycleHooks.handleServerStopped(it)
        }

        ExplosionEvents.START.register { level, explosion ->
            EventHooks.onExplosionStart(level, explosion)
        }

        ExplosionEvents.DETONATE.register { level, explosion, entities, diameter ->
            EventHooks.onExplosionDetonate(level, explosion, entities, diameter)
        }

        EntityEvents.EnteringSection.EVENT.register { event ->
            CommonHooks.onEntityEnterSection(event.entity, event.packedOldPos, event.packedNewPos)
        }

        dev.architectury.event.events.common.EntityEvent.ANIMAL_TAME.register { animal, player ->
            if (EventHooks.onAnimalTame(animal, player))
                EventResult.interruptDefault()
            else
                EventResult.pass()
        }

        ServerTickEvents.START_SERVER_TICK.register { server ->
            EventHooks.fireServerTickPre((server as MinecraftServerAccessor)::callHaveTime, server)
        }

        ServerTickEvents.END_SERVER_TICK.register { server ->
            EventHooks.fireServerTickPost((server as MinecraftServerAccessor)::callHaveTime, server)
        }

        ServerTickEvents.START_WORLD_TICK.register { level ->
            EventHooks.fireLevelTickPre(level, (level.server as MinecraftServerAccessor)::callHaveTime)
        }

        ServerTickEvents.END_WORLD_TICK.register { level ->
            EventHooks.fireLevelTickPost(level, (level.server as MinecraftServerAccessor)::callHaveTime)
        }

        PlayerTickEvent.Pre.EVENT.register { event ->
            EventHooks.firePlayerTickPre(event.entity)
        }

        PlayerTickEvent.Post.EVENT.register { event ->
            EventHooks.firePlayerTickPost(event.entity)
        }

        ServerWorldEvents.LOAD.register { server, level ->
            NeoForge.EVENT_BUS.post(LevelEvent.Load(level))
        }

        ServerWorldEvents.UNLOAD.register { server, level ->
            NeoForge.EVENT_BUS.post(LevelEvent.Unload(level))
        }

        LivingDropsEvent.EVENT.register { event ->
            if (CommonHooks.onLivingDrops(event.entity, event.source, event.drops, event.isRecentlyHit))
                event.isCanceled = true
        }
    }

    companion object {
        const val MOD_ID = "kilt"

        lateinit var instance: Kilt
        val logger: Logger = LoggerFactory.getLogger(Kilt::class.java)
        val loader: KiltLoader
            get() = KiltLoader.instance
        val gson = GsonBuilder().setPrettyPrinting().create()

        fun load(onServer: Boolean) {
            loader.initMods()

            // config load should be here
            var loaded = false

            if (!onServer) {
                KiltClient.lateRegisterEvents()
            }
        }
    }
}