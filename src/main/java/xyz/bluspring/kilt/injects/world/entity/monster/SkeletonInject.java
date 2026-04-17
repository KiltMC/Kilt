package xyz.bluspring.kilt.injects.world.entity.monster;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.level.Level;

@Mixin(Skeleton.class)
public abstract class SkeletonInject extends AbstractSkeleton {
    @Shadow
    private int conversionTime;

    protected SkeletonInject(EntityType<? extends AbstractSkeleton> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "doFreezeConversion", at = @At("HEAD"), cancellable = true)
    private void kilt$tryConvertEntity(CallbackInfo ci) {
        if (!EventHooks.canLivingConvert(this, EntityType.STRAY, timer -> this.conversionTime = timer))
            ci.cancel();
    }

    @WrapOperation(method = "doFreezeConversion", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Skeleton;convertTo(Lnet/minecraft/world/entity/EntityType;Z)Lnet/minecraft/world/entity/Mob;"))
    private Mob kilt$callLivingConvertEvent(Skeleton instance, EntityType entityType, boolean b, Operation<Mob> original) {
        Mob stray = original.call(instance, entityType, b);

        if (stray != null)
            EventHooks.onLivingConvert(this, stray);

        return stray;
    }
}
