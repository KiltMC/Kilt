package xyz.bluspring.kilt.injects.world.item.crafting;

import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.WithConditions;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.world.item.crafting.RecipeInjection;
import xyz.bluspring.kilt.workarounds.ContextAwareReloadListenerWorkaround;
import xyz.bluspring.kilt.workarounds.SkippedConditionException;

import net.minecraft.resources.RegistryOps;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerInject extends SimpleJsonResourceReloadListener implements ContextAwareReloadListenerWorkaround {
    public RecipeManagerInject(Gson gson, String directory) {
        super(gson, directory);
    }

    @WrapOperation(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;parse(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;"))
    private <E, R> DataResult<R> kilt$tryUseConditionalRecipeDecode(Codec<E> instance, DynamicOps<JsonElement> dynamicOps, Object o, Operation<DataResult<R>> original, @Local RegistryOps<JsonElement> registryOps, @Share("conditionalRegistryOps") LocalRef<ConditionalOps<JsonElement>> conditionalOps) {
        if (conditionalOps.get() == null)
            conditionalOps.set(new ConditionalOps<>(registryOps, this.kilt$asContextAware().getContext()));

        DataResult<Optional<WithConditions<Recipe<?>>>> decoded = RecipeInjection.CONDITIONAL_CODEC.parse(conditionalOps.get(), (JsonElement) o);

        if (decoded.hasResultOrPartial()) {
            var decodedRecipe = decoded.getOrThrow(JsonParseException::new);
            if (decodedRecipe.isPresent()) {
                if (!decodedRecipe.orElseThrow().conditions().isEmpty())
                    return DataResult.success((R) decoded.getOrThrow().orElseThrow().carrier());
            } else {
                throw new SkippedConditionException("Skipping loading recipe as its conditions were not met");
            }
        }

        return original.call(instance, dynamicOps, o);
    }

    @WrapOperation(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"))
    private void kilt$handleException(Logger instance, String s, Object o, Object p, Operation<Void> original) {
        if (p instanceof SkippedConditionException ex) {
            instance.debug(ex.getMessage());
        } else {
            original.call(instance, s, o, p);
        }
    }
}
