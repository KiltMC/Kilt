package xyz.bluspring.kilt.injects.client.resources;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.util.List;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import net.neoforged.neoforge.client.resources.NeoForgeSplashHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.resources.SplashManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

@Mixin(SplashManager.class)
public abstract class SplashManagerInject {
    @WrapOperation(method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Ljava/util/List;", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/ResourceManager;openAsReader(Lnet/minecraft/resources/ResourceLocation;)Ljava/io/BufferedReader;"))
    private BufferedReader kilt$tryLoadSplashesFromNeo(ResourceManager instance, ResourceLocation resourceLocation, Operation<BufferedReader> original, @Cancellable CallbackInfoReturnable<List<String>> cir) throws FileNotFoundException {
        if (instance.getResourceOrThrow(resourceLocation).sourcePackId().equals("vanilla")) {
            cir.setReturnValue(NeoForgeSplashHooks.loadSplashes(instance));
            return null;
        }

        return original.call(instance, resourceLocation);
    }
}
