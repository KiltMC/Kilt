// TRACKED HASH: 0e6708ad54e01d212d8aaafd85dc2fd7b82ad930
package xyz.bluspring.kilt.forgeinjects.client.resources.sounds;

import net.fabricmc.fabric.api.client.sound.v1.FabricSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.client.resources.sounds.SoundInstanceInjection;

import java.util.concurrent.CompletableFuture;

@Mixin(SoundInstance.class)
public interface SoundInstanceInject extends SoundInstanceInjection, FabricSoundInstance {
    @Override
    default CompletableFuture<AudioStream> getStream(SoundBufferLibrary soundBuffers, Sound sound, boolean looping) {
        // Kilt: Redirect to Fabric's handling
        // TODO: redirect Fabric's calls to getAudioStream to also handle Forge's
        return this.getAudioStream(soundBuffers, sound.getPath(), looping);
    }
}