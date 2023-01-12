package net.minecraftforge.event.entity.player

import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.world.Container
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.GameType
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.event.entity.living.LivingEvent
import net.minecraftforge.eventbus.api.Cancelable
import java.io.File
import java.util.Optional

open class PlayerEvent(player: Player?) : LivingEvent(player) {
    override val entity = player

    open class HarvestCheck(player: Player, state: BlockState, success: Boolean) : PlayerEvent(player) {
        val targetBlock = state
        var canHarvest = success
    }

    @Cancelable
    open class BreakSpeed(player: Player, val state: BlockState, original: Float, pos: BlockPos?) : PlayerEvent(player) {
        private val LEGACY_UNKNOWN = BlockPos(0, -1, 0)

        val position: Optional<BlockPos> = Optional.ofNullable(pos)
        val originalSpeed = original
        var newSpeed: Float = original
        val pos: BlockPos = position.orElse(LEGACY_UNKNOWN)
    }

    open class NameFormat(player: Player, val username: Component) : PlayerEvent(player) {
        var displayname: Component = username
    }

    open class TabListNameFormat(player: Player) : PlayerEvent(player) {
        var displayName: Component? = null
    }

    open class Clone(_new: Player, oldPlayer: Player, wasDeath: Boolean) : PlayerEvent(_new) {
        val original = oldPlayer
        val isWasDeath = wasDeath
    }

    open class StartTracking(player: Player, val target: Entity) : PlayerEvent(player)

    open class StopTracking(player: Player, val target: Entity) : PlayerEvent(player)

    open class LoadFromFile(player: Player, originDirectory: File, val playerUUID: String) : PlayerEvent(player) {
        val playerDirectory = originDirectory

        fun getPlayerFile(suffix: String): File {
            if ("dat" == suffix)
                throw IllegalArgumentException("The suffix 'dat' is reserved")

            return File(playerDirectory, "$playerUUID.$suffix")
        }
    }

    open class SaveToFile(player: Player, originDirectory: File, val playerUUID: String) : PlayerEvent(player) {
        val playerDirectory = originDirectory

        fun getPlayerFile(suffix: String): File {
            if ("dat" == suffix)
                throw IllegalArgumentException("The suffix 'dat' is reserved")

            return File(playerDirectory, "$playerUUID.$suffix")
        }
    }

    open class ItemPickupEvent(player: Player, entPickedUp: ItemEntity, val stack: ItemStack) : PlayerEvent(player) {
        val originalStack = entPickedUp
    }

    open class ItemCraftedEvent(player: Player, val crafting: ItemStack, craftMatrix: Container) : PlayerEvent(player) {
        val inventory = craftMatrix
    }

    open class ItemSmeltedEvent(player: Player, crafting: ItemStack) : PlayerEvent(player) {
        val smelting = crafting
    }

    open class PlayerLoggedInEvent(player: Player) : PlayerEvent(player)

    open class PlayerLoggedOutEvent(player: Player) : PlayerEvent(player)

    open class PlayerRespawnEvent(player: Player, endConquered: Boolean) : PlayerEvent(player) {
        val isEndConquered = endConquered
    }

    open class PlayerChangedDimensionEvent(player: Player, fromDim: ResourceKey<Level>, toDim: ResourceKey<Level>) : PlayerEvent(player) {
        val from = fromDim
        val to = toDim
    }

    @Cancelable
    open class PlayerChangeGameModeEvent(player: Player, val currentGameMode: GameType, var newGameMode: GameType) : PlayerEvent(player)
}