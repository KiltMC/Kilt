package xyz.bluspring.kilt.injections.client.gui;

import java.util.Collection;
import java.util.Map;

import net.neoforged.neoforge.client.gui.GuiLayerManager;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;

public interface LayeredDrawInjection {
    default GuiLayerManager kilt$getLayerManager() {
        throw KiltHelper.createMixinException(LayeredDrawInjection.class, "kilt$getLayerManager");
    }

    default Collection<ResourceLocation> kilt$getOrderedLayerIds() {
        throw KiltHelper.createMixinException(LayeredDrawInjection.class, "kilt$getOrderedLayerIds");
    }

    default Map<ResourceLocation, LayeredDraw.Layer> kilt$getNamedLayers() {
        throw KiltHelper.createMixinException(LayeredDrawInjection.class, "kilt$getNamedLayers");
    }

    default void kilt$add(ResourceLocation id) {
        this.kilt$addVanilla(id, GuiLayerManager.KILT_EMPTY_LAYER);
    }

    default void kilt$addVanilla(ResourceLocation id, LayeredDraw.Layer layer) {
        throw KiltHelper.createMixinException(LayeredDrawInjection.class, "kilt$addVanilla");
    }

    default Collection<LayeredDraw> kilt$getInnerDraws() {
        throw KiltHelper.createMixinException(LayeredDrawInjection.class, "kilt$getInnerDraws");
    }

    default void kilt$updateInternalLayers() {
        throw KiltHelper.createMixinException(LayeredDrawInjection.class, "kilt$updateInternalLayers");
    }

    default int kilt$getLayerCount() {
        throw KiltHelper.createMixinException(LayeredDrawInjection.class, "kilt$getLayerCount");
    }
}
