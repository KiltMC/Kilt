package xyz.bluspring.kilt.forgeinjects.client.multiplayer;

import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.level.GameType;
import net.minecraftforge.client.event.ClientPlayerChangeGameTypeEvent;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInfo.class)
public class PlayerInfoInject {
    @Shadow private GameType gameMode;

    @Inject(method = "setGameMode", at = @At("HEAD"))
    public void kilt$hookGameTypeEvent(GameType gameType, CallbackInfo ci) {
        if (this.gameMode != gameType) {
            var event = new ClientPlayerChangeGameTypeEvent((PlayerInfo) (Object) this, this.gameMode, gameType);
            MinecraftForge.EVENT_BUS.post(event);
        }
    }
}
