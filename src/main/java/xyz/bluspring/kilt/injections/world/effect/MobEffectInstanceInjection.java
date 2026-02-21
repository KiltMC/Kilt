package xyz.bluspring.kilt.injections.world.effect;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.EffectCure;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MobEffectInstanceInjection {
    default Set<EffectCure> neoforge$getCures() {
        throw KiltHelper.createMixinException(MobEffectInstanceInjection.class, "neoforge$getCures");
    }

    interface DetailsInjection {
        Optional<Set<EffectCure>> cures();
        void kilt$setCures(Optional<Set<EffectCure>> cures);
    }
}
