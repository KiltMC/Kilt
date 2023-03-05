package xyz.bluspring.kilt.forgeinjects.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import xyz.bluspring.kilt.injections.advancements.critereon.ItemPredicateInjection;

import java.util.Map;

@Mixin(ItemPredicate.class)
public class ItemPredicateInject implements ItemPredicateInjection {
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;deserializeEnchantments(Lnet/minecraft/nbt/ListTag;)Ljava/util/Map;", ordinal = 0), method = "matches")
    public Map<Enchantment, Integer> kilt$getAllForgeEnchantments(ListTag listTag, ItemStack itemStack) {
        return itemStack.getAllEnchantments();
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/critereon/MinMaxBounds$Ints;fromJson(Lcom/google/gson/JsonElement;)Lnet/minecraft/advancements/critereon/MinMaxBounds$Ints;", ordinal = 0, shift = At.Shift.BEFORE), method = "fromJson", locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private static void kilt$applyForgeAdvancementType(JsonElement jsonElement, CallbackInfoReturnable<ItemPredicate> cir, JsonObject jsonObject) {
        if (jsonObject.has("type")) {
            var resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "type"));
            if (ItemPredicateInjection.customPredicates.containsKey(resourceLocation)) {
                cir.setReturnValue(ItemPredicateInjection.customPredicates.get(resourceLocation).apply(jsonObject));
            } else
                throw new JsonSyntaxException("There is no ItemPredicate of type " + resourceLocation);
        }
    }
}
