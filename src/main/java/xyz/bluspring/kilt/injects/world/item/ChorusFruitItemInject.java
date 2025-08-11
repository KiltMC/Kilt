package xyz.bluspring.kilt.injects.world.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ChorusFruitItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChorusFruitItem.class)
public abstract class ChorusFruitItemInject {
    @WrapOperation(method = "finishUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;randomTeleport(DDDZ)Z"))
    private boolean kilt$checkTeleportEvent(LivingEntity instance, double x, double y, double z, boolean broadcastTeleport, Operation<Boolean> original, @Cancellable CallbackInfoReturnable<ItemStack> cir, @Local(ordinal = 1) ItemStack stack, @Local(argsOnly = true) Level level, @Local Vec3 vec3) {
        var event = EventHooks.onChorusFruitTeleport(instance, x, y, z);

        if (event.isCanceled()) {
            level.gameEvent(GameEvent.TELEPORT, vec3, GameEvent.Context.of(instance));
            cir.setReturnValue(stack);
            return false;
        }

        return original.call(instance, event.getTargetX(), event.getTargetY(), event.getTargetZ(), broadcastTeleport);
    }
}
