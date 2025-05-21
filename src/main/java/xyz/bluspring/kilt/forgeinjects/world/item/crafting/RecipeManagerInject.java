package xyz.bluspring.kilt.forgeinjects.world.item.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.item.crafting.RecipeManagerInjection;

import java.util.Map;

@Mixin(RecipeManager.class)
public class RecipeManagerInject implements RecipeManagerInjection {
    @CreateStatic
    private static Recipe<?> fromJson(ResourceLocation location, JsonObject json, ICondition.IContext context) {
        return RecipeManagerInjection.fromJson(location, json, context);
    }

    @Shadow @Final private static Logger LOGGER;
    private ICondition.IContext context = ICondition.IContext.EMPTY;

    @Override
    public void setContext(ICondition.IContext context) {
        this.context = context;
    }

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/RecipeManager;fromJson(Lnet/minecraft/resources/ResourceLocation;Lcom/google/gson/JsonObject;)Lnet/minecraft/world/item/crafting/Recipe;"), method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V")
    public Recipe<?> kilt$useForgeFromJson(ResourceLocation recipeId, JsonObject json, Operation<Recipe<?>> original, @Local Map.Entry<ResourceLocation, JsonElement> entry) {
        try {
            if (entry.getValue().isJsonObject() && !CraftingHelper.processConditions(entry.getValue().getAsJsonObject(), "conditions", this.context)) {
                LOGGER.debug("Skipping loading recipe {} as its conditions were not met", recipeId);
                return null;
            }

            var recipe = RecipeManagerInjection.fromJson(recipeId, GsonHelper.convertToJsonObject(entry.getValue(), "top element"), this.context);

            if (recipe == null) {
                LOGGER.info("Skipping loading recipe {} as its serializer returned null", recipeId);
                return null;
            }

            return recipe;
        } catch (JsonSyntaxException ignored) {
            return original.call(recipeId, json); // Allow mods like Supplementaries to process it instead
        }
    }
}
