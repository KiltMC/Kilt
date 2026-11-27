package xyz.bluspring.kilt.injects.blaze3d.vertex;

import java.util.List;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.blaze3d.vertex.VertexFormatInjection;

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
}
