// TRACKED HASH: bae5e4c8e94da7615bf681d057b392b75e3f2405
package xyz.bluspring.kilt.forgeinjects.world.item.crafting;

import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.SimpleCookingSerializer;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SimpleCookingSerializer.class)
public abstract class SimpleCookingSerializerInject {
    @WrapOperation(method = "fromJson(Lnet/minecraft/resources/ResourceLocation;Lcom/google/gson/JsonObject;)Lnet/minecraft/world/item/crafting/AbstractCookingRecipe;", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/GsonHelper;getAsString(Lcom/google/gson/JsonObject;Ljava/lang/String;)Ljava/lang/String;", ordinal = 0))
    private String kilt$useEmptyStringIfObject(JsonObject json, String memberName, Operation<String> original) {
        if (json.get(memberName).isJsonObject()) {
            return "minecraft:air";
        }

        return original.call(json, memberName);
    }

    @WrapOperation(method = "fromJson(Lnet/minecraft/resources/ResourceLocation;Lcom/google/gson/JsonObject;)Lnet/minecraft/world/item/crafting/AbstractCookingRecipe;", at = @At(value = "NEW", target = "(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack kilt$tryCreateStackFromJson(ItemLike item, Operation<ItemStack> original, @Local(argsOnly = true) JsonObject json) {
        if (json.get("result").isJsonObject()) {
            return ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
        }

        return original.call(item);
    }
}