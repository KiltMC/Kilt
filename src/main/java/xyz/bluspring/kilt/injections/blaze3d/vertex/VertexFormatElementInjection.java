package xyz.bluspring.kilt.injections.blaze3d.vertex;

import com.mojang.blaze3d.vertex.VertexFormatElement;
import xyz.bluspring.kilt.helpers.FabricImplemented;
import xyz.bluspring.kilt.util.KiltHelper;

@FabricImplemented(VertexFormatElement.class)
public interface VertexFormatElementInjection {
    static int findNextId() {
        throw KiltHelper.createMixinException(VertexFormatElementInjection.class, "findNextId");
    }
}
