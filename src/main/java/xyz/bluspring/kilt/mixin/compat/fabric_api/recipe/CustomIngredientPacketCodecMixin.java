package xyz.bluspring.kilt.mixin.compat.fabric_api.recipe;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.impl.recipe.ingredient.CustomIngredientPacketCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CustomIngredientPacketCodec.class)
public abstract class CustomIngredientPacketCodecMixin {
    @ModifyExpressionValue(method = "encode(Lnet/minecraft/network/RegistryFriendlyByteBuf;Lnet/minecraft/world/item/crafting/Ingredient;)V", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/impl/recipe/ingredient/CustomIngredientPacketCodec;shouldEncodeFallback(Lnet/fabricmc/fabric/api/recipe/v1/ingredient/CustomIngredient;)Z"))
    private boolean kilt$avoidVanillaEncodingIfNeo(boolean original, @Local(argsOnly = true) Ingredient ingredient) {
        return original && ingredient.isSimple();
    }

    @Inject(method = "encode(Lnet/minecraft/network/RegistryFriendlyByteBuf;Lnet/minecraft/world/item/crafting/Ingredient;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/RegistryFriendlyByteBuf;writeResourceLocation(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/network/FriendlyByteBuf;"))
    private void kilt$encodeAsNeoIfPossible(RegistryFriendlyByteBuf buf, Ingredient value, CallbackInfo ci) {
        // TODO: fuck you do this
        //if (value.)
    }

    @Inject(method = "decode(Lnet/minecraft/network/RegistryFriendlyByteBuf;)Lnet/minecraft/world/item/crafting/Ingredient;", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/api/recipe/v1/ingredient/CustomIngredientSerializer;get(Lnet/minecraft/resources/ResourceLocation;)Lnet/fabricmc/fabric/api/recipe/v1/ingredient/CustomIngredientSerializer;"))
    private void kilt$tryGetNeoIngredientSerializer(RegistryFriendlyByteBuf buf, CallbackInfoReturnable<Ingredient> cir) {

    }
}
