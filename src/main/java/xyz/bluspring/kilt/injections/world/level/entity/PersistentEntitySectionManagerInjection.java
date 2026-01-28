package xyz.bluspring.kilt.injections.world.level.entity;

import net.minecraft.world.level.entity.EntityAccess;
import xyz.bluspring.kilt.util.KiltHelper;

public interface PersistentEntitySectionManagerInjection<T extends EntityAccess> {
    default void kilt$markWithoutEvent() {
        throw KiltHelper.createMixinException(PersistentEntitySectionManagerInjection.class, "kilt$markWithoutEvent");
    }

    default boolean addNewEntityWithoutEvent(T entity) {
        throw KiltHelper.createMixinException(PersistentEntitySectionManagerInjection.class, "addNewEntityWithoutEvent");
    }
}
