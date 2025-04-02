// TRACKED HASH: 8ce7cfcc1608a79d687631411c28c60d1064aad3
package xyz.bluspring.kilt.forgeinjects.server.level;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayer.class)
public class ServerPlayerInject {
    // Handled by Fabric API
    /*@Inject(at = @At("HEAD"), method = "startSleepInBed", cancellable = true)
    public void kilt$checkPlayerSleepEvent(BlockPos blockPos, CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> cir) {
        var ret = ForgeEventFactory.onPlayerSleepInBed((ServerPlayer) (Object) this, Optional.of(blockPos));
        if (ret != null)
            cir.setReturnValue(Either.left(ret));
    }*/
}