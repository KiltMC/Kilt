package xyz.bluspring.kilt.forgeinjects.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.PortalParticle;
import net.minecraft.client.particle.ReversePortalParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ReversePortalParticle.class)
public abstract class ReversePortalParticleInject extends PortalParticle {
    protected ReversePortalParticleInject(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
    }

    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/particle/ReversePortalParticle;z:D", ordinal = 2, shift = At.Shift.AFTER))
    private void kilt$updateParticleBoundingBox(CallbackInfo ci) {
        this.setPos(this.x, this.y, this.z);
    }
}
