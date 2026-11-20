package xyz.bluspring.kilt.injections.server.level;

import java.util.OptionalLong;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.neoforged.neoforge.capabilities.ICapabilityInvalidationListener;
import net.neoforged.neoforge.entity.PartEntity;
import xyz.bluspring.kilt.injections.world.level.LevelInjection;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.BlockPos;

public interface ServerLevelInjection extends LevelInjection {
    default Int2ObjectMap<PartEntity<?>> kilt$getEntityParts() {
        throw KiltHelper.createMixinException(ServerLevelInjection.class, "kilt$getEntityParts");
    }

    default void registerCapabilityListener(BlockPos pos, ICapabilityInvalidationListener listener) {
        throw KiltHelper.createMixinException(ServerLevelInjection.class, "registerCapabilityListener");
    }

    default void cleanCapabilityListenerReferences() {
        throw KiltHelper.createMixinException(ServerLevelInjection.class, "cleanCapabilityListenerReferences");
    }

    default OptionalLong getSeedOverride() {
        throw KiltHelper.createMixinException(ServerLevelInjection.class, "getSeedOverride");
    }
}
