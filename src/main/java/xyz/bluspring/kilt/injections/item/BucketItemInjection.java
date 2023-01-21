package xyz.bluspring.kilt.injections.item;

import net.minecraft.world.level.material.Fluid;

public interface BucketItemInjection {
    default Fluid getFluid() {
        throw new IllegalStateException();
    }
}
