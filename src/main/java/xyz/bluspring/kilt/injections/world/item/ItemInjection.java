package xyz.bluspring.kilt.injections.world.item;

import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public interface ItemInjection {
    default void initializeClient(Consumer<IClientItemExtensions> consumer) {
    }
}
