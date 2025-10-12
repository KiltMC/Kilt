// TRACKED HASH: f66e5adf9c424c85d6e35a89c7556d71a35a6f6f
package xyz.bluspring.kilt.injects.world.item.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.crafting.CraftingHelper;
import net.neoforged.neoforge.common.crafting.conditions.ICondition;
import net.neoforged.neoforge.resource.ContextAwareReloadListener;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.item.crafting.RecipeManagerInjection;
import xyz.bluspring.kilt.injections.world.item.crafting.RecipeInjection;

import java.util.Map;

@Mixin(RecipeManager.class)
public class RecipeManagerInject {

    @Shadow @Final private static Logger LOGGER;

    @Shadow
    @Final
    private HolderLookup.Provider registries;

    @WrapOperation(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/HolderLookup$Provider;createSerializationContext(Lcom/mojang/serialization/DynamicOps;)Lnet/minecraft/resources/RegistryOps;"
            )
    )
    private RegistryOps<JsonElement> wrapConditionalContext(HolderLookup.Provider instance, DynamicOps<JsonElement> ops, Operation<RegistryOps<JsonElement>> original) {
        if (this.registries != ((ContextAwareReloadListener) (Object) this).getRegistryLookup()) // Sanity check and warn if anything ends up different so it can be debugged more easily (hopefully this never happens)
            LOGGER.warn("Kilt: Registry Lookup is different from ContextAwareReloadListener#getRegistryLookup!");

        return new ConditionalOps<>(original.call(instance, ops), ((ContextAwareReloadListener) (Object) this).getContext());
    }

    @ModifyReceiver(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/serialization/Codec;parse(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;"
            )
    )
    private Codec<Recipe<?>> useConditionalCodec(Codec<Recipe<?>> instance, DynamicOps<JsonElement> dynamicOps, Object o) {
        if (instance != RecipeInjection.CONDITIONAL_CODEC) {
            return ConditionalOps.createConditionalCodecWithConditions(instance);
        }

        return RecipeInjection.CONDITIONAL_CODEC;
    }

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/RecipeManager;fromJson(Lnet/minecraft/resources/ResourceLocation;Lcom/google/gson/JsonObject;)Lnet/minecraft/world/item/crafting/Recipe;"), method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V")
    public Recipe<?> kilt$useForgeFromJson(ResourceLocation resourceLocation, JsonObject jsonObject, Operation<Recipe<?>> original, @Local Map.Entry<ResourceLocation, JsonElement> entry) {
        try {
            if (entry.getValue().isJsonObject() && !CraftingHelper.processConditions(entry.getValue().getAsJsonObject(), "conditions", this.context)) {
                LOGGER.debug("Skipping loading recipe {} as its conditions were not met", resourceLocation);
                return null;
            }

            var recipe = RecipeManagerInjection.fromJson(resourceLocation, GsonHelper.convertToJsonObject(entry.getValue(), "top element"), this.context);

            if (recipe == null) {
                recipe = original.call(resourceLocation, jsonObject);

                if (recipe == null) {
                    LOGGER.info("Skipping loading recipe {} as its serializer returned null", resourceLocation);
                    return null;
                }
            }

            return recipe;
        } catch (Throwable e) {
            return original.call(resourceLocation, jsonObject);
        }
    }
}