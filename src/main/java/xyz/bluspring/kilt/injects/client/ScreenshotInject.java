// TRACKED HASH: 8a63553e7e8b516c065627b4127c8fb4678ebce1
package xyz.bluspring.kilt.injects.client;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.NativeImage;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.ScreenshotEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;

@Mixin(Screenshot.class)
public abstract class ScreenshotInject {
    private static final AtomicReference<ScreenshotEvent> kilt$target = new AtomicReference<>();

    @Inject(method = "lambda$grab$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/TracingExecutor;execute(Ljava/lang/Runnable;)V"), cancellable = true)
    private static void kilt$runScreenshotEvent(File workDir, String forceName, Consumer<Component> callback, NativeImage image, CallbackInfo ci, @Local(name = "file") File file) {
        var event = ClientHooks.onScreenshot(image, file);

        if (event.isCanceled()) {
            callback.accept(event.getCancelMessage());
            ci.cancel();
            return;
        }

        kilt$target.set(event);
    }

    @Inject(method = "lambda$grab$1", at = @At("TAIL"))
    private static void kilt$resetTarget(CallbackInfo ci) {
        kilt$target.set(null);
    }

    @ModifyReceiver(method = "lambda$grab$2", at = @At(value = "INVOKE", target = "Ljava/io/File;getAbsoluteFile()Ljava/io/File;"))
    private static File kilt$changePathTarget(File originalPath) {
        if (kilt$target.get() != null)
            return kilt$target.get().getScreenshotFile();
        else
            return originalPath;
    }

    @ModifyArg(method = "lambda$grab$1", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/NativeImage;writeToFile(Ljava/io/File;)V"))
    private static File kilt$useForgePath(File file) {
        if (kilt$target.get() != null)
            return kilt$target.get().getScreenshotFile();

        return file;
    }

    @WrapOperation(method = "lambda$grab$1", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"))
    private static <T> void kilt$useForgeEventSuccess(Consumer<T> instance, T t, Operation<Void> original) {
        if (kilt$target.get() != null && kilt$target.get().getResultMessage() != null)
            original.call(instance, kilt$target.get().getResultMessage());
        else
            original.call(instance, t);
    }
}
