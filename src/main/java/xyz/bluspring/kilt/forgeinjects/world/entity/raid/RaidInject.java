// TRACKED HASH: bb7fa96f1e8d107efb8fe68dc5e9402195156dfd
package xyz.bluspring.kilt.forgeinjects.world.entity.raid;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraftforge.common.IExtensibleEnum;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.entity.RaidRaiderTypeInjection;

@Mixin(Raid.class)
public class RaidInject {
    @Mixin(Raid.RaiderType.class)
    public static abstract class RaiderTypeInject implements RaidRaiderTypeInjection, IExtensibleEnum {
        @CreateStatic
        private static Raid.RaiderType create(String name, EntityType<? extends Raider> typeIn, int[] waveCountsIn) {
            return RaidRaiderTypeInjection.create(name, typeIn, waveCountsIn);
        }

        @Shadow
        @Final
        @Mutable
        public static Raid.RaiderType[] VALUES;

        @Override
        public void init() {
            VALUES = Raid.RaiderType.values();
        }
    }
}