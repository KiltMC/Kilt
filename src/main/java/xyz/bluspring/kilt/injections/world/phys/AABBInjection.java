package xyz.bluspring.kilt.injections.world.phys;

import net.minecraft.world.phys.AABB;

public interface AABBInjection {
    AABB INFINITE = new AABB(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

    boolean isInfinite();
}
