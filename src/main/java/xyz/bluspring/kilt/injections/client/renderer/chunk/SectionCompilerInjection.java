package xyz.bluspring.kilt.injections.client.renderer.chunk;

import java.util.List;

import com.mojang.blaze3d.vertex.VertexSorting;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.core.SectionPos;

public interface SectionCompilerInjection {
    default void kilt$setAdditionalRenderers(List<AddSectionGeometryEvent.AdditionalSectionRenderer> renderers) {
        throw KiltHelper.createMixinException(SectionCompilerInjection.class, "kilt$setAdditionalRenderers");
    }

    default SectionCompiler.Results compile(SectionPos pos, RenderChunkRegion region, VertexSorting sorting, SectionBufferBuilderPack bufferBuilderPack, List<AddSectionGeometryEvent.AdditionalSectionRenderer> additionalRenderers) {
        throw KiltHelper.createMixinException(SectionCompilerInjection.class, "compile");
    }
}
