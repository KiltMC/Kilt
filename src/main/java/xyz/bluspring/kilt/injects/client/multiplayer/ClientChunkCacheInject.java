// TRACKED HASH: 3fd5753ef5b9d72f05b48abf20914c123afe80ca
package xyz.bluspring.kilt.injects.client.multiplayer;

import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

@Mixin(ClientChunkCache.class)
public abstract class ClientChunkCacheInject {
    @Inject(method = "drop", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientChunkCache$Storage;drop(ILnet/minecraft/world/level/chunk/LevelChunk;)V"))
    private void kilt$callUnloadEvent(ChunkPos pos, CallbackInfo ci, @Local(name = "currentChunk") LevelChunk chunk) {
        NeoForge.EVENT_BUS.post(new ChunkEvent.Unload(chunk));
    }

    @Inject(method = "replaceWithPacketData", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;onChunkLoaded(Lnet/minecraft/world/level/ChunkPos;)V", shift = At.Shift.AFTER))
    private void kilt$callLoadEvent(CallbackInfoReturnable<LevelChunk> cir, @Local(name = "chunk") LevelChunk chunk) {
        NeoForge.EVENT_BUS.post(new ChunkEvent.Load(chunk, false));
    }
}
