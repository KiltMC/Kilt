package xyz.bluspring.kilt.injections.blaze3d.vertex;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import xyz.bluspring.kilt.util.KiltHelper;

public interface VertexFormatInjection {
    default ImmutableMap<String, VertexFormatElement> getElementMapping() {
        throw KiltHelper.createMixinException(VertexFormatInjection.class, "getElementMapping");
    }
}
