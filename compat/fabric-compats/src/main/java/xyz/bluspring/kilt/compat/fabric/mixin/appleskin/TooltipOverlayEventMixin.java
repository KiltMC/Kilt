package xyz.bluspring.kilt.compat.fabric.mixin.appleskin;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import squeek.appleskin.api.event.TooltipOverlayEvent;
import xyz.bluspring.kilt.helpers.mixin.Extends;

@IfModLoaded("appleskin")
@Pseudo
@Extends(Event.class)
@Mixin(TooltipOverlayEvent.class)
public abstract class TooltipOverlayEventMixin implements ICancellableEvent {
    @Shadow
    public boolean isCanceled;

    @Override
    public boolean isCanceled() {
        return this.isCanceled;
    }

    @Override
    public void setCanceled(boolean canceled) {
        this.isCanceled = canceled;
    }
}
