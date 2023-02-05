package xyz.bluspring.kilt.forgeinjects.world.entity.raid;

import net.minecraft.world.entity.raid.Raid;
import net.minecraftforge.common.IExtensibleEnum;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.entity.RaidRaiderTypeInjection;

@Mixin(Raid.RaiderType.class)
public abstract class RaidRaiderTypeInject implements RaidRaiderTypeInjection, IExtensibleEnum {
    @Shadow @Final @Mutable
    public static Raid.RaiderType[] VALUES;

    @Override
    public void init() {
        VALUES = Raid.RaiderType.values();
    }
}
