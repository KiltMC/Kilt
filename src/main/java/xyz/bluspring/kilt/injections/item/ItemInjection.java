package xyz.bluspring.kilt.injections.item;

import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public interface ItemInjection {
    default void initializeClient(Consumer<IClientItemExtensions> consumer) {
    }
}
