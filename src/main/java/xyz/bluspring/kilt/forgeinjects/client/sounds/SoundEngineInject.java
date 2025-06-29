package xyz.bluspring.kilt.forgeinjects.client.sounds;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.audio.Library;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.sound.SoundEngineLoadEvent;
import net.neoforged.fml.ModLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.client.sounds.ChannelAccessHandleInjection;

@Mixin(SoundEngine.class)
public abstract class SoundEngineInject {
    @Inject(method = {"<init>", "reload"}, at = @At("TAIL"))
    private void kilt$callEngineLoadEvent(CallbackInfo ci) {
        ModLoader.get().postEvent(new SoundEngineLoadEvent((SoundEngine) (Object) this));
    }

    @WrapOperation(method = "play", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/sounds/SoundInstance;canPlaySound()Z"))
    private boolean kilt$checkCanPlaySound(SoundInstance instance, Operation<Boolean> original, @Local(argsOnly = true) LocalRef<SoundInstance> soundInstance) {
        soundInstance.set(ForgeHooksClient.playSound((SoundEngine) (Object) this, instance));
        return soundInstance.get() != null && original.call(soundInstance.get());
    }

    @Inject(method = "play", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/ChannelAccess$ChannelHandle;execute(Ljava/util/function/Consumer;)V", shift = At.Shift.AFTER))
    private void kilt$prepareChannelInfo(SoundInstance soundInstance, CallbackInfo ci, @Local ChannelAccess.ChannelHandle channelHandle, @Local Sound sound) {
        var injection = ((ChannelAccessHandleInjection) channelHandle);

        if (sound.shouldStream())
            injection.kilt$setPool(Library.Pool.STREAMING);
        else
            injection.kilt$setPool(Library.Pool.STATIC);

        injection.kilt$setSoundInstance(soundInstance);
        injection.kilt$setSoundEngine((SoundEngine) (Object) this);
    }

    // Kilt: PlaySoundSourceEvent and PlayStreamingSourceEvent is handled in ChannelAccessHandleMixin
    // Kilt: soundInstance.getStream() redirect is handled by Fabric API
}
