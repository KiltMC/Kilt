package xyz.bluspring.kilt.injections.server;

import net.minecraft.server.packs.repository.RepositorySource;

public interface PackRepositoryInjection {
    default void addPackFinder(RepositorySource packFinder) {
        throw new IllegalStateException();
    }
}
