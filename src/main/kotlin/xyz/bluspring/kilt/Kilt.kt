package xyz.bluspring.kilt

import com.google.gson.GsonBuilder
import io.github.fabricators_of_create.porting_lib.core.event.BaseEvent
import io.github.fabricators_of_create.porting_lib.entity.events.CriticalHitEvent
import io.github.fabricators_of_create.porting_lib.entity.events.EntityEvents
import io.github.fabricators_of_create.porting_lib.entity.events.LivingEntityEvents
import io.github.fabricators_of_create.porting_lib.entity.events.PlayerInteractionEvents
import io.github.fabricators_of_create.porting_lib.entity.events.PlayerTickEvents
import io.github.fabricators_of_create.porting_lib.event.common.BlockEvents
import io.github.fabricators_of_create.porting_lib.event.common.ExplosionEvents
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents
import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult
import net.minecraftforge.common.ForgeHooks
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.ForgeEventFactory
import net.minecraftforge.event.level.BlockEvent
import net.minecraftforge.event.level.LevelEvent
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.server.ServerLifecycleHooks
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.bluspring.kilt.client.KiltClient
import xyz.bluspring.kilt.loader.KiltLoader
import xyz.bluspring.kilt.mixin.MinecraftServerAccessor
import xyz.bluspring.kilt.util.KiltHelper
import java.util.*

class Kilt : ModInitializer {
    override fun onInitialize() {
        // We have no reason to retain this info.
        KiltHelper.clearForgeClassNodes()

        registerFabricEvents()
    }

    @Suppress("removal")
    private fun registerFabricEvents() {
        UseBlockCallback.EVENT.register { player, world, hand, hitResult ->
            val pos = hitResult.blockPos
            val event = ForgeHooks.onRightClickBlock(player, hand, pos, hitResult)
            val stack = player.getItemInHand(hand)

            val canUseItem = (player.isSecondaryUseActive && (!player.mainHandItem.isEmpty || !player.offhandItem.isEmpty)) && !(player.mainHandItem.doesSneakBypassUse(player.level(), pos, player) && player.offhandItem.doesSneakBypassUse(player.level(), pos, player))

            if (event.useBlock == Event.Result.ALLOW || (event.useBlock != Event.Result.DENY && !canUseItem))
                return@register event.useBlock.toVanilla()

            if (event.useItem == Event.Result.ALLOW || (!stack.isEmpty && !player.cooldowns.isOnCooldown(stack.item))) {
                if (event.useItem == Event.Result.DENY)
                    return@register InteractionResult.PASS

                val result = stack.onItemUseFirst(UseOnContext(player, hand, hitResult))

                if (result != InteractionResult.PASS && result != InteractionResult.FAIL) {
                    return@register result
                }
            }

            InteractionResult.PASS
        }

        UseItemCallback.EVENT.register { player, world, hand ->
            InteractionResultHolder(ForgeHooks.onItemRightClick(player, hand) ?: InteractionResult.PASS, player.getItemInHand(hand))
        }

        UseEntityCallback.EVENT.register { player, world, hand, entity, hitResult ->
            ForgeHooks.onInteractEntity(player, entity, hand) ?: InteractionResult.PASS
        }

        AttackBlockCallback.EVENT.register { player, world, hand, pos, direction ->
            ForgeHooks.onLeftClickBlock(player, pos, direction).result.toVanilla()
        }

        PlayerInteractionEvents.LEFT_CLICK_EMPTY.register { event ->
            ForgeHooks.onEmptyLeftClick(event.player)
        }

        CriticalHitEvent.CRITICAL_HIT.register { event ->
            val forgeEvent = ForgeHooks.getCriticalHit(event.player, event.entity, event.isVanillaCritical, event.oldDamageModifier)

            if (forgeEvent == null) {
                event.result = BaseEvent.Result.DENY
            } else {
                event.result = BaseEvent.Result.valueOf(forgeEvent.result.name)

                if (forgeEvent.damageModifier != forgeEvent.oldDamageModifier)
                    event.damageModifier = forgeEvent.damageModifier
            }
        }

        LivingEntityEvents.LivingTickEvent.TICK.register { event ->
            if (ForgeHooks.onLivingTick(event.entity))
                event.isCanceled = true
        }

        EntitySleepEvents.ALLOW_SLEEPING.register { player, pos ->
            ForgeEventFactory.onPlayerSleepInBed(player, Optional.of(pos))
        }

        EntitySleepEvents.ALLOW_SETTING_SPAWN.register { player, pos ->
            !ForgeEventFactory.onPlayerSpawnSet(player, player.level().dimension(), pos, false)
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
            ServerLifecycleHooks.expectServerStopped()
            ServerLifecycleHooks.handleServerStopped(it)
        }

        ExplosionEvents.START.register { level, explosion ->
            ForgeEventFactory.onExplosionStart(level, explosion)
        }

        ExplosionEvents.DETONATE.register { level, explosion, entities, diameter ->
            ForgeEventFactory.onExplosionDetonate(level, explosion, entities, diameter)
        }

		BlockEvents.BLOCK_BREAK.register { event ->
			val level = event.level
			if (level is Level) {
				val forgeEvent = BlockEvent.BreakEvent(level, event.pos, event.state, event.player);
				forgeEvent.expToDrop = event.expToDrop
				forgeEvent.isCanceled = event.isCanceled
				forgeEvent.result = portingLibToEventBus(event.result)
				MinecraftForge.EVENT_BUS.post(forgeEvent)
				event.isCanceled = forgeEvent.isCanceled
				event.expToDrop = forgeEvent.expToDrop
				event.result = eventBusToPortingLib(forgeEvent.result)
			}
		}

        EntityEvents.ENTERING_SECTION.register { entity, packedOldPos, packedNewPos ->
            ForgeHooks.onEntityEnterSection(entity, packedOldPos, packedNewPos)
        }

        ServerTickEvents.START_SERVER_TICK.register { server ->
            ForgeEventFactory.onPreServerTick((server as MinecraftServerAccessor)::callHaveTime, server)
        }

        ServerTickEvents.END_SERVER_TICK.register { server ->
            ForgeEventFactory.onPostServerTick((server as MinecraftServerAccessor)::callHaveTime, server)
        }

        ServerTickEvents.START_WORLD_TICK.register { level ->
            ForgeEventFactory.onPreLevelTick(level, (level.server as MinecraftServerAccessor)::callHaveTime)
        }

        ServerTickEvents.END_WORLD_TICK.register { level ->
            ForgeEventFactory.onPostLevelTick(level, (level.server as MinecraftServerAccessor)::callHaveTime)
        }

        PlayerTickEvents.START.register { player ->
            ForgeEventFactory.onPlayerPreTick(player)
        }

        PlayerTickEvents.END.register { player ->
            ForgeEventFactory.onPlayerPostTick(player)
        }

        ServerWorldEvents.LOAD.register { server, level ->
            MinecraftForge.EVENT_BUS.post(LevelEvent.Load(level))
        }

        ServerWorldEvents.UNLOAD.register { server, level ->
            MinecraftForge.EVENT_BUS.post(LevelEvent.Unload(level))
        }

        LivingEntityEvents.LOOTING_LEVEL.register { source, target, level, _ ->
            ForgeHooks.getLootingLevel(target, source, level)
        }

        LivingEntityEvents.DROPS.register { target, source, drops, level, recentlyHit ->
            ForgeHooks.onLivingDrops(target, source, drops, level, recentlyHit)
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

		fun portingLibToEventBus(result: BaseEvent.Result): Event.Result {
			return when (result) {
				BaseEvent.Result.ALLOW -> Event.Result.ALLOW
				BaseEvent.Result.DEFAULT -> Event.Result.DEFAULT
				BaseEvent.Result.DENY -> Event.Result.DENY
				else -> Event.Result.DEFAULT
			}
		}

		fun eventBusToPortingLib(result: Event.Result): BaseEvent.Result {
			return when (result) {
				Event.Result.ALLOW -> BaseEvent.Result.ALLOW
				Event.Result.DEFAULT -> BaseEvent.Result.DEFAULT
				Event.Result.DENY -> BaseEvent.Result.DENY
				else -> BaseEvent.Result.DEFAULT
			}
		}

        fun Event.Result.toVanilla(): InteractionResult {
            return when (this) {
                Event.Result.DEFAULT -> InteractionResult.PASS
                Event.Result.DENY -> InteractionResult.FAIL
                Event.Result.ALLOW -> InteractionResult.SUCCESS
            }
        }
    }
}