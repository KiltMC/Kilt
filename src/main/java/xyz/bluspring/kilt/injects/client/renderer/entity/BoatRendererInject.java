package xyz.bluspring.kilt.injects.client.renderer.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.client.renderer.entity.BoatRendererInjection;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.Map;

@Mixin(BoatRenderer.class)
public abstract class BoatRendererInject implements BoatRendererInjection {
    @Shadow @Final private Map<Boat.Type, Pair<ResourceLocation, ListModel<Boat>>> boatResources;

    @WrapOperation(method = {"getTextureLocation(Lnet/minecraft/world/entity/vehicle/Boat;)Lnet/minecraft/resources/ResourceLocation;", "render(Lnet/minecraft/world/entity/vehicle/Boat;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"}, at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 0))
    private <K, V> V kilt$tryUseForgeGetModel(Map<K, V> instance, Object o, Operation<V> original, @Local(argsOnly = true) Boat boat) {
        if (KiltHelper.INSTANCE.hasMethodOverride(this.getClass(), BoatRenderer.class, "getModelWithLocation", Boat.class)) {
            return (V) this.getModelWithLocation(boat);
        }

        return original.call(instance, o);
    }

    @Override
    public Pair<ResourceLocation, ListModel<Boat>> getModelWithLocation(Boat boat) {
        return this.boatResources.get(boat.getVariant());
    }
}
