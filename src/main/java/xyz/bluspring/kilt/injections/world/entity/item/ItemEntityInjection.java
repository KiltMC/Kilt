package xyz.bluspring.kilt.injections.world.entity.item;

import xyz.bluspring.kilt.util.KiltHelper;

public interface ItemEntityInjection {
    default int kilt$getLifespan() {
        throw KiltHelper.createMixinException(ItemEntityInjection.class, "kilt$getLifespan");
    }
}
