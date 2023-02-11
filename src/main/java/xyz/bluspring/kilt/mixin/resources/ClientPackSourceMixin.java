package xyz.bluspring.kilt.mixin.resources;

import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.resources.KiltResourceLoader;

import java.util.function.Consumer;

@Mixin(ClientPackSource.class)
public class ClientPackSourceMixin {
    @Inject(method = "loadPacks", at = @At("RETURN"))
    public void kilt$loadKiltResources(Consumer<Pack> consumer, Pack.PackConstructor packConstructor, CallbackInfo ci) {
        KiltResourceLoader.loadResources(PackType.CLIENT_RESOURCES, consumer, packConstructor);
    }
}
