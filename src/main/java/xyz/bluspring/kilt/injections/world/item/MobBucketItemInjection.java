package xyz.bluspring.kilt.injections.world.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.level.material.Fluid;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

public interface MobBucketItemInjection {
    static MobBucketItem create(Supplier<? extends EntityType<? extends Entity>> entitySupplier, Supplier<? extends Fluid> fluidSupplier, Supplier<? extends SoundEvent> soundSupplier, Item.Properties properties) {
        try {
            var mobBucketInit = MobBucketItem.class.getDeclaredConstructor(Supplier.class, Supplier.class, Supplier.class, Item.Properties.class);
            return mobBucketInit.newInstance(entitySupplier, fluidSupplier, soundSupplier, properties);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
