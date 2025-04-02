package xyz.bluspring.kilt.injections.server.level;

import net.minecraft.network.chat.Component;

public interface ServerPlayerInjection {
    String getLanguage();

    Component getTabListHeader();
    void setTabListHeader(Component header);

    Component getTabListFooter();
    void setTabListFooter(Component footer);

    void setTabListHeaderFooter(Component header, Component footer);

    void refreshTabListName();
}
