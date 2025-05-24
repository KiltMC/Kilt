package xyz.bluspring.kilt.forgeinjects.world.entity.animal.horse;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.animal.horse.SkeletonTrapGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkeletonTrapGoal.class)
public abstract class SkeletonTrapGoalInject {
    @Shadow @Final private SkeletonHorse horse;

    // MC-206338 / MinecraftForge#7509
    @WrapMethod(method = "tick")
    private void kilt$avoidSkeletonHorseGoalCrash(Operation<Void> original) {
        var level = (ServerLevel) this.horse.level();
        level.getServer().tell(new TickTask(level.getServer().getTickCount(), original::call));
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void kilt$cancelIfDead(CallbackInfo ci) {
        if (!this.horse.isAlive())
            ci.cancel();
    }
}
