package xyz.bluspring.kilt.injects.client.particle;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.PortalParticle;
import net.minecraft.client.particle.TextureSheetParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PortalParticle.class)
public abstract class PortalParticleInject extends TextureSheetParticle {
    protected PortalParticleInject(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
    }

    @Definition(id = "z", field = "Lnet/minecraft/client/particle/PortalParticle;z:D")
    @Definition(id = "zStart", field = "Lnet/minecraft/client/particle/PortalParticle;zStart:D")
    @Definition(id = "zd", field = "Lnet/minecraft/client/particle/PortalParticle;zd:D")
    @Definition(id = "f", local = @Local(type = float.class, ordinal = 0))
    @Expression("this.z = this.zStart + this.zd * (double) f")
    @Inject(method = "tick", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER))
    private void kilt$updateParticleBoundingBox(CallbackInfo ci) {
        this.setPos(this.x, this.y, this.z);
    }
}
