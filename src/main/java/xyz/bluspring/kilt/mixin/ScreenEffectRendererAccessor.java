package xyz.bluspring.kilt.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ScreenEffectRenderer.class)
public interface ScreenEffectRendererAccessor {
    @Invoker
    static void callRenderWater(Minecraft minecraft, PoseStack poseStack) {
        throw new UnsupportedOperationException();
    }

    @Accessor("UNDERWATER_LOCATION")
    static ResourceLocation getUnderwaterLocation() {
        throw new UnsupportedOperationException();
    }
}
