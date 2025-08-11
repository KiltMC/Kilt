package xyz.bluspring.kilt.injects.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.particle.VibrationSignalParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VibrationSignalParticle.class)
public abstract class VibrationSignalParticleInject extends TextureSheetParticle {
    protected VibrationSignalParticleInject(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(DDD)D", ordinal = 2, shift = At.Shift.AFTER))
    private void kilt$updateParticleBoundingBox(CallbackInfo ci) {
        this.setPos(this.x, this.y, this.z);
    }
}
