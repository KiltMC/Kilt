// TRACKED HASH: 2338786a3de83872cfd4444b471ad7adaf64d51c
package xyz.bluspring.kilt.forgeinjects.world.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.MobBucketItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Supplier;

@Mixin(MobBucketItem.class)
public class MobBucketItemInject {
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

    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/world/item/MobBucketItem;emptySound:Lnet/minecraft/sounds/SoundEvent;"), method = "playEmptySound")
    public SoundEvent kilt$useForgeEmptySound(MobBucketItem instance) {
        return getEmptySound();
    }

    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/world/item/MobBucketItem;type:Lnet/minecraft/world/entity/EntityType;"), method = "spawn")
    public EntityType<?> kilt$spawnUsingForgeEntityType(MobBucketItem instance) {
        return getFishType();
    }

    @Redirect(method = "appendHoverText", at = @At(value = "FIELD", target = "Lnet/minecraft/world/item/MobBucketItem;type:Lnet/minecraft/world/entity/EntityType;"))
    public EntityType<?> kilt$checkUsingForgeEntityType(MobBucketItem instance) {
        return getFishType();
    }

    // A CreateInitializer could be added here, but because BucketItem is adding its own constructor,
    // it can't be done feasibly.
}