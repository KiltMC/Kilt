package xyz.bluspring.kilt.injects.world.entity.projectile;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;
import xyz.bluspring.kilt.workarounds.KiltNoOpImpls;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowInject extends Projectile {
    public AbstractArrowInject(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Definition(id = "blockState", local = @Local(type = BlockState.class))
    @Definition(id = "is", method = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z")
    @Definition(id = "POWDER_SNOW", field = "Lnet/minecraft/world/level/block/Blocks;POWDER_SNOW:Lnet/minecraft/world/level/block/Block;")
    @Expression("blockState.is(POWDER_SNOW)")
    @ModifyExpressionValue(method = "tick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$clearFireIfFluidExtinguishes(boolean original) {
        var entity = this;
        return original || this.isInFluidType((fluidType, height) -> entity.canFluidExtinguish(fluidType));
    }

    @Definition(id = "hitResult", local = @Local(type = HitResult.class))
    @Expression("hitResult != null")
    @ModifyExpressionValue(method = "tick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkIsNotMiss(boolean original, @Local HitResult hitResult) {
        return original && hitResult.getType() != HitResult.Type.MISS;
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;hitTargetOrDeflectSelf(Lnet/minecraft/world/phys/HitResult;)Lnet/minecraft/world/entity/projectile/ProjectileDeflection;"))
    private ProjectileDeflection kilt$checkShouldHandleProjectileImpact(AbstractArrow instance, HitResult hitResult, Operation<ProjectileDeflection> original) {
        if (EventHooks.onProjectileImpact(instance, hitResult)) {
            return KiltNoOpImpls.NO_OP_PROJECTILE_DEFLECTION;
        }

        return original.call(instance, hitResult);
    }

    @Definition(id = "hasImpulse", field = "Lnet/minecraft/world/entity/projectile/AbstractArrow;hasImpulse:Z")
    @Expression("this.hasImpulse = true")
    @WrapWithCondition(method = "tick", at = @At("MIXINEXTRAS:EXPRESSION"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;hitTargetOrDeflectSelf(Lnet/minecraft/world/phys/HitResult;)Lnet/minecraft/world/entity/projectile/ProjectileDeflection;")))
    private boolean kilt$avoidSettingImpulseIfCancelled(AbstractArrow instance, boolean value, @Local ProjectileDeflection deflection) {
        return deflection != KiltNoOpImpls.NO_OP_PROJECTILE_DEFLECTION;
    }
}
