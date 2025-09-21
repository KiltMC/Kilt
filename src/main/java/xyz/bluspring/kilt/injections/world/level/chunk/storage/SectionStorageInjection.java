package xyz.bluspring.kilt.injections.world.level.chunk.storage;

public interface SectionStorageInjection {
    default void remove(long sectionPosAsLong) {
        throw new IllegalStateException();
    }
}
