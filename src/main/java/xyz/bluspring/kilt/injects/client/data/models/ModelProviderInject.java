package xyz.bluspring.kilt.injects.client.data.models;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.client.data.models.ModelProviderInjection;

import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.data.PackOutput;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;

@Mixin(ModelProvider.class)
public abstract class ModelProviderInject implements ModelProviderInjection {
    @Unique private String modId = "minecraft";

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$initModId(PackOutput output, CallbackInfo ci) {
        if (output instanceof FabricPackOutput o) {
            this.modId = o.getModId();
        }
    }

    @Override
    public String kilt$getModId() {
        return this.modId;
    }

    @Override
    public void kilt$setModId(String modId) {
        this.modId = modId;
    }
}
