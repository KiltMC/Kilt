package xyz.bluspring.kilt.mixin.workarounds.early_screen_delay;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.realmsclient.client.RealmsClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.main.GameConfig;
import net.minecraft.server.packs.resources.ReloadInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Use lower priority for screen delay. This gives priority to Fabric mods,
// allowing them to set their menu screen without interference from the Forge event.
@Mixin(value = Minecraft.class, priority = 500)
public class MinecraftMixin {

    @Unique
    private Runnable kilt$setScreen;

    @WrapOperation(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"
            )
    )
    public void kilt$delaySettingInitialBanScreen(Minecraft instance, Screen guiScreen, Operation<Void> original) {
        kilt$setScreen = () -> original.call(instance, guiScreen);
    }

    @WrapOperation(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;setInitialScreen(Lcom/mojang/realmsclient/client/RealmsClient;Lnet/minecraft/server/packs/resources/ReloadInstance;Lnet/minecraft/client/main/GameConfig$QuickPlayData;)V"
            )
    )
    public void kilt$delaySettingInitialScreen(
            Minecraft instance, RealmsClient realmsClient, ReloadInstance reloadInstance,
            GameConfig.QuickPlayData quickPlayData, Operation<Void> original
    ) {
        kilt$setScreen = () -> original.call(instance, realmsClient, reloadInstance, quickPlayData);
    }

    @Inject(
            method = "method_29338",
            at = @At(
                    value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;onGameLoadFinished()V"
            ),
            order = 1001 // Needs to be after forge load.
    )
    private void kilt$setScreenAfterLoading(CallbackInfo ci) {
        if (kilt$setScreen != null) {
            kilt$setScreen.run();
            kilt$setScreen = null;
        }
    }
    
}
