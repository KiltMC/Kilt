package xyz.bluspring.kilt.injects.world.entity.decoration;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HangingEntity.class)
public abstract class HangingEntityInject extends Entity {
    @Shadow protected Direction direction;

    public HangingEntityInject(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyExpressionValue(method = "survives", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isSolid()Z"))
    private boolean kilt$checkCanSupportCenter(boolean original, @Local BlockPos.MutableBlockPos pos) {
        return Block.canSupportCenter(this.level(), pos, this.direction) || original;
    }
}
