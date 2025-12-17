package xyz.bluspring.kilt.injections.server.level;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import org.jetbrains.annotations.Nullable;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.OptionalInt;
import java.util.function.Consumer;

public interface ServerPlayerInjection {
    default String getLanguage() {
        throw KiltHelper.createMixinException(ServerPlayerInjection.class, "getLanguage");
    }

    default Component getTabListHeader() {
        throw KiltHelper.createMixinException(ServerPlayerInjection.class, "getTabListHeader");
    }

    default void setTabListHeader(Component header) {
        throw KiltHelper.createMixinException(ServerPlayerInjection.class, "setTabListHeader");
    }

    default Component getTabListFooter() {
        throw KiltHelper.createMixinException(ServerPlayerInjection.class, "getTabListFooter");
    }

    default void setTabListFooter(Component footer) {
        throw KiltHelper.createMixinException(ServerPlayerInjection.class, "setTabListFooter");
    }

    default void setTabListHeaderFooter(Component header, Component footer) {
        throw KiltHelper.createMixinException(ServerPlayerInjection.class, "setTabListHeaderFooter");
    }

    default void refreshTabListName() {
        throw KiltHelper.createMixinException(ServerPlayerInjection.class, "refreshTabListName");
    }
}
