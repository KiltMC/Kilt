package xyz.bluspring.kilt.forgeinjects.world.entity.vehicle;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IMinecartCollisionHandler;
import net.minecraftforge.common.extensions.IForgeAbstractMinecart;
import net.minecraftforge.common.extensions.IForgeBaseRailBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.entity.AbstractMinecartInjection;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartInject extends Entity implements AbstractMinecartInjection, IForgeAbstractMinecart {
    public AbstractMinecartInject(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow protected abstract double getMaxSpeed();

    private static IMinecartCollisionHandler COLLISIONS = null;

    public IMinecartCollisionHandler getCollisionHandler() {
        return COLLISIONS;
    }

    @CreateStatic
    private static void registerCollisionHandler(IMinecartCollisionHandler handler) {
        COLLISIONS = handler;
    }

    @Inject(method = "push", at = @At("HEAD"), cancellable = true)
    public void kilt$handleForgeCollision(Entity entity, CallbackInfo ci) {
        if (getCollisionHandler() != null) {
            getCollisionHandler().onEntityCollision((AbstractMinecart) (Object) this, entity);
            ci.cancel();
        }
    }

    // TODO: onMinecartPass, need to figure out how to add a mixin there

    @Inject(method = "moveAlongTrack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/AbstractMinecart;getDeltaMovement()Lnet/minecraft/world/phys/Vec3;", ordinal = 9), cancellable = true)
    public void kilt$checkIfShouldDoRailFunctions(BlockPos pos, BlockState state, CallbackInfo ci) {
        if (!shouldDoRailFunctions())
            ci.cancel();
    }

    private boolean canUseRail = true;

    @Override
    public boolean canUseRail() {
        return canUseRail;
    }

    @Override
    public void setCanUseRail(boolean use) {
        canUseRail = use;
    }

    private float currentSpeedCapOnRail = getMaxCartSpeedOnRail();

    @Override
    public float getCurrentCartSpeedCapOnRail() {
        return currentSpeedCapOnRail;
    }

    @Override
    public void setCurrentCartSpeedCapOnRail(float value) {
        currentSpeedCapOnRail = value;
    }

    private Float maxSpeedAirLateral = null;

    @Override
    public float getMaxSpeedAirLateral() {
        return maxSpeedAirLateral == null ? (float) this.getMaxSpeed() : maxSpeedAirLateral;
    }

    @Override
    public void setMaxSpeedAirLateral(float value) {
        maxSpeedAirLateral = value;
    }

    private float maxSpeedAirVertical = DEFAULT_MAX_SPEED_AIR_VERTICAL;

    @Override
    public float getMaxSpeedAirVertical() {
        return maxSpeedAirVertical;
    }

    @Override
    public void setMaxSpeedAirVertical(float value) {
        maxSpeedAirVertical = value;
    }

    @Override
    public double getMaxSpeedWithRail() {
        if (!canUseRail())
            return getMaxSpeed();

        var pos = getCurrentRailPosition();
        var state = this.level.getBlockState(pos);

        if (!state.is(BlockTags.RAILS))
            return getMaxSpeed();

        var railMaxSpeed = ((IForgeBaseRailBlock) state.getBlock()).getRailMaxSpeed(state, this.level, pos, (AbstractMinecart) (Object) this);

        return Math.min(railMaxSpeed, getCurrentCartSpeedCapOnRail());
    }

    private double dragAir = DEFAULT_AIR_DRAG;

    @Override
    public double getDragAir() {
        return dragAir;
    }

    @Override
    public void setDragAir(double value) {
        dragAir = value;
    }
}
