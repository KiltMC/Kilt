package xyz.bluspring.kilt.injects.world.entity.ai.navigation;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WallClimberNavigation.class)
public abstract class WallClimberNavigationInject {
    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;getBbWidth()F"))
    private float kilt$fixMC94054(Mob instance, Operation<Float> original) {
        return Math.max(original.call(instance), 1f);
    }
}
