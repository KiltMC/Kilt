// TRACKED HASH: a603a767feff06f5cab970bb1912dd85f074516c
package xyz.bluspring.kilt.injects.blaze3d.vertex;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.blaze3d.vertex.VertexFormatInjection;

import java.util.List;

@Mixin(VertexFormat.class)
public abstract class VertexFormatInject implements VertexFormatInjection {
    @Shadow @Final private List<VertexFormatElement> elements;
    @Shadow @Final private List<String> names;

    @Override
    public ImmutableMap<String, VertexFormatElement> getElementMapping() {
        ImmutableMap.Builder<String, VertexFormatElement> builder = ImmutableMap.builder();
        for (int i = 0; i < elements.size(); i++) {
            builder.put(names.get(i), elements.get(i));
        }

        return builder.build();
    }

    @Override
    public boolean hasPosition() {
        return this.elements.stream().anyMatch(e -> e.usage() == VertexFormatElement.Usage.POSITION);
    }

    @Override
    public boolean hasNormal() {
        return this.elements.stream().anyMatch(e -> e.usage() == VertexFormatElement.Usage.NORMAL);
    }

    @Override
    public boolean hasColor() {
        return this.elements.stream().anyMatch(e -> e.usage() == VertexFormatElement.Usage.COLOR);
    }

    @Override
    public boolean hasUV(int which) {
        return this.elements.stream().anyMatch(e -> e.usage() == VertexFormatElement.Usage.UV && e.index() == which);
    }
}