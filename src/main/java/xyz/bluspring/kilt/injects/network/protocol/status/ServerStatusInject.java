// TRACKED HASH: 7e5bca00d790e710c28c06cbb62cc29ce25e147b
package xyz.bluspring.kilt.injects.network.protocol.status;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.status.ServerStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.network.protocol.status.ServerStatusInjection;

import java.util.Optional;

@Mixin(ServerStatus.class)
public abstract class ServerStatusInject implements ServerStatusInjection {
    @Unique private boolean isModded = false;

    public ServerStatusInject(Component description, Optional<ServerStatus.Players> players, Optional<ServerStatus.Version> version, Optional<ServerStatus.Favicon> favicon, boolean enforcesSecureChat) {}

    @CreateInitializer
    public ServerStatusInject(Component description, Optional<ServerStatus.Players> players, Optional<ServerStatus.Version> version, Optional<ServerStatus.Favicon> favicon, boolean enforcesSecureChat, boolean isModded) {
        this(description, players, version, favicon, enforcesSecureChat);
        this.kilt$setModded(isModded);
    }

    @Override
    public boolean isModded() {
        return this.isModded;
    }

    @Override
    public void kilt$setModded(boolean isModded) {
        this.isModded = isModded;
    }

    // thanks @TropheusJ
    // https://gist.github.com/TropheusJ/6fc33a167f63fbfab0b6eb8afd298ed8
    @ModifyReturnValue(method = "method_49092", at = @At("RETURN"))
    private static App<RecordCodecBuilder.Mu<ServerStatus>, ServerStatus> kilt$appendForgeData(App<RecordCodecBuilder.Mu<ServerStatus>, ServerStatus> original, @Local(argsOnly = true) RecordCodecBuilder.Instance<ServerStatus> instance) {
        return instance.group(original,
            Codec.BOOL
                .lenientOptionalFieldOf("isModded", Boolean.FALSE)
                .forGetter(ServerStatus::isModded)
        )
            .apply(instance, (status, isModded) -> {
                status.kilt$setModded(isModded);
                return status;
            });
    }
}