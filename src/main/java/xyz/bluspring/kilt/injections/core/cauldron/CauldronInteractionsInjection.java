package xyz.bluspring.kilt.injections.core.cauldron;

import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.event.RegisterCauldronInteractionEvent;
import xyz.bluspring.kilt.mixin.CauldronInteractionsAccessor;

public interface CauldronInteractionsInjection {
    static void registerCustomInteractions() {
        ModLoader.postEvent(new RegisterCauldronInteractionEvent.Dispatcher(CauldronInteractionsAccessor.kilt$getIdMapper()));
        ModLoader.postEvent(new RegisterCauldronInteractionEvent.Interaction(CauldronInteractionsAccessor.kilt$getIdMapper()));
    }
}
