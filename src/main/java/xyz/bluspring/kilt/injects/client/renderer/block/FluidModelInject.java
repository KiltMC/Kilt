package xyz.bluspring.kilt.injects.client.renderer.block;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.neoforged.neoforge.client.fluid.CustomFluidRenderer;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.neoforged.neoforge.client.fluid.FluidTintSources;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.ClientRecordMixinRefMaps;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.client.renderer.block.FluidModelInjection;

import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.sprite.Material;

@Mixin(FluidModel.class)
public abstract class FluidModelInject implements FluidModelInjection {
    @Shadow public abstract @Nullable BlockTintSource tintSource();

    public FluidModelInject(ChunkSectionLayer layer, Material.Baked stillMaterial, Material.Baked flowingMaterial, Material.@Nullable Baked overlayMaterial, @Nullable BlockTintSource tintSource) {
    }

    @CreateInitializer
    public FluidModelInject(ChunkSectionLayer layer, Material.Baked stillMaterial, Material.Baked flowingMaterial, Material.@Nullable Baked overlayMaterial, @Nullable FluidTintSource fluidTintSource) {
        this(layer, stillMaterial, flowingMaterial, overlayMaterial, (BlockTintSource) fluidTintSource);
        ClientRecordMixinRefMaps.EXTENDED_FLUID_MODEL.put((FluidModel) (Object) this, new ClientRecordMixinRefMaps.ExtendedFluidModel(fluidTintSource, null));
    }

    @CreateInitializer
    public FluidModelInject(ChunkSectionLayer layer, Material.Baked stillMaterial, Material.Baked flowingMaterial, Material.@Nullable Baked overlayMaterial, @Nullable FluidTintSource fluidTintSource, @Nullable CustomFluidRenderer customRenderer) {
        this(layer, stillMaterial, flowingMaterial, overlayMaterial, (BlockTintSource) fluidTintSource);
        ClientRecordMixinRefMaps.EXTENDED_FLUID_MODEL.put((FluidModel) (Object) this, new ClientRecordMixinRefMaps.ExtendedFluidModel(fluidTintSource, customRenderer));
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$storeFallbackTintSource(ChunkSectionLayer layer, Material.Baked stillMaterial, Material.Baked flowingMaterial, Material.Baked overlayMaterial, BlockTintSource tintSource, CallbackInfo ci) {
        ClientRecordMixinRefMaps.EXTENDED_FLUID_MODEL.put((FluidModel) (Object) this, new ClientRecordMixinRefMaps.ExtendedFluidModel(FluidTintSources.of(tintSource), null));
    }

    @Override
    public @Nullable FluidTintSource fluidTintSource() {
        var extension = ClientRecordMixinRefMaps.EXTENDED_FLUID_MODEL.get(this);
        if (extension != null)
            return extension.fluidTintSource();

        return FluidTintSources.of(this.tintSource());
    }

    @Override
    public @Nullable CustomFluidRenderer customRenderer() {
        var extension = ClientRecordMixinRefMaps.EXTENDED_FLUID_MODEL.get(this);
        if (extension != null)
            return extension.customRenderer();

        return null;
    }

    @Mixin(FluidModel.Unbaked.class)
    public abstract static class UnbakedInject implements FluidModelInjection.UnbakedInjection {
        public UnbakedInject(Material stillMaterial, Material flowingMaterial, @Nullable Material overlayMaterial, @Nullable BlockTintSource tintSource) {
        }

        @CreateInitializer
        public UnbakedInject(Material stillMaterial, Material flowingMaterial, @Nullable Material overlayMaterial, @Nullable FluidTintSource fluidTintSource) {
            this(stillMaterial, flowingMaterial, overlayMaterial, (BlockTintSource) fluidTintSource);
            ClientRecordMixinRefMaps.EXTENDED_UNBAKED_FLUID_MODEL.put((FluidModel.Unbaked) (Object) this, new ClientRecordMixinRefMaps.ExtendedFluidModel(fluidTintSource, null));
        }

        @CreateInitializer
        public UnbakedInject(Material stillMaterial, Material flowingMaterial, @Nullable Material overlayMaterial, @Nullable FluidTintSource fluidTintSource, @Nullable CustomFluidRenderer customRenderer) {
            this(stillMaterial, flowingMaterial, overlayMaterial, (BlockTintSource) fluidTintSource);
            ClientRecordMixinRefMaps.EXTENDED_UNBAKED_FLUID_MODEL.put((FluidModel.Unbaked) (Object) this, new ClientRecordMixinRefMaps.ExtendedFluidModel(fluidTintSource, customRenderer));
        }

        @Shadow public abstract @Nullable BlockTintSource tintSource();

        @Inject(method = "<init>", at = @At("TAIL"))
        private void kilt$storeFallbackTintSource(Material stillMaterial, Material flowingMaterial, Material overlayMaterial, BlockTintSource tintSource, CallbackInfo ci) {
            ClientRecordMixinRefMaps.EXTENDED_UNBAKED_FLUID_MODEL.put((FluidModel.Unbaked) (Object) this, new ClientRecordMixinRefMaps.ExtendedFluidModel(FluidTintSources.of(tintSource), null));
        }

        @Override
        public @Nullable FluidTintSource fluidTintSource() {
            var extension = ClientRecordMixinRefMaps.EXTENDED_UNBAKED_FLUID_MODEL.get(this);
            if (extension != null)
                return extension.fluidTintSource();

            return FluidTintSources.of(this.tintSource());
        }

        @Override
        public @Nullable CustomFluidRenderer customRenderer() {
            var extension = ClientRecordMixinRefMaps.EXTENDED_UNBAKED_FLUID_MODEL.get(this);
            if (extension != null)
                return extension.customRenderer();

            return null;
        }

        @ModifyReturnValue(method = "bake", at = @At("RETURN"))
        private FluidModel kilt$attachExtendedModelData(FluidModel original) {
            ClientRecordMixinRefMaps.EXTENDED_FLUID_MODEL.put(original, ClientRecordMixinRefMaps.EXTENDED_UNBAKED_FLUID_MODEL.getOrDefault(this, new ClientRecordMixinRefMaps.ExtendedFluidModel(this.fluidTintSource(), this.customRenderer())));
            return original;
        }
    }
}
