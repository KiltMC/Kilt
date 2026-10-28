package xyz.bluspring.kilt

import com.google.gson.GsonBuilder
import com.mojang.datafixers.util.Either
import io.github.fabricators_of_create.porting_lib.entity.events.EntityEvents
import io.github.fabricators_of_create.porting_lib.entity.events.living.LivingDropsEvent
import io.github.fabricators_of_create.porting_lib.entity.events.player.CriticalHitEvent
import io.github.fabricators_of_create.porting_lib.entity.events.player.PlayerInteractEvent
import io.github.fabricators_of_create.porting_lib.entity.events.tick.PlayerTickEvent
import io.github.fabricators_of_create.porting_lib.event.common.ExplosionEvents
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer
import net.minecraft.core.BlockPos
import net.minecraft.core.Registry
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Unit
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.bus.api.EventPriority
import net.neoforged.neoforge.common.CommonHooks
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.common.crafting.IngredientType
import net.neoforged.neoforge.common.extensions.IBlockExtension
import net.neoforged.neoforge.common.util.TriState
import net.neoforged.neoforge.event.EventHooks
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent
import net.neoforged.neoforge.event.level.LevelEvent
import net.neoforged.neoforge.registries.NeoForgeRegistries
import net.neoforged.neoforge.registries.callback.AddCallback
import net.neoforged.neoforge.server.ServerLifecycleHooks
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.bluspring.kilt.api.event.UnhandledLivingDeathCallback
import xyz.bluspring.kilt.client.KiltClient
import xyz.bluspring.kilt.loader.KiltLoader
import xyz.bluspring.kilt.mixin.MinecraftServerAccessor
import xyz.bluspring.kilt.util.KiltHelper
import xyz.bluspring.kilt.workarounds.resource.FabricCustomIngredientSerializer
import net.fabricmc.fabric.api.util.TriState as FabricTriState

class Kilt : ModInitializer {
    override fun onInitialize() {
        // We have no reason to retain this info.
        KiltHelper.clearForgeClassNodes()
        FabricCustomIngredientSerializer.EmptyCustomIngredient.TYPE // bonk

        registerFabricEvents()

        // Some mods don't call super.die(). Guess it's our job to handle the events for everyone.
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, LivingDeathEvent::class.java) { event ->
            if (!event.entity.`kilt$wasDeathHandledByUs`() && !event.isCanceled) {
                val level = event.entity.level()
                if (level !is ServerLevel)
                    return@addListener

                val plEvent = io.github.fabricators_of_create.porting_lib.entity.events.living.LivingDeathEvent(event.entity, event.source)
                if (plEvent.post()) {
                    event.isCanceled = true
                    return@addListener
                }

                if (UnhandledLivingDeathCallback.EVENT.invoker().shouldCancelUnhandledLivingDeath(event.entity, event.source)) {
                    event.isCanceled = true
                    return@addListener
                }

                val killer = event.source.directEntity
                if (killer != null)
                    ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.invoker().afterKilledOtherEntity(level, killer, event.entity)

                ServerLivingEntityEvents.AFTER_DEATH.invoker().afterDeath(event.entity, event.source)
            }
        }

        NeoForgeRegistries.INGREDIENT_TYPES.addCallback(object : AddCallback<IngredientType<*>> {
            override fun onAdd(registry: Registry<IngredientType<*>>, id: Int, key: ResourceKey<IngredientType<*>>, value: IngredientType<*>) {
                if (CustomIngredientSerializer.get(key.location()) == null)
                    FabricCustomIngredientSerializer.getOrCreate(value)
            }
        })
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

            if (forgeEvent.useBlock != TriState.DEFAULT)
                event.useBlock = FabricTriState.of(forgeEvent.useBlock.isTrue)

            if (forgeEvent.useItem != TriState.DEFAULT)
                event.useItem = FabricTriState.of(forgeEvent.useItem.isTrue)
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

        /*PlayerInteractEvent.RightClickEmpty.EVENT.register { event ->
            CommonHooks.onEmptyClick(event.entity, event.hand)
        }*/

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
                    InteractionResult.SUCCESS
                } else {
                    InteractionResult.FAIL
                }
            }
            return@register InteractionResult.PASS
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

        EntityElytraEvents.CUSTOM.register { entity, tickElytra ->
            val chestPiece = entity.getItemBySlot(EquipmentSlot.CHEST)
            chestPiece.canElytraFly(entity) && chestPiece.elytraFlightTick(entity, entity.fallFlyingTicks)
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
            // config load should be here
            var loaded = false

            if (!onServer) {
                KiltClient.lateRegisterEvents()
            }
        }

        @JvmStatic
        fun id(name: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(MOD_ID, name)
    }
}
