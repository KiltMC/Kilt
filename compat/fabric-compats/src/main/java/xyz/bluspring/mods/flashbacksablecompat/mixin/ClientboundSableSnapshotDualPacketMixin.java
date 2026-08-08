package xyz.bluspring.mods.flashbacksablecompat.mixin;

import dev.ryanhcode.sable.network.packets.ClientboundSableSnapshotDualPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.mods.flashbacksablecompat.compat.ActionSableSnapshot;
import xyz.bluspring.mods.flashbacksablecompat.compat.SableSupport;

import net.minecraft.world.level.Level;

@Mixin(ClientboundSableSnapshotDualPacket.class)
public abstract class ClientboundSableSnapshotDualPacketMixin {
    @Inject(method = "handleClient(Lnet/minecraft/world/level/Level;)V", at = @At("HEAD"))
    private void saveSnapshotInfoPacket(Level level, CallbackInfo ci) {
        if (!SableSupport.shouldWritePacket()) {
            return;
        }

        SableSupport.submitPacket(ActionSableSnapshot.INSTANCE, (ClientboundSableSnapshotDualPacket) (Object) this);
    }
}
