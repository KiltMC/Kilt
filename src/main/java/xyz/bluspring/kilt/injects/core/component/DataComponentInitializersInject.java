package xyz.bluspring.kilt.injects.core.component;

import java.util.List;
import java.util.Set;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.internal.DataComponentModifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

@Mixin(DataComponentInitializers.class)
public abstract class DataComponentInitializersInject {
    @Unique private static final ThreadLocal<HolderLookup.Provider> kilt$context = ThreadLocal.withInitial(() -> RegistryAccess.EMPTY);

    @WrapMethod(method = "createInitializerForRegistry")
    private static <T> DataComponentInitializers.PendingComponents<T> kilt$storeContext(HolderLookup.Provider context, @Coerce Object elementBuilders, Operation<DataComponentInitializers.PendingComponents<T>> original) {
        kilt$context.set(context);
        try {
            return original.call(context, elementBuilders);
        } finally {
            kilt$context.set(RegistryAccess.EMPTY);
        }
    }

    @Inject(method = "lambda$createInitializerForRegistry$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/component/DataComponentMap$Builder;build()Lnet/minecraft/core/component/DataComponentMap;"))
    private static <T> void kilt$modifyDefaultItemComponents(HolderLookup.RegistryLookup<T> registry, List entries, Set elementsWithComponents, ResourceKey<? extends Registry<T>> elementKey, DataComponentMap.Builder elementBuilder, CallbackInfo ci, @Local(name = "element") Holder.Reference<T> element) {
        var context = kilt$context.get();
        if (elementKey.isFor(Registries.ITEM) && context != RegistryAccess.EMPTY) {
            DataComponentModifiers.apply(context, (Item) element.value(), elementBuilder);
        }
    }
}
