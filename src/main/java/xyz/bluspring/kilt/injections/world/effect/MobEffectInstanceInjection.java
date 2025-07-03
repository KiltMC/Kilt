package xyz.bluspring.kilt.injections.world.effect;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.EffectCure;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MobEffectInstanceInjection {
    Set<EffectCure> getCures();

    interface DetailsInjection {
        Optional<Set<EffectCure>> cures();
        void kilt$setCures(Optional<Set<EffectCure>> cures);
    }
}
