package xyz.bluspring.kilt.injects.core.cauldron;

import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.core.cauldron.CauldronInteractionsInjection;

import net.minecraft.core.cauldron.CauldronInteractions;

@Mixin(CauldronInteractions.class)
public abstract class CauldronInteractionsInject implements CauldronInteractionsInjection {
    @CreateStatic
    private static void registerCustomInteractions() {
        CauldronInteractionsInjection.registerCustomInteractions();
    }
}
