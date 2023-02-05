package xyz.bluspring.kilt.injections.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import xyz.bluspring.kilt.mixin.RaiderTypeAccessor;
import xyz.bluspring.kilt.util.EnumUtils;

public interface RaidRaiderTypeInjection {
    static Raid.RaiderType create(String name, EntityType<? extends Raider> typeIn, int[] waveCountsIn) {
        return EnumUtils.addEnumToClass(
                Raid.RaiderType.class, RaiderTypeAccessor.getValues(),
                name, (size) -> RaiderTypeAccessor.createRaiderType(name, size, typeIn, waveCountsIn),
                (values) -> RaiderTypeAccessor.setValues(values.toArray(new Raid.RaiderType[0]))
        );
    }
}
