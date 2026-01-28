package xyz.bluspring.kilt.injections.server.level;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.capabilities.ICapabilityInvalidationListener;
import net.neoforged.neoforge.entity.PartEntity;
import xyz.bluspring.kilt.injections.world.level.LevelInjection;
import xyz.bluspring.kilt.util.KiltHelper;

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

    @Override
    default void setDayTimeFraction(float dayTimeFraction) {
        throw KiltHelper.createMixinException(ServerLevelInjection.class, "setDayTimeFraction");
    }

    @Override
    default float getDayTimeFraction() {
        throw KiltHelper.createMixinException(ServerLevelInjection.class, "getDayTimeFraction");
    }

    @Override
    default float getDayTimePerTick() {
        throw KiltHelper.createMixinException(ServerLevelInjection.class, "getDayTimePerTick");
    }
}
