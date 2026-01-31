package xyz.bluspring.kilt.compat.forge.mixin.quark;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.quark.base.item.QuarkMusicDiscItem;
import org.violetmoon.quark.content.tools.module.AmbientDiscsModule;

@Mixin(value = LevelRenderer.class, priority = 1051)
public class LevelRendererMixin {
    // Kilt TODO: fix
//    @Shadow
//    private RecordItem kilt$currentRecordItem;
//
//    @Inject(
//            method = "playStreamingMusic(Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/core/BlockPos;)V",
//            at = @At(value = "JUMP", ordinal = 1),
//            cancellable = true
//    )
//    public void playStreamingMusic(SoundEvent soundIn, BlockPos pos, CallbackInfo info) {
//        if(this.kilt$currentRecordItem instanceof QuarkMusicDiscItem quarkDisc && AmbientDiscsModule.Client.playAmbientSound(quarkDisc, pos))
//            info.cancel();
//    }
}
