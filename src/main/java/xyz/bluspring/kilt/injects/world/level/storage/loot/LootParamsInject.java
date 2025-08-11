package xyz.bluspring.kilt.injects.world.level.storage.loot;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.level.storage.loot.LootParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;

@Mixin(LootParams.class)
public abstract class LootParamsInject {
    @Mixin(LootParams.Builder.class)
    public static abstract class BuilderInject {
        @WrapOperation(method = "create", at = @At(value = "INVOKE", target = "Ljava/util/Set;isEmpty()Z"))
        private boolean kilt$allowCustomLootParams(Set instance, Operation<Boolean> original) {
            return true;
        }
    }
}
