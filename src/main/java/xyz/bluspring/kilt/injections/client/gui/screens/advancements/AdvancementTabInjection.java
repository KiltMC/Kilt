package xyz.bluspring.kilt.injections.client.gui.screens.advancements;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementTabType;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;

public interface AdvancementTabInjection {
    int getPage();
    void kilt$setPage(int page);

    static AdvancementTab create(Minecraft mc, AdvancementsScreen screen, AdvancementTabType type, int index, int page, Advancement adv, DisplayInfo info) {
        var tab = new AdvancementTab(mc, screen, type, index, adv, info);
        ((AdvancementTabInjection) tab).kilt$setPage(page);

        return tab;
    }
}
