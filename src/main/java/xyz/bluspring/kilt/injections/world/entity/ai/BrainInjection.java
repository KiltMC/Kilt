package xyz.bluspring.kilt.injections.world.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.util.BrainBuilder;

public interface BrainInjection<E extends LivingEntity> {
    default BrainBuilder<E> createBuilder() {
        throw new IllegalStateException();
    }

    default void copyFromBuilder(BrainBuilder<E> builder) {
        throw new IllegalStateException();
    }
}
