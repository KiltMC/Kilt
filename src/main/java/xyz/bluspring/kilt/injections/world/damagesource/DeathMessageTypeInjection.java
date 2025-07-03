package xyz.bluspring.kilt.injections.world.damagesource;

import net.neoforged.neoforge.common.damagesource.IDeathMessageProvider;

public interface DeathMessageTypeInjection {
    IDeathMessageProvider getMessageFunction();
}
