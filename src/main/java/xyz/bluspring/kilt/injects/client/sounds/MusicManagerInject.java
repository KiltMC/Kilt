package xyz.bluspring.kilt.injects.client.sounds;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.client.ClientHooks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.sounds.Music;

@Mixin(MusicManager.class)
public abstract class MusicManagerInject {
    @Shadow private @Nullable SoundInstance currentMusic;
    @Shadow public abstract void stopPlaying();
    @Shadow private int nextSongDelay;

    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getSituationalMusic()Lnet/minecraft/sounds/Music;"))
    private Music kilt$handleSelectMusicEvent(Music original) {
        return ClientHooks.selectMusic(original, this.currentMusic);
    }

    @Definition(id = "currentMusic", field = "Lnet/minecraft/client/sounds/MusicManager;currentMusic:Lnet/minecraft/client/resources/sounds/SoundInstance;")
    @Expression("this.currentMusic != null")
    @Inject(method = "tick", at = @At("MIXINEXTRAS:EXPRESSION"), cancellable = true)
    private void kilt$stopPlayingIfMusicIsNull(CallbackInfo ci, @Local Music music) {
        if (music == null) {
            if (this.currentMusic != null) {
                this.stopPlaying();
            }

            this.nextSongDelay = 0;
            ci.cancel();
        }
    }
}
