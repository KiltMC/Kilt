package xyz.bluspring.kilt.mixin.compat.forge.pixelmon;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Pseudo
@Mixin(targets = "com.pixelmonmod.pixelmon.client.render.entity.renderers.AbstractPokemonRenderer")
public abstract class AbstractPokemonRendererMixin {
    @Unique private static final Class kilt$layerClass;

    static {
        try {
            kilt$layerClass = Class.forName("com.pixelmonmod.pixelmon.client.render.entity.layers.PokemonShimLayer");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @WrapOperation(method = "render(Lcom/pixelmonmod/pixelmon/entities/pixelmon/AbstractClientEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;", ordinal = 0))
    private <E> E kilt$pixelmon$avoidTrinketsLayerCrash(List<E> instance, int i, Operation<E> original) {
        for (E e : instance) {
            if (e.getClass().equals(kilt$layerClass))
                return e;
        }

        return original.call(instance, i);
    }
}
