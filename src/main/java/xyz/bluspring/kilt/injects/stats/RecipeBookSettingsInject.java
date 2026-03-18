// TRACKED HASH: 44767e4d3a832ae3e4f6603b5367e7a335ce7243
package xyz.bluspring.kilt.injects.stats;

import java.util.Map;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.datafixers.util.Pair;
import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.stats.RecipeBookSettingsInjection;

import net.minecraft.stats.RecipeBookSettings;
import net.minecraft.world.inventory.RecipeBookType;

// Increase priority, because Farmer's Delight Refabricated makes the map immutable again.
@Mixin(value = RecipeBookSettings.class, priority = 1500)
public abstract class RecipeBookSettingsInject implements RecipeBookSettingsInjection {
    @Final @Mutable
    @Shadow private static Map<RecipeBookType, Pair<String, String>> TAG_FIELDS;

    @Inject(at = @At("TAIL"), method = "<clinit>")
    private static void kilt$makeTagFieldsMutable(CallbackInfo ci) {
        TAG_FIELDS = CommonHooks.buildRecipeBookTypeTagFields(TAG_FIELDS);
    }

    @ModifyExpressionValue(method = "read(Lnet/minecraft/network/FriendlyByteBuf;)Lnet/minecraft/stats/RecipeBookSettings;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/RecipeBookType;values()[Lnet/minecraft/world/inventory/RecipeBookType;"))
    private static RecipeBookType[] kilt$tryUseFilteredRecipeValues(RecipeBookType[] original) {
        return CommonHooks.kilt$getFilteredRecipeBookTypeValues(original);
    }
}
