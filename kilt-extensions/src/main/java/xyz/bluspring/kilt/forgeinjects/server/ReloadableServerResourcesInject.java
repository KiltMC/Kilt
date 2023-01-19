package xyz.bluspring.kilt.forgeinjects.server;

import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.tags.TagManager;
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
    public void kilt$addContext(RegistryAccess.Frozen frozen, Commands.CommandSelection commandSelection, int i, CallbackInfo ci) {
        this.kilt$context = new ConditionContext(this.tagManager);
    }

    @NotNull
    @Override
    public ICondition.IContext getConditionContext() {
        return kilt$context;
    }
}
