package xyz.bluspring.kilt.forgeinjects.server.gui;

import net.minecraft.server.gui.MinecraftServerGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CountDownLatch;

@Mixin(MinecraftServerGui.class)
public abstract class MinecraftServerGuiInject {
    @Unique private CountDownLatch latch = new CountDownLatch(1);

    @Inject(method = "start", at = @At("TAIL"))
    private void kilt$startCountDownLatch(CallbackInfo ci) {
        this.latch.countDown();
    }

    @Inject(method = "print", at = @At("HEAD"))
    private void kilt$awaitLatch(CallbackInfo ci) {
        try {
            this.latch.await();
        } catch (InterruptedException ignored) {}
    }
}
