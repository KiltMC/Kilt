// TRACKED HASH: 7e5bca00d790e710c28c06cbb62cc29ce25e147b
package xyz.bluspring.kilt.forgeinjects.network.protocol.status;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.util.ExtraCodecs;
import net.minecraftforge.network.ServerStatusPing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import xyz.bluspring.kilt.injections.network.ServerStatusInjection;

import javax.annotation.Nullable;
import java.util.Optional;

@Mixin(ServerStatus.class)
public class ServerStatusInject implements ServerStatusInjection {
    @Unique
    private Optional<ServerStatusPing> forgeData;

    @Override
    @Nullable
    public Optional<ServerStatusPing> forgeData() {
        return forgeData;
    }

    @Override
    public void setForgeData(Optional<ServerStatusPing> data) {
        forgeData = data;
    }

    /**
     * @author BluSpring
     * @reason I don't think there's any other sane way for me to do this, if there is please let me know
     */
    @Overwrite
    private static App<RecordCodecBuilder.Mu<ServerStatus>, ServerStatus> method_49092(RecordCodecBuilder.Instance<ServerStatus> instance) {
        return instance.group(
                ExtraCodecs.COMPONENT.optionalFieldOf("description", CommonComponents.EMPTY)
                        .forGetter(ServerStatus::description),
                ServerStatus.Players.CODEC.optionalFieldOf("players")
                        .forGetter(ServerStatus::players),
                ServerStatus.Version.CODEC.optionalFieldOf("version")
                        .forGetter(ServerStatus::version),
                ServerStatus.Favicon.CODEC.optionalFieldOf("favicon")
                        .forGetter(ServerStatus::favicon),
                Codec.BOOL.optionalFieldOf("enforcesSecureChat", false)
                        .forGetter(ServerStatus::enforcesSecureChat),
                // this down here is the singular reason as to why this *has* to be an overwrite.
                ServerStatusPing.CODEC.optionalFieldOf("forgeData")
                        .forGetter(ServerStatusInjection::forgeData)
        ).apply(instance, (description, players, version, favicon, secureChat, forgeData) -> {
            var status = new ServerStatus(description, players, version, favicon, secureChat);
            status.setForgeData(forgeData);

            return status;
        });
    }
}