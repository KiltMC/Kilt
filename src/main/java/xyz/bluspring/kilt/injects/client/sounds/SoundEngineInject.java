package xyz.bluspring.kilt.injects.client.sounds;

import java.util.function.Consumer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.Library;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.sound.SoundEngineLoadEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.SoundConsumerStorage;
import xyz.bluspring.kilt.injections.client.sounds.ChannelAccessHandleInjection;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;

@Mixin(SoundEngine.class)
public abstract class SoundEngineInject {
    @Inject(method = {"<init>", "reload"}, at = @At("TAIL"))
    private void kilt$callEngineLoadEvent(CallbackInfo ci) {
        ModLoader.postEvent(new SoundEngineLoadEvent((SoundEngine) (Object) this));
    }

    @WrapOperation(method = "play", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/sounds/SoundInstance;canPlaySound()Z"))
    private boolean kilt$checkCanPlaySound(SoundInstance instance, Operation<Boolean> original, @Local(argsOnly = true, name = "instance") LocalRef<SoundInstance> soundInstance) {
        soundInstance.set(ClientHooks.playSound((SoundEngine) (Object) this, instance));
        return soundInstance.get() != null && original.call(soundInstance.get());
    }

    @Inject(method = "play", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/ChannelAccess$ChannelHandle;execute(Ljava/util/function/Consumer;)V", shift = At.Shift.AFTER))
    private void kilt$prepareChannelInfo(SoundInstance instance, CallbackInfoReturnable<SoundEngine.PlayResult> cir, @Local(name = "handle") ChannelAccess.ChannelHandle channelHandle, @Local(name = "sound") Sound sound) {
        var injection = ((ChannelAccessHandleInjection) channelHandle);

        if (sound.shouldStream())
            injection.kilt$setPool(Library.Pool.STREAMING);
        else
            injection.kilt$setPool(Library.Pool.STATIC);

        injection.kilt$setSoundInstance(instance);
        injection.kilt$setSoundEngine((SoundEngine) (Object) this);
    }

    @ModifyArg(method = "lambda$play$1", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/ChannelAccess$ChannelHandle;execute(Ljava/util/function/Consumer;)V"))
    private static Consumer<Channel> kilt$storeSourceConsumer(Consumer<Channel> consumer) {
        SoundConsumerStorage.soundConsumerChannels.add(consumer);
        return consumer;
    }

    @ModifyArg(method = "lambda$play$3", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/ChannelAccess$ChannelHandle;execute(Ljava/util/function/Consumer;)V"))
    private static Consumer<Channel> kilt$storeStreamConsumer(Consumer<Channel> consumer) {
        SoundConsumerStorage.soundConsumerChannels.add(consumer);
        return consumer;
    }

    // Kilt: PlaySoundSourceEvent and PlayStreamingSourceEvent is handled in ChannelAccessHandleMixin
    // Kilt: soundInstance.getStream() redirect is handled by Fabric API
}
