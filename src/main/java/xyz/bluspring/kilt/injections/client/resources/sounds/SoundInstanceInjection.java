package xyz.bluspring.kilt.injections.client.resources.sounds;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;

import java.util.concurrent.CompletableFuture;

@FabricInjectedInterface(SoundInstance.class)
public interface SoundInstanceInjection {
    default CompletableFuture<AudioStream> getStream(SoundBufferLibrary soundBuffers, Sound sound, boolean looping) {
        throw new IllegalStateException();
    }
}
