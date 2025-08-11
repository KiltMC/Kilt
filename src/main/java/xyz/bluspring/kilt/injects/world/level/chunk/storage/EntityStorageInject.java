package xyz.bluspring.kilt.injects.world.level.chunk.storage;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityStorage.class)
public abstract class EntityStorageInject {
    @Shadow @Final private static Logger LOGGER;

    @WrapOperation(method = "method_31734", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;save(Lnet/minecraft/nbt/CompoundTag;)Z"))
    private static boolean kilt$catchThrownException(Entity instance, CompoundTag compound, Operation<Boolean> original) {
        try {
            return original.call(instance, compound);
        } catch (Exception e) {
            LOGGER.error("An Entity type {} has thrown an exception trying to write state. It will not persist. Report this to the mod author", instance.getType(), e);
            return false;
        }
    }
}
