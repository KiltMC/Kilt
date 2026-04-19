package xyz.bluspring.kilt.injections.world.entity.vehicle;

import java.util.function.Supplier;

import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.world.item.Item;

public interface BoatInjection {
    interface TypeInjection {
        default Item getSticks() {
            throw KiltHelper.createMixinException(TypeInjection.class, "getSticks");
        }

        default boolean isRaft() {
            throw KiltHelper.createMixinException(TypeInjection.class, "isRaft");
        }

        default void kilt$setRaft(boolean raft) {
            throw KiltHelper.createMixinException(TypeInjection.class, "kilt$setRaft");
        }

        default Supplier<Item> kilt$getBoatItem() {
            throw KiltHelper.createMixinException(TypeInjection.class, "kilt$getBoatItem");
        }

        default Supplier<Item> kilt$getChestBoatItem() {
            throw KiltHelper.createMixinException(TypeInjection.class, "kilt$getChestBoatItem");
        }
    }
}
