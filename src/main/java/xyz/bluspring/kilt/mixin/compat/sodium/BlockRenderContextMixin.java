package xyz.bluspring.kilt.mixin.compat.sodium;

import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import net.minecraftforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.sodium.BlockRenderContextInjection;

@Mixin(value = BlockRenderContext.class, remap = false)
public class BlockRenderContextMixin implements BlockRenderContextInjection {
    private ModelData klit$data;

    @Override
    public void kilt$set(ModelData data) {
        klit$data = data;
    }

    @Override
    public ModelData klit$data() {
        return klit$data;
    }
}
