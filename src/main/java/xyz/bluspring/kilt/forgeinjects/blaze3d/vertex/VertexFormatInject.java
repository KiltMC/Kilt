package xyz.bluspring.kilt.forgeinjects.blaze3d.vertex;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import it.unimi.dsi.fastutil.ints.IntList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.blaze3d.vertex.VertexFormatInjection;

@Mixin(VertexFormat.class)
public class VertexFormatInject implements VertexFormatInjection {
    @Shadow @Final private ImmutableMap<String, VertexFormatElement> elementMapping;

    @Shadow @Final private IntList offsets;

    @Shadow @Final private ImmutableList<VertexFormatElement> elements;

    @Override
    public ImmutableMap<String, VertexFormatElement> getElementMapping() {
        return this.elementMapping;
    }

    @Override
    public int getOffset(int index) {
        return this.offsets.getInt(index);
    }

    @Override
    public boolean hasPosition() {
        return this.elements.stream().anyMatch(e -> e.getUsage() == VertexFormatElement.Usage.POSITION);
    }

    @Override
    public boolean hasNormal() {
        return this.elements.stream().anyMatch(e -> e.getUsage() == VertexFormatElement.Usage.NORMAL);
    }

    @Override
    public boolean hasColor() {
        return this.elements.stream().anyMatch(e -> e.getUsage() == VertexFormatElement.Usage.COLOR);
    }

    @Override
    public boolean hasUV(int which) {
        return this.elements.stream().anyMatch(e -> e.getUsage() == VertexFormatElement.Usage.UV && e.getIndex() == which);
    }
}
