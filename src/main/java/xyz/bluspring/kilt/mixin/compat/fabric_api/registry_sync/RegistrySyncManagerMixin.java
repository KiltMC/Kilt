package xyz.bluspring.kilt.mixin.compat.fabric_api.registry_sync;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.minecraft.core.Registry;
import net.minecraftforge.registries.RegistryManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RegistrySyncManager.class)
public abstract class RegistrySyncManagerMixin {
    @WrapOperation(method = "createAndPopulateRegistryMap", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;getId(Ljava/lang/Object;)I"))
    private static <T> int kilt$useForgeIdMap(Registry<T> instance, @Nullable T t, Operation<Integer> original) {
        var forgeRegistry = RegistryManager.ACTIVE.getRegistry(instance.key());

        if (forgeRegistry != null) {
            return forgeRegistry.getID(t);
        }

        //noinspection MixinExtrasOperationParameters
        return original.call(instance, t);
    }
}
