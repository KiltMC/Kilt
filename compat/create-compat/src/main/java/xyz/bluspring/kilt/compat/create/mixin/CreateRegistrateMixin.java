package xyz.bluspring.kilt.compat.create.mixin;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import com.simibubi.create.foundation.data.CreateRegistrate;
import net.minecraftforge.eventbus.api.IEventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@IfModLoaded("create")
@Mixin(CreateRegistrate.class)
public abstract class CreateRegistrateMixin extends AbstractRegistrateMixin<CreateRegistrate> {
    @Unique
    public CreateRegistrate registerEventListeners(IEventBus bus) {
        return super.registerEventListeners(bus);
    }
}
