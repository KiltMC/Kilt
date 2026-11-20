package xyz.bluspring.kilt.injects.server;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ConditionContext;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.event.DefaultDataComponentsBoundEvent;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.resource.ContextAwareReloadListener;
import net.neoforged.neoforge.resource.ListenerKey;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.server.ReloadableServerResourcesInjection;

import net.minecraft.commands.Commands;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.flag.FeatureFlagSet;

@Mixin(ReloadableServerResources.class)
public abstract class ReloadableServerResourcesInject implements ReloadableServerResourcesInjection {
    @Shadow
    @Final
    private List<Registry.PendingTags<?>> postponedTags;
    @Unique private RegistryAccess registryAccess = RegistryAccess.EMPTY;
    @Unique private HolderLookup.Provider loadingContext = RegistryAccess.EMPTY;
    @Unique private ConditionContext context;
    @Unique private final Map<ListenerKey<?>, PreparableReloadListener> retainedListeners = new IdentityHashMap<>();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$initContexts(LayeredRegistryAccess<RegistryLayer> fullLayers, HolderLookup.Provider loadingContext, FeatureFlagSet enabledFeatures, Commands.CommandSelection commandSelection, List<Registry.PendingTags<?>> postponedTags, PermissionSet functionCompilationPermissions, List<DataComponentInitializers.PendingComponents<?>> newComponents, CallbackInfo ci) {
        this.registryAccess = fullLayers.compositeAccess();
        this.loadingContext = loadingContext;
        this.context = new ConditionContext(this.postponedTags, this.registryAccess, enabledFeatures);
    }

    @Override
    public <T extends PreparableReloadListener> T getListener(ListenerKey<T> key) {
        PreparableReloadListener listener = this.retainedListeners.get(key);
        if (listener == null) {
            throw new IllegalArgumentException("No listener registered for key " + key);
        }

        return (T) listener;
    }

    @Override
    public ICondition.IContext getConditionContext() {
        return this.context;
    }

    @Override
    public HolderLookup.Provider getRegistryLookup() {
        return this.loadingContext;
    }

    @Override
    public Map<ListenerKey<?>, PreparableReloadListener> kilt$getRetainedListeners() {
        return this.retainedListeners;
    }

    @WrapOperation(method = "lambda$loadResources$2", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/ReloadableServerResources;listeners()Ljava/util/List;"))
    private static List<PreparableReloadListener> kilt$tryUseCustomListeners(ReloadableServerResources result, Operation<List<PreparableReloadListener>> original, @Local(argsOnly = true, name = "fullRegistries") ReloadableServerRegistries.LoadResult fullRegistries, @Share("listeners") LocalRef<List<PreparableReloadListener>> listenersRef) {
        List<PreparableReloadListener> listeners = EventHooks.onResourceReload(result, fullRegistries.layers().compositeAccess(), result.kilt$getRetainedListeners());
        for (PreparableReloadListener rl : listeners) {
            if (rl instanceof ContextAwareReloadListener carl) {
                carl.injectContext(result.getConditionContext(), result.getRegistryLookup());
            }
        }

        listenersRef.set(listeners);
        var current = new ArrayList<>(original.call(result));
        for (PreparableReloadListener listener : listeners) {
            if (!current.contains(listener))
                current.add(listener);
        }

        return current;
    }

    @Definition(id = "done", method = "Lnet/minecraft/server/packs/resources/ReloadInstance;done()Ljava/util/concurrent/CompletableFuture;")
    @Expression("?.done()")
    @ModifyExpressionValue(method = "lambda$loadResources$2", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static CompletableFuture<?> kilt$clearReloadContexts(CompletableFuture<?> original, @Local(name = "result") ReloadableServerResources result, @Share("listeners") LocalRef<List<PreparableReloadListener>> listenersRef) {
        return original.thenRun(() -> {
            ((ConditionContext) result.getConditionContext()).clear();
            for (PreparableReloadListener listener : listenersRef.get()) {
                if (listener instanceof ContextAwareReloadListener carl) {
                    carl.injectContext(ICondition.IContext.EMPTY, RegistryAccess.EMPTY);
                }
            }
        });
    }

    @Definition(id = "newComponents", field = "Lnet/minecraft/server/ReloadableServerResources;newComponents:Ljava/util/List;")
    @Definition(id = "forEach", method = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V")
    @Definition(id = "apply", method = "Lnet/minecraft/core/component/DataComponentInitializers$PendingComponents;apply()V")
    @Expression("this.newComponents.forEach(::apply)")
    @WrapOperation(method = "updateComponentsAndStaticRegistryTags", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void kilt$callUpdateEvents(List instance, Consumer consumer, Operation<Void> original) {
        NeoForge.EVENT_BUS.post(new TagsUpdatedEvent.ServerDataLoad((ReloadableServerResources) (Object) this, this.registryAccess));
        original.call(instance, consumer);
        NeoForge.EVENT_BUS.post(new DefaultDataComponentsBoundEvent(false, false));
    }
}
