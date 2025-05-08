package xyz.bluspring.kilt.forgeinjects.world.entity.ai.village;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.village.VillageSiegeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillageSiege.class)
public abstract class VillageSiegeInject {
    @WrapOperation(method = "tryToSetupSiege", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/village/VillageSiege;findRandomSpawnPos(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 kilt$callVillageSiegeEvent(VillageSiege instance, ServerLevel level, BlockPos pos, Operation<Vec3> original, @Cancellable CallbackInfoReturnable<Boolean> cir, @Local Player player) {
        var siegeLocation = original.call(instance, level, pos);

        if (siegeLocation != null && MinecraftForge.EVENT_BUS.post(new VillageSiegeEvent((VillageSiege) (Object) this, level, player, siegeLocation))) {
            cir.setReturnValue(false);
            return null;
        }

        return siegeLocation;
    }
}
