package xyz.bluspring.kilt.injects.world.level;

import java.util.Iterator;
import java.util.UUID;
import java.util.function.Predicate;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.world.chunk.ForcedChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.world.level.TicketStorageInjection;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.Ticket;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.TicketStorage;

@Mixin(TicketStorage.class)
public abstract class TicketStorageInject implements TicketStorageInjection {
    @Shadow protected abstract LongSet getAllChunksWithTicketThat(Predicate<Ticket> ticketCheck);

    @Inject(method = "<init>(Lit/unimi/dsi/fastutil/longs/Long2ObjectOpenHashMap;Lit/unimi/dsi/fastutil/longs/Long2ObjectOpenHashMap;)V", at = @At("TAIL"))
    private void kilt$updateForcedNaturalSpawning(CallbackInfo ci) {
        this.updateForcedNaturalSpawning();
    }

    @Inject(method = "addTicket(JLnet/minecraft/server/level/Ticket;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/TicketStorage;setDirty()V", ordinal = 1))
    private void kilt$addNewForcedSpawningTicket(long key, Ticket ticket, CallbackInfoReturnable<Boolean> cir) {
        if (ticket.getType().forceNaturalSpawning())
            this.chunksWithForceNaturalSpawning.add(key);
    }

    @Inject(method = "removeTicket(JLnet/minecraft/server/level/Ticket;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/TicketStorage;setDirty()V", ordinal = 0))
    private void kilt$updateForcedSpawningTickets(long key, Ticket ticket, CallbackInfoReturnable<Boolean> cir) {
        if (ticket.getType().forceNaturalSpawning()) {
            this.updateForcedNaturalSpawning();
        }
    }

    @Inject(method = "deactivateTicketsOnClosing", at = @At("TAIL"))
    private void kilt$deactivateTicketsOnClosing(CallbackInfo ci) {
        this.blockForcedChunks.deactivateTicketsOnClosing();
        this.entityForcedChunks.deactivateTicketsOnClosing();
    }

    @Definition(id = "chunkTicketsIterator", local = @Local(type = Iterator.class, name = "chunkTicketsIterator"))
    @Definition(id = "remove", method = "Ljava/util/Iterator;remove()V")
    @Expression("chunkTicketsIterator.remove()")
    @Inject(method = "removeTicketIf", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER))
    private void kilt$checkShouldUpdateNaturalSpawning(CallbackInfo ci, @Local(name = "ticket") Ticket ticket, @Share("updateNaturalSpawning") LocalBooleanRef updateNaturalSpawning) {
        if (ticket.getType().forceNaturalSpawning()) {
            updateNaturalSpawning.set(true);
        }
    }

    @Inject(method = "removeTicketIf", at = @At("TAIL"))
    private void kilt$tryUpdateNaturalSpawning(CallbackInfo ci, @Share("updateNaturalSpawning") LocalBooleanRef updateNaturalSpawning) {
        if (updateNaturalSpawning.get()) {
            this.updateForcedNaturalSpawning();
        }
    }

    @Unique private final ForcedChunkManager.TicketTracker<BlockPos> blockForcedChunks = new ForcedChunkManager.TicketTracker<>((TicketStorage) (Object) this, NeoForgeMod.BLOCK_TICKET, NeoForgeMod.BLOCK_WITH_NATURAL_SPAWNING_TICKET);
    @Unique private final ForcedChunkManager.TicketTracker<UUID> entityForcedChunks = new ForcedChunkManager.TicketTracker<>((TicketStorage) (Object) this, NeoForgeMod.ENTITY_TICKET, NeoForgeMod.ENTITY_WITH_NATURAL_SPAWNING_TICKET);
    @Unique private LongSet chunksWithForceNaturalSpawning = new LongOpenHashSet();

    @Override
    public ForcedChunkManager.TicketTracker<BlockPos> getBlockForcedChunks() {
        return this.blockForcedChunks;
    }

    @Override
    public ForcedChunkManager.TicketTracker<UUID> getEntityForcedChunks() {
        return this.entityForcedChunks;
    }

    private void updateForcedNaturalSpawning() {
        this.chunksWithForceNaturalSpawning = this.getAllChunksWithTicketThat(ticket -> ticket.getType().forceNaturalSpawning());
    }

    @Override
    public boolean shouldForceNaturalSpawning(ChunkPos pos) {
        return this.chunksWithForceNaturalSpawning.contains(pos.pack());
    }
}
