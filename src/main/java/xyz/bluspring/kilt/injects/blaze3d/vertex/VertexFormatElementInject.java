package xyz.bluspring.kilt.injects.blaze3d.vertex;

import com.mojang.blaze3d.vertex.VertexFormatElement;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;

@Mixin(VertexFormatElement.class)
public abstract class VertexFormatElementInject {
    @Shadow @Final public static @Nullable VertexFormatElement[] BY_ID;

    @CreateStatic
    private static int findNextId() {
        for (int i = 0; i < BY_ID.length; i++) {
            if (BY_ID[i] == null)
                return i;
        }

        throw new IllegalStateException("VertexFormatElement count limit exceeded");
    }
}
