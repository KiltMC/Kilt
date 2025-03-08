package xyz.bluspring.kilt.mixin.compat.geckolib;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;
import software.bernie.geckolib3.util.EModelRenderCycle;
import software.bernie.geckolib3.util.IRenderCycle;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.helpers.mixin.Extends;

@IfModLoaded("geckolib3")
@Mixin(GeoItemRenderer.class)
@Extends(BlockEntityWithoutLevelRenderer.class)
public abstract class GeoItemRendererMixin<T extends Item & IAnimatable> {
    @Shadow public abstract void render(T animatable, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, ItemStack stack);

    @Shadow private IRenderCycle currentModelRenderCycle;

    @Shadow protected AnimatedGeoModel<T> modelProvider;

    private void kilt$mixin$superCall(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        throw new IllegalStateException();
    }

    @CreateInitializer
    public void kilt$mixin$initializer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet, AnimatedGeoModel<T> modelProvider) {
        kilt$mixin$superCall(dispatcher, modelSet);
        this.currentModelRenderCycle = EModelRenderCycle.INITIAL;
        this.modelProvider = modelProvider;
    }

    public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (transformType == ItemTransforms.TransformType.GUI) {
            poseStack.pushPose();
            MultiBufferSource.BufferSource defaultBufferSource = bufferSource instanceof MultiBufferSource.BufferSource bufferSource2 ? bufferSource2 : Minecraft.getInstance().renderBuffers().bufferSource();
            Lighting.setupForFlatItems();
            this.render((T) stack.getItem(), poseStack, bufferSource, packedLight, stack);
            defaultBufferSource.endBatch();
            RenderSystem.enableDepthTest();
            Lighting.setupFor3DItems();
            poseStack.popPose();
        } else {
            this.render((T) stack.getItem(), poseStack, bufferSource, packedLight, stack);
        }
    }
}
