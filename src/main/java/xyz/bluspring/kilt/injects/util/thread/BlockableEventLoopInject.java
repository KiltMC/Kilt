package xyz.bluspring.kilt.injects.util.thread;

import java.util.concurrent.CompletableFuture;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.util.thread.BlockableEventLoop;

@Mixin(BlockableEventLoop.class)
public abstract class BlockableEventLoopInject {
    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    public abstract String name();

    @ModifyReturnValue(method = "submitAsync", at = @At("RETURN"))
    private CompletableFuture<Void> kilt$logSubmitError(CompletableFuture<Void> original) {
        return original.exceptionallyCompose(ex -> {
            LOGGER.error(LogUtils.FATAL_MARKER, "Error executing task on {}", this.name(), ex);
            return CompletableFuture.failedStage(ex);
        });
    }
}
