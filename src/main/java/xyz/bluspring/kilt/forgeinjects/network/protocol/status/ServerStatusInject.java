// TRACKED HASH: 7e5bca00d790e710c28c06cbb62cc29ce25e147b
package xyz.bluspring.kilt.forgeinjects.network.protocol.status;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraftforge.network.ServerStatusPing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
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

    // thanks @TropheusJ
    // https://gist.github.com/TropheusJ/6fc33a167f63fbfab0b6eb8afd298ed8
    @ModifyReturnValue(method = "method_49092", at = @At("RETURN"))
    private static App<RecordCodecBuilder.Mu<ServerStatus>, ServerStatus> method_49092(App<RecordCodecBuilder.Mu<ServerStatus>, ServerStatus> original, @Local(argsOnly = true) RecordCodecBuilder.Instance<ServerStatus> instance) {
        return instance.group(original,
            ServerStatusPing.CODEC
                .optionalFieldOf("forgeData")
                .forGetter(ServerStatusInjection::forgeData)
        )
            .apply(instance, (status, forgeData) -> {
                status.setForgeData(forgeData);
                return status;
            });
    }
}