package xyz.bluspring.kilt.compat.create.mixin.flywheel;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;

@Mixin(targets = "dev.engine_room.flywheel.lib.model.baked.FabricMeshEmitterManager")
public interface FabricMeshEmitterManagerAccessor {
    @Invoker("prepareForModel")
    BakedModel kilt$callPrepareForModel(BakedModel model, RenderType defaultLayer, boolean useAo, boolean defaultAo);
}
