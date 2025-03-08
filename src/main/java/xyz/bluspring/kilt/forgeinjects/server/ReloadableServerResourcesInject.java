package xyz.bluspring.kilt.forgeinjects.server;

import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.tags.TagManager;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.conditions.ConditionContext;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.event.TagsUpdatedEvent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.ReloadableServerResourcesInjection;
import xyz.bluspring.kilt.injections.item.crafting.RecipeManagerInjection;
import xyz.bluspring.kilt.injections.server.ServerAdvancementManagerInjection;

@Mixin(ReloadableServerResources.class)
public class ReloadableServerResourcesInject implements ReloadableServerResourcesInjection {
    @Shadow @Final private TagManager tagManager;
    @Shadow @Final private RecipeManager recipes;
    @Shadow @Final private ServerAdvancementManager advancements;
    @Unique
    private ICondition.IContext kilt$context;

    @Inject(at = @At("TAIL"), method = "<init>")
    public void kilt$addContext(RegistryAccess.Frozen frozen, Commands.CommandSelection commandSelection, int i, CallbackInfo ci) {
        this.kilt$context = new ConditionContext(this.tagManager);
        ((RecipeManagerInjection) this.recipes).setContext(this.kilt$context);
        ((ServerAdvancementManagerInjection) this.advancements).kilt$setContext(this.kilt$context);
    }

    @Inject(method = "updateRegistryTags(Lnet/minecraft/core/RegistryAccess;)V", at = @At("TAIL"))
    private void kilt$updateTags(RegistryAccess registryAccess, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new TagsUpdatedEvent(registryAccess, false, false));
    }

    @NotNull
    @Override
    public ICondition.IContext getConditionContext() {
        return kilt$context;
    }
}
