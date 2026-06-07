package xyz.bluspring.kilt.mixin.compat.porting_lib;

import java.util.Map;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.fabricators_of_create.porting_lib.loot.LootModifierManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LootModifierManager.class)
public abstract class LootModifierManagerMixin {
    @WrapOperation(method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Ljava/util/Map;", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
    private <K, V> V kilt$porting_lib$avoidTempadCrash(Map<K, V> instance, K k, V v, Operation<V> original) {
        // Kilt: Otherwise Tempad causes problems, we have to make sure the elements actually exist.
        if (v == null) {
            return null;
        }

        return original.call(instance, k, v);
    }
}
