package xyz.bluspring.kilt.injections.client.gui.screens.inventory;

import net.neoforged.neoforge.client.gui.CreativeTabsScreenPage;
import xyz.bluspring.kilt.util.KiltHelper;

public interface CreativeModeInventoryScreenInjection {
    default CreativeTabsScreenPage getCurrentPage() {
        throw KiltHelper.createMixinException(CreativeModeInventoryScreenInjection.class, "getCurrentPage");
    }

    default void setCurrentPage(CreativeTabsScreenPage currentPage) {
        throw KiltHelper.createMixinException(CreativeModeInventoryScreenInjection.class, "setCurrentPage");
    }
}
