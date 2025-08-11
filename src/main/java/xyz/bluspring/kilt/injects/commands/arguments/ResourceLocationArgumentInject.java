// TRACKED HASH: ce5d57dd4f5c5f0e5989a792e2bc3dc505a01ded
package xyz.bluspring.kilt.injects.commands.arguments;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.advancements.Advancement;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ResourceLocationArgument.class)
public abstract class ResourceLocationArgumentInject {
    @Redirect(method = "getAdvancement", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/ServerAdvancementManager;getAdvancement(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/advancements/Advancement;"))
    private static Advancement kilt$getFromSourceAdvancements(ServerAdvancementManager instance, ResourceLocation id, @Local(argsOnly = true) CommandContext<CommandSourceStack> ctx) {
        return ctx.getSource().getAdvancement(id);
    }

    @Inject(method = "getRecipe", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/arguments/ResourceLocationArgument;getId(Lcom/mojang/brigadier/context/CommandContext;Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;"))
    private static void kilt$useSourceRecipeManager(CommandContext<CommandSourceStack> context, String name, CallbackInfoReturnable<Recipe<?>> cir, @Local LocalRef<RecipeManager> recipeManager) {
        recipeManager.set(context.getSource().getRecipeManager());
    }
}