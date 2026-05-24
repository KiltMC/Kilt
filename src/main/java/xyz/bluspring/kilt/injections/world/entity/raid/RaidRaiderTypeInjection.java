package xyz.bluspring.kilt.injections.world.entity.raid;

import java.util.function.Supplier;

import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.raid.Raider;

public interface RaidRaiderTypeInjection {

    default Supplier<EntityType<? extends Raider>> kilt$getEntityTypeSupplier() {
        throw KiltHelper.createMixinException(RaidRaiderTypeInjection.class, "kilt$getEntityTypeSupplier");
    }

    default boolean kilt$isNeo() {
        throw KiltHelper.createMixinException(RaidRaiderTypeInjection.class, "kilt$isNeo");
    }
}
