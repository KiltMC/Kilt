package xyz.bluspring.kilt.compat.fabric.mixin.sable;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import dev.ryanhcode.sable.api.block.propeller.BlockEntityPropeller;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.AbstractOverride;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

@IfModLoaded("sable")
@Mixin(BlockEntityPropeller.class)
public interface BlockEntityPropellerMixin {
    // Kilt TODO: If Sable solves this in a future update, remove this.

    @AbstractOverride
    default Level getLevel() {
        if (this instanceof BlockEntity blockEntity) {
            return blockEntity.getLevel();
        }

        return null;
    }

    @AbstractOverride
    default BlockPos getBlockPos() {
        if (this instanceof BlockEntity blockEntity) {
            return blockEntity.getBlockPos();
        }

        return null;
    }
}
