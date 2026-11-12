package xyz.bluspring.kilt.compat.fabric.mixin.appleskin;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.neoforged.bus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import squeek.appleskin.api.event.FoodValuesEvent;
import xyz.bluspring.kilt.helpers.mixin.Extends;

@IfModLoaded("appleskin")
@Pseudo
@Extends(Event.class)
@Mixin(FoodValuesEvent.class)
public abstract class FoodValuesEventMixin {
}
