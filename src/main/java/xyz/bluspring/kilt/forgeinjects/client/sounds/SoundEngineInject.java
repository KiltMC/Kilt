package xyz.bluspring.kilt.forgeinjects.client.sounds;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.SoundBuffer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.sound.PlaySoundSourceEvent;
import net.minecraftforge.client.event.sound.PlayStreamingSourceEvent;
import net.minecraftforge.client.event.sound.SoundEngineLoadEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundEngine.class)
public abstract class SoundEngineInject {
    @Inject(method = {"<init>", "reload"}, at = @At("TAIL"))
    private void kilt$callEngineLoadEvent(CallbackInfo ci) {
        ModLoader.get().postEvent(new SoundEngineLoadEvent((SoundEngine) (Object) this));
    }

    @Unique private static final ThreadLocal<SoundInstance> kilt$soundInstance = new ThreadLocal<>();
    @Unique private static final ThreadLocal<SoundEngine> kilt$soundEngine = new ThreadLocal<>();

    @WrapOperation(method = "play", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/sounds/SoundInstance;canPlaySound()Z"))
    private boolean kilt$checkCanPlaySound(SoundInstance instance, Operation<Boolean> original, @Local(argsOnly = true) LocalRef<SoundInstance> soundInstance) {
        soundInstance.set(ForgeHooksClient.playSound((SoundEngine) (Object) this, instance));
        kilt$soundInstance.set(soundInstance.get());
        kilt$soundEngine.set((SoundEngine) (Object) this);

        return soundInstance.get() != null && original.call(soundInstance.get());
    }

    @Inject(method = "play", at = @At("TAIL"))
    private void kilt$resetSoundInstance(SoundInstance sound, CallbackInfo ci) {
        kilt$soundInstance.remove();
        kilt$soundEngine.remove();
    }

    @Inject(method = "method_19752", at = @At("TAIL"))
    private static void kilt$callPlaySoundSourceEvent(SoundBuffer soundBuffer, Channel channel, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new PlaySoundSourceEvent(kilt$soundEngine.get(), kilt$soundInstance.get(), channel));
    }

    // Kilt: soundInstance.getStream() redirect is handled by Fabric API

    @Inject(method = "method_19755", at = @At("TAIL"))
    private static void kilt$callPlayStreamSourceEvent(AudioStream audioStream, Channel channel, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new PlayStreamingSourceEvent(kilt$soundEngine.get(), kilt$soundInstance.get(), channel));
    }
}
