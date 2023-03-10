package xyz.bluspring.kilt.forgeinjects.server.level;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ServerPlayer.class)
public class ServerPlayerInject {
    @Inject(at = @At("HEAD"), method = "startSleepInBed", cancellable = true)
    public void kilt$checkPlayerSleepEvent(BlockPos blockPos, CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> cir) {
        var ret = ForgeEventFactory.onPlayerSleepInBed((ServerPlayer) (Object) this, Optional.of(blockPos));
        if (ret != null)
            cir.setReturnValue(Either.left(ret));
    }
}
