package xyz.bluspring.kilt.injects.commands;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.ResourceLocation;

@Mixin(SharedSuggestionProvider.class)
public interface SharedSuggestionProviderInject {
    @Definition(id = "resourceLocation", local = @Local(type = ResourceLocation.class))
    @Definition(id = "getNamespace", method = "Lnet/minecraft/resources/ResourceLocation;getNamespace()Ljava/lang/String;")
    @Definition(id = "equals", method = "Ljava/lang/String;equals(Ljava/lang/Object;)Z")
    @Expression("resourceLocation.getNamespace().equals('minecraft')")
    @ModifyExpressionValue(method = "filterResources(Ljava/lang/Iterable;Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Consumer;)V", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static boolean kilt$alwaysAllowModdedSuggestions(boolean original) {
        return true;
    }
}
