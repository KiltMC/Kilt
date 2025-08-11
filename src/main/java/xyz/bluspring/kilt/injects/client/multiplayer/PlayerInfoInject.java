// TRACKED HASH: 18838234833312d3c610b6e9eee97297572aa05e
package xyz.bluspring.kilt.injects.client.multiplayer;

import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInfo.class)
public class PlayerInfoInject {
    @Shadow private GameType gameMode;

    @Inject(method = "setGameMode", at = @At("HEAD"))
    public void kilt$hookGameTypeEvent(GameType pGameMode, CallbackInfo ci) {
        ClientHooks.onClientChangeGameType((PlayerInfo) (Object) this, this.gameMode, pGameMode);
    }
}