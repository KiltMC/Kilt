package xyz.bluspring.kilt.injects.client.multiplayer;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.telemetry.WorldSessionTelemetryManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerLinks;
import net.minecraft.world.flag.FeatureFlagSet;
import net.neoforged.neoforge.network.connection.ConnectionType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.client.multiplayer.CommonListenerCookieInjection;

import java.util.Map;

@Mixin(CommonListenerCookie.class)
public abstract class CommonListenerCookieInject implements CommonListenerCookieInjection {
    private ConnectionType connectionType;

    @Override
    public ConnectionType connectionType() {
        return this.connectionType;
    }

    @Override
    public void kilt$setConnectionType(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    public CommonListenerCookieInject(GameProfile localGameProfile, WorldSessionTelemetryManager telemetryManager, RegistryAccess.Frozen receivedRegistries, FeatureFlagSet enabledFeatures, @Nullable String serverBrand, @Nullable ServerData serverData, @Nullable Screen postDisconnectScreen, Map<ResourceLocation, byte[]> serverCookies, @Nullable ChatComponent.State chatState, boolean strictErrorHandling, Map<String, String> customReportDetails, ServerLinks serverLinks) {}

    @CreateInitializer
    public CommonListenerCookieInject(GameProfile localGameProfile, WorldSessionTelemetryManager telemetryManager, RegistryAccess.Frozen receivedRegistries, FeatureFlagSet enabledFeatures, @Nullable String serverBrand, @Nullable ServerData serverData, @Nullable Screen postDisconnectScreen, Map<ResourceLocation, byte[]> serverCookies, @Nullable ChatComponent.State chatState, boolean strictErrorHandling, Map<String, String> customReportDetails, ServerLinks serverLinks, ConnectionType connectionType) {
        this(localGameProfile, telemetryManager, receivedRegistries, enabledFeatures, serverBrand, serverData, postDisconnectScreen, serverCookies, chatState, strictErrorHandling, customReportDetails, serverLinks);
        this.connectionType = connectionType;
    }
}
