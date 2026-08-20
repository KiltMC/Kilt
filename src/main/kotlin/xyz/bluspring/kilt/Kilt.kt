package xyz.bluspring.kilt

import com.google.gson.GsonBuilder
import com.mojang.datafixers.util.Either
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.util.EventResult
import net.minecraft.core.BlockPos
import net.minecraft.core.GlobalPos
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Unit
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.LevelData
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.common.extensions.IBlockExtension
import net.neoforged.neoforge.event.EventHooks
import net.neoforged.neoforge.event.level.LevelEvent
import net.neoforged.neoforge.server.ServerLifecycleHooks
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.bluspring.kilt.client.KiltClient
import xyz.bluspring.kilt.loader.KiltLoader
import xyz.bluspring.kilt.mixin.MinecraftServerAccessor
import xyz.bluspring.kilt.util.KiltHelper

class Kilt : ModInitializer {
    override fun onInitialize() {
        // We have no reason to retain this info.
        KiltHelper.clearForgeClassNodes()

        registerFabricEvents()
    }

    @Suppress("removal")
    private fun registerFabricEvents() {
        // Kilt: We rely on this event so
        /*PlayerInteractEvent.RightClickBlock.EVENT.register { event ->
            val forgeEvent = CommonHooks.onRightClickBlock(event.entity, event.hand, event.pos, event.hitVec)

            event.cancellationResult = forgeEvent.cancellationResult

            if (!forgeEvent.useBlock.isDefault)
                event.useBlock = TriState.of(forgeEvent.useBlock.isTrue)

            if (!forgeEvent.useItem.isDefault)
                event.useItem = TriState.of(forgeEvent.useItem.isTrue)
        }*/

//        PlayerInteractEvent.RightClickItem.EVENT.register { event ->
//            event.cancellationResult = CommonHooks.onItemRightClick(event.entity, event.hand)
//        }
//
//        PlayerInteractEvent.EntityInteract.EVENT.register { event ->
//            event.cancellationResult = CommonHooks.onInteractEntity(event.entity, event.target, event.hand)
//        }
//
//        PlayerInteractEvent.EntityInteractSpecific.EVENT.register { event ->
//            event.cancellationResult = CommonHooks.onInteractEntityAt(event.entity, event.target, event.localPos, event.hand)
//        }
//
//        PlayerInteractEvent.LeftClickBlock.EVENT.register { event ->
//            val forgeEvent = CommonHooks.onLeftClickBlock(event.entity, event.pos, event.face, when (event.action) {
//                PlayerInteractEvent.LeftClickBlock.Action.START -> ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK
//                PlayerInteractEvent.LeftClickBlock.Action.STOP -> ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK
//                PlayerInteractEvent.LeftClickBlock.Action.ABORT -> ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK
//                else -> ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK
//            })
//
//            if (forgeEvent.isCanceled)
//                event.isCanceled = true
//        }
//
//        CriticalHitEvent.EVENT.register { event ->
//            CommonHooks.fireCriticalHit(event.entity, event.target, event.isVanillaCritical, event.damageMultiplier)
//        }
//
//        PlayerInteractEvent.LeftClickEmpty.EVENT.register { event ->
//            CommonHooks.onEmptyLeftClick(event.entity)
//        }
//
//        CriticalHitEvent.EVENT.register { event ->
//            val forgeEvent = CommonHooks.fireCriticalHit(event.entity, event.entity, event.isVanillaCritical, event.vanillaMultiplier)
//
//            if (forgeEvent.isCriticalHit != event.isCriticalHit)
//                event.isCriticalHit = forgeEvent.isCriticalHit
//
//            if (forgeEvent.damageMultiplier != event.damageMultiplier)
//                event.damageMultiplier = forgeEvent.damageMultiplier
//        }
//
//        PlayerInteractEvent.RightClickEmpty.EVENT.register { event ->
//            CommonHooks.onEmptyClick(event.entity, event.hand)
//        }

        EntitySleepEvents.ALLOW_SLEEPING.register { player, pos ->
            if (player !is ServerPlayer) // istg
                return@register null

            EventHooks.canPlayerStartSleeping(player, pos, Either.right(Unit.INSTANCE)).left().orElse(null)
        }

        EntitySleepEvents.SET_BED_OCCUPATION_STATE.register { entity, pos, state, occupied ->
            if (
                KiltHelper.hasMethodOverride(
                    state.block.javaClass, IBlockExtension::class.java, "setBedOccupied",
                    BlockState::class.java, Level::class.java, BlockPos::class.java,
                    LivingEntity::class.java, Boolean::class.javaPrimitiveType!!
                )
            ) {
                state.setBedOccupied(entity.level(), pos, entity, occupied)
                return@register true
            } else {
                return@register false
            }
        }

        EntitySleepEvents.ALLOW_BED.register { entity, pos, state, bool ->
            if (
                KiltHelper.hasMethodOverride(
                    state.block.javaClass, IBlockExtension::class.java, "isBed",
                    BlockState::class.java, BlockGetter::class.java, BlockPos::class.java, LivingEntity::class.java
                )
            ) {
                return@register if (state.isBed(entity.level(), pos, entity)) {
                    EventResult.ALLOW
                } else {
                    EventResult.DENY
                }
            }
            return@register EventResult.PASS
        }

        EntitySleepEvents.MODIFY_SLEEPING_DIRECTION.register { entity, pos, direction ->
            val state: BlockState = entity.level().getBlockState(pos)
            if (
                (
                    KiltHelper.hasMethodOverride(
                        state.block.javaClass, IBlockExtension::class.java, "getBedDirection",
                        BlockState::class.java, LevelReader::class.java, BlockPos::class.java
                    ) || // If bed is not BedBlock we need to run the NeoForge getBedDirection for the correct result even if not overridden.
                    KiltHelper.hasMethodOverride(
                        state.block.javaClass, IBlockExtension::class.java, "isBed",
                        BlockState::class.java, BlockGetter::class.java, BlockPos::class.java, LivingEntity::class.java
                    )
                ) &&
                state.isBed(entity.level(), pos, entity)
            ) {
                return@register state.getBedDirection(entity.level(), pos)
            }
            return@register direction
        }

        EntitySleepEvents.ALLOW_SETTING_SPAWN.register { player, pos ->
            !EventHooks.onPlayerSpawnSet(player, ServerPlayer.RespawnConfig(LevelData.RespawnData(GlobalPos(player.level().dimension(), pos), 0f, 0f), false))
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

//        ExplosionEvents.START.register { level, explosion ->
//            EventHooks.onExplosionStart(level, explosion)
//        }
//
//        ExplosionEvents.DETONATE.register { level, explosion, entities, diameter ->
//            EventHooks.onExplosionDetonate(level, explosion, entities, diameter)
//        }
//
//        EntityEvents.EnteringSection.EVENT.register { event ->
//            CommonHooks.onEntityEnterSection(event.entity, event.packedOldPos, event.packedNewPos)
//        }

        ServerTickEvents.START_SERVER_TICK.register { server ->
            EventHooks.fireServerTickPre((server as MinecraftServerAccessor)::callHaveTime, server)
        }

        ServerTickEvents.END_SERVER_TICK.register { server ->
            EventHooks.fireServerTickPost((server as MinecraftServerAccessor)::callHaveTime, server)
        }

        ServerTickEvents.START_LEVEL_TICK.register { level ->
            EventHooks.fireLevelTickPre(level, (level.server as MinecraftServerAccessor)::callHaveTime)
        }

        ServerTickEvents.END_LEVEL_TICK.register { level ->
            EventHooks.fireLevelTickPost(level, (level.server as MinecraftServerAccessor)::callHaveTime)
        }

//        PlayerTickEvent.Pre.EVENT.register { event ->
//            EventHooks.firePlayerTickPre(event.entity)
//        }
//
//        PlayerTickEvent.Post.EVENT.register { event ->
//            EventHooks.firePlayerTickPost(event.entity)
//        }

        ServerLevelEvents.LOAD.register { server, level ->
            NeoForge.EVENT_BUS.post(LevelEvent.Load(level))
        }

        ServerLevelEvents.UNLOAD.register { server, level ->
            NeoForge.EVENT_BUS.post(LevelEvent.Unload(level))
        }

//        LivingDropsEvent.EVENT.register { event ->
//            if (CommonHooks.onLivingDrops(event.entity, event.source, event.drops, event.isRecentlyHit))
//                event.isCanceled = true
//        }
    }

    companion object {
        const val MOD_ID = "kilt"

        lateinit var instance: Kilt
        val logger: Logger = LoggerFactory.getLogger(Kilt::class.java)
        val loader: KiltLoader
            get() = KiltLoader.instance
        val gson = GsonBuilder().setPrettyPrinting().create()

        fun load(onServer: Boolean) {
            // config load should be here
            var loaded = false

            if (!onServer) {
                KiltClient.lateRegisterEvents()
            }
        }

        @JvmStatic
        fun id(name: String): Identifier {
            return Identifier.fromNamespaceAndPath(MOD_ID, name)
        }
    }
}
