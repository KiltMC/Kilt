package xyz.bluspring.kilt.compat.fabric.mixin.creativecore;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import team.creative.creativecore.common.level.ISubLevel;
import team.creative.creativecore.common.network.CreativeNetwork;
import team.creative.creativecore.common.network.CreativeNetworkPacket;
import team.creative.creativecore.common.network.CreativePacket;

import java.util.HashMap;

@IfModLoaded("creativecore")
@Mixin(CreativeNetwork.class)
public abstract class CreativeNetworkMixin {
    @Shadow @Final private HashMap<Class<? extends CreativePacket>, CreativeNetworkPacket> packetTypes;

    @Shadow @Final private HashMap<Class<? extends CreativePacket>, ResourceLocation> packetTypeChannels;

    @Unique
    public void sendToClient(CreativePacket message, Level level, BlockPos pos) {
        if (level instanceof ISubLevel subLevel)
            sendToClientTracking(message, subLevel.getHolder());
        else
            sendToClient(message, level.getChunkAt(pos));
    }

    @Unique
    public void sendToClient(CreativePacket message, LevelChunk chunk) {
        var buf = new FriendlyByteBuf(Unpooled.buffer());
        this.packetTypes.get(message.getClass()).write(message, buf);
        PlayerLookup.tracking((ServerLevel) chunk.getLevel(), chunk.getPos()).forEach(player -> {
            ServerPlayNetworking.send(player, this.packetTypeChannels.get(message.getClass()), buf);
        });
    }

    @Unique
    public void sendToClientTracking(CreativePacket message, Entity entity) {
        var buf = new FriendlyByteBuf(Unpooled.buffer());
        this.packetTypes.get(message.getClass()).write(message, buf);
        PlayerLookup.tracking(entity).forEach(player -> {
            ServerPlayNetworking.send(player, this.packetTypeChannels.get(message.getClass()), buf);
        });
    }

    @Unique
    public void sendToClientTrackingAndSelf(CreativePacket message, Entity entity) {
        if (entity.level() instanceof ISubLevel subLevel)
            sendToClientTrackingAndSelf(message, subLevel.getHolder());
        else {
            var buf = new FriendlyByteBuf(Unpooled.buffer());
            this.packetTypes.get(message.getClass()).write(message, buf);
            PlayerLookup.tracking(entity).forEach(player -> {
                ServerPlayNetworking.send(player, this.packetTypeChannels.get(message.getClass()), buf);
            });
        }
    }
}
