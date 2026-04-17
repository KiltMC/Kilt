package xyz.bluspring.kilt.injections.world.entity.item;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;
import xyz.bluspring.kilt.util.KiltHelper;

public interface ItemEntityInjection {
    default int kilt$getLifespan() {
        throw KiltHelper.createMixinException(ItemEntityInjection.class, "kilt$getLifespan");
    }

    @Nullable
    default UUID getTarget() {
        throw KiltHelper.createMixinException(ItemEntityInjection.class, "getTarget");
    }
}
