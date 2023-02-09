package xyz.bluspring.kilt.injections.world.level.levelgen;

import net.minecraft.core.Registry;
import net.minecraft.util.Mth;
import xyz.bluspring.kilt.mixin.DebugLevelSourceAccessor;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public interface DebugLevelSourceInjection {
    static void initValidStates() {
        // f_64114_ = ALL_BLOCKS
        // f_64115_ = GRID_WIDTH
        // f_64116_ = GRID_HEIGHT

        DebugLevelSourceAccessor.setAllBlocks(
                StreamSupport.stream(Registry.BLOCK.spliterator(), false).flatMap((block) ->
                    block.getStateDefinition().getPossibleStates().stream()
                ).collect(Collectors.toList())
        );
        DebugLevelSourceAccessor.setGridWidth(Mth.ceil(Mth.sqrt((float) DebugLevelSourceAccessor.getAllBlocks().size())));
        DebugLevelSourceAccessor.setGridHeight(
                (int) ((float) DebugLevelSourceAccessor.getAllBlocks().size() / (float) DebugLevelSourceAccessor.getGridWidth())
        );
    }
}
