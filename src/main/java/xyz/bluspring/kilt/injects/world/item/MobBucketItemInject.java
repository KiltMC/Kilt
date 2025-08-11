// TRACKED HASH: 2338786a3de83872cfd4444b471ad7adaf64d51c
package xyz.bluspring.kilt.injects.world.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.MobBucketItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.function.Supplier;

@Mixin(MobBucketItem.class)
public abstract class MobBucketItemInject {
    @Shadow @Final private SoundEvent emptySound;
    @Shadow @Final private EntityType<?> type;
    private Supplier<? extends EntityType<?>> entityTypeSupplier;
    private Supplier<? extends SoundEvent> emptySoundSupplier;

    private void setEntityTypeSupplier(Supplier<? extends EntityType<?>> supplier) {
        entityTypeSupplier = supplier;
    }

    private void setEmptySoundSupplier(Supplier<? extends SoundEvent> supplier) {
        emptySoundSupplier = supplier;
    }

    protected SoundEvent getEmptySound() {
        if (emptySoundSupplier == null)
            return this.emptySound;
        else
            return emptySoundSupplier.get();
    }

    protected EntityType<?> getFishType() {
        if (entityTypeSupplier == null)
            return this.type;
        else
            return entityTypeSupplier.get();
    }

    @WrapOperation(at = @At(value = "FIELD", target = "Lnet/minecraft/world/item/MobBucketItem;emptySound:Lnet/minecraft/sounds/SoundEvent;"), method = "playEmptySound")
    public SoundEvent kilt$useForgeEmptySound(MobBucketItem instance, Operation<SoundEvent> original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(this.getClass(), MobBucketItem.class, "getEmptySound") || this.emptySoundSupplier != null) {
            return this.getEmptySound();
        }

        return original.call(instance);
    }

    @WrapOperation(at = @At(value = "FIELD", target = "Lnet/minecraft/world/item/MobBucketItem;type:Lnet/minecraft/world/entity/EntityType;"), method = "spawn")
    public EntityType<?> kilt$spawnUsingForgeEntityType(MobBucketItem instance, Operation<EntityType<?>> original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(this.getClass(), MobBucketItem.class, "getFishType") || this.entityTypeSupplier != null) {
            return this.getFishType();
        }

        return original.call(instance);
    }

    @WrapOperation(method = "appendHoverText", at = @At(value = "FIELD", target = "Lnet/minecraft/world/item/MobBucketItem;type:Lnet/minecraft/world/entity/EntityType;"))
    public EntityType<?> kilt$checkUsingForgeEntityType(MobBucketItem instance, Operation<EntityType<?>> original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(this.getClass(), MobBucketItem.class, "getFishType") || this.entityTypeSupplier != null) {
            return this.getFishType();
        }

        return original.call(instance);
    }

    // A CreateInitializer could be added here, but because BucketItem is adding its own constructor,
    // it can't be done feasibly.
}