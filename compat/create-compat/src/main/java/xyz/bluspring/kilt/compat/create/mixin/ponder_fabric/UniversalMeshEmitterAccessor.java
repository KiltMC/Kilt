package xyz.bluspring.kilt.compat.create.mixin.ponder_fabric;

import net.createmod.catnip.client.render.model.ShadeSeparatedBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.renderer.RenderType;

@Mixin(targets = "net.createmod.catnip.impl.client.render.model.UniversalMeshEmitter")
public interface UniversalMeshEmitterAccessor {
    @Invoker("prepare")
    void kilt$invokePrepare(ShadeSeparatedBufferSource bufferSource, RenderType layer);
}
