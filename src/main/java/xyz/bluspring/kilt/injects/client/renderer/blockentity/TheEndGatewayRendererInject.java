package xyz.bluspring.kilt.injects.client.renderer.blockentity;

import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.world.phys.AABBInjection;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.TheEndGatewayRenderer;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.phys.AABB;

@Mixin(TheEndGatewayRenderer.class)
public abstract class TheEndGatewayRendererInject extends TheEndPortalRenderer<TheEndGatewayBlockEntity> {
    public TheEndGatewayRendererInject(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public AABB getRenderBoundingBox(TheEndGatewayBlockEntity blockEntity) {
        return blockEntity.isSpawning() || blockEntity.isCoolingDown() ? AABBInjection.INFINITE : super.getRenderBoundingBox(blockEntity);
    }
}
