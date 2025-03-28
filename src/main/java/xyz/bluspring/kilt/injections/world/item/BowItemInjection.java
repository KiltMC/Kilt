package xyz.bluspring.kilt.injections.world.item;

import net.minecraft.world.entity.projectile.AbstractArrow;

public interface BowItemInjection {
    default AbstractArrow customArrow(AbstractArrow arrow) {
        return arrow;
    }
}
