package xyz.bluspring.kilt.injections.world.entity;

import java.util.Stack;

import net.neoforged.neoforge.common.damagesource.DamageContainer;
import org.jspecify.annotations.Nullable;
import xyz.bluspring.kilt.util.KiltHelper;

public interface LivingEntityInjection {
    default @Nullable Stack<DamageContainer> kilt$getDamageContainers() {
        throw KiltHelper.createMixinException(LivingEntityInjection.class, "kilt$getDamageContainers");
    }
}
