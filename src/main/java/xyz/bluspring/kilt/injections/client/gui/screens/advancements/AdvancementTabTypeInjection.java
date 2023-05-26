package xyz.bluspring.kilt.injections.client.gui.screens.advancements;

import net.minecraft.client.gui.screens.advancements.AdvancementTabType;

import java.util.Arrays;

public interface AdvancementTabTypeInjection {
    int MAX_TABS = Arrays.stream(AdvancementTabType.values()).mapToInt(AdvancementTabType::getMax).sum();
}
