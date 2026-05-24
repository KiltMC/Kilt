package xyz.bluspring.kilt.injects.client.model.geom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ModelLayers.class)
public abstract class ModelLayersInject {
    // Kilt: some* are handled by Fabric API
    
    @WrapOperation(
            method = {
                    "createRaftModelName",
                    "createChestRaftModelName",
                    "createBoatModelName",
                    "createChestBoatModelName"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/geom/ModelLayers;createLocation(Ljava/lang/String;Ljava/lang/String;)Lnet/minecraft/client/model/geom/ModelLayerLocation;"
            )
    )
    private static ModelLayerLocation kilt$handleBoatModelNames(
            String key, String model, Operation<ModelLayerLocation> createLocation
    ) {
        if (key.contains(":")) {
            ResourceLocation id = ResourceLocation.tryParse(StringUtils.substringAfter(key, "/"));
            if (id != null) {
                String prefix = StringUtils.substringBefore(key, "/") + "/";
                return new ModelLayerLocation(id.withPrefix(prefix), model);
            }
        }
        
        return createLocation.call(key, model);
    }
    
}
