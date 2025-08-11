package xyz.bluspring.kilt.injects.client.multiplayer;

import net.minecraft.client.multiplayer.TagCollector;
import net.minecraft.core.RegistryAccess;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TagCollector.class)
public abstract class TagCollectorInject {
    @Inject(method = "updateTags", at = @At("TAIL"))
    private void kilt$callTagsUpdatedEvent(RegistryAccess registryAccess, boolean isMemoryConnection, CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new TagsUpdatedEvent(registryAccess, true, isMemoryConnection));
    }
}
