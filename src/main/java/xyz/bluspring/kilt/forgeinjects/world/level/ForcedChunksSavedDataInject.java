package xyz.bluspring.kilt.forgeinjects.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraftforge.common.world.ForgeChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.ForcedChunksSavedDataInjection;

import java.util.UUID;

@Mixin(ForcedChunksSavedData.class)
public class ForcedChunksSavedDataInject implements ForcedChunksSavedDataInjection {
    private final ForgeChunkManager.TicketTracker<BlockPos> blockForcedChunks = new ForgeChunkManager.TicketTracker<>();
    private final ForgeChunkManager.TicketTracker<UUID> entityForcedChunks = new ForgeChunkManager.TicketTracker<>();

    @Override
    public ForgeChunkManager.TicketTracker<BlockPos> getBlockForcedChunks() {
        return blockForcedChunks;
    }

    @Override
    public ForgeChunkManager.TicketTracker<UUID> getEntityForcedChunks() {
        return entityForcedChunks;
    }

    @Inject(at = @At("RETURN"), method = "load")
    private static void kilt$loadForgeForcedChunks(CompoundTag compoundTag, CallbackInfoReturnable<ForcedChunksSavedData> cir) {
        var savedData = (ForcedChunksSavedDataInjection) cir.getReturnValue();
        ForgeChunkManager.readForgeForcedChunks(compoundTag, savedData.getBlockForcedChunks(), savedData.getEntityForcedChunks());
    }

    @Inject(at = @At("RETURN"), method = "save")
    public void kilt$writeForgeForcedChunks(CompoundTag compoundTag, CallbackInfoReturnable<CompoundTag> cir) {
        ForgeChunkManager.writeForgeForcedChunks(compoundTag, blockForcedChunks, entityForcedChunks);
    }
}
