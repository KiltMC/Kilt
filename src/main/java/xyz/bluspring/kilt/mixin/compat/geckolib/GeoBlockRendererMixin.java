package xyz.bluspring.kilt.mixin.compat.geckolib;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;

@Mixin(GeoBlockRenderer.class)
public abstract class GeoBlockRendererMixin<T extends BlockEntity & IAnimatable> {
    public GeoBlockRendererMixin(AnimatedGeoModel<T> modelProvider) {}

    @CreateInitializer
    public GeoBlockRendererMixin(BlockEntityRendererProvider.Context renderProvider, AnimatedGeoModel<T> modelProvider) {
        this(modelProvider);
    }
}
