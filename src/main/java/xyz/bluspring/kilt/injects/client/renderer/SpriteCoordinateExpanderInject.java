package xyz.bluspring.kilt.injects.client.renderer;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SpriteCoordinateExpander;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SpriteCoordinateExpander.class)
public abstract class SpriteCoordinateExpanderInject {
    @ModifyReturnValue(method = {"addVertex(FFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;", "setColor", "setUv", "setUv1", "setUv2", "setNormal"}, at = @At("RETURN"))
    private VertexConsumer kilt$chainUseVertexConsumer(VertexConsumer original) {
        return (SpriteCoordinateExpander) (Object) this;
    }
}
