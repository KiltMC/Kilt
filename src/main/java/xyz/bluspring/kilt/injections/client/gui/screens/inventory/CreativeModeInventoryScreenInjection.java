package xyz.bluspring.kilt.injections.client.gui.screens.inventory;

import net.neoforged.neoforge.client.gui.CreativeTabsScreenPage;

public interface CreativeModeInventoryScreenInjection {
    CreativeTabsScreenPage getCurrentPage();
    void setCurrentPage(CreativeTabsScreenPage currentPage);
}
