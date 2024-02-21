package xyz.bluspring.kilt.injections.blaze3d.vertex;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public interface VertexFormatInjection {
    ImmutableMap<String, VertexFormatElement> getElementMapping();
    int getOffset(int index);
    boolean hasPosition();
    boolean hasNormal();
    boolean hasColor();
    boolean hasUV(int which);
}
