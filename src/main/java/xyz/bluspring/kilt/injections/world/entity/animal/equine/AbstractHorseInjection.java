package xyz.bluspring.kilt.injections.world.entity.animal.equine;

import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.world.Container;

public interface AbstractHorseInjection {
    default Container getInventory() {
        throw KiltHelper.createMixinException(AbstractHorseInjection.class, "getInventory");
    }
}
