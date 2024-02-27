// TRACKED HASH: 8a63553e7e8b516c065627b4127c8fb4678ebce1
package xyz.bluspring.kilt.forgeinjects.client;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ScreenshotEvent;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Mixin(Screenshot.class)
public class ScreenshotInject {
    private static final AtomicReference<ScreenshotEvent> kilt$target = new AtomicReference<>();

    @Inject(method = "_grab", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/ExecutorService;execute(Ljava/lang/Runnable;)V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private static void kilt$runScreenshotEvent(File gameDirectory, String screenshotName, RenderTarget buffer, Consumer<Component> messageConsumer, CallbackInfo ci, NativeImage nativeImage, File file, File file2) {
        var event = new ScreenshotEvent(nativeImage, file2);
        MinecraftForge.EVENT_BUS.post(event);

        if (event.isCanceled()) {
            messageConsumer.accept(event.getCancelMessage());
            ci.cancel();
            return;
        }

        kilt$target.set(event);
    }

    @Inject(method = "method_1661", at = @At("TAIL"))
    private static void kilt$resetTarget(NativeImage nativeImage, File file, Consumer consumer, CallbackInfo ci) {
        kilt$target.set(null);
    }

    @ModifyReceiver(method = "method_1664", at = @At(value = "INVOKE", target = "Ljava/io/File;getAbsolutePath()Ljava/lang/String;"))
    private static File kilt$changePathTarget(File originalPath) {
        if (kilt$target.get() != null)
            return kilt$target.get().getScreenshotFile();
        else
            return originalPath;
    }

    @ModifyArg(method = "method_1661", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/NativeImage;writeToFile(Ljava/io/File;)V"))
    private static File kilt$useForgePath(File file) {
        if (kilt$target.get() != null)
            return kilt$target.get().getScreenshotFile();

        return file;
    }

    @Redirect(method = "method_1661", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"))
    private static void kilt$useForgeEventSuccess(Consumer<Component> instance, Object component) {
        if (kilt$target.get() != null)
            instance.accept(kilt$target.get().getResultMessage());
        else
            instance.accept((Component) component);
    }
}