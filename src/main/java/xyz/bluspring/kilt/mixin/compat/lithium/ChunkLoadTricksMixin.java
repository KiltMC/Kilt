package xyz.bluspring.kilt.mixin.compat.lithium;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.level.chunk.ChunkAccess;

@Pseudo
@IfModLoaded("lithium")
@Mixin(targets = "net.caffeinemc.mods.lithium.common.world.ChunkLoadTricks")
public abstract class ChunkLoadTricksMixin {
    @Inject(method = "tryRetrieveCurrentlyLoading", at = @At("HEAD"), cancellable = true)
    private static void kilt$tryReturnOurCurrentlyLoading(ChunkHolder holder, CallbackInfoReturnable<ChunkAccess> cir) {
        var loading = holder.kilt$getCurrentlyLoading();
        if (loading != null)
            cir.setReturnValue(loading);
    }
}
