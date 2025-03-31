package xyz.bluspring.kilt.forgeinjects.world.level.chunk;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.ChunkAccessInjection;

import java.util.Map;

@Mixin(ChunkAccess.class)
public class ChunkAccessInject implements ChunkAccessInjection {
    @Shadow @Final protected Map<BlockPos, BlockEntity> blockEntities;

    @Nullable
    @Override
    public LevelAccessor getWorldForge() {
        return null;
    }
}
