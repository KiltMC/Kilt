package xyz.bluspring.kilt.injects.world.entity.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityInject extends Entity {
    @Shadow private BlockState blockState;

    public FallingBlockEntityInject(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z", ordinal = 0))
    private boolean kilt$checkCanBeHydrated(FluidState instance, TagKey<Fluid> tag, Operation<Boolean> original, @Local BlockPos pos) {
        return original.call(instance, tag) || this.blockState.canBeHydrated(this.level(), pos, instance, pos);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z", ordinal = 1))
    private boolean kilt$checkCanBeHydrated(FluidState instance, TagKey<Fluid> tag, Operation<Boolean> original, @Local BlockPos pos, @Local BlockHitResult hitResult) {
        return original.call(instance, tag) || this.blockState.canBeHydrated(this.level(), pos, instance, hitResult.getBlockPos());
    }
}
