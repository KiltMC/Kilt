package xyz.bluspring.kilt.injections.world.item;

import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.function.Consumer;

public interface ItemInjection {
    default void initializeClient(Consumer<IClientItemExtensions> consumer) {
        throw KiltHelper.createMixinException(ItemInjection.class, "initializeClient");
    }

    interface TooltipContextInjection {
        default Level level() {
            throw KiltHelper.createMixinException(ItemInjection.class, "level");
        }
    }
}
