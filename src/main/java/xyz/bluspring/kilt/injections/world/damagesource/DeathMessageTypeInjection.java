package xyz.bluspring.kilt.injections.world.damagesource;

import net.minecraft.world.damagesource.DeathMessageType;
import net.neoforged.neoforge.common.damagesource.IDeathMessageProvider;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;
import xyz.bluspring.kilt.util.KiltHelper;

@FabricInjectedInterface(DeathMessageType.class)
public interface DeathMessageTypeInjection {
    default IDeathMessageProvider getMessageFunction() {
        throw KiltHelper.createMixinException(DeathMessageTypeInjection.class, "getMessageFunction");
    }
}
