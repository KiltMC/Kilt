package xyz.bluspring.kilt.forgeinjects.client.renderer.block.model;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.block.model.MultiVariant;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.client.resources.model.ModelBakeryInjection;

import java.util.function.Function;

@Mixin(MultiVariant.class)
public class MultiVariantInject {
    @WrapOperation(method = "bake", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/ModelBakery;bake(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/resources/model/ModelState;)Lnet/minecraft/client/resources/model/BakedModel;"))
    public BakedModel kilt$useForgeBake(ModelBakery instance, ResourceLocation location, ModelState modelState, Operation<BakedModel> original, @Local(argsOnly = true) Function<Material, TextureAtlasSprite> spriteGetter) {
        var originalGetter = ((ModelBakeryInjection) instance).kilt$getSpriteGetter();
        if (spriteGetter != originalGetter || originalGetter == null) {
            return ((ModelBakeryInjection) instance).bake(location, modelState, spriteGetter);
        }

        return original.call(instance, location, modelState);
    }
}
