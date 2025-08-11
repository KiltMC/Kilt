package xyz.bluspring.kilt.injects.core.dispenser;

import net.minecraft.core.dispenser.BoatDispenseItemBehavior;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BoatDispenseItemBehavior.class)
public abstract class BoatDispenseItemBehaviorInject extends DefaultDispenseItemBehavior {
    // TODO: how on earth do we accomplish this one
}
