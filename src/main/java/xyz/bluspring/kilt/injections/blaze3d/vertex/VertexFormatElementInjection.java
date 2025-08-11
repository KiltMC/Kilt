package xyz.bluspring.kilt.injections.blaze3d.vertex;

import com.mojang.blaze3d.vertex.VertexFormatElement;

public interface VertexFormatElementInjection {
    static int findNextId() {
        for (int i = 0; i < VertexFormatElement.BY_ID.length; i++) {
            if (VertexFormatElement.BY_ID[i] == null)
                return i;
        }

        throw new IllegalStateException("VertexFormatElement count limit exceeded");
    }
}
