package xyz.bluspring.kilt.injections.world.phys;

import net.minecraft.world.phys.AABB;
import xyz.bluspring.kilt.util.KiltHelper;

public interface AABBInjection {
    AABB INFINITE = new AABB(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

    default boolean isInfinite() {
        throw KiltHelper.createMixinException(AABBInjection.class, "isInfinite");
    }
}
