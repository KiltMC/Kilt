package xyz.bluspring.kilt.injections.world.level.storage.loot;

public interface LootTableInjection {
    default void freeze() {
        throw new IllegalStateException();
    }
}
