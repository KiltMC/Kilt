package xyz.bluspring.kilt.mixin;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(DebugLevelSource.class)
public interface DebugLevelSourceAccessor {
    @Accessor("ALL_BLOCKS")
    static List<BlockState> getAllBlocks() {
        throw new UnsupportedOperationException();
    }

    @Mutable
    @Accessor("ALL_BLOCKS")
    static void setAllBlocks(List<BlockState> allBlocks) {
        throw new UnsupportedOperationException();
    }

    @Accessor("GRID_WIDTH")
    static int getGridWidth() {
        throw new UnsupportedOperationException();
    }

    @Mutable
    @Accessor("GRID_WIDTH")
    static void setGridWidth(int gridWidth) {
        throw new UnsupportedOperationException();
    }

    @Accessor("GRID_HEIGHT")
    static int getGridHeight() {
        throw new UnsupportedOperationException();
    }

    @Mutable
    @Accessor("GRID_HEIGHT")
    static void setGridHeight(int gridHeight) {
        throw new UnsupportedOperationException();
    }
}
