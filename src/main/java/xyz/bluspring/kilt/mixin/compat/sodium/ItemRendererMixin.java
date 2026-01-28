package xyz.bluspring.kilt.mixin.compat.sodium;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadView;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.SodiumRenderStorage;

import java.util.List;

@IfModLoaded("sodium")
@Mixin(value = ItemRenderer.class, priority = 1500)
public class ItemRendererMixin {
    @TargetHandler(mixin = "net.caffeinemc.mods.sodium.mixin.features.render.model.item.ItemRendererMixin", name = "renderBakedItemQuads")
    @WrapOperation(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/immediate/model/BakedModelEncoder;writeQuadVertices(Lnet/caffeinemc/mods/sodium/api/vertex/buffer/VertexBufferWriter;Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/caffeinemc/mods/sodium/client/model/quad/ModelQuadView;IIIZ)V"))
    private void handleItemRenderingColorizer(VertexBufferWriter writer, PoseStack.Pose pose, ModelQuadView quad, int color, int light, int overlay, boolean colorize, Operation<Void> original) {
        SodiumRenderStorage.ITEM_RENDERING = true;
        original.call(writer, pose, quad, color, light, overlay, colorize);
        SodiumRenderStorage.ITEM_RENDERING = false;
    }
}
