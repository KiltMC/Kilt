package xyz.bluspring.kilt.injections.world.level.entity;

import net.minecraft.world.level.entity.EntityAccess;

public interface PersistentEntitySectionManagerInjection<T extends EntityAccess> {
    boolean addNewEntityWithoutEvent(T entity);
}
