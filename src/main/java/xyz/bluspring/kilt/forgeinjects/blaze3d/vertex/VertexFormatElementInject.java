// TRACKED HASH: a4d34a5fd04e3ce764b01db9dddb850217a82d89
package xyz.bluspring.kilt.forgeinjects.blaze3d.vertex;

import com.mojang.blaze3d.vertex.VertexFormatElement;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.blaze3d.vertex.VertexFormatElementInjection;

@Mixin(VertexFormatElement.class)
public class VertexFormatElementInject implements VertexFormatElementInjection {
    @Shadow @Final private int count;

    @Override
    public int getElementCount() {
        return this.count;
    }
}