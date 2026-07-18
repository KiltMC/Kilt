package xyz.bluspring.kilt.injections.world.item;

import java.util.function.Consumer;

import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface ItemInjection {
    default void initializeClient(Consumer<IClientItemExtensions> consumer) {
        throw KiltHelper.createMixinException(ItemInjection.class, "initializeClient");
    }

    default void modifyDefaultComponentsFrom(DataComponentPatch patch) {
        throw KiltHelper.createMixinException(ItemInjection.class, "modifyDefaultComponentsFrom");
    }

    interface TooltipContextInjection {
        default Level level() {
            throw KiltHelper.createMixinException(TooltipContextInjection.class, "level");
        }

        default Player player() {
            throw KiltHelper.createMixinException(TooltipContextInjection.class, "player");
        }
    }
}
