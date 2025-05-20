// TRACKED HASH: c1c579e966fc78d57072df71b93acdf023e75c9e
package xyz.bluspring.kilt.forgeinjects.world.entity.vehicle;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.extensions.IForgeBoat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.util.KiltHelper;

@Mixin(Boat.class)
public abstract class BoatInject extends Entity implements IForgeBoat {
    @Shadow private int lerpSteps;

    @Shadow private double lerpX;

    @Shadow private double lerpY;

    @Shadow private double lerpZ;

    @Shadow private double lerpYRot;

    @Shadow private double lerpXRot;

    public BoatInject(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @WrapOperation(method = {"getWaterLevelAbove", "checkInWater", "isUnderwater"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    public boolean kilt$checkIfBoatingInFluid(FluidState instance, TagKey<Fluid> tag, Operation<Boolean> original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(this.getClass(), Boat.class, "canBoatInFluid", FluidState.class)) {
            return this.canBoatInFluid(instance);
        }

        return original.call(instance, tag);
    }

    @WrapOperation(method = "getGroundFriction", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;getFriction()F"))
    public float kilt$useForgeFriction(Block instance, Operation<Float> original, @Local BlockState state, @Local BlockPos.MutableBlockPos mutableBlockPos) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getClass(), Block.class, "getFriction", BlockState.class, LevelReader.class, BlockPos.class, Entity.class)) {
            return instance.getFriction(state, this.level(), mutableBlockPos, (Boat) (Object) this);
        }

        return original.call(instance);
    }

    @WrapOperation(method = "checkFallDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    public boolean kilt$checkIfBoatIsInFluidBeforeState(FluidState instance, TagKey<Fluid> tag, Operation<Boolean> original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(this.getClass(), Boat.class, "canBoatInFluid", FluidState.class)) {
            return !this.canBoatInFluid(instance);
        }

        return original.call(instance, tag);
    }

    @WrapOperation(method = "canAddPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/Boat;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z"))
    public boolean kilt$checkIfCanBoatBeforePassenger(Boat instance, TagKey tagKey, Operation<Boolean> original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(this.getClass(), Boat.class, "canBoatInFluid", FluidState.class)) {
            return this.canBoatInFluid(this.getEyeInFluidType());
        }

        return original.call(instance, tagKey);
    }

    // a forge fix, might as well
    @Override
    protected void addPassenger(Entity passenger) {
        super.addPassenger(passenger);
        if (this.isControlledByLocalInstance() && this.lerpSteps > 0) {
            this.lerpSteps = 0;
            this.absMoveTo(this.lerpX, this.lerpY, this.lerpZ, (float) this.lerpYRot, (float) this.lerpXRot);
        }
    }
}