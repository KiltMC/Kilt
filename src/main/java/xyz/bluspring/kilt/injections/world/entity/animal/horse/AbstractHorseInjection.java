package xyz.bluspring.kilt.injections.world.entity.animal.horse;

import net.minecraft.world.Container;
import xyz.bluspring.kilt.util.KiltHelper;

public interface AbstractHorseInjection {
    default Container getInventory() {
        throw KiltHelper.createMixinException(AbstractHorseInjection.class, "getInventory");
    }
}
