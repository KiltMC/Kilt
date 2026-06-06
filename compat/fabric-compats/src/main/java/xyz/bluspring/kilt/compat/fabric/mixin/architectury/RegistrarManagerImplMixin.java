package xyz.bluspring.kilt.compat.fabric.mixin.architectury;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import dev.architectury.registry.registries.fabric.RegistrarManagerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.compat.fabric.architectury.KiltArchitecturyApiCompat;
import xyz.bluspring.kilt.loader.KiltLoader;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

@IfModLoaded("architectury")
@Pseudo
@Mixin(RegistrarManagerImpl.RegistrarImpl.class)
public abstract class RegistrarManagerImplMixin<T> {
    @Shadow
    private Registry<T> delegate;

    @WrapOperation(method = "register", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;register(Lnet/minecraft/core/Registry;Lnet/minecraft/resources/ResourceLocation;Ljava/lang/Object;)Ljava/lang/Object;"))
    private T kilt$delayRegisterIfNeeded(Registry<T> registry, ResourceLocation name, T value, Operation<T> original) {
        // Defer to Kilt for registration, because deferred registry sucks.
        if (KiltLoader.Companion.getInstance().hasMod(name.getNamespace())) {
            KiltArchitecturyApiCompat.delayForRegisterEvent(delegate, () -> original.call(registry, name, value));
            return value;
        }

        return original.call(registry, name, value);
    }

//    @Shadow private Registry<T> delegate;
//
//    @Unique private Set<Integer> kilt$registeredIds;
//    @Unique private Set<ResourceLocation> kilt$registeredKeys = new HashSet<>();
//    @Unique private Set<T> kilt$registeredValues = new HashSet<>();
//
//    @Inject(method = "<init>", at = @At("TAIL"))
//    private void kilt$architectury$detectRegisterEvents(String modId, Registry<T> delegate, CallbackInfo ci) {
//        var ids = this.kilt$registeredIds = new HashSet<>();
//        var keys = this.kilt$registeredKeys = new HashSet<>();
//        var values = this.kilt$registeredValues = new HashSet<>();
//
//        var event = RegistryEntryAddedCallback.event(delegate);
//        var earlyId = ResourceLocation.fromNamespaceAndPath(Kilt.MOD_ID, "early");
//        event.addPhaseOrdering(earlyId, Event.DEFAULT_PHASE);
//        event.register(earlyId, (id, key, value) -> {
//                ids.add(id);
//                keys.add(key);
//                values.add(value);
//            });
//    }
//
//    // FIXME: This is very much a workaround but idk how else to fix this.
//    //        The main issue is that Kilt is somehow preventing Fabric's RegistryEntryAdded callback from running, which causes issues with Architectury's spawn egg items especially.
//    @ModifyExpressionValue(method = "register", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;register(Lnet/minecraft/core/Registry;Lnet/minecraft/resources/ResourceLocation;Ljava/lang/Object;)Ljava/lang/Object;"))
//    private T kilt$architectury$forceAddRegister(T original) {
//        var id = delegate.getId(original);
//        var key = delegate.getKey(original);
//
//        if (!(this.kilt$registeredIds.contains(id) && this.kilt$registeredKeys.contains(key) && this.kilt$registeredValues.contains(original))) {
//            RegistryEntryAddedCallback.event(delegate).invoker().onEntryAdded(id, key, original);
//        }
//
//        return original;
//    }
}
