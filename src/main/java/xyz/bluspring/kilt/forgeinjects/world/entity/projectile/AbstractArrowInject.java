package xyz.bluspring.kilt.forgeinjects.world.entity.projectile;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowInject extends Projectile {
    @Shadow public abstract void setPierceLevel(byte pierceLevel);

    @Unique private final IntOpenHashSet ignoredEntities = new IntOpenHashSet();

    public AbstractArrowInject(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z", ordinal = 0))
    private boolean kilt$clearFireIfFluidExtinguishes(boolean original) {
        var entity = this;

        return original || this.isInFluidType((fluidType, height) -> entity.canFluidExtinguish(fluidType));
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;onHit(Lnet/minecraft/world/phys/HitResult;)V"))
    private void kilt$handleOnHit(AbstractArrow instance, HitResult hitResult, Operation<Void> original, @Local LocalRef<EntityHitResult> entityHitResult, @Share("currentImpulse") LocalBooleanRef currentImpulse) {
        var result = ForgeEventFactory.onProjectileImpactResultNullable(instance, hitResult);
        if (result == null) {
            if (hitResult.getType() != HitResult.Type.ENTITY) {
                entityHitResult.set(null); // This is our best way of getting a break in here, honestly.
                return;
            }

            result = ProjectileImpactEvent.ImpactResult.SKIP_ENTITY;
        }

        switch (result) {
            case SKIP_ENTITY: {
                if (hitResult.getType() != HitResult.Type.ENTITY) {
                    original.call(instance, hitResult);
                    currentImpulse.set(true);
                    break;
                }

                ignoredEntities.add(entityHitResult.get().getEntity().getId());
                entityHitResult.set(null);
                break;
            }

            case STOP_AT_CURRENT_NO_DAMAGE: {
                this.discard();
                entityHitResult.set(null);
                break;
            }

            case STOP_AT_CURRENT: {
                this.setPierceLevel((byte) 0);
            }

            case DEFAULT: {
                original.call(instance, hitResult);
                currentImpulse.set(true);
            }
        }
    }

    @Definition(id = "hasImpulse", field = "Lnet/minecraft/world/entity/projectile/AbstractArrow;hasImpulse:Z")
    @Expression("this.hasImpulse = @(true)")
    @ModifyExpressionValue(method = "tick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$usActualImpulseValue(boolean original, @Share("currentImpulse") LocalBooleanRef currentImpulse) {
        return original && currentImpulse.get();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;getDeltaMovement()Lnet/minecraft/world/phys/Vec3;", ordinal = 1), cancellable = true)
    private void kilt$cancelIfRemoved(CallbackInfo ci) {
        if (this.isRemoved())
            ci.cancel();
    }

    @ModifyReturnValue(method = "canHitEntity", at = @At("RETURN"))
    private boolean kilt$checkEntityIgnored(boolean original, @Local(argsOnly = true) Entity entity) {
        return original && !this.ignoredEntities.contains(entity.getId());
    }
}
