package xyz.bluspring.mods.flashbacksablecompat.mixin;

import dev.ryanhcode.sable.network.packets.ClientboundSableSnapshotInfoDualPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.mods.flashbacksablecompat.compat.ActionSableSnapshotInfo;
import xyz.bluspring.mods.flashbacksablecompat.compat.SableSupport;

import net.minecraft.world.level.Level;

@Mixin(ClientboundSableSnapshotInfoDualPacket.class)
public abstract class ClientboundSableSnapshotInfoDualPacketMixin {
    @Inject(method = "handleClient", at = @At("HEAD"))
    private void saveSnapshotInfoPacket(Level level, CallbackInfo ci) {
        if (!SableSupport.shouldWritePacket()) {
            return;
        }

        SableSupport.submitPacket(ActionSableSnapshotInfo.INSTANCE, (ClientboundSableSnapshotInfoDualPacket) (Object) this);
    }
}
