package xyz.bluspring.kilt.mixin.compat.sodium;

import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import net.minecraftforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xyz.bluspring.kilt.injections.sodium.BlockRenderContextInjection;

@Mixin(value = BlockRenderContext.class, remap = false)
public class BlockRenderContextMixin implements BlockRenderContextInjection {
    @Unique
    private ModelData kilt$data;

    @Override
    public void kilt$setModelData(ModelData data) {
        kilt$data = data;
    }

    @Override
    public ModelData kilt$getModelData() {
        return kilt$data;
    }
}
