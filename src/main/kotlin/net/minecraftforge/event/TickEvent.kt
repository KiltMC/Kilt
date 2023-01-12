package net.minecraftforge.event

import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.fml.LogicalSide
import java.util.function.BooleanSupplier

open class TickEvent(
    @JvmField val type: Type,
    @JvmField val side: LogicalSide,
    @JvmField val phase: Phase
) : Event() {
    enum class Type {
        LEVEL, PLAYER, CLIENT, SERVER, RENDER
    }

    enum class Phase {
        START, END
    }

    class ServerTickEvent(phase: Phase, private val haveTime: BooleanSupplier, val server: MinecraftServer) : TickEvent(Type.SERVER, LogicalSide.SERVER, phase) {
        fun haveTime(): Boolean {
            return haveTime.asBoolean
        }
    }

    class ClientTickEvent(phase: Phase) : TickEvent(Type.CLIENT, LogicalSide.CLIENT, phase)

    class LevelTickEvent(side: LogicalSide, phase: Phase, @JvmField val level: Level, private val haveTime: BooleanSupplier) : TickEvent(Type.LEVEL, side, phase) {
        fun haveTime(): Boolean {
            return haveTime.asBoolean
        }
    }

    class PlayerTickEvent(phase: Phase, @JvmField val player: Player) : TickEvent(Type.PLAYER, if (player is ServerPlayer) LogicalSide.SERVER else LogicalSide.CLIENT, phase)

    class RenderTickEvent(phase: Phase, @JvmField val renderTickTime: Float) : TickEvent(Type.RENDER, LogicalSide.CLIENT, phase)
}