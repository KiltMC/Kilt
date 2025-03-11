package xyz.bluspring.kilt.mixin.compat.sodium;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.BakedChunkModelBuilder;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xyz.bluspring.kilt.compat.sodium.ChunkModelRenderTypeHolder;

@IfModLoaded("sodium")
@Mixin(BakedChunkModelBuilder.class)
public abstract class BakedChunkModelBuilderMixin implements ChunkModelRenderTypeHolder {
    @Unique private RenderType kilt$renderType;

    @Override
    public RenderType kilt$getRenderType() {
        return this.kilt$renderType;
    }

    @Override
    public void kilt$setRenderType(RenderType renderType) {
        this.kilt$renderType = renderType;
    }
}
