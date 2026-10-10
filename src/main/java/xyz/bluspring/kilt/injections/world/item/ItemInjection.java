package xyz.bluspring.kilt.injections.world.item;

import java.util.function.Consumer;
import java.util.function.Function;

import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public interface ItemInjection {
    default void initializeClient(Consumer<IClientItemExtensions> consumer) {
        throw KiltHelper.createMixinException(ItemInjection.class, "initializeClient");
    }

    default void modifyDefaultComponentsFrom(DataComponentPatch patch) {
        throw KiltHelper.createMixinException(ItemInjection.class, "modifyDefaultComponentsFrom");
    }

    default void resetDefaultResource() {
        throw KiltHelper.createMixinException(ItemInjection.class, "resetDefaultResource");
    }

    default ItemResource computeDefaultResource(Function<Item, ItemResource> resourceConstructor) {
        throw KiltHelper.createMixinException(ItemInjection.class, "computeDefaultResource");
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
