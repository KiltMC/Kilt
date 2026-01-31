// TRACKED HASH: f83737c1722cf4974da22578c25c45ca640818c9
package xyz.bluspring.kilt.injects.world.level;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.neoforged.neoforge.common.world.chunk.ForcedChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.world.level.ForcedChunksSavedDataInjection;

import java.util.UUID;

@Mixin(ForcedChunksSavedData.class)
public abstract class ForcedChunksSavedDataInject implements ForcedChunksSavedDataInjection {
    @ModifyReturnValue(method = "load", at = @At("RETURN"))
    private static ForcedChunksSavedData kilt$readModForcedChunks(ForcedChunksSavedData original, @Local(argsOnly = true) CompoundTag tag) {
        ForcedChunkManager.readModForcedChunks(tag, original.neo$getBlockForcedChunks(), original.neo$getEntityForcedChunks());
        return original;
    }

    @ModifyReturnValue(method = "save", at = @At("RETURN"))
    private CompoundTag kilt$writeModForcedChunks(CompoundTag original) {
        ForcedChunkManager.writeModForcedChunks(original, this.neo$blockForcedChunks, this.neo$entityForcedChunks);
        return original;
    }

    @Unique private final ForcedChunkManager.TicketTracker<BlockPos> neo$blockForcedChunks = new ForcedChunkManager.TicketTracker<>();
    @Unique private final ForcedChunkManager.TicketTracker<UUID> neo$entityForcedChunks = new ForcedChunkManager.TicketTracker<>();

    @Override
    public ForcedChunkManager.TicketTracker<BlockPos> neo$getBlockForcedChunks() {
        return this.neo$blockForcedChunks;
    }

    @Override
    public ForcedChunkManager.TicketTracker<UUID> neo$getEntityForcedChunks() {
        return this.neo$entityForcedChunks;
    }
}