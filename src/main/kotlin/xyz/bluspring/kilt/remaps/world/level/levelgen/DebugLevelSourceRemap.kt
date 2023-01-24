package xyz.bluspring.kilt.remaps.world.level.levelgen

import net.minecraft.core.Registry
import net.minecraft.util.Mth
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.levelgen.DebugLevelSource
import net.minecraft.world.level.levelgen.structure.StructureSet
import xyz.bluspring.kilt.mixin.DebugLevelSourceAccessor
import java.util.stream.Collectors
import java.util.stream.StreamSupport

open class DebugLevelSourceRemap(registry: Registry<StructureSet>, registry2: Registry<Biome>) : DebugLevelSource(registry, registry2) {
    companion object {
        @JvmStatic
        fun initValidStates() {
            // f_64114_ = ALL_BLOCKS
            // f_64115_ = GRID_WIDTH
            // f_64116_ = GRID_HEIGHT

            DebugLevelSourceAccessor.setAllBlocks(
                StreamSupport.stream(Registry.BLOCK.spliterator(), false).flatMap { block ->
                    block.stateDefinition.possibleStates.stream()
                }.collect(Collectors.toList())
            )
            DebugLevelSourceAccessor.setGridWidth(Mth.ceil(Mth.sqrt(DebugLevelSourceAccessor.getAllBlocks().size.toFloat())))
            DebugLevelSourceAccessor.setGridHeight(
                (DebugLevelSourceAccessor.getAllBlocks().size.toFloat() / DebugLevelSourceAccessor.getGridWidth()
                    .toFloat()).toInt()
            )
        }
    }
}