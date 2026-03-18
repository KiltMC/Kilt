package xyz.bluspring.kilt.injects.client.renderer;

import java.util.SequencedMap;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.event.RegisterRenderBuffersEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;

@Mixin(RenderBuffers.class)
public abstract class RenderBuffersInject {
    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource;immediate(Lcom/mojang/blaze3d/vertex/ByteBufferBuilder;)Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;"))
    private void kilt$callRegisterRenderBuffers(int bufferCount, CallbackInfo ci, @Local SequencedMap<RenderType, ByteBufferBuilder> renderBuffers) {
        ModLoader.postEvent(new RegisterRenderBuffersEvent(renderBuffers));
    }
}
