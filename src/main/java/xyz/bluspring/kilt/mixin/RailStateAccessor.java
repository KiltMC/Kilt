package xyz.bluspring.kilt.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RailState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RailState.class)
public interface RailStateAccessor {
    @Accessor
    BlockPos getPos();
}
