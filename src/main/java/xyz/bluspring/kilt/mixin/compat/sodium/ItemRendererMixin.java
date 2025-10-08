package xyz.bluspring.kilt.mixin.compat.sodium;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import me.jellysquid.mods.sodium.client.model.quad.ModelQuadView;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
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
    @TargetHandler(mixin = "me.jellysquid.mods.sodium.mixin.features.render.model.item.ItemRendererMixin", name = "renderBakedItemQuads")
    @WrapOperation(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/immediate/model/BakedModelEncoder;writeQuadVertices(Lnet/caffeinemc/mods/sodium/api/vertex/buffer/VertexBufferWriter;Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lme/jellysquid/mods/sodium/client/model/quad/ModelQuadView;III)V"))
    private void handleItemRenderingColorizer(VertexBufferWriter writer, PoseStack.Pose matrices, ModelQuadView quad, int color, int light, int overlay, Operation<Void> original) {
        SodiumRenderStorage.ITEM_RENDERING = true;
        original.call(writer, matrices, quad, color, light, overlay);
        SodiumRenderStorage.ITEM_RENDERING = false;
    }
}
