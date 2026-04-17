package xyz.bluspring.kilt.injections.world.entity.raid;

import java.util.function.Supplier;

import xyz.bluspring.kilt.mixin.RaiderTypeAccessor;
import xyz.bluspring.kilt.util.EnumUtils;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;

public interface RaidRaiderTypeInjection {
    static Raid.RaiderType create(String name, EntityType<? extends Raider> typeIn, int[] waveCountsIn) {
        return EnumUtils.addEnumToClass(
                Raid.RaiderType.class, RaiderTypeAccessor.getValues(),
                name, (size) -> RaiderTypeAccessor.createRaiderType(name, size, typeIn, waveCountsIn),
                (values) -> RaiderTypeAccessor.setValues(values.toArray(new Raid.RaiderType[0]))
        );
    }

    default Supplier<EntityType<? extends Raider>> kilt$getEntityTypeSupplier() {
        throw KiltHelper.createMixinException(RaidRaiderTypeInjection.class, "kilt$getEntityTypeSupplier");
    }

    default boolean kilt$isNeo() {
        throw KiltHelper.createMixinException(RaidRaiderTypeInjection.class, "kilt$isNeo");
    }
}
