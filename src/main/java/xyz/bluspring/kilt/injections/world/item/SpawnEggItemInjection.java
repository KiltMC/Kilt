package xyz.bluspring.kilt.injections.world.item;

import org.jspecify.annotations.Nullable;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.dispenser.DispenseItemBehavior;

public interface SpawnEggItemInjection {
    default @Nullable DispenseItemBehavior createDispenseBehavior() {
        throw KiltHelper.createMixinException(SpawnEggItemInjection.class, "createDispenseBehavior");
    }
}
