package xyz.bluspring.kilt.injects.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.client.renderer.ItemBlockRenderTypesInjection;
import xyz.bluspring.kilt.injections.world.phys.AABBInjection;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.PistonHeadRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

@Mixin(PistonHeadRenderer.class)
public abstract class PistonHeadRendererInject implements BlockEntityRenderer<PistonMovingBlockEntity> {
    @Shadow public BlockRenderDispatcher blockRenderer;

    @Inject(method = "renderBlock", at = @At("HEAD"), cancellable = true)
    private void kilt$callForgeRenderMovedPistonBlocks(BlockPos pos, BlockState state, PoseStack poseStack, MultiBufferSource bufferSource, Level level, boolean extended, int packedOverlay, CallbackInfo ci) {
        var blockRenderer = this.blockRenderer == null ? Minecraft.getInstance().getBlockRenderer() : this.blockRenderer;
        if (this.blockRenderer == null)
            this.blockRenderer = blockRenderer;

        var model = blockRenderer.getBlockModel(state);

        // Kilt: Defer to Forge if needed, but this may cause a small performance penalty.
        if (!model.getRenderTypes(state, RandomSource.create(state.getSeed(pos)), ModelData.EMPTY).equals(ItemBlockRenderTypesInjection.getRenderLayers(state))) {
            ClientHooks.renderPistonMovedBlocks(pos, state, poseStack, bufferSource, level, extended, packedOverlay, blockRenderer);
            ci.cancel();
        }
    }

    @Override
    public AABB getRenderBoundingBox(PistonMovingBlockEntity blockEntity) {
        return AABBInjection.INFINITE;
    }
}
