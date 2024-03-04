// TRACKED HASH: adf04c863ae196ab1f285d81e3ca8d315398a1ac
package xyz.bluspring.kilt.forgeinjects.client.model.geom;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;

@Mixin(LayerDefinitions.class)
public class LayerDefinitionsInject {
    @Inject(at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap$Builder;build()Lcom/google/common/collect/ImmutableMap;", shift = At.Shift.BEFORE, remap = false), method = "createRoots", locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private static void kilt$registerLayerDefinitions(CallbackInfoReturnable<Map<ModelLayerLocation, LayerDefinition>> cir, ImmutableMap.Builder<ModelLayerLocation, LayerDefinition> builder, LayerDefinition layerDefinition, LayerDefinition layerDefinition2, LayerDefinition layerDefinition3, LayerDefinition layerDefinition4, LayerDefinition layerDefinition5, LayerDefinition layerDefinition6, LayerDefinition layerDefinition7, LayerDefinition layerDefinition8, LayerDefinition layerDefinition9, LayerDefinition layerDefinition10, LayerDefinition layerDefinition11, LayerDefinition layerDefinition12, LayerDefinition layerDefinition13, LayerDefinition layerDefinition14, LayerDefinition layerDefinition15, LayerDefinition layerDefinition16, LayerDefinition layerDefinition17, LayerDefinition layerDefinition18, LayerDefinition layerDefinition19, LayerDefinition layerDefinition20, LayerDefinition layerDefinition21) {
        ForgeHooksClient.loadLayerDefinitions(builder);
    }
}