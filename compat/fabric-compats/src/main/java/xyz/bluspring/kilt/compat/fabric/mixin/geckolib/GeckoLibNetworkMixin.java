package xyz.bluspring.kilt.compat.fabric.mixin.geckolib;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import software.bernie.geckolib.network.AbstractPacket;
import software.bernie.geckolib.network.GeckoLibNetwork;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;

@IfModLoaded("geckolib")
@Pseudo
@Mixin(GeckoLibNetwork.class)
public abstract class GeckoLibNetworkMixin {
    @CreateStatic
    private static <M> void send(M packet, PacketDistributor.PacketTarget distributor) {
        if (packet instanceof AbstractPacket abstractPacket) {
            distributor.send(ServerPlayNetworking.createS2CPacket(abstractPacket.getPacketID(), abstractPacket.encode()));
        }
    }
}
