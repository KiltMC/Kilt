// TRACKED HASH: 3e4618eedaf96d01e4102a4396d98f0626daefc7
package xyz.bluspring.kilt.injects.client.player;

import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RemotePlayer.class)
public abstract class RemotePlayerInject {
    @Inject(method = "hurt", at = @At("HEAD"))
    private void kilt$runPlayerAttackEvent(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        CommonHooks.onPlayerAttack((RemotePlayer) (Object) this, source, amount);
    }
}