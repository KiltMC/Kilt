package xyz.bluspring.kilt.injections.client.gui.screens.worldselection;

import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.world.level.WorldDataConfiguration;

public interface WorldCreationContextInjection {
    WorldCreationContext withDataConfiguration(WorldDataConfiguration dataConfiguration);
}
