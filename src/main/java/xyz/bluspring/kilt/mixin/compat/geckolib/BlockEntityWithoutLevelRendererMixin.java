package xyz.bluspring.kilt.mixin.compat.geckolib;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;

@Mixin(BlockEntityWithoutLevelRenderer.class)
public class BlockEntityWithoutLevelRendererMixin {
    public BlockEntityWithoutLevelRendererMixin(BlockEntityRenderDispatcher blockEntityRenderDispatcher, EntityModelSet entityModelSet) {}

    @CreateInitializer
    public BlockEntityWithoutLevelRendererMixin() {
        this(null, null);
    }
}
