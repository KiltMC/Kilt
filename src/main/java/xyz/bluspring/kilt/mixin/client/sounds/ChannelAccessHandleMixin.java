package xyz.bluspring.kilt.mixin.client.sounds;

import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.Library;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.neoforged.neoforge.client.event.sound.PlaySoundSourceEvent;
import net.neoforged.neoforge.client.event.sound.PlayStreamingSourceEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.SoundConsumerStorage;
import xyz.bluspring.kilt.injections.client.sounds.ChannelAccessHandleInjection;

import java.util.function.Consumer;

@Mixin(ChannelAccess.ChannelHandle.class)
public abstract class ChannelAccessHandleMixin implements ChannelAccessHandleInjection {
    @Shadow @Nullable private Channel channel;
    @Unique private Library.Pool kilt$pool;
    @Unique private SoundEngine kilt$soundEngine;
    @Unique private SoundInstance kilt$soundInstance;

    @Override
    public void kilt$setPool(Library.Pool pool) {
        this.kilt$pool = pool;
    }

    @Override
    public void kilt$setSoundEngine(SoundEngine engine) {
        this.kilt$soundEngine = engine;
    }

    @Override
    public void kilt$setSoundInstance(SoundInstance instance) {
        this.kilt$soundInstance = instance;
    }

    @Inject(method = "method_19737", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", shift = At.Shift.AFTER))
    private void kilt$callPlaySoundEvents(Consumer<Channel> consumer, CallbackInfo ci) {
        if (this.channel != null && kilt$soundEngine != null && kilt$soundInstance != null && SoundConsumerStorage.soundConsumerChannels.remove(consumer)) {
            if (kilt$pool == Library.Pool.STATIC) {
                NeoForge.EVENT_BUS.post(new PlaySoundSourceEvent(kilt$soundEngine, kilt$soundInstance, this.channel));
            } else if (kilt$pool == Library.Pool.STREAMING) {
                NeoForge.EVENT_BUS.post(new PlayStreamingSourceEvent(kilt$soundEngine, kilt$soundInstance, this.channel));
            }
        }
    }
}
