package xyz.bluspring.kilt.forgeinjects.server.packs.resources;

import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(MultiPackResourceManager.class)
public abstract class MultiPackResourceManagerInject {
    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/util/HashMap;<init>()V", shift = At.Shift.AFTER))
    private void kilt$initPacks(PackType type, List<PackResources> packs, CallbackInfo ci) {
        packs.forEach(pack -> pack.init(type));
    }
}
