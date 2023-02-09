package xyz.bluspring.kilt.injections.item;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

import java.util.function.Supplier;

public interface BucketItemInjection {
    static BucketItem create(Supplier<? extends Fluid> fluidSupplier, Item.Properties properties) {
        return new BucketItem(fluidSupplier.get(), properties);
    }

    default Fluid getFluid() {
        throw new IllegalStateException();
    }
}
