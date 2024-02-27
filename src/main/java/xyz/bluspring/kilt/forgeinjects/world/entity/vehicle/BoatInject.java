// TRACKED HASH: c1c579e966fc78d57072df71b93acdf023e75c9e
package xyz.bluspring.kilt.forgeinjects.world.entity.vehicle;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.extensions.IForgeBlockState;
import net.minecraftforge.common.extensions.IForgeBoat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

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

    @Redirect(method = "getWaterLevelAbove", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    public boolean kilt$checkIfBoatingInFluid(FluidState instance, TagKey<Fluid> tag) {
        return this.canBoatInFluid(instance);
    }

    @Redirect(method = "getGroundFriction", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;getFriction()F"))
    public float kilt$useForgeFriction(Block instance, @Local BlockState state, @Local BlockPos.MutableBlockPos mutableBlockPos) {
        return ((IForgeBlockState) state).getFriction(this.level(), mutableBlockPos, (Boat) (Object) this);
    }

    @Redirect(method = "checkInWater", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    public boolean kilt$checkIfBoatingInWater(FluidState instance, TagKey<Fluid> tag) {
        return this.canBoatInFluid(instance);
    }

    @Redirect(method = "isUnderwater", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    public boolean kilt$checkIfBoatingUnderwater(FluidState instance, TagKey<Fluid> tag) {
        return this.canBoatInFluid(instance);
    }

    @Redirect(method = "checkFallDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    public boolean kilt$checkIfBoatIsInFluidBeforeState(FluidState instance, TagKey<Fluid> tag) {
        return !this.canBoatInFluid(instance);
    }

    @Redirect(method = "canAddPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/Boat;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z"))
    public boolean kilt$checkIfCanBoatBeforePassenger(Boat instance, TagKey tagKey) {
        return this.canBoatInFluid(this.getEyeInFluidType());
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