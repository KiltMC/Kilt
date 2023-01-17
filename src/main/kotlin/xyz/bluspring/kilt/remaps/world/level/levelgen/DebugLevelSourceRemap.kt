package xyz.bluspring.kilt.remaps.world.level.levelgen

import net.minecraft.core.Registry
import net.minecraft.util.Mth
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.levelgen.DebugLevelSource
import xyz.bluspring.kilt.mixin.DebugLevelSourceAccessor
import java.util.stream.Collectors
import java.util.stream.StreamSupport

object DebugLevelSourceRemap {
    @JvmStatic
    fun initValidStates() {
        // f_64114_ = ALL_BLOCKS
        // f_64115_ = GRID_WIDTH
        // f_64116_ = GRID_HEIGHT

        DebugLevelSourceAccessor.setAllBlocks(StreamSupport.stream(Registry.BLOCK.spliterator(), false).flatMap { block ->
                block.stateDefinition.possibleStates.stream()
            }.collect(Collectors.toList()))
        DebugLevelSourceAccessor.setGridWidth(Mth.ceil(Mth.sqrt(DebugLevelSourceAccessor.getAllBlocks().size.toFloat())))
        DebugLevelSourceAccessor.setGridHeight((DebugLevelSourceAccessor.getAllBlocks().size.toFloat() / DebugLevelSourceAccessor.getGridWidth().toFloat()).toInt())
    }
}