package xyz.bluspring.mods.flashbacksablecompat.compat

import com.moulberry.flashback.Flashback
import com.moulberry.flashback.action.ActionRegistry
import com.moulberry.flashback.io.ReplayWriter
import dev.ryanhcode.sable.Sable
import dev.ryanhcode.sable.SableCommonEvents
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer
import dev.ryanhcode.sable.network.tcp.SableTCPPacket
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.level.lighting.LevelLightEngine
import java.util.function.Consumer

object SableSupport {
    @JvmStatic
    fun shouldWritePacket(): Boolean {
        return Flashback.RECORDER != null && Flashback.RECORDER.readyToWrite()
    }

    @JvmStatic
    fun <T : SableTCPPacket> submitPacket(action: ActionSableUdp<T>, packet: T) {
        Minecraft.getInstance().submit(Runnable {
            if (shouldWritePacket()) {
                Flashback.RECORDER.submitCustomTask(Consumer { writer: ReplayWriter? ->
                    writer!!.startAction(action)
                    action.streamCodec.encode(writer.friendlyByteBuf(), packet)
                    writer.finishAction(action)
                })
            }
        })
    }

    @JvmStatic
    fun initialize() {
        ActionRegistry.register(ActionSableSnapshot)
        ActionRegistry.register(ActionSableSnapshotInfo)
    }

    // Matches https://github.com/ryanhcode/sable/blob/main/common/src/main/java/dev/ryanhcode/sable/mixin/plot/LevelChunkMixin.java
    @JvmStatic
    fun handleSubLevelPlotBlockChange(level: Level, pos: BlockPos?, state: BlockState) {
        if (pos != null) {
            val subLevel = Sable.HELPER.getContaining(level, pos)
                ?: return

            subLevel.plot.onBlockChange(pos, state)
        }
    }

    @JvmStatic
    fun handleSubLevelBlockChange(level: Level?, chunk: LevelChunk, pos: BlockPos, oldState: BlockState?, newState: BlockState?) {
        if (level is ServerLevel && oldState !== newState) {
            SableCommonEvents.handleBlockChange(level, chunk, pos.x, pos.y, pos.z, oldState, newState)
        }
    }

    // Matches https://github.com/ryanhcode/sable/blob/main/common/src/main/java/dev/ryanhcode/sable/mixin/plot/lighting/LevelChunkMixin.java
    @JvmStatic
    fun tryUseSableLightEngine(level: Level?, chunk: LevelChunk, original: LevelLightEngine?): LevelLightEngine? {
        val container = SubLevelContainer.getContainer(level)

        if (container != null && level is ServerLevel) {
            val plot = container.getPlot(chunk.pos)

            if (plot != null) {
                return plot.lightEngine
            }
        }

        return original
    }

    @JvmStatic
    fun getSableChunks(level: Level?): List<LevelChunk> {
        val container = SubLevelContainer.getContainer(level)
            ?: return mutableListOf()

        val chunks: MutableList<LevelChunk> = ArrayList()
        for (subLevel in container.allSubLevels) {
            for (loadedChunk in subLevel.plot.loadedChunks) {
                chunks.add(loadedChunk.chunk)
            }
        }

        return chunks
    }
}
