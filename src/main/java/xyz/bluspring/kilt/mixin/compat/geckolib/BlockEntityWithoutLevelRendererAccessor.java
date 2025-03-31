package xyz.bluspring.kilt.mixin.compat.geckolib;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockEntityWithoutLevelRenderer.class)
public interface BlockEntityWithoutLevelRendererAccessor {
    @Accessor
    void setBlockEntityRenderDispatcher(BlockEntityRenderDispatcher dispatcher);

    @Accessor
    void setEntityModelSet(EntityModelSet modelSet);
}
