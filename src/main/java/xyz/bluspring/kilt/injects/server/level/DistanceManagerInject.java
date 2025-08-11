package xyz.bluspring.kilt.injects.server.level;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.Ticket;
import net.minecraft.util.SortedArraySet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(DistanceManager.class)
public abstract class DistanceManagerInject {
    @Unique private final Long2ObjectOpenHashMap<SortedArraySet<Ticket<?>>> forcedTickets = new Long2ObjectOpenHashMap<>();
    @Unique private final AtomicBoolean kilt$isForceTicks = new AtomicBoolean(false);

    @Inject(method = "addTicket(JLnet/minecraft/server/level/Ticket;)V", at = @At("TAIL"))
    private void kilt$setForcedTickTicket(long chunkPos, Ticket<?> ticket, CallbackInfo ci) {

    }
}
