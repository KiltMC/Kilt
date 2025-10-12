// TRACKED HASH: d24928420f3c1ebf622411bc07206c361aa737b9
package xyz.bluspring.kilt.injects.server;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.commands.Commands;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.tags.TagManager;
import net.minecraft.world.flag.FeatureFlagSet;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ConditionContext;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.resource.ContextAwareReloadListener;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.server.ReloadableServerResourcesInjection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Mixin(ReloadableServerResources.class)
public class ReloadableServerResourcesInject implements ReloadableServerResourcesInjection {
    @Shadow @Final private TagManager tagManager;
    @Shadow
    @Final
    private ReloadableServerResources.ConfigurableRegistryLookup registryLookup;
    @Shadow
    @Final
    private ReloadableServerRegistries.Holder fullRegistryHolder;
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

    @Override
    public HolderLookup.Provider getRegistryLookup() {
        return registryLookup;
    }

    @ModifyArg(method = "method_58296", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/SimpleReloadInstance;create(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Z)Lnet/minecraft/server/packs/resources/ReloadInstance;"))
    private static List<PreparableReloadListener> kilt$addForgeResourceReloadListener(List<PreparableReloadListener> listeners, @Local ReloadableServerResources serverResources, @Local(argsOnly = true) LayeredRegistryAccess<RegistryLayer> registryAccess, @Local(argsOnly = true) Commands.CommandSelection commandSelection, @Share("listeners") LocalRef<List<PreparableReloadListener>> listenersRef) {
        var list = new ArrayList<>(listeners);
        list.addAll(EventHooks.onResourceReload(serverResources, registryAccess.compositeAccess()));
        listeners.forEach(rl -> {
            if (rl instanceof ContextAwareReloadListener srl) srl.injectContext(serverResources.getConditionContext(), serverResources.getRegistryLookup());
        });
        listenersRef.set(list);
        return list;
    }

    @WrapOperation(method = "method_58296", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;thenApply(Ljava/util/function/Function;)Ljava/util/concurrent/CompletableFuture;"))
    private static CompletableFuture injectContext(CompletableFuture instance, Function function, Operation<CompletableFuture> original, @Share("listeners") LocalRef<List<PreparableReloadListener>> listenersRef) {
        return original.call(instance.thenRun(() -> {
            // Clear context after reload completes
            listenersRef.get().forEach(rl -> {
                if (rl instanceof ContextAwareReloadListener srl) {
                    srl.injectContext(ICondition.IContext.EMPTY, RegistryAccess.EMPTY);
                }
            });
        }), function);
    }

    @Inject(method = "updateRegistryTags()V", at = @At("TAIL"))
    private void kilt$callTagUpdateEvent(CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new TagsUpdatedEvent(this.fullRegistryHolder.get(), false, false));
    }
}