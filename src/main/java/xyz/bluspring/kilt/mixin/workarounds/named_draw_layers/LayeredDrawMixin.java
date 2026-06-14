package xyz.bluspring.kilt.mixin.workarounds.named_draw_layers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.GuiLayerManager;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.client.gui.LayeredDrawInjection;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;

@Mixin(LayeredDraw.class)
public abstract class LayeredDrawMixin implements LayeredDrawInjection {
    @Shadow @Final private List<LayeredDraw.Layer> layers;

    @Unique private final GuiLayerManager kilt$layerManager = new GuiLayerManager();
    @Unique private final List<ResourceLocation> kilt$orderedLayerIds = new ArrayList<>();
    @Unique private final Map<ResourceLocation, LayeredDraw.Layer> kilt$layerMap = new HashMap<>();
    @Unique private final List<LayeredDraw> kilt$innerDraws = new ArrayList<>();

    @Override
    public GuiLayerManager kilt$getLayerManager() {
        return this.kilt$layerManager;
    }

    @Override
    public void kilt$addVanilla(ResourceLocation id, LayeredDraw.Layer layer) {
        this.kilt$orderedLayerIds.add(id);
        this.kilt$layerManager.kilt$addVanilla(id, layer);
        this.kilt$layerMap.put(id, layer);
    }

    @Override
    public Collection<LayeredDraw> kilt$getInnerDraws() {
        return this.kilt$innerDraws;
    }

    @Override
    public Collection<ResourceLocation> kilt$getOrderedLayerIds() {
        return this.kilt$orderedLayerIds;
    }

    @Override
    public Map<ResourceLocation, LayeredDraw.Layer> kilt$getNamedLayers() {
        return this.kilt$layerMap;
    }

    @Override
    public void kilt$updateInternalLayers() {
        for (LayeredDraw innerDraw : this.kilt$innerDraws) {
            innerDraw.kilt$updateInternalLayers();
        }

        var existing = this.kilt$layerManager.kilt$getLayers();

        int currentLayerIndex = 0;
        for (GuiLayerManager.NamedLayer namedLayer : existing) {
            // Set the index of where to start adding layers.
            var actualLayer = this.kilt$layerMap.get(namedLayer.name());
            if (actualLayer != null) {
                var index = this.layers.indexOf(actualLayer);
                if (index != -1) {
                    currentLayerIndex = index + 1;
                    continue;
                }
            }

            // Avoid double-adding Vanilla layers
            if (this.layers.contains(namedLayer.layer()))
                continue;

            // Otherwise, let's add the layer after <index>.
            if (currentLayerIndex < this.layers.size()) {
                this.layers.add(currentLayerIndex, namedLayer.layer());
            } else {
                this.layers.add(namedLayer.layer());
            }
        }
    }

    @Inject(method = "add(Lnet/minecraft/client/gui/LayeredDraw;Ljava/util/function/BooleanSupplier;)Lnet/minecraft/client/gui/LayeredDraw;", at = @At("HEAD"))
    private void kilt$registerInternalLayerManagerToOurs(LayeredDraw layeredDraw, BooleanSupplier renderInner, CallbackInfoReturnable<LayeredDraw> cir) {
        var externalManager = layeredDraw.kilt$getLayerManager();
        this.kilt$layerManager.add(externalManager, renderInner);
        this.kilt$layerMap.putAll(layeredDraw.kilt$getNamedLayers());
        this.kilt$innerDraws.addAll(layeredDraw.kilt$getInnerDraws());
        this.kilt$innerDraws.add(layeredDraw);
    }

    @Unique
    private static boolean kilt$hasAnyInnerDraw(ResourceLocation id, Collection<LayeredDraw> draws) {
        for (LayeredDraw draw : draws) {
            if (draw.kilt$getOrderedLayerIds().contains(id))
                return true;

            if (kilt$hasAnyInnerDraw(id, draw.kilt$getInnerDraws()))
                return true;
        }

        return false;
    }

    @Override
    public int kilt$getLayerCount() {
        int count = this.layers.size();

        for (LayeredDraw draw : this.kilt$innerDraws) {
            count += draw.kilt$getLayerCount();
        }

        return count;
    }

    @WrapOperation(method = "renderInner", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/LayeredDraw$Layer;render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"))
    private void kilt$tryRenderLayer(LayeredDraw.Layer instance, GuiGraphics guiGraphics, DeltaTracker deltaTracker, Operation<Void> original) {
        var layerManager = this.kilt$getLayerManager();
        var named = layerManager.kilt$findNamedLayer(instance);

        if (named != null) {
            // Locate named layer in child draws
            if (kilt$hasAnyInnerDraw(named.name(), this.kilt$innerDraws)) {
                // Negate the translation that occurs afterward, we can't exactly wrap blocks of code.
                guiGraphics.pose().translate(0f, 0f, -LayeredDraw.Z_SEPARATION);
                return;
            }

            if (!NeoForge.EVENT_BUS.post(new RenderGuiLayerEvent.Pre(guiGraphics, deltaTracker, named.name(), instance)).isCanceled()) {
                original.call(instance, guiGraphics, deltaTracker);
                NeoForge.EVENT_BUS.post(new RenderGuiLayerEvent.Post(guiGraphics, deltaTracker, named.name(), instance));
            }
        } else {
            original.call(instance, guiGraphics, deltaTracker);
        }
    }
}
