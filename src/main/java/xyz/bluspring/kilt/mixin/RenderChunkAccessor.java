package xyz.bluspring.kilt.mixin;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChunkRenderDispatcher.RenderChunk.class)
public interface RenderChunkAccessor {
    @Invoker
    void callBeginLayer(BufferBuilder builder);
}
