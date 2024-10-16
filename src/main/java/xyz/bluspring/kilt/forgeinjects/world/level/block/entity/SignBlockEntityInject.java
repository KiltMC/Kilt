// TRACKED HASH: 11d973dfa8089566cf79371384467aeadb0d1a77
package xyz.bluspring.kilt.forgeinjects.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.workarounds.CapabilityInvalidationWorkaround;

@Mixin(SignBlockEntity.class)
public abstract class SignBlockEntityInject extends BlockEntity implements CapabilityInvalidationWorkaround {
    public SignBlockEntityInject(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(this.getBlockPos(), this.getBlockPos().offset(1, 1, 1));
    }

    @Override
    public void invalidateCaps() {
        this.kilt$invalidateCaps();
    }
}