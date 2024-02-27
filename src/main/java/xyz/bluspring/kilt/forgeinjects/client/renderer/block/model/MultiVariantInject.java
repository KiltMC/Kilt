// TRACKED HASH: 3a34e5cb897d49c03f55df3dbb55c53653624264
package xyz.bluspring.kilt.forgeinjects.client.renderer.block.model;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.block.model.MultiVariant;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.bluspring.kilt.injections.client.resources.model.ModelBakeryInjection;

import java.util.function.Function;

@Mixin(MultiVariant.class)
public class MultiVariantInject {
    @Redirect(method = "bake", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/ModelBaker;bake(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/resources/model/ModelState;)Lnet/minecraft/client/resources/model/BakedModel;"))
    public BakedModel kilt$useForgeBake(ModelBaker instance, ResourceLocation location, ModelState state, @Local Function<Material, TextureAtlasSprite> spriteGetter) {
        return ((ModelBakeryInjection.ModelBakerImplInjection) instance).bake(location, state, spriteGetter);
    }
}