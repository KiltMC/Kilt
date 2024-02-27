// TRACKED HASH: d24928420f3c1ebf622411bc07206c361aa737b9
package xyz.bluspring.kilt.forgeinjects.server;

import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.tags.TagManager;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraftforge.common.crafting.conditions.ConditionContext;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.ReloadableServerResourcesInjection;

@Mixin(ReloadableServerResources.class)
public class ReloadableServerResourcesInject implements ReloadableServerResourcesInjection {
    @Shadow @Final private TagManager tagManager;
    @Unique
    private ICondition.IContext kilt$context;

    @Inject(at = @At("TAIL"), method = "<init>")
    public void kilt$addContext(RegistryAccess.Frozen registryAccess, FeatureFlagSet enabledFeatures, Commands.CommandSelection commandSelection, int functionCompilationLevel, CallbackInfo ci) {
        this.kilt$context = new ConditionContext(this.tagManager);
    }

    @NotNull
    @Override
    public ICondition.IContext getConditionContext() {
        return kilt$context;
    }
}