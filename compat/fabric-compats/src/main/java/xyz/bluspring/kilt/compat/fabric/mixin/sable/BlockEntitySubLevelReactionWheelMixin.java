package xyz.bluspring.kilt.compat.fabric.mixin.sable;

import dev.ryanhcode.sable.api.block.BlockEntitySubLevelReactionWheel;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.AbstractOverride;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(BlockEntitySubLevelReactionWheel.class)
public interface BlockEntitySubLevelReactionWheelMixin {
    // Kilt TODO: If Sable solves this in a future update, remove this.
    
    @AbstractOverride
    default BlockState getBlockState() {
        if (this instanceof BlockEntity blockEntity) {
            return blockEntity.getBlockState();
        }

        return null;
    }
}
