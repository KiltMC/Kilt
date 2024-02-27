// TRACKED HASH: 53c5190929b57765472764e578af300291448097
package xyz.bluspring.kilt.forgeinjects.world.level.chunk;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.ChunkAccessInjection;

@Mixin(ChunkAccess.class)
public class ChunkAccessInject implements ChunkAccessInjection {
    @Nullable
    @Override
    public LevelAccessor getWorldForge() {
        return null;
    }
}