package xyz.bluspring.kilt.injections.world.level.block;

import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.function.Consumer;

public interface BlockInjection {
    default void initializeClient(Consumer<IClientBlockExtensions> consumer) {
        throw KiltHelper.createMixinException(BlockInjection.class, "initializeClient");
    }
}
