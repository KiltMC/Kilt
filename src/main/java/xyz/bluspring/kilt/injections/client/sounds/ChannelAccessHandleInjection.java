package xyz.bluspring.kilt.injections.client.sounds;

import com.mojang.blaze3d.audio.Library;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;

public interface ChannelAccessHandleInjection {
    void kilt$setPool(Library.Pool pool);
    void kilt$setSoundInstance(SoundInstance instance);
    void kilt$setSoundEngine(SoundEngine engine);
}
