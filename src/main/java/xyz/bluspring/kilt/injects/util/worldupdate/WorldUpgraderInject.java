package xyz.bluspring.kilt.injects.util.worldupdate;

import net.neoforged.neoforge.common.IOUtilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.util.worldupdate.WorldUpgrader;

@Mixin(WorldUpgrader.class)
public abstract class WorldUpgraderInject {
    @Inject(method = "work", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/DimensionDataStorage;save()V", shift = At.Shift.AFTER))
    private void kilt$waitUntilIOWorkerComplete(CallbackInfo ci) {
        IOUtilities.waitUntilIOWorkerComplete();
    }
}
